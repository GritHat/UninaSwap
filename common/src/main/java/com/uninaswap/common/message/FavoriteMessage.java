package com.uninaswap.common.message;

import com.uninaswap.common.dto.FavoriteDTO;
import com.uninaswap.common.dto.ListingDTO;

import java.util.List;

/**
 * 
 */
public class FavoriteMessage extends Message {

    /**
     * 
     */
    public enum Type {
        
        ADD_FAVORITE_REQUEST,
        REMOVE_FAVORITE_REQUEST,
        GET_USER_FAVORITES_REQUEST,
        IS_FAVORITE_REQUEST,
        TOGGLE_FAVORITE_REQUEST,

        
        ADD_FAVORITE_RESPONSE,
        REMOVE_FAVORITE_RESPONSE,
        GET_USER_FAVORITES_RESPONSE,
        IS_FAVORITE_RESPONSE,
        TOGGLE_FAVORITE_RESPONSE
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private Long userId;
    /**
     * 
     */
    private String listingId;
    /**
     * 
     */
    private FavoriteDTO favorite;
    /**
     * 
     */
    private List<FavoriteDTO> favorites;
    /**
     * 
     */
    private List<ListingDTO> favoriteListings;
    /**
     * 
     */
    private boolean favoriteEnabled;
    /**
     * 
     */
    private int page = 0;
    /**
     * 
     */
    private int size = 20;
    /**
     * 
     */
    private long totalElements = 0;
    /**
     * 
     */
    private int totalPages = 0;

    
    /**
     * 
     */
    public FavoriteMessage() {
        super();
        setMessageType("favorite");
    }

    
    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return
     */
    public String getListingId() {
        return listingId;
    }

    /**
     * @param listingId
     */
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    /**
     * @return
     */
    public FavoriteDTO getFavorite() {
        return favorite;
    }

    /**
     * @param favorite
     */
    public void setFavorite(FavoriteDTO favorite) {
        this.favorite = favorite;
    }

    /**
     * @return
     */
    public List<FavoriteDTO> getFavorites() {
        return favorites;
    }

    /**
     * @param favorites
     */
    public void setFavorites(List<FavoriteDTO> favorites) {
        this.favorites = favorites;
    }

    /**
     * @return
     */
    public List<ListingDTO> getFavoriteListings() {
        return favoriteListings;
    }

    /**
     * @param favoriteListings
     */
    public void setFavoriteListings(List<ListingDTO> favoriteListings) {
        this.favoriteListings = favoriteListings;
    }

    /**
     * @return
     */
    public boolean isFavoriteEnabled() {
        return favoriteEnabled;
    }

    /**
     * @param favoriteEnabled
     */
    public void setFavoriteEnabled(boolean favoriteEnabled) {
        this.favoriteEnabled = favoriteEnabled;
    }

    /**
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * @param totalElements
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * @return
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}