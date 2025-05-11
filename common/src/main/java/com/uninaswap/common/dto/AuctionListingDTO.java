package com.uninaswap.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.uninaswap.common.enums.Currency;

/**
 * DTO representing a listing for auctioning items with bids
 */
public class AuctionListingDTO extends ListingDTO {
    
    private static final long serialVersionUID = 1L;
    
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private Currency currency;
    private LocalDateTime startTime; // Added missing startTime field
    private LocalDateTime endTime;
    private BigDecimal minimumBidIncrement;
    private BigDecimal currentHighestBid;
    private UserDTO highestBidder;
    private int durationInDays; // Added missing durationInDays field
    
    // Default constructor
    public AuctionListingDTO() {
        super();
    }
    
    @Override
    public String getListingTypeValue() {
        return "AUCTION";
    }
    
    @Override
    public String getPriceInfo() {
        if (currentHighestBid != null && currentHighestBid.compareTo(BigDecimal.ZERO) > 0) {
            return "Current bid: " + currentHighestBid + " " + currency;
        } else {
            return "Starting at: " + startingPrice + " " + currency;
        }
    }
    
    /**
     * Checks if the auction has ended
     */
    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }
    
    /**
     * Checks if this auction has met its reserve price
     */
    public boolean isReserveMet() {
        return reservePrice == null || 
               currentHighestBid != null && currentHighestBid.compareTo(reservePrice) >= 0;
    }
    
    /**
     * Calculate minimum next bid amount
     */
    public BigDecimal getMinimumNextBid() {
        if (currentHighestBid != null && currentHighestBid.compareTo(BigDecimal.ZERO) > 0) {
            return currentHighestBid.add(minimumBidIncrement);
        } else {
            return startingPrice;
        }
    }
    
    // Getters and setters
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }
    
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }
    
    public BigDecimal getReservePrice() {
        return reservePrice;
    }
    
    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public BigDecimal getMinimumBidIncrement() {
        return minimumBidIncrement;
    }
    
    public void setMinimumBidIncrement(BigDecimal minimumBidIncrement) {
        this.minimumBidIncrement = minimumBidIncrement;
    }
    
    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }
    
    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }
    
    public UserDTO getHighestBidder() {
        return highestBidder;
    }
    
    public void setHighestBidder(UserDTO highestBidder) {
        this.highestBidder = highestBidder;
    }
    
    public int getDurationInDays() {
        return durationInDays;
    }
    
    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }
}