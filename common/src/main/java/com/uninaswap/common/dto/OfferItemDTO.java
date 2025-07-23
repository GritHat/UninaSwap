package com.uninaswap.common.dto;

import com.uninaswap.common.enums.ItemCondition;

/**
 * 
 */
public class OfferItemDTO {
    /**
     * 
     */
    private String itemId;
    /**
     * 
     */
    private String itemName;
    /**
     * 
     */
    private String itemImagePath;
    /**
     * 
     */
    private ItemCondition condition;
    /**
     * 
     */
    private int quantity;
    /**
     * 
     */
    private ItemDTO item;

    /**
     * 
     */
    public OfferItemDTO() {
    }

    /**
     * @param itemId
     * @param itemName
     * @param itemImagePath
     * @param condition
     * @param quantity
     * @param item
     */
    public OfferItemDTO(String itemId, String itemName, String itemImagePath, ItemCondition condition,
            int quantity, ItemDTO item) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemImagePath = itemImagePath;
        this.condition = condition;
        this.quantity = quantity;
        this.item = item;
    }

    
    /**
     * @return
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * @param itemId
     */
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    /**
     * @return
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @param itemName
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * @return
     */
    public String getItemImagePath() {
        return itemImagePath;
    }

    /**
     * @param itemImagePath
     */
    public void setItemImagePath(String itemImagePath) {
        this.itemImagePath = itemImagePath;
    }

    /**
     * @return
     */
    public ItemCondition getCondition() {
        return condition;
    }

    /**
     * @param condition
     */
    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    /**
     * @return
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @return
     */
    public ItemDTO getItem() {
        return item;
    }

    /**
     * @param item
     */
    public void setItem(ItemDTO item) {
        this.item = item;
    }
}