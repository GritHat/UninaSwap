package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.ThankYouOfferStatus;

/**
 * Represents a thank-you offer for a gift listing
 */
@Entity
@Table(name = "thank_you_offers")
public class ThankYouOfferEntity {
    
    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "gift_listing_id", nullable = false)
    private GiftListingEntity giftListing;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ThankYouOfferStatus status;
    
    // Money component of the thank-you
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private Currency currency;
    
    // Items component of the thank-you
    @OneToMany(mappedBy = "thankYouOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThankYouOfferItemEntity> offerItems = new ArrayList<>();
    
    // Message to the gift giver
    private String message;
    
    // Constructor
    public ThankYouOfferEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ThankYouOfferStatus.PENDING;
    }
    
    /**
     * Add an item to this thank-you offer with quantity
     * @param item The item to add
     * @param quantity Number of this item to include
     */
    public void addItem(ItemEntity item, int quantity) {
        ThankYouOfferItemEntity offerItem = new ThankYouOfferItemEntity(this, item, quantity);
        this.offerItems.add(offerItem);
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public GiftListingEntity getGiftListing() {
        return giftListing;
    }
    
    public void setGiftListing(GiftListingEntity giftListing) {
        this.giftListing = giftListing;
    }
    
    public UserEntity getUser() {
        return user;
    }
    
    public void setUser(UserEntity user) {
        this.user = user;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public ThankYouOfferStatus getStatus() {
        return status;
    }
    
    public void setStatus(ThankYouOfferStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
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
    
    public List<ThankYouOfferItemEntity> getOfferItems() {
        return offerItems;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}