package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.FavoriteViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
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

/**
 * 
 */
public class FavoritesService {
    /**
     * 
     */
    private static FavoritesService instance;

    /**
     * 
     */
    private final WebSocketClient webSocketClient = WebSocketClient.getInstance();
    /**
     * 
     */
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    /**
     * 
     */
    private final Set<String> favoriteItemIds = new HashSet<>();
    /**
     * 
     */
    private final Set<String> favoriteListingIds = new HashSet<>();
    /**
     * 
     */
    private final Set<Long> favoriteUserIds = new HashSet<>();
    /**
     * 
     */
    private final ObservableList<FavoriteViewModel> userFavorites = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<ListingDTO> favoriteListings = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<ListingViewModel> favoriteListingViewModels = FXCollections.observableArrayList();
    /**
     * 
     */
    private final List<Consumer<String>> listingFavoriteChangeListeners = new ArrayList<>();
    /**
     * 
     */
    private final List<Consumer<String>> itemFavoriteChangeListeners = new ArrayList<>();
    /**
     * 
     */
    private final List<Consumer<Long>> userFavoriteChangeListeners = new ArrayList<>();

    /**
     * 
     */
    private CompletableFuture<?> futureToComplete;
    /**
     * 
     */
    private Consumer<FavoriteMessage> messageCallback;

    /**
     * 
     */
    private FavoritesService() {
        webSocketClient.registerMessageHandler(FavoriteMessage.class, this::handleFavoriteMessage);
    }

    /**
     * @return
     */
    public static synchronized FavoritesService getInstance() {
        if (instance == null) {
            instance = new FavoritesService();
        }
        return instance;
    }

    /**
     * Add a listing to favorites on server
     * 
     * @param listingId The ID of the listing to favorite
     * @return A CompletableFuture that completes with the FavoriteViewModel if the
     *         request is successful, or fails with an exception if the connection
     *         fails or the request is invalid.
     */
    /**
     * @param listingId
     * @return
     */
    public CompletableFuture<FavoriteViewModel> addFavoriteToServer(String listingId) {
        CompletableFuture<FavoriteViewModel> future = new CompletableFuture<>();
        System.err.println("Adding favorite to server: " + listingId);
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
     * 
     * @param listingId The ID of the listing to unfavorite
     * @return A CompletableFuture that completes with true if the request is successful,
     *         or false if the listing was not favorited or the request failed.
     */
    /**
     * @param listingId
     * @return
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
     * 
     * @return A CompletableFuture that completes with a list of FavoriteDTOs if the request is successful,
     *         or fails with an exception if the connection fails or the request is invalid.
     *         This method retrieves all favorites for the current user, including listings, items, and
     */
    /**
     * @return
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
     * 
     * @param listingId The ID of the listing to check
     * @return A CompletableFuture that completes with true if the listing is favorited,
     *         or false if it is not favorited or the request failed.
     *         This method checks the server to see if the listing is in the user's favorites.
     *         It does not check local favorites, so it should be used when the server state
     *         needs to be verified (e.g., after a sync or when the UI is initialized).
     */
    /**
     * @param listingId
     * @return
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
     * 
     * @param listingId The ID of the listing to toggle favorite status
     * @return A CompletableFuture that completes with true if the listing is now favorited,
     *         or false if it is now unfavorited, or fails with an exception if the request fails.
     *         This method toggles the favorite status of a listing on the server
     */
    /**
     * @param listingId
     * @return
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

    /**
     * Set a callback to be called when a favorite message is received
     * 
     * @param callback The callback to set
     */
    /**
     * @param itemId
     */
    public void addFavorite(String itemId) {
        favoriteItemIds.add(itemId);
        System.out.println("Added item to favorites: " + itemId);
        notifyItemFavoriteChange(itemId);
    }

    /**
     * Remove an item from favorites
     * 
     * @param itemId The ID of the item to remove from favorites
     */
    /**
     * @param itemId
     */
    public void removeFavorite(String itemId) {
        favoriteItemIds.remove(itemId);
        System.out.println("Removed item from favorites: " + itemId);
        notifyItemFavoriteChange(itemId);
    }

    /**
     * @param itemId
     * @return
     */
    public boolean isFavorite(String itemId) {
        return favoriteItemIds.contains(itemId);
    }

    /**
     * @return
     */
    public Set<String> getAllFavorites() {
        return Collections.unmodifiableSet(favoriteItemIds);
    }

    /**
     * @param listingId
     */
    public void addFavoriteListing(String listingId) {
        favoriteListingIds.add(listingId);
        System.out.println("Added listing to favorites: " + listingId);
        notifyListingFavoriteChange(listingId);
        addFavoriteToServer(listingId)
                .exceptionally(ex -> {
                    System.err.println("Failed to sync favorite to server: " + ex.getMessage());
                    return null;
                });
    }

    /**
     * @param listingId
     */
    public void removeFavoriteListing(String listingId) {
        favoriteListingIds.remove(listingId);
        System.out.println("Removed listing from favorites: " + listingId);
        notifyListingFavoriteChange(listingId);
        removeFavoriteFromServer(listingId)
                .exceptionally(ex -> {
                    System.err.println("Failed to sync favorite removal to server: " + ex.getMessage());
                    return null;
                });
    }

    /**
     * @param listingId
     * @return
     */
    public boolean isFavoriteListing(String listingId) {
        return favoriteListingIds.contains(listingId);
    }

    /**
     * @return
     */
    public Set<String> getAllFavoriteListings() {
        return Collections.unmodifiableSet(favoriteListingIds);
    }

    /**
     * @param userId
     */
    public void addFavoriteUser(Long userId) {
        favoriteUserIds.add(userId);
        System.out.println("Added user to favorites: " + userId);
        notifyUserFavoriteChange(userId);
    }

    /**
     * @param userId
     */
    public void removeFavoriteUser(Long userId) {
        favoriteUserIds.remove(userId);
        System.out.println("Removed user from favorites: " + userId);
        notifyUserFavoriteChange(userId);
    }

    /**
     * @param userId
     * @return
     */
    public boolean isFavoriteUser(Long userId) {
        return favoriteUserIds.contains(userId);
    }

    /**
     * @return
     */
    public Set<Long> getAllFavoriteUsers() {
        return Collections.unmodifiableSet(favoriteUserIds);
    }

    /**
     * @param listener
     */
    public void addListingFavoriteChangeListener(Consumer<String> listener) {
        listingFavoriteChangeListeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeListingFavoriteChangeListener(Consumer<String> listener) {
        listingFavoriteChangeListeners.remove(listener);
    }

    /**
     * @param listingId
     */
    private void notifyListingFavoriteChange(String listingId) {
        for (Consumer<String> listener : listingFavoriteChangeListeners) {
            try {
                listener.accept(listingId);
            } catch (Exception e) {
                System.err.println("Error in listing favorite change listener: " + e.getMessage());
            }
        }
    }

    /**
     * @param listener
     */
    public void addItemFavoriteChangeListener(Consumer<String> listener) {
        itemFavoriteChangeListeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeItemFavoriteChangeListener(Consumer<String> listener) {
        itemFavoriteChangeListeners.remove(listener);
    }

    /**
     * @param itemId
     */
    private void notifyItemFavoriteChange(String itemId) {
        for (Consumer<String> listener : itemFavoriteChangeListeners) {
            try {
                listener.accept(itemId);
            } catch (Exception e) {
                System.err.println("Error in item favorite change listener: " + e.getMessage());
            }
        }
    }
    /**
     * @param listener
     */
    public void addUserFavoriteChangeListener(Consumer<Long> listener) {
        userFavoriteChangeListeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeUserFavoriteChangeListener(Consumer<Long> listener) {
        userFavoriteChangeListeners.remove(listener);
    }

    /**
     * @param userId
     */
    private void notifyUserFavoriteChange(Long userId) {
        for (Consumer<Long> listener : userFavoriteChangeListeners) {
            try {
                listener.accept(userId);
            } catch (Exception e) {
                System.err.println("Error in user favorite change listener: " + e.getMessage());
            }
        }
    }

    /**
     * 
     */
    public void clearAllFavorites() {
        favoriteItemIds.clear();
        favoriteListingIds.clear();
        favoriteUserIds.clear();
        userFavorites.clear();
        favoriteListings.clear();
        System.out.println("Cleared all favorites");
    }

    /**
     * @return
     */
    public int getTotalFavoritesCount() {
        return favoriteItemIds.size() + favoriteListingIds.size() + favoriteUserIds.size();
    }

    /**
     * @return
     */
    public ObservableList<FavoriteViewModel> getUserFavoritesList() {
        if (userFavorites.isEmpty()) {
            refreshUserFavorites();
        }
        return userFavorites;
    }

    /**
     * @return
     */
    public ObservableList<ListingDTO> getFavoriteListingsList() {
        if (favoriteListings.isEmpty()) {
            refreshUserFavorites();
        }
        return favoriteListings;
    }

    /**
     * @return
     */
    public ObservableList<ListingViewModel> getFavoriteListingViewModels() {
        if (favoriteListingViewModels.isEmpty()) {
            refreshUserFavorites();
        }
        return favoriteListingViewModels;
    }

    /**
     * 
     */
    public void refreshUserFavorites() {
        getUserFavorites()
                .thenAccept(favorites -> {
                    Platform.runLater(() -> {
                        List<FavoriteViewModel> favoriteViewModels = favorites.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        userFavorites.setAll(favoriteViewModels);
                        List<ListingDTO> listings = favorites.stream()
                                .map(FavoriteDTO::getListing)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        favoriteListings.setAll(listings);
                        List<ListingViewModel> listingViewModels = listings.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        favoriteListingViewModels.setAll(listingViewModels);
                        syncLocalFavoriteIds(favorites);
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Error refreshing favorites: " + ex.getMessage());
                    return null;
                });
    }

    /**
     * @param favorites
     */
    private void syncLocalFavoriteIds(List<FavoriteDTO> favorites) {
        Set<String> serverListingIds = favorites.stream()
                .map(FavoriteDTO::getListingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        favoriteListingIds.clear();
        favoriteListingIds.addAll(serverListingIds);
    }

    /**
     * @param message
     */
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

                        if (favoriteViewModel.getListing() != null) {
                            favoriteListings.add(message.getFavorite().getListing());
                            favoriteListingViewModels.add(favoriteViewModel.getListing());
                        }
                        if (message.getFavorite().getListingId() != null) {
                            favoriteListingIds.add(message.getFavorite().getListingId());
                        }

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
                        String listingId = message.getListingId();
                        userFavorites.removeIf(fav -> fav.getListingId().equals(listingId));
                        favoriteListings.removeIf(listing -> listing.getId().equals(listingId));
                        favoriteListingViewModels.removeIf(listing -> listing.getId().equals(listingId));
                        favoriteListingIds.remove(listingId);

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

                        List<ListingDTO> listings = favorites.stream()
                                .map(FavoriteDTO::getListing)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        favoriteListings.setAll(listings);

                        List<ListingViewModel> listingViewModels = listings.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        favoriteListingViewModels.setAll(listingViewModels);
                        syncLocalFavoriteIds(favorites);

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
                            ((CompletableFuture<Boolean>) futureToComplete).complete(message.isFavoriteEnabled());
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
                            ((CompletableFuture<Boolean>) futureToComplete).complete(message.isFavoriteEnabled());
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
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }
}
