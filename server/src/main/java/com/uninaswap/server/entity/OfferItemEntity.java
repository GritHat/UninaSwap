package com.uninaswap.server.entity;

import jakarta.persistence.*;

/**
 * Join entity that connects Offers to Items with quantity
 */
@Entity
@Table(name = "offer_items")
public class OfferItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "offer_id")
    private OfferEntity offer;
    
    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemEntity item;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    
    public OfferItemEntity() {
    }
    
    
    public OfferItemEntity(OfferEntity offer, ItemEntity item, Integer quantity) {
        this.offer = offer;
        this.item = item;
        this.quantity = quantity;
    }
    
    
    public Long getId() {
        return id;
    }
    
    public OfferEntity getOffer() {
        return offer;
    }
    
    public void setOffer(OfferEntity offer) {
        this.offer = offer;
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