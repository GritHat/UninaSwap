package com.uninaswap.common.message;

import java.util.List;

import com.uninaswap.common.dto.ItemDTO;

public class ItemMessage extends Message {
    public enum Type {
        GET_ITEMS_REQUEST,
        GET_ITEMS_RESPONSE,
        ADD_ITEM_REQUEST,
        ADD_ITEM_RESPONSE,
        UPDATE_ITEM_REQUEST,
        UPDATE_ITEM_RESPONSE,
        DELETE_ITEM_REQUEST,
        DELETE_ITEM_RESPONSE
    }
    
    private Type type;
    private ItemDTO item;
    private List<ItemDTO> items;
    private String errorMessage;
    private boolean success;
    
    // For image upload
    private String imageBase64;
    private String imageName;
    
    // Default constructor
    public ItemMessage() {
        setMessageType("ITEM");
    }
    
    // Getters and setters
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public ItemDTO getItem() {
        return item;
    }
    
    public void setItem(ItemDTO item) {
        this.item = item;
    }
    
    public List<ItemDTO> getItems() {
        return items;
    }
    
    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getImageBase64() {
        return imageBase64;
    }
    
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    public String getImageName() {
        return imageName;
    }
    
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}