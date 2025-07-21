package com.uninaswap.common.message;

import com.uninaswap.common.dto.FavoriteDTO;
import com.uninaswap.common.dto.ListingDTO;

import java.util.List;

public class FavoriteMessage extends Message {

    public enum Type {
        // Requests
        ADD_FAVORITE_REQUEST,
        REMOVE_FAVORITE_REQUEST,
        GET_USER_FAVORITES_REQUEST,
        IS_FAVORITE_REQUEST,
        TOGGLE_FAVORITE_REQUEST,

        // Responses
        ADD_FAVORITE_RESPONSE,
        REMOVE_FAVORITE_RESPONSE,
        GET_USER_FAVORITES_RESPONSE,
        IS_FAVORITE_RESPONSE,
        TOGGLE_FAVORITE_RESPONSE
    }

    private Type type;
    private Long userId;
    private String listingId;
    private FavoriteDTO favorite;
    private List<FavoriteDTO> favorites;
    private List<ListingDTO> favoriteListings;
    private boolean favoriteEnabled;
    private int page = 0;
    private int size = 20;
    private long totalElements = 0;
    private int totalPages = 0;

    // Default constructor
    public FavoriteMessage() {
        super();
        setMessageType("favorite");
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public FavoriteDTO getFavorite() {
        return favorite;
    }

    public void setFavorite(FavoriteDTO favorite) {
        this.favorite = favorite;
    }

    public List<FavoriteDTO> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavoriteDTO> favorites) {
        this.favorites = favorites;
    }

    public List<ListingDTO> getFavoriteListings() {
        return favoriteListings;
    }

    public void setFavoriteListings(List<ListingDTO> favoriteListings) {
        this.favoriteListings = favoriteListings;
    }

    public boolean isFavoriteEnabled() {
        return favoriteEnabled;
    }

    public void setFavoriteEnabled(boolean favoriteEnabled) {
        this.favoriteEnabled = favoriteEnabled;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}