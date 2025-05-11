package com.uninaswap.client.service;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.common.message.ItemMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ItemService {
    private static ItemService instance;
    private final WebSocketClient webSocketClient;
    private final ObservableList<ItemDTO> userItems = FXCollections.observableArrayList();
    
    private ItemService() {
        this.webSocketClient = WebSocketManager.getClient();
        this.webSocketClient.registerMessageHandler(ItemMessage.class, this::handleItemMessage);
    }
    
    public static synchronized ItemService getInstance() {
        if (instance == null) {
            instance = new ItemService();
        }
        return instance;
    }
    
    // Get all items for the current user
    public CompletableFuture<List<ItemDTO>> getUserItems() {
        CompletableFuture<List<ItemDTO>> future = new CompletableFuture<>();
        
        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.GET_ITEMS_REQUEST);
        
        webSocketClient.sendMessage(message)
            .thenRun(() -> {
                // Message sent, wait for response in handleItemMessage
            })
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        
        return future;
    }
    
    // Add a new item
    public CompletableFuture<ItemDTO> addItem(ItemDTO item) {
        CompletableFuture<ItemDTO> future = new CompletableFuture<>();
        
        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.ADD_ITEM_REQUEST);
        message.setItem(item);
        
        webSocketClient.sendMessage(message)
            .thenRun(() -> {
                // Message sent, wait for response in handleItemMessage
            })
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        
        return future;
    }
    
    // Update an existing item
    public CompletableFuture<ItemDTO> updateItem(ItemDTO item) {
        CompletableFuture<ItemDTO> future = new CompletableFuture<>();
        
        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.UPDATE_ITEM_REQUEST);
        message.setItem(item);
        
        webSocketClient.sendMessage(message)
            .thenRun(() -> {
                // Message sent, wait for response in handleItemMessage
            })
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        
        return future;
    }
    
    // Delete an item
    public CompletableFuture<Boolean> deleteItem(String itemId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.DELETE_ITEM_REQUEST);
        
        ItemDTO item = new ItemDTO();
        item.setId(itemId);
        message.setItem(item);
        
        webSocketClient.sendMessage(message)
            .thenRun(() -> {
                // Message sent, wait for response in handleItemMessage
            })
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        
        return future;
    }
    
    // Upload item image
    public CompletableFuture<String> uploadItemImage(String base64Image, String fileName) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.UPLOAD_IMAGE_REQUEST);
        message.setImageBase64(base64Image);
        message.setImageName(fileName);
        
        webSocketClient.sendMessage(message)
            .thenRun(() -> {
                // Message sent, wait for response in handleItemMessage
            })
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        
        return future;
    }
    
    // Get observable list of user items for UI binding
    public ObservableList<ItemDTO> getUserItemsList() {
        if (userItems.isEmpty()) {
            refreshUserItems();
        }
        return userItems;
    }
    
    // Refresh the user's items
    public void refreshUserItems() {
        getUserItems()
            .thenAccept(items -> {
                Platform.runLater(() -> {
                    userItems.clear();
                    userItems.addAll(items);
                });
            })
            .exceptionally(ex -> {
                System.err.println("Error refreshing user items: " + ex.getMessage());
                return null;
            });
    }
    
    // Handle incoming item messages
    private CompletableFuture<?> futureToComplete;
    private Consumer<ItemMessage> messageCallback;
    
    private void handleItemMessage(ItemMessage message) {
        switch (message.getType()) {
            case GET_ITEMS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        userItems.clear();
                        userItems.addAll(message.getItems());
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ItemDTO>>) futureToComplete).complete(message.getItems());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to get items: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
                
            case ADD_ITEM_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        userItems.add(message.getItem());
                        if (futureToComplete != null) {
                            ((CompletableFuture<ItemDTO>) futureToComplete).complete(message.getItem());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                new Exception("Failed to add item: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;
            default:
                System.out.println("Unknown item message type: " + message.getType());
            // Handle other message types similarly...
        }
        
        // Call any registered callback
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }
    
    // Set a callback for incoming messages
    public void setMessageCallback(Consumer<ItemMessage> callback) {
        this.messageCallback = callback;
    }
}