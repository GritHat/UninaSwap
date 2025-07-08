package com.uninaswap.client.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class FavoritesService {
    private static FavoritesService instance;
    private final Set<String> favoriteItemIds;
    private final Set<String> favoriteListingIds; // NEW: For listings
    private final Set<Long> favoriteUserIds; // NEW: For users

    // NEW: Listener functionality
    private final List<Consumer<String>> listingFavoriteChangeListeners = new ArrayList<>();
    private final List<Consumer<String>> itemFavoriteChangeListeners = new ArrayList<>();
    private final List<Consumer<Long>> userFavoriteChangeListeners = new ArrayList<>();

    private FavoritesService() {
        this.favoriteItemIds = new HashSet<>();
        this.favoriteListingIds = new HashSet<>(); // Initialize listing favorites
        this.favoriteUserIds = new HashSet<>(); // Initialize user favorites
        // TODO: Load favorites from backend or local file if needed
    }

    public static FavoritesService getInstance() {
        if (instance == null) {
            instance = new FavoritesService();
        }
        return instance;
    }

    // === ITEM FAVORITES (existing) ===
    public void addFavorite(String itemId) {
        favoriteItemIds.add(itemId);
        // TODO: Save to backend or locally if needed
        System.out.println("Added item to favorites: " + itemId);
        notifyItemFavoriteChange(itemId);
    }

    public void removeFavorite(String itemId) {
        favoriteItemIds.remove(itemId);
        // TODO: Update backend or locally if needed
        System.out.println("Removed item from favorites: " + itemId);
        notifyItemFavoriteChange(itemId);
    }

    public boolean isFavorite(String itemId) {
        return favoriteItemIds.contains(itemId);
    }

    public Set<String> getAllFavorites() {
        return Collections.unmodifiableSet(favoriteItemIds);
    }

    // === LISTING FAVORITES (new) ===
    public void addFavoriteListing(String listingId) {
        favoriteListingIds.add(listingId);
        // TODO: Save to backend or locally if needed
        System.out.println("Added listing to favorites: " + listingId);
        notifyListingFavoriteChange(listingId);
    }

    public void removeFavoriteListing(String listingId) {
        favoriteListingIds.remove(listingId);
        // TODO: Update backend or locally if needed
        System.out.println("Removed listing from favorites: " + listingId);
        notifyListingFavoriteChange(listingId);
    }

    public boolean isFavoriteListing(String listingId) {
        return favoriteListingIds.contains(listingId);
    }

    public Set<String> getAllFavoriteListings() {
        return Collections.unmodifiableSet(favoriteListingIds);
    }

    // === USER FAVORITES (new) ===
    public void addFavoriteUser(Long userId) {
        favoriteUserIds.add(userId);
        // TODO: Save to backend or locally if needed
        System.out.println("Added user to favorites: " + userId);
        notifyUserFavoriteChange(userId);
    }

    public void removeFavoriteUser(Long userId) {
        favoriteUserIds.remove(userId);
        // TODO: Update backend or locally if needed
        System.out.println("Removed user from favorites: " + userId);
        notifyUserFavoriteChange(userId);
    }

    public boolean isFavoriteUser(Long userId) {
        return favoriteUserIds.contains(userId);
    }

    public Set<Long> getAllFavoriteUsers() {
        return Collections.unmodifiableSet(favoriteUserIds);
    }

    // === LISTENER MANAGEMENT (new) ===

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
        System.out.println("Cleared all favorites");

        // Notify listeners about clearing (optional)
        // You could implement this if needed
    }

    public int getTotalFavoritesCount() {
        return favoriteItemIds.size() + favoriteListingIds.size() + favoriteUserIds.size();
    }

    // === BACKEND SYNC METHODS (for future implementation) ===
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
