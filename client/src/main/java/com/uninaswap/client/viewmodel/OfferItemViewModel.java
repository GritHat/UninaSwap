package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.ItemCondition;
import javafx.beans.property.*;

/**
 * 
 */
public class OfferItemViewModel {
    /**
     * 
     */
    private final StringProperty itemId = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty itemName = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty itemImagePath = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<ItemCondition> condition = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    /**
     * 
     */
    private final ObjectProperty<ItemViewModel> item = new SimpleObjectProperty<>();

    // Constructors
    /**
     * 
     */
    public OfferItemViewModel() {
    }

    /**
     * @param itemId
     * @param itemName
     * @param itemImagePath
     * @param condition
     * @param quantity
     * @param item
     */
    public OfferItemViewModel(String itemId, String itemName, String itemImagePath,
            ItemCondition condition, int quantity, ItemViewModel item) {
        setItemId(itemId);
        setItemName(itemName);
        setItemImagePath(itemImagePath);
        setCondition(condition);
        setQuantity(quantity);
        setItem(item);
    }

    // Property getters
    /**
     * @return
     */
    public StringProperty itemIdProperty() {
        return itemId;
    }

    /**
     * @return
     */
    public StringProperty itemNameProperty() {
        return itemName;
    }

    /**
     * @return
     */
    public StringProperty itemImagePathProperty() {
        return itemImagePath;
    }

    /**
     * @return
     */
    public ObjectProperty<ItemCondition> conditionProperty() {
        return condition;
    }

    /**
     * @return
     */
    public IntegerProperty quantityProperty() {
        return quantity;
    }

    /**
     * @return
     */
    public ObjectProperty<ItemViewModel> itemProperty() {
        return item;
    }

    // Getters and setters
    /**
     * @return
     */
    public String getItemId() {
        return itemId.get();
    }

    /**
     * @param itemId
     */
    public void setItemId(String itemId) {
        this.itemId.set(itemId);
    }

    /**
     * @return
     */
    public String getItemName() {
        return itemName.get();
    }

    /**
     * @param itemName
     */
    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }

    /**
     * @return
     */
    public String getItemImagePath() {
        return itemImagePath.get();
    }

    /**
     * @param itemImagePath
     */
    public void setItemImagePath(String itemImagePath) {
        this.itemImagePath.set(itemImagePath);
    }

    /**
     * @return
     */
    public ItemCondition getCondition() {
        return condition.get();
    }

    /**
     * @param condition
     */
    public void setCondition(ItemCondition condition) {
        this.condition.set(condition);
    }

    /**
     * @return
     */
    public int getQuantity() {
        return quantity.get();
    }

    /**
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    /**
     * @return
     */
    public ItemViewModel getItem() {
        return item.get();
    }

    /**
     * @param item
     */
    public void setItem(ItemViewModel item) {
        this.item.set(item);
    }
}