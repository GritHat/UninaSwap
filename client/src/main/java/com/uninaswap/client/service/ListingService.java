package com.uninaswap.client.service;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.message.ListingMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListingService {
    private static ListingService instance;
    private final WebSocketClient webSocketClient;
    private final ObservableList<ListingDTO> userListings = FXCollections.observableArrayList();
    private final ObservableList<ListingDTO> allListings = FXCollections.observableArrayList();
    
    private CompletableFuture<?> futureToComplete;
    private Consumer<ListingMessage> messageCallback;
    
    private ListingService() {
        this.webSocketClient = WebSocketManager.getClient();
        this.webSocketClient.registerMessageHandler(ListingMessage.class, this::handleListingMessage);
    }
    
    public static synchronized ListingService getInstance() {
        if (instance == null) {
            instance = new ListingService();
        }
        return instance;
    }
    
    // Get all active listings
    public CompletableFuture<List<ListingDTO>> getListings(int page, int size) {
        CompletableFuture<List<ListingDTO>> future = new CompletableFuture<>();
        
        ListingMessage message = new ListingMessage();
        message.setType(ListingMessage.Type.GET_LISTINGS_REQUEST);
        message.setPage(page);
        message.setSize(size);
        
        this.futureToComplete = future;
        
        webSocketClient.sendMessage(message)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                this.futureToComplete = null;
                return null;
            });
        
        return future;
    }
    
    // Get current user's listings
    public CompletableFuture<List<ListingDTO>> getMyListings() {
        CompletableFuture<List<ListingDTO>> future = new CompletableFuture<>();
        
        ListingMessage message = new ListingMessage();
        message.setType(ListingMessage.Type.GET_MY_LISTINGS_REQUEST);
        
        this.futureToComplete = future;
        
        webSocketClient.sendMessage(message)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                this.futureToComplete = null;
                return null;
            });
        
        return future;
    }
    
    // Create a new listing
    public CompletableFuture<ListingDTO> createListing(ListingDTO listing) {
        CompletableFuture<ListingDTO> future = new CompletableFuture<>();
        
        ListingMessage message = new ListingMessage();
        message.setType(ListingMessage.Type.CREATE_LISTING_REQUEST);
        message.setListing(listing);
        message.setListingTypeValue(listing.getListingTypeValue());
        
        this.futureToComplete = future;
        
        webSocketClient.sendMessage(message)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                this.futureToComplete = null;
                return null;
            });
        
        return future;
    }
    
    // Update an existing listing
    public CompletableFuture<ListingDTO> updateListing(ListingDTO listing) {
        CompletableFuture<ListingDTO> future = new CompletableFuture<>();
        
        ListingMessage message = new ListingMessage();
        message.setType(ListingMessage.Type.UPDATE_LISTING_REQUEST);
        message.setListing(listing);
        message.setListingTypeValue(listing.getListingTypeValue());
        
        this.futureToComplete = future;
        
        webSocketClient.sendMessage(message)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                this.futureToComplete = null;
                return null;
            });
        
        return future;
    }
    
    // Delete a listing
    public CompletableFuture<Boolean> deleteListing(String listingId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        ListingMessage message = new ListingMessage();
        message.setType(ListingMessage.Type.DELETE_LISTING_REQUEST);
        message.setListingId(listingId);
        
        this.futureToComplete = future;
        
        webSocketClient.sendMessage(message)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                this.futureToComplete = null;
                return null;
            });
        
        return future;
    }
    
    // Get listing details by ID
    public CompletableFuture<ListingDTO> getListingDetails(String listingId) {
        CompletableFuture<ListingDTO> future = new CompletableFuture<>();
        
        ListingMessage message = new ListingMessage();
        message.setType(ListingMessage.Type.GET_LISTING_DETAIL_REQUEST);
        message.setListingId(listingId);
        
        this.futureToComplete = future;
        
        webSocketClient.sendMessage(message)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                this.futureToComplete = null;
                return null;
            });
        
        return future;
    }
    
    // Get observable lists for UI binding
    public ObservableList<ListingDTO> getUserListingsObservable() {
        if (userListings.isEmpty()) {
            refreshUserListings();
        }
        return userListings;
    }
    
    public ObservableList<ListingDTO> getAllListingsObservable() {
        if (allListings.isEmpty()) {
            refreshAllListings();
        }
        return allListings;
    }
    
    // Refresh listing data
    public void refreshUserListings() {
        getMyListings()
            .thenAccept(listings -> {
                Platform.runLater(() -> {
                    userListings.clear();
                    userListings.addAll(listings);
                });
            })
            .exceptionally(ex -> {
                System.err.println("Error refreshing user listings: " + ex.getMessage());
                return null;
            });
    }
    
    public void refreshAllListings() {
        getListings(0, 50) // First page with 50 items
            .thenAccept(listings -> {
                Platform.runLater(() -> {
                    allListings.clear();
                    allListings.addAll(listings);
                });
            })
            .exceptionally(ex -> {
                System.err.println("Error refreshing all listings: " + ex.getMessage());
                return null;
            });
    }
    
    // Handle incoming messages
    @SuppressWarnings("unchecked")
    private void handleListingMessage(ListingMessage message) {
        switch (message.getType()) {
            case GET_LISTINGS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        allListings.clear();
                        allListings.addAll(message.getListings());
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ListingDTO>>) futureToComplete).complete(message.getListings());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to get listings: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
                
            case GET_MY_LISTINGS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        userListings.clear();
                        userListings.addAll(message.getListings());
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ListingDTO>>) futureToComplete).complete(message.getListings());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to get user listings: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
                
            case CREATE_LISTING_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        userListings.add(message.getListing());
                        allListings.add(message.getListing());
                        if (futureToComplete != null) {
                            ((CompletableFuture<ListingDTO>) futureToComplete).complete(message.getListing());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to create listing: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
                
            case UPDATE_LISTING_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        // Update in both lists
                        ListingDTO updated = message.getListing();
                        updateListingInObservableList(userListings, updated);
                        updateListingInObservableList(allListings, updated);
                        
                        if (futureToComplete != null) {
                            ((CompletableFuture<ListingDTO>) futureToComplete).complete(updated);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to update listing: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
                
            case DELETE_LISTING_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        // Remove from both lists
                        String deletedId = message.getListingId();
                        userListings.removeIf(listing -> listing.getId().equals(deletedId));
                        allListings.removeIf(listing -> listing.getId().equals(deletedId));
                        
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(true);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to delete listing: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
                
            case GET_LISTING_DETAIL_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<ListingDTO>) futureToComplete).complete(message.getListing());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to get listing details: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
                
            default:
                System.out.println("Unknown listing message type: " + message.getType());
                break;
        }
        
        // Call any registered callback
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }
    
    // Helper method to update a listing in an ObservableList
    private void updateListingInObservableList(ObservableList<ListingDTO> list, ListingDTO updated) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updated.getId())) {
                list.set(i, updated);
                break;
            }
        }
    }
    
    // Set a callback for incoming messages
    public void setMessageCallback(Consumer<ListingMessage> callback) {
        this.messageCallback = callback;
    }
}