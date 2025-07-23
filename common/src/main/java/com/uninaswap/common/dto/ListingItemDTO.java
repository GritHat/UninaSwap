package com.uninaswap.common.dto;

import java.io.Serializable;

/**
 * 
 */
public class ListingItemDTO implements Serializable {
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
    private String itemCategory;
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
    public ListingItemDTO() {
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
    public String getItemCategory() {
        return itemCategory;
    }

    /**
     * @param itemCategory
     */
    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
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