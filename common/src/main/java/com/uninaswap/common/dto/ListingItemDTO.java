package com.uninaswap.common.dto;

import java.io.Serializable;

public class ListingItemDTO implements Serializable {
    private String itemId;
    private Integer quantity;
    private String itemName;  // For display convenience
    private String itemImagePath;  // For display convenience
    
    // Default constructor
    public ListingItemDTO() {}
    
    // Getters and setters
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public String getItemImagePath() {
        return itemImagePath;
    }
    
    public void setItemImagePath(String itemImagePath) {
        this.itemImagePath = itemImagePath;
    }
}