package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * DTO representing a user's favorite listing
 */
public class FavoriteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String listingId;
    private UserDTO user;
    private ListingDTO listing;
    private LocalDateTime createdAt;

    // Default constructor
    public FavoriteDTO() {
    }

    // Constructor
    public FavoriteDTO(Long userId, String listingId) {
        this.userId = userId;
        this.listingId = listingId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public ListingDTO getListing() {
        return listing;
    }

    public void setListing(ListingDTO listing) {
        this.listing = listing;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}