package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.uninaswap.common.enums.Currency;

/**
 * Represents a listing for auctioning an item with bids
 */
@Entity
@Table(name = "auction_listings")
@DiscriminatorValue("AUCTION")
public class AuctionListingEntity extends ListingEntity {
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal startingPrice;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal reservePrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumBidIncrement;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal currentHighestBid;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highest_bidder_id")
    private UserEntity highestBidder;
    
    // Default constructor
    public AuctionListingEntity() {
        super();
    }
    
    // Constructor with required fields
    public AuctionListingEntity(String title, String description, UserEntity creator, String imagePath,
                         BigDecimal startingPrice, BigDecimal reservePrice, Currency currency,
                         LocalDateTime endTime, BigDecimal minimumBidIncrement) {
        super();
        this.setTitle(title);
        this.setDescription(description);
        this.setCreator(creator);
        this.setImagePath(imagePath);
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.currency = currency;
        this.endTime = endTime;
        this.minimumBidIncrement = minimumBidIncrement;
        this.currentHighestBid = startingPrice;
    }
    
    @Override
    public String getListingType() {
        return "Auction";
    }
    
    @Override
    public String getPriceInfo() {
        if (currentHighestBid != null) {
            return "Current bid: " + currentHighestBid + " " + currency;
        } else {
            return "Starting at: " + startingPrice + " " + currency;
        }
    }
    
    /**
     * Checks if the auction has ended
     */
    @Transient
    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }
    
    // Getters and Setters
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
    
    public UserEntity getHighestBidder() {
        return highestBidder;
    }
    
    public void setHighestBidder(UserEntity highestBidder) {
        this.highestBidder = highestBidder;
    }
}