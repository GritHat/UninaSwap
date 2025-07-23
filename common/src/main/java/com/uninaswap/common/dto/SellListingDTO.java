package com.uninaswap.common.dto;

import java.math.BigDecimal;

import com.uninaswap.common.enums.Currency;

/**
 * 
 */
public class SellListingDTO extends ListingDTO {
    /**
     * 
     */
    private BigDecimal price;
    /**
     * 
     */
    private Currency currency;
    /**
     * 
     */
    private String pickupLocation;
    
    // Default constructor
    /**
     * 
     */
    public SellListingDTO() {}
    
    /**
     *
     */
    @Override
    public String getListingTypeValue() {
        return "SELL";
    }
    
    /**
     *
     */
    @Override
    public String getPriceInfo() {
        return price + " " + currency;
    }
    
    // Getters and setters
    /**
     * @return
     */
    public BigDecimal getPrice() {
        return price;
    }
    
    /**
     * @param price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
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
     *
     */
    @Override
    public String getPickupLocation() {
        return pickupLocation;
    }

    /**
     *
     */
    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}