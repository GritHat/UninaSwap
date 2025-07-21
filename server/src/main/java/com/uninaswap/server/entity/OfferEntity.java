package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.OfferStatus;

/**
 * Represents an offer made on a listing
 */
@Entity
@Table(name = "offers")
public class OfferEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "listing_id", nullable = false)
    private ListingEntity listing;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    // Money component of the offer
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private DeliveryType deliveryType;

    // Items component of the offer - UPDATED to use OfferItem
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferItemEntity> offerItems = new ArrayList<>();

    private String message;

    // Constructor
    public OfferEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = OfferStatus.PENDING;
    }

    /**
     * Add an item to this offer with quantity
     * 
     * @param item     The item to add
     * @param quantity Number of this item to include
     */
    public void addItem(ItemEntity item, int quantity) {
        OfferItemEntity offerItem = new OfferItemEntity(this, item, quantity);
        this.offerItems.add(offerItem);
    }

    /**
     * Remove an item from this offer
     * 
     * @param item The item to remove
     */
    public void removeItem(ItemEntity item) {
        offerItems.removeIf(oi -> oi.getItem().equals(item));
    }

    /**
     * Get all items in this offer
     * 
     * @return List of items (without quantity information)
     */
    @Transient
    public List<ItemEntity> getOfferedItems() {
        return offerItems.stream()
                .map(OfferItemEntity::getItem)
                .collect(Collectors.toList());
    }

    /**
     * Get the quantity of a specific item in this offer
     * 
     * @param item The item to check
     * @return The quantity, or 0 if not found
     */
    @Transient
    public int getItemQuantity(ItemEntity item) {
        return offerItems.stream()
                .filter(oi -> oi.getItem().equals(item))
                .mapToInt(OfferItemEntity::getQuantity)
                .findFirst()
                .orElse(0);
    }

    /**
     * Get the total number of items in this offer (sum of quantities)
     * 
     * @return Total item count
     */
    @Transient
    public int getTotalItemCount() {
        return offerItems.stream()
                .mapToInt(OfferItemEntity::getQuantity)
                .sum();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public ListingEntity getListing() {
        return listing;
    }

    public void setListing(ListingEntity listing) {
        this.listing = listing;
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

    public List<OfferItemEntity> getOfferItems() {
        return offerItems;
    }

    public void setOfferItems(List<OfferItemEntity> offerItems) {
        this.offerItems = offerItems;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}