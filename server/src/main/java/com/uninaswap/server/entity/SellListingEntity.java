package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.uninaswap.common.enums.Currency;

/**
 * Represents a listing for selling items at a fixed price
 */
@Entity
@Table(name = "sell_listings")
@DiscriminatorValue("SELL")
public class SellListingEntity extends ListingEntity {
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    private String pickupLocation;
    
    // Default constructor
    public SellListingEntity() {
        super();
    }
    
    // Constructor with required fields
    public SellListingEntity(String title, String description, UserEntity creator,
                      BigDecimal price, Currency currency, String pickupLocation) {
        super();
        this.setTitle(title);
        this.setDescription(description);
        this.setCreator(creator);
        this.price = price;
        this.currency = currency;
        this.pickupLocation = pickupLocation;
    }
    
    /**
     * Add an item to this listing with quantity
     * @param item The item to add
     * @param quantity Number of this item to include
     */
    public void addItem(ItemEntity item, int quantity) {
        ListingItemEntity listingItem = new ListingItemEntity(this, item, quantity);
        this.getListingItems().add(listingItem);
    }
    
    /**
     * Calculate the total value of all items in this listing
     * @return Total value
     */
    @Transient
    public BigDecimal getTotalValue() {
        return price;  // For SellListing, the price is the fixed price for all items
    }
    
    @Override
    public String getListingType() {
        return "Sell";
    }
    
    @Override
    public String getPriceInfo() {
        return price + " " + currency;
    }
    
    // Getters and Setters
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    @Override
    public String getPickupLocation() {
        return pickupLocation;
    }

    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}