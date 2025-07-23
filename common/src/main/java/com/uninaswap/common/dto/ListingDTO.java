package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.uninaswap.common.enums.ListingStatus;

/**
 * 
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "listingType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SellListingDTO.class, name = "SELL"),
    @JsonSubTypes.Type(value = TradeListingDTO.class, name = "TRADE"),
    @JsonSubTypes.Type(value = GiftListingDTO.class, name = "GIFT"),
    @JsonSubTypes.Type(value = AuctionListingDTO.class, name = "AUCTION")
})
public abstract class ListingDTO implements Serializable {
    /**
     * 
     */
    private String id;
    /**
     * 
     */
    private String title;
    /**
     * 
     */
    private String description;
    /**
     * 
     */
    private LocalDateTime createdAt;
    /**
     * 
     */
    private LocalDateTime updatedAt;
    /**
     * 
     */
    private UserDTO creator;
    /**
     * 
     */
    private ListingStatus status;
    /**
     * 
     */
    private boolean featured;
    /**
     * 
     */
    private List<ListingItemDTO> items = new ArrayList<>();
    
    // Default constructor
    /**
     * 
     */
    public ListingDTO() {}
    
    // Define abstract methods to be implemented by subclasses
    /**
     * @return
     */
    public abstract String getListingTypeValue();
    /**
     * @return
     */
    public abstract String getPriceInfo();
    
    // Getters and setters
    /**
     * @return
     */
    public String getId() {
        return id;
    }
    
    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @return
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * @return
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * @return
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * @return
     */
    public UserDTO getCreator() {
        return creator;
    }
    
    /**
     * @param creator
     */
    public void setCreator(UserDTO creator) {
        this.creator = creator;
    }
    
    /**
     * @return
     */
    public ListingStatus getStatus() {
        return status;
    }
    
    /**
     * @param status
     */
    public void setStatus(ListingStatus status) {
        this.status = status;
    }
    
    /**
     * @return
     */
    public boolean isFeatured() {
        return featured;
    }
    
    /**
     * @param featured
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
    /**
     * @return
     */
    public List<ListingItemDTO> getItems() {
        return items;
    }
    
    /**
     * @param items
     */
    public void setItems(List<ListingItemDTO> items) {
        this.items = items;
    }
    
    /**
     * @param item
     */
    public void addItem(ListingItemDTO item) {
        this.items.add(item);
    }

    /**
     * @return
     */
    public abstract String getPickupLocation();
    /**
     * @param pickupLocation
     */
    public abstract void setPickupLocation(String pickupLocation);
}