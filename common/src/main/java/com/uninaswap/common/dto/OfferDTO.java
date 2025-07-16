package com.uninaswap.common.dto;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.OfferStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OfferDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String listingId;
    private ListingDTO listing;
    private UserDTO offeringUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OfferStatus status;

    // Money component
    private BigDecimal amount;
    private Currency currency;

    // Items component
    private List<OfferItemDTO> offerItems = new ArrayList<>();

    // Message
    private String message;

    // Default constructor
    public OfferDTO() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public UserDTO getOfferingUser() {
        return offeringUser;
    }

    public void setOfferingUser(UserDTO user) {
        this.offeringUser = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public List<OfferItemDTO> getOfferItems() {
        return offerItems;
    }

    public void setOfferItems(List<OfferItemDTO> offerItems) {
        this.offerItems = offerItems;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ListingDTO getListing() {
        return listing;
    }

    public void setListing(ListingDTO listing) {
        this.listing = listing;
    }
}
