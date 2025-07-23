package com.uninaswap.common.message;

import java.util.List;

import com.uninaswap.common.dto.ItemDTO;

/**
 * 
 */
public class ItemMessage extends Message {
    /**
     * 
     */
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

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private ItemDTO item;
    /**
     * 
     */
    private List<ItemDTO> items;
    /**
     * 
     */
    private String errorMessage;

    
    /**
     * 
     */
    public ItemMessage() {
        setMessageType("item");
    }

    
    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
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

    /**
     * @return
     */
    public List<ItemDTO> getItems() {
        return items;
    }

    /**
     * @param items
     */
    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    /**
     *
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     *
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}