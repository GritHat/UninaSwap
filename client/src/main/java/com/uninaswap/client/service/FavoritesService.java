package com.uninaswap.client.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FavoritesService {
    private static FavoritesService instance;
    private final Set<String> favoriteItemIds;

    private FavoritesService() {
        this.favoriteItemIds = new HashSet<>();
        // TODO: Carica i preferiti dal backend o dal file locale se necessario
    }

    public static FavoritesService getInstance() {
        if (instance == null) {
            instance = new FavoritesService();
        }
        return instance;
    }

    public void addFavorite(String itemId) {
        favoriteItemIds.add(itemId);
        // TODO: Salva sul backend o in locale se necessario
    }

    public void removeFavorite(String itemId) {
        favoriteItemIds.remove(itemId);
        // TODO: Aggiorna backend o locale se necessario
    }

    public boolean isFavorite(String itemId) {
        return favoriteItemIds.contains(itemId);
    }

    public Set<String> getAllFavorites() {
        return Collections.unmodifiableSet(favoriteItemIds);
    }

    // Optional: per sincronizzare con backend
    // public void syncFavoritesFromBackend(List<String> backendFavorites) { ... }
}
