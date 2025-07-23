package com.uninaswap.common.dto;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.OfferStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class OfferDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String id;
    /**
     * 
     */
    private String listingId;
    /**
     * 
     */
    private ListingDTO listing;
    /**
     * 
     */
    private UserDTO offeringUser;
    /**
     * 
     */
    private LocalDateTime createdAt;
    /**
     * 
     */
    private LocalDateTime updatedAt;
    /**
     * 
     */
    private OfferStatus status;
    /**
     * 
     */
    private DeliveryType deliveryType;

    
    /**
     * 
     */
    private BigDecimal amount;
    /**
     * 
     */
    private Currency currency;

    
    /**
     * 
     */
    private List<OfferItemDTO> offerItems = new ArrayList<>();

    
    /**
     * 
     */
    private String message;

    
    /**
     * 
     */
    public OfferDTO() {
    }

    
    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
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
    public UserDTO getOfferingUser() {
        return offeringUser;
    }

    /**
     * @param user
     */
    public void setOfferingUser(UserDTO user) {
        this.offeringUser = user;
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

    /**
     * @return
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return
     */
    public OfferStatus getStatus() {
        return status;
    }

    /**
     * @param status
     */
    public void setStatus(OfferStatus status) {
        this.status = status;
    }

    /**
     * @return
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * @param amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * @return
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * @param currency
     */
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    /**
     * @return
     */
    public List<OfferItemDTO> getOfferItems() {
        return offerItems;
    }

    /**
     * @param offerItems
     */
    public void setOfferItems(List<OfferItemDTO> offerItems) {
        this.offerItems = offerItems;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
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
    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    /**
     * @param deliveryType
     */
    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }
}
