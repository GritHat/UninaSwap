package com.uninaswap.server.entity;

import jakarta.persistence.*;

/**
 * Join entity that connects ThankYouOffers to Items with quantity
 */
@Entity
@Table(name = "thank_you_offer_items")
public class ThankYouOfferItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "thank_you_offer_id")
    private ThankYouOfferEntity thankYouOffer;
    
    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemEntity item;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    // Default constructor
    public ThankYouOfferItemEntity() {
    }
    
    // Constructor
    public ThankYouOfferItemEntity(ThankYouOfferEntity thankYouOffer, ItemEntity item, Integer quantity) {
        this.thankYouOffer = thankYouOffer;
        this.item = item;
        this.quantity = quantity;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public ThankYouOfferEntity getThankYouOffer() {
        return thankYouOffer;
    }
    
    public void setThankYouOffer(ThankYouOfferEntity thankYouOffer) {
        this.thankYouOffer = thankYouOffer;
    }
    
    public ItemEntity getItem() {
        return item;
    }
    
    public void setItem(ItemEntity item) {
        this.item = item;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}