package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.uninaswap.common.enums.Currency;

/**
 * Represents a listing for trading items with support for pure trade, money offers, or mixed offers
 */
@Entity
@Table(name = "trade_listings")
@DiscriminatorValue("TRADE")
public class TradeListingEntity extends ListingEntity {
    
    // Item trade properties
    @ManyToMany
    @JoinTable(
        name = "trade_desired_items",
        joinColumns = @JoinColumn(name = "listing_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<ItemEntity> desiredItems = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(
        name = "trade_desired_categories",
        joinColumns = @JoinColumn(name = "listing_id")
    )
    @Column(name = "category")
    private List<String> desiredCategories = new ArrayList<>();
    
    // Money offer properties
    @Column(nullable = false)
    private boolean acceptMoneyOffers = false;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal referencePrice;
    
    @Enumerated(EnumType.STRING)
    private Currency currency;
    
    // Mixed offer properties
    @Column(nullable = false)
    private boolean acceptMixedOffers = false;
    
    @Column(nullable = false)
    private boolean acceptOtherOffers = false;
    
    // Default constructor
    public TradeListingEntity() {
        super();
    }
    
    // Constructor with required fields
    public TradeListingEntity(String title, String description, UserEntity creator,
                       List<String> desiredCategories, boolean acceptOtherOffers) {
        super();
        this.setTitle(title);
        this.setDescription(description);
        this.setCreator(creator);
        this.desiredCategories = desiredCategories;
        this.acceptOtherOffers = acceptOtherOffers;
    }
    
    /**
     * Set up money offer settings
     * @param acceptMoneyOffers Whether to accept pure money offers
     * @param referencePrice The reference price for the items
     * @param currency The currency for money offers
     */
    public void setupMoneyOfferSettings(boolean acceptMoneyOffers, BigDecimal referencePrice, Currency currency) {
        this.acceptMoneyOffers = acceptMoneyOffers;
        this.referencePrice = referencePrice;
        this.currency = currency;
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
     * Add a desired item that would be accepted in trade
     * @param item The item to add as desired
     */
    public void addDesiredItem(ItemEntity item) {
        this.desiredItems.add(item);
    }
    
    @Override
    public String getListingType() {
        return "Trade";
    }
    
    @Override
    public String getPriceInfo() {
        StringBuilder sb = new StringBuilder();
        
        // Trade items info
        if (!desiredItems.isEmpty() || !desiredCategories.isEmpty()) {
            sb.append("Trading for: ");
            
            if (!desiredItems.isEmpty()) {
                sb.append(desiredItems.stream()
                    .map(ItemEntity::getName)
                    .collect(Collectors.joining(", ")));
            }
            
            if (!desiredCategories.isEmpty()) {
                if (!desiredItems.isEmpty()) {
                    sb.append(" or ");
                }
                sb.append("categories: ");
                sb.append(String.join(", ", desiredCategories));
            }
        }
        
        // Money offer info
        if (acceptMoneyOffers) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            
            if (referencePrice != null) {
                sb.append("Also accepting money offers (~");
                sb.append(referencePrice).append(" ").append(currency);
                sb.append(")");
            } else {
                sb.append("Also accepting money offers");
            }
        }
        
        // Mixed offer info
        if (acceptMixedOffers) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append("Mixed offers welcome");
        }
        
        // Other offers
        if (acceptOtherOffers) {
            sb.append(" (other offers welcome)");
        }
        
        return sb.toString();
    }
    
    // Getters and Setters for existing fields
    public List<ItemEntity> getDesiredItems() {
        return desiredItems;
    }
    
    public void setDesiredItems(List<ItemEntity> desiredItems) {
        this.desiredItems = desiredItems;
    }
    
    public List<String> getDesiredCategories() {
        return desiredCategories;
    }
    
    public void setDesiredCategories(List<String> desiredCategories) {
        this.desiredCategories = desiredCategories;
    }
    
    public boolean isAcceptOtherOffers() {
        return acceptOtherOffers;
    }
    
    public void setAcceptOtherOffers(boolean acceptOtherOffers) {
        this.acceptOtherOffers = acceptOtherOffers;
    }
    
    // Getters and setters for new fields
    public boolean isAcceptMoneyOffers() {
        return acceptMoneyOffers;
    }
    
    public void setAcceptMoneyOffers(boolean acceptMoneyOffers) {
        this.acceptMoneyOffers = acceptMoneyOffers;
    }
    
    public BigDecimal getReferencePrice() {
        return referencePrice;
    }
    
    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }
    
    public Currency getCurrency() {
        return currency;
    }
    
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    
    public boolean isAcceptMixedOffers() {
        return acceptMixedOffers;
    }
    
    public void setAcceptMixedOffers(boolean acceptMixedOffers) {
        this.acceptMixedOffers = acceptMixedOffers;
    }
}