package com.uninaswap.common.dto;

import java.math.BigDecimal;

import com.uninaswap.common.enums.Currency;

public class SellListingDTO extends ListingDTO {
    private BigDecimal price;
    private Currency currency;
    
    // Default constructor
    public SellListingDTO() {}
    
    @Override
    public String getListingType() {
        return "SELL";
    }
    
    @Override
    public String getPriceInfo() {
        return price + " " + currency;
    }
    
    // Getters and setters
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
}