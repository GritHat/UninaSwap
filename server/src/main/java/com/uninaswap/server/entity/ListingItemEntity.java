package com.uninaswap.server.entity;

import jakarta.persistence.*;

/**
 * Join entity that connects Listings to Items with quantity
 */
@Entity
@Table(name = "listing_items")
public class ListingItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "listing_id")
    private ListingEntity listing;
    
    @ManyToOne
    @JoinColumn(name = "item_id")
    private ItemEntity item;
    
    @Column(nullable = false)
    private Integer quantity = 1;
    
    // Default constructor
    public ListingItemEntity() {
    }
    
    // Constructor
    public ListingItemEntity(ListingEntity listing, ItemEntity item, Integer quantity) {
        this.listing = listing;
        this.item = item;
        this.quantity = quantity;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public ListingEntity getListing() {
        return listing;
    }
    
    public void setListing(ListingEntity listing) {
        this.listing = listing;
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