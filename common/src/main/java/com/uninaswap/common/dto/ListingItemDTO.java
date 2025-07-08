package com.uninaswap.common.dto;

import java.io.Serializable;

public class ListingItemDTO implements Serializable {
    private String itemId;
    private String itemName;
    private String itemImagePath;
    private String itemCategory; // Add this if not present
    private int quantity;

    // Default constructor
    public ListingItemDTO() {
    }

    // Getters and setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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

    // Add getter/setter for category if needed
    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }
}