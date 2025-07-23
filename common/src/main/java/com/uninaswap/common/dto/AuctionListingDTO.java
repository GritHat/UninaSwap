package com.uninaswap.common.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.uninaswap.common.enums.Currency;

/**
 * DTO representing a listing for auctioning items with bids
 */
/**
 * 
 */
public class AuctionListingDTO extends ListingDTO {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 
     */
    private BigDecimal startingPrice;
    /**
     * 
     */
    private BigDecimal reservePrice;
    /**
     * 
     */
    private Currency currency;
    /**
     * 
     */
    private LocalDateTime startTime; // Added missing startTime field
    /**
     * 
     */
    private LocalDateTime endTime;
    /**
     * 
     */
    private BigDecimal minimumBidIncrement;
    /**
     * 
     */
    private BigDecimal currentHighestBid;
    /**
     * 
     */
    private UserDTO highestBidder;
    /**
     * 
     */
    private int durationInDays; // Added missing durationInDays field
    /**
     * 
     */
    private boolean anyBids = false; // Indicates if there are any bids
    /**
     * 
     */
    private String pickupLocation; // Added pickup location field
    
    // Default constructor
    /**
     * 
     */
    public AuctionListingDTO() {
        super();
    }
    
    /**
     *
     */
    @Override
    public String getListingTypeValue() {
        return "AUCTION";
    }
    
    /**
     *
     */
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
    /**
     * @return
     */
    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }
    
    /**
     * Checks if this auction has met its reserve price
     */
    /**
     * @return
     */
    public boolean isReserveMet() {
        return reservePrice == null || 
               currentHighestBid != null && currentHighestBid.compareTo(reservePrice) >= 0;
    }
    
    /**
     * Calculate minimum next bid amount
     */
    /**
     * @return
     */
    public BigDecimal getMinimumNextBid() {
        if (currentHighestBid != null && currentHighestBid.compareTo(BigDecimal.ZERO) > 0) {
            return currentHighestBid.add(minimumBidIncrement);
        } else {
            return startingPrice;
        }
    }
    
    // Getters and setters
    /**
     * @return
     */
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }
    
    /**
     * @param startingPrice
     */
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }
    
    /**
     * @return
     */
    public BigDecimal getReservePrice() {
        return reservePrice;
    }
    
    /**
     * @param reservePrice
     */
    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
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
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * @param startTime
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    /**
     * @return
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * @param endTime
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    /**
     * @return
     */
    public BigDecimal getMinimumBidIncrement() {
        return minimumBidIncrement;
    }
    
    /**
     * @param minimumBidIncrement
     */
    public void setMinimumBidIncrement(BigDecimal minimumBidIncrement) {
        this.minimumBidIncrement = minimumBidIncrement;
    }
    
    /**
     * @return
     */
    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }
    
    /**
     * @param currentHighestBid
     */
    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }
    
    /**
     * @return
     */
    public UserDTO getHighestBidder() {
        return highestBidder;
    }
    
    /**
     * @param highestBidder
     */
    public void setHighestBidder(UserDTO highestBidder) {
        this.highestBidder = highestBidder;
    }
    
    /**
     * @return
     */
    public int getDurationInDays() {
        return durationInDays;
    }
    
    /**
     * @param durationInDays
     */
    public void setDurationInDays(int durationInDays) {
        this.durationInDays = durationInDays;
    }

    /**
     * @return
     */
    public boolean hasAnyBids() {
        return anyBids;
    }

    /**
     * @param hasBids
     */
    public void setAnyBids(boolean hasBids) {
        this.anyBids = hasBids;
    }

    /**
     *
     */
    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    /**
     *
     */
    @Override
    public String getPickupLocation() {
        return pickupLocation;
    }
}