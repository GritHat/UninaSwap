package com.uninaswap.client.service;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ItemViewModel;
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
    private final LocaleService localeService = LocaleService.getInstance();
    private final EventBusService eventBus = EventBusService.getInstance();
    private final WebSocketClient webSocketClient;
    private final ObservableList<ItemDTO> userItems = FXCollections.observableArrayList();
    private final ObservableList<ItemViewModel> userItemViewModels = FXCollections.observableArrayList();
    private boolean needsRefresh = false;

    private ItemService() {
        this.webSocketClient = WebSocketClient.getInstance();
        this.webSocketClient.registerMessageHandler(ItemMessage.class, this::handleItemMessage);
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            System.out.println("ItemService: Received USER_LOGGED_OUT event");
            clearAllData();
        });
    }

    public static synchronized ItemService getInstance() {
        if (instance == null) {
            instance = new ItemService();
        }
        return instance;
    }

    private void clearAllData() {
        Platform.runLater(() -> {
            System.out.println("ItemService: Clearing all cached data...");
            System.out.println("ItemService: Before clear - userItems size: " + userItems.size() +
                    ", userItemViewModels size: " + userItemViewModels.size());
            userItems.clear();
            userItemViewModels.clear();
            needsRefresh = true;
            if (futureToComplete != null) {
                futureToComplete.cancel(true);
                futureToComplete = null;
            }

            System.out.println("ItemService: Cleared all cached data on logout");
        });

        System.out.println("ItemService: After clear - userItems size: " + userItems.size() +
                ", userItemViewModels size: " + userItemViewModels.size());
        System.out.println("ItemService: Cleared all cached data on logout");
    }

    public CompletableFuture<List<ItemDTO>> getUserItems() {
        CompletableFuture<List<ItemDTO>> future = new CompletableFuture<>();

        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.GET_ITEMS_REQUEST);
        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    public CompletableFuture<ItemDTO> addItem(ItemDTO item) {
        CompletableFuture<ItemDTO> future = new CompletableFuture<>();

        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.ADD_ITEM_REQUEST);
        message.setItem(item);
        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    public CompletableFuture<ItemDTO> updateItem(ItemDTO item) {
        CompletableFuture<ItemDTO> future = new CompletableFuture<>();

        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.UPDATE_ITEM_REQUEST);
        message.setItem(item);
        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    public CompletableFuture<Boolean> deleteItem(String itemId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ItemMessage message = new ItemMessage();
        message.setType(ItemMessage.Type.DELETE_ITEM_REQUEST);

        ItemDTO item = new ItemDTO();
        item.setId(itemId);
        message.setItem(item);
        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    public ObservableList<ItemDTO> getUserItemsList() {
        if (userItems.isEmpty() || needsRefresh) {
            refreshUserItems();
            needsRefresh = false;
        }
        return userItems;
    }

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

    /**
     * Get user's items as ViewModels for UI binding (cached and auto-updating)
     * 
     * @return ObservableList of ItemViewModels that automatically updates
     */
    public ObservableList<ItemViewModel> getUserItemsListAsViewModel() {
        // If we need initial load
        if (userItemViewModels.isEmpty() || needsRefresh) {
            loadUserItemsAsViewModels();
        }

        return userItemViewModels;
    }

    private void loadUserItemsAsViewModels() {
        needsRefresh = false;

        getUserItems()
                .thenAccept(items -> {
                    Platform.runLater(() -> {
                        userItems.clear();
                        userItems.addAll(items);

                        userItemViewModels.clear();
                        items.forEach(itemDTO -> {
                            ItemViewModel itemViewModel = ViewModelMapper.getInstance().toViewModel(itemDTO);
                            userItemViewModels.add(itemViewModel);
                        });
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Error loading user items: " + ex.getMessage());
                    return null;
                });
    }

    private CompletableFuture<?> futureToComplete;
    private Consumer<ItemMessage> messageCallback;

    @SuppressWarnings("unchecked")
    private void handleItemMessage(ItemMessage message) {
        switch (message.getType()) {
            case GET_ITEMS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        userItems.clear();
                        userItems.addAll(message.getItems());
                        userItemViewModels.clear();
                        message.getItems().forEach(itemDTO -> {
                            ItemViewModel itemViewModel = ViewModelMapper.getInstance().toViewModel(itemDTO);
                            userItemViewModels.add(itemViewModel);
                        });

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
                        ItemViewModel itemViewModel = ViewModelMapper.getInstance().toViewModel(message.getItem());
                        userItemViewModels.add(itemViewModel);

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

            case UPDATE_ITEM_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        for (int i = 0; i < userItems.size(); i++) {
                            if (userItems.get(i).getId().equals(message.getItem().getId())) {
                                userItems.set(i, message.getItem());
                                ItemViewModel updatedViewModel = ViewModelMapper.getInstance()
                                        .toViewModel(message.getItem());
                                userItemViewModels.set(i, updatedViewModel);
                                break;
                            }
                        }

                        if (futureToComplete != null) {
                            ((CompletableFuture<ItemDTO>) futureToComplete).complete(message.getItem());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to update item: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case DELETE_ITEM_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        userItems.removeIf(item -> item.getId().equals(message.getItem().getId()));
                        userItemViewModels.removeIf(item -> item.getId().equals(message.getItem().getId()));

                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(true);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to delete item: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            default:
                System.out.println("Unknown item message type: " + message.getType());
                break;
        }
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    public void saveItem(ItemDTO item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            // Add new item
            addItem(item)
                    .thenAccept(_ -> {
                        publishItemUpdatedEvent(item);
                    })
                    .exceptionally(ex -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("item.add.error.title"),
                                localeService.getMessage("item.add.error.header"),
                                ex.getMessage());
                        return null;
                    });
        } else {
            updateItem(item)
                    .thenAccept(_ -> {
                        publishItemUpdatedEvent(item);
                    })
                    .exceptionally(ex -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("item.edit.error.title"),
                                localeService.getMessage("item.edit.error.header"),
                                ex.getMessage());
                        return null;
                    });
        }
    }

    private void publishItemUpdatedEvent(ItemDTO item) {
        eventBus.publishEvent(EventTypes.ITEM_UPDATED, ViewModelMapper.getInstance().toViewModel(item));
    }

    public void setMessageCallback(Consumer<ItemMessage> callback) {
        this.messageCallback = callback;
    }

    public void setNeedsRefresh(boolean needsRefresh) {
        this.needsRefresh = needsRefresh;
    }
}