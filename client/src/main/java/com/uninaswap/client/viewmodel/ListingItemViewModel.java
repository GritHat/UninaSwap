package com.uninaswap.client.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ListingItemViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty imagePath = new SimpleStringProperty();
    private final ObjectProperty<ItemViewModel> item = new SimpleObjectProperty<>();
    private final IntegerProperty quantity = new SimpleIntegerProperty(0);

    // Constructors
    public ListingItemViewModel() {
    }

    public ListingItemViewModel(String id, String name, String description, String imagePath,
            ItemViewModel item, int quantity) {
        setId(id);
        setName(name);
        setDescription(description);
        setImagePath(imagePath);
        setItem(item);
        setQuantity(quantity);
    }

    // Property getters
    public StringProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public ObjectProperty<ItemViewModel> itemProperty() {
        return item;
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    // Setters
    public void setId(String id) {
        this.id.set(id);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    public void setItem(ItemViewModel item) {
        this.item.set(item);
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    // Getters

    public String getId() {
        return id.get();
    }

    public String getName() {
        return name.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public ItemViewModel getItem() {
        return item.get();
    }

    public int getQuantity() {
        return quantity.get();

    }
}
