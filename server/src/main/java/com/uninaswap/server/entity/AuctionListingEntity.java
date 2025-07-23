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
    private LocalDateTime startTime; // Added missing startTime field
    
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumBidIncrement;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal currentHighestBid;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highest_bidder_id")
    private UserEntity highestBidder;
    
    @Column(nullable = false)
    private boolean hasBids = false;
    
    private String pickupLocation;

    // Default constructor
    public AuctionListingEntity() {
        super();
        this.startTime = LocalDateTime.now(); // Initialize start time to now by default
    }
    
    // Constructor with required fields
    public AuctionListingEntity(String title, String description, UserEntity creator, String imagePath,
                         BigDecimal startingPrice, BigDecimal reservePrice, Currency currency,
                         LocalDateTime endTime, BigDecimal minimumBidIncrement, String pickupLocation) {
        super();
        this.setTitle(title);
        this.setDescription(description);
        this.setCreator(creator);
        this.setImagePath(imagePath);
        this.startingPrice = startingPrice;
        this.reservePrice = reservePrice;
        this.currency = currency;
        this.startTime = LocalDateTime.now(); // Set start time to now
        this.endTime = endTime;
        this.minimumBidIncrement = minimumBidIncrement;
        this.pickupLocation = pickupLocation;
        // Do not set current highest bid yet - it will be null until first bid
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
     * Place a bid on this auction
     * @param bidder The user placing the bid
     * @param bidAmount The bid amount
     * @return true if bid was successful, false otherwise
     */
    public boolean placeBid(UserEntity bidder, BigDecimal bidAmount) {
        // Validate bid amount
        if (bidAmount == null || bidAmount.compareTo(getMinimumNextBid()) < 0) {
            return false;
        }
        
        // Check if auction has ended
        if (isEnded()) {
            return false;
        }
        
        // Update bid info
        this.currentHighestBid = bidAmount;
        this.highestBidder = bidder;
        this.hasBids = true;
        return true;
    }
    
    /**
     * Get the minimum amount required for the next bid
     * @return The minimum valid next bid amount
     */
    @Transient
    public BigDecimal getMinimumNextBid() {
        if (currentHighestBid != null) {
            return currentHighestBid.add(minimumBidIncrement);
        } else {
            return startingPrice;
        }
    }
    
    /**
     * Check if the reserve price has been met by bids
     * @return true if reserve is met or no reserve set
     */
    @Transient
    public boolean isReserveMet() {
        if (reservePrice == null) {
            return true;
        }
        return currentHighestBid != null && currentHighestBid.compareTo(reservePrice) >= 0;
    }
    
    @Override
    public String getListingType() {
        return "AUCTION";
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
    
    public UserEntity getHighestBidder() {
        return highestBidder;
    }
    
    public void setHighestBidder(UserEntity highestBidder) {
        this.highestBidder = highestBidder;
    }
    
    public boolean getHasBids() {
        return hasBids;
    }
    
    public void setHasBids(boolean hasBids) {
        this.hasBids = hasBids;
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