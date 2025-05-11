package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.uninaswap.common.enums.ListingStatus;

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
    private String id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO creator;
    private ListingStatus status;
    private boolean featured;
    private List<ListingItemDTO> items = new ArrayList<>();
    
    // Default constructor
    public ListingDTO() {}
    
    // Define abstract methods to be implemented by subclasses
    public abstract String getListingType();
    public abstract String getPriceInfo();
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public UserDTO getCreator() {
        return creator;
    }
    
    public void setCreator(UserDTO creator) {
        this.creator = creator;
    }
    
    public ListingStatus getStatus() {
        return status;
    }
    
    public void setStatus(ListingStatus status) {
        this.status = status;
    }
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
    public List<ListingItemDTO> getItems() {
        return items;
    }
    
    public void setItems(List<ListingItemDTO> items) {
        this.items = items;
    }
    
    public void addItem(ListingItemDTO item) {
        this.items.add(item);
    }
}