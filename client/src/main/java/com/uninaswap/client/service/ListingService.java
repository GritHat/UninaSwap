package com.uninaswap.client.service;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.message.ListingMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListingService {
    private static ListingService instance;
    private final WebSocketClient webSocketClient;
    private final ObservableList<ListingViewModel> userListings = FXCollections.observableArrayList();
    private final ObservableList<ListingViewModel> allListings = FXCollections.observableArrayList();

    private CompletableFuture<?> futureToComplete;
    private Consumer<ListingMessage> messageCallback;

    // Add a flag to track if we're loading more or refreshing
    private boolean isLoadingMore = false;

    private ListingService() {
        this.webSocketClient = WebSocketClient.getInstance();
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
    public ObservableList<ListingViewModel> getUserListingsObservable() {
        if (userListings.isEmpty()) {
            refreshUserListings();
        }
        return userListings;
    }

    public ObservableList<ListingViewModel> getAllListingsObservable() {
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
                        List<ListingViewModel> viewModels = listings.stream()
                                .map(ViewModelMapper.getInstance()::toViewModel)
                                .toList();
                        userListings.clear();
                        userListings.addAll(viewModels);
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Error refreshing user listings: " + ex.getMessage());
                    return null;
                });
    }

    // Update the refreshAllListings method to support initial loading only
    public void refreshAllListings() {
        getListings(0, 50) // First page with 50 items
                .thenAccept(listings -> {
                    Platform.runLater(() -> {
                        List<ListingViewModel> viewModels = listings.stream()
                                .map(ViewModelMapper.getInstance()::toViewModel)
                                .toList();
                        allListings.clear(); // Clear existing data
                        allListings.addAll(viewModels);
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Error refreshing all listings: " + ex.getMessage());
                    return null;
                });
    }

    // Add method to append listings (for pagination)
    public void appendListings(List<ListingDTO> newListings) {
        Platform.runLater(() -> {
            List<ListingViewModel> viewModels = newListings.stream()
                    .map(ViewModelMapper.getInstance()::toViewModel)
                    .toList();
            allListings.addAll(viewModels); // Append instead of replacing
        });
    }

    // Add method to set loading more flag
    public void setLoadingMore(boolean loadingMore) {
        this.isLoadingMore = loadingMore;
    }

    // Handle incoming messages
    @SuppressWarnings("unchecked")
    private void handleListingMessage(ListingMessage message) {
        // SAFETY CHECK: Handle null type
        if (message.getType() == null) {
            System.err.println("Received message with null type: " + message.getErrorMessage());

            // If there's an error message, it's likely a failed request
            if (!message.isSuccess() && futureToComplete != null) {
                futureToComplete.completeExceptionally(
                        new Exception("Server error: " + message.getErrorMessage()));
                futureToComplete = null;
            }
            return;
        }

        switch (message.getType()) {
            case GET_LISTINGS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<ListingDTO> listings = message.getListings() != null ? message.getListings()
                                : new ArrayList<>();
                        List<ListingViewModel> viewModels = listings.stream()
                                .map(ViewModelMapper.getInstance()::toViewModel)
                                .toList();
                        
                        // Check if we're loading more or refreshing
                        if (isLoadingMore) {
                            // Append new listings to existing ones
                            allListings.addAll(viewModels);
                            System.out.println("Added " + viewModels.size() + " more listings. Total: " + allListings.size());
                        } else {
                            // Replace all listings (initial load or refresh)
                            allListings.setAll(viewModels);
                            System.out.println("Loaded " + viewModels.size() + " listings (initial/refresh)");
                        }
                        
                        // Reset the flag
                        isLoadingMore = false;
                        
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ListingDTO>>) futureToComplete).complete(listings);
                            futureToComplete = null;
                        }
                    } else {
                        isLoadingMore = false; // Reset flag on error too
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
                        List<ListingDTO> listings = message.getListings() != null ? message.getListings()
                                : new ArrayList<>();
                        List<ListingViewModel> viewModels = listings.stream()
                                .map(ViewModelMapper.getInstance()::toViewModel)
                                .toList();
                        userListings.setAll(viewModels);
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ListingDTO>>) futureToComplete).complete(
                                    message.getListings() != null ? message.getListings() : new ArrayList<>());
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
                        userListings.add(ViewModelMapper.getInstance().toViewModel(message.getListing()));
                        allListings.add(ViewModelMapper.getInstance().toViewModel(message.getListing()));
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
                        updateListingInObservableList(userListings, ViewModelMapper.getInstance().toViewModel(updated));
                        updateListingInObservableList(allListings, ViewModelMapper.getInstance().toViewModel(updated));

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

            case GET_USER_LISTINGS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<ListingDTO> listings = message.getListings() != null ? message.getListings()
                                : new ArrayList<>();
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ListingDTO>>) futureToComplete).complete(listings);
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
    private void updateListingInObservableList(ObservableList<ListingViewModel> list, ListingViewModel updated) {
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

    // Get listings for a specific user by user ID
    public CompletableFuture<List<ListingDTO>> getUserListings(Long userId) {
        CompletableFuture<List<ListingDTO>> future = new CompletableFuture<>();

        ListingMessage message = new ListingMessage();
        message.setType(ListingMessage.Type.GET_USER_LISTINGS_REQUEST);
        message.setUserId(userId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }
}