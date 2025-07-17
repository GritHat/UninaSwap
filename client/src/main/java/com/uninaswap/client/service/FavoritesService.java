package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.client.viewmodel.FavoriteViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.dto.FavoriteDTO;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.message.FavoriteMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FavoritesService {
    private static FavoritesService instance;

    private final WebSocketClient webSocketClient = WebSocketManager.getClient();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();

    // Local favorites tracking (for quick UI updates)
    private final Set<String> favoriteItemIds = new HashSet<>();
    private final Set<String> favoriteListingIds = new HashSet<>();
    private final Set<Long> favoriteUserIds = new HashSet<>();

    // Observable lists for UI binding
    private final ObservableList<FavoriteViewModel> userFavorites = FXCollections.observableArrayList();
    private final ObservableList<ListingDTO> favoriteListings = FXCollections.observableArrayList();

    // Listener functionality
    private final List<Consumer<String>> listingFavoriteChangeListeners = new ArrayList<>();
    private final List<Consumer<String>> itemFavoriteChangeListeners = new ArrayList<>();
    private final List<Consumer<Long>> userFavoriteChangeListeners = new ArrayList<>();

    private CompletableFuture<?> futureToComplete;
    private Consumer<FavoriteMessage> messageCallback;

    private FavoritesService() {
        // Register message handler
        webSocketClient.registerMessageHandler(FavoriteMessage.class, this::handleFavoriteMessage);
    }

    public static synchronized FavoritesService getInstance() {
        if (instance == null) {
            instance = new FavoritesService();
        }
        return instance;
    }

    // === SERVER COMMUNICATION METHODS ===

    /**
     * Add a listing to favorites on server
     */
    public CompletableFuture<FavoriteViewModel> addFavoriteToServer(String listingId) {
        CompletableFuture<FavoriteViewModel> future = new CompletableFuture<>();

        FavoriteMessage message = new FavoriteMessage();
        message.setType(FavoriteMessage.Type.ADD_FAVORITE_REQUEST);
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

    /**
     * Remove a listing from favorites on server
     */
    public CompletableFuture<Boolean> removeFavoriteFromServer(String listingId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FavoriteMessage message = new FavoriteMessage();
        message.setType(FavoriteMessage.Type.REMOVE_FAVORITE_REQUEST);
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

    /**
     * Get user's favorites from server
     */
    public CompletableFuture<List<FavoriteDTO>> getUserFavorites() {
        CompletableFuture<List<FavoriteDTO>> future = new CompletableFuture<>();

        FavoriteMessage message = new FavoriteMessage();
        message.setType(FavoriteMessage.Type.GET_USER_FAVORITES_REQUEST);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Check if a listing is favorited on server
     */
    public CompletableFuture<Boolean> isFavoriteOnServer(String listingId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FavoriteMessage message = new FavoriteMessage();
        message.setType(FavoriteMessage.Type.IS_FAVORITE_REQUEST);
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

    /**
     * Toggle favorite status on server
     */
    public CompletableFuture<Boolean> toggleFavoriteOnServer(String listingId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FavoriteMessage message = new FavoriteMessage();
        message.setType(FavoriteMessage.Type.TOGGLE_FAVORITE_REQUEST);
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

    // === LOCAL FAVORITES METHODS (for quick UI updates) ===

    // Item favorites (existing)
    public void addFavorite(String itemId) {
        favoriteItemIds.add(itemId);
        System.out.println("Added item to favorites: " + itemId);
        notifyItemFavoriteChange(itemId);
    }

    public void removeFavorite(String itemId) {
        favoriteItemIds.remove(itemId);
        System.out.println("Removed item from favorites: " + itemId);
        notifyItemFavoriteChange(itemId);
    }

    public boolean isFavorite(String itemId) {
        return favoriteItemIds.contains(itemId);
    }

    public Set<String> getAllFavorites() {
        return Collections.unmodifiableSet(favoriteItemIds);
    }

    // Listing favorites
    public void addFavoriteListing(String listingId) {
        favoriteListingIds.add(listingId);
        System.out.println("Added listing to favorites: " + listingId);
        notifyListingFavoriteChange(listingId);

        // Also sync with server
        addFavoriteToServer(listingId)
                .exceptionally(ex -> {
                    System.err.println("Failed to sync favorite to server: " + ex.getMessage());
                    return null;
                });
    }

    public void removeFavoriteListing(String listingId) {
        favoriteListingIds.remove(listingId);
        System.out.println("Removed listing from favorites: " + listingId);
        notifyListingFavoriteChange(listingId);

        // Also sync with server
        removeFavoriteFromServer(listingId)
                .exceptionally(ex -> {
                    System.err.println("Failed to sync favorite removal to server: " + ex.getMessage());
                    return null;
                });
    }

    public boolean isFavoriteListing(String listingId) {
        return favoriteListingIds.contains(listingId);
    }

    public Set<String> getAllFavoriteListings() {
        return Collections.unmodifiableSet(favoriteListingIds);
    }

    // User favorites
    public void addFavoriteUser(Long userId) {
        favoriteUserIds.add(userId);
        System.out.println("Added user to favorites: " + userId);
        notifyUserFavoriteChange(userId);
    }

    public void removeFavoriteUser(Long userId) {
        favoriteUserIds.remove(userId);
        System.out.println("Removed user from favorites: " + userId);
        notifyUserFavoriteChange(userId);
    }

    public boolean isFavoriteUser(Long userId) {
        return favoriteUserIds.contains(userId);
    }

    public Set<Long> getAllFavoriteUsers() {
        return Collections.unmodifiableSet(favoriteUserIds);
    }

    // === MESSAGE HANDLING ===

    @SuppressWarnings("unchecked")
    private void handleFavoriteMessage(FavoriteMessage message) {
        if (message.getType() == null) {
            System.err.println("Received favorite message with null type: " + message.getErrorMessage());
            if (!message.isSuccess() && futureToComplete != null) {
                futureToComplete.completeExceptionally(
                        new Exception("Server error: " + message.getErrorMessage()));
                futureToComplete = null;
            }
            return;
        }

        switch (message.getType()) {
            case ADD_FAVORITE_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        FavoriteViewModel favoriteViewModel = viewModelMapper.toViewModel(message.getFavorite());
                        userFavorites.add(favoriteViewModel);
                        if (futureToComplete != null) {
                            ((CompletableFuture<FavoriteViewModel>) futureToComplete).complete(favoriteViewModel);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to add favorite: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case REMOVE_FAVORITE_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(true);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to remove favorite: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_USER_FAVORITES_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<FavoriteDTO> favorites = message.getFavorites() != null ? message.getFavorites()
                                : new ArrayList<>();
                        List<FavoriteViewModel> favoriteViewModels = favorites.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        userFavorites.setAll(favoriteViewModels);

                        // Update favorite listings
                        if (message.getFavoriteListings() != null) {
                            favoriteListings.setAll(message.getFavoriteListings());
                        }

                        if (futureToComplete != null) {
                            ((CompletableFuture<List<FavoriteDTO>>) futureToComplete).complete(favorites);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get favorites: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case IS_FAVORITE_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(message.isFavorite());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to check favorite status: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case TOGGLE_FAVORITE_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(message.isFavorite());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to toggle favorite: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            default:
                System.out.println("Unhandled favorite message type: " + message.getType());
                break;
        }

        // Call any registered callback
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    // === LISTENER MANAGEMENT ===

    // Listing favorite listeners
    public void addListingFavoriteChangeListener(Consumer<String> listener) {
        listingFavoriteChangeListeners.add(listener);
    }

    public void removeListingFavoriteChangeListener(Consumer<String> listener) {
        listingFavoriteChangeListeners.remove(listener);
    }

    private void notifyListingFavoriteChange(String listingId) {
        for (Consumer<String> listener : listingFavoriteChangeListeners) {
            try {
                listener.accept(listingId);
            } catch (Exception e) {
                System.err.println("Error in listing favorite change listener: " + e.getMessage());
            }
        }
    }

    // Item favorite listeners
    public void addItemFavoriteChangeListener(Consumer<String> listener) {
        itemFavoriteChangeListeners.add(listener);
    }

    public void removeItemFavoriteChangeListener(Consumer<String> listener) {
        itemFavoriteChangeListeners.remove(listener);
    }

    private void notifyItemFavoriteChange(String itemId) {
        for (Consumer<String> listener : itemFavoriteChangeListeners) {
            try {
                listener.accept(itemId);
            } catch (Exception e) {
                System.err.println("Error in item favorite change listener: " + e.getMessage());
            }
        }
    }

    // User favorite listeners
    public void addUserFavoriteChangeListener(Consumer<Long> listener) {
        userFavoriteChangeListeners.add(listener);
    }

    public void removeUserFavoriteChangeListener(Consumer<Long> listener) {
        userFavoriteChangeListeners.remove(listener);
    }

    private void notifyUserFavoriteChange(Long userId) {
        for (Consumer<Long> listener : userFavoriteChangeListeners) {
            try {
                listener.accept(userId);
            } catch (Exception e) {
                System.err.println("Error in user favorite change listener: " + e.getMessage());
            }
        }
    }

    // === UTILITY METHODS ===

    public void clearAllFavorites() {
        favoriteItemIds.clear();
        favoriteListingIds.clear();
        favoriteUserIds.clear();
        userFavorites.clear();
        favoriteListings.clear();
        System.out.println("Cleared all favorites");
    }

    public int getTotalFavoritesCount() {
        return favoriteItemIds.size() + favoriteListingIds.size() + favoriteUserIds.size();
    }

    // === OBSERVABLE LISTS GETTERS ===

    public ObservableList<FavoriteViewModel> getUserFavoritesList() {
        return userFavorites;
    }

    public ObservableList<ListingDTO> getFavoriteListingsList() {
        return favoriteListings;
    }

    // Set a callback for incoming messages
    public void setMessageCallback(Consumer<FavoriteMessage> callback) {
        this.messageCallback = callback;
    }

    // === SYNC METHODS ===

    public void syncFavoritesFromBackend(Set<String> backendFavoriteItems,
            Set<String> backendFavoriteListings,
            Set<Long> backendFavoriteUsers) {
        // Sync items
        favoriteItemIds.clear();
        favoriteItemIds.addAll(backendFavoriteItems);

        // Sync listings
        favoriteListingIds.clear();
        favoriteListingIds.addAll(backendFavoriteListings);

        // Sync users
        favoriteUserIds.clear();
        favoriteUserIds.addAll(backendFavoriteUsers);

        System.out.println("Synced favorites from backend: " +
                favoriteItemIds.size() + " items, " +
                favoriteListingIds.size() + " listings, " +
                favoriteUserIds.size() + " users");
    }

    public void syncFavoritesToBackend() {
        // TODO: Implement backend sync
        System.out.println("Syncing favorites to backend...");
    }
}
