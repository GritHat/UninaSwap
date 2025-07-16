package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.ItemCondition;
import javafx.beans.property.*;

public class OfferItemViewModel {
    private final StringProperty itemId = new SimpleStringProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final StringProperty itemImagePath = new SimpleStringProperty();
    private final ObjectProperty<ItemCondition> condition = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty();

    // Constructors
    public OfferItemViewModel() {
    }

    public OfferItemViewModel(String itemId, String itemName, String itemImagePath,
            ItemCondition condition, int quantity) {
        setItemId(itemId);
        setItemName(itemName);
        setItemImagePath(itemImagePath);
        setCondition(condition);
        setQuantity(quantity);
    }

    // Property getters
    public StringProperty itemIdProperty() {
        return itemId;
    }

    public StringProperty itemNameProperty() {
        return itemName;
    }

    public StringProperty itemImagePathProperty() {
        return itemImagePath;
    }

    public ObjectProperty<ItemCondition> conditionProperty() {
        return condition;
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    // Getters and setters
    public String getItemId() {
        return itemId.get();
    }

    public void setItemId(String itemId) {
        this.itemId.set(itemId);
    }

    public String getItemName() {
        return itemName.get();
    }

    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }

    public String getItemImagePath() {
        return itemImagePath.get();
    }

    public void setItemImagePath(String itemImagePath) {
        this.itemImagePath.set(itemImagePath);
    }

    public ItemCondition getCondition() {
        return condition.get();
    }

    public void setCondition(ItemCondition condition) {
        this.condition.set(condition);
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }
}