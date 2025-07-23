package com.uninaswap.common.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.uninaswap.common.enums.Currency;

/**
 * DTO representing a listing for trading items with support for various offer types
 */
/**
 * 
 */
public class TradeListingDTO extends ListingDTO {
    
    // Item trade properties
    /**
     * 
     */
    private List<ItemDTO> desiredItems = new ArrayList<>();
    /**
     * 
     */
    private List<String> desiredCategories = new ArrayList<>();
    
    // Money offer properties
    /**
     * 
     */
    private boolean acceptMoneyOffers = false;
    /**
     * 
     */
    private BigDecimal referencePrice;
    /**
     * 
     */
    private Currency currency;
    
    // Mixed offer properties
    /**
     * 
     */
    private boolean acceptMixedOffers = false;
    /**
     * 
     */
    private boolean acceptOtherOffers = false;

    /**
     * 
     */
    private String pickupLocation;
    
    // Default constructor
    /**
     * 
     */
    public TradeListingDTO() {
        super();
    }
    
    /**
     *
     */
    @Override
    public String getListingTypeValue() {
        return "TRADE";
    }
    
    /**
     *
     */
    @Override
    public String getPriceInfo() {
        StringBuilder sb = new StringBuilder();
        
        // Trade items info
        if (!desiredItems.isEmpty() || !desiredCategories.isEmpty()) {
            sb.append("Trading for: ");
            
            if (!desiredItems.isEmpty()) {
                sb.append(desiredItems.stream()
                    .map(ItemDTO::getName)
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
    
    // Getters and setters
    /**
     * @return
     */
    public List<ItemDTO> getDesiredItems() {
        return desiredItems;
    }
    
    /**
     * @param desiredItems
     */
    public void setDesiredItems(List<ItemDTO> desiredItems) {
        this.desiredItems = desiredItems;
    }
    
    /**
     * @return
     */
    public List<String> getDesiredCategories() {
        return desiredCategories;
    }
    
    /**
     * @param desiredCategories
     */
    public void setDesiredCategories(List<String> desiredCategories) {
        this.desiredCategories = desiredCategories;
    }
    
    /**
     * @return
     */
    public boolean isAcceptMoneyOffers() {
        return acceptMoneyOffers;
    }
    
    /**
     * @param acceptMoneyOffers
     */
    public void setAcceptMoneyOffers(boolean acceptMoneyOffers) {
        this.acceptMoneyOffers = acceptMoneyOffers;
    }
    
    /**
     * @return
     */
    public BigDecimal getReferencePrice() {
        return referencePrice;
    }
    
    /**
     * @param referencePrice
     */
    public void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
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
    public boolean isAcceptMixedOffers() {
        return acceptMixedOffers;
    }
    
    /**
     * @param acceptMixedOffers
     */
    public void setAcceptMixedOffers(boolean acceptMixedOffers) {
        this.acceptMixedOffers = acceptMixedOffers;
    }
    
    /**
     * @return
     */
    public boolean isAcceptOtherOffers() {
        return acceptOtherOffers;
    }
    
    /**
     * @param acceptOtherOffers
     */
    public void setAcceptOtherOffers(boolean acceptOtherOffers) {
        this.acceptOtherOffers = acceptOtherOffers;
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