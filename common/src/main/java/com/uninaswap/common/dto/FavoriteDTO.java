package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * DTO representing a user's favorite listing
 */
/**
 * 
 */
public class FavoriteDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private Long id;
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
    private UserDTO user;
    /**
     * 
     */
    private ListingDTO listing;
    /**
     * 
     */
    private LocalDateTime createdAt;

    // Default constructor
    /**
     * 
     */
    public FavoriteDTO() {
    }

    // Constructor
    /**
     * @param userId
     * @param listingId
     */
    public FavoriteDTO(Long userId, String listingId) {
        this.userId = userId;
        this.listingId = listingId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    /**
     * @return
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
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
    public UserDTO getUser() {
        return user;
    }

    /**
     * @param user
     */
    public void setUser(UserDTO user) {
        this.user = user;
    }

    /**
     * @return
     */
    public ListingDTO getListing() {
        return listing;
    }

    /**
     * @param listing
     */
    public void setListing(ListingDTO listing) {
        this.listing = listing;
    }

    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}