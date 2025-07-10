package com.uninaswap.common.dto;

import com.uninaswap.common.enums.ItemCondition;

public class OfferItemDTO {
    private String itemId;
    private String itemName;
    private String itemImagePath;
    private ItemCondition condition;
    private int quantity;

    public OfferItemDTO(String itemId, String itemName, String itemImagePath, ItemCondition condition,
            int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemImagePath = itemImagePath;
        this.condition = condition;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public ItemCondition getCondition() {
        return condition;
    }

    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}