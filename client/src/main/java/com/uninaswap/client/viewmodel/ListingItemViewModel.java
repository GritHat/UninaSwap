package com.uninaswap.client.viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 
 */
public class ListingItemViewModel {
    /**
     * 
     */
    private final StringProperty id = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty name = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty description = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty imagePath = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<ItemViewModel> item = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final IntegerProperty quantity = new SimpleIntegerProperty(0);

    // Constructors
    /**
     * 
     */
    public ListingItemViewModel() {
    }

    /**
     * @param id
     * @param name
     * @param description
     * @param imagePath
     * @param item
     * @param quantity
     */
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
    /**
     * @return
     */
    public StringProperty idProperty() {
        return id;
    }

    /**
     * @return
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * @return
     */
    public StringProperty descriptionProperty() {
        return description;
    }

    /**
     * @return
     */
    public StringProperty imagePathProperty() {
        return imagePath;
    }

    /**
     * @return
     */
    public ObjectProperty<ItemViewModel> itemProperty() {
        return item;
    }

    /**
     * @return
     */
    public IntegerProperty quantityProperty() {
        return quantity;
    }

    // Setters
    /**
     * @param id
     */
    public void setId(String id) {
        this.id.set(id);
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description.set(description);
    }

    /**
     * @param imagePath
     */
    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    /**
     * @param item
     */
    public void setItem(ItemViewModel item) {
        this.item.set(item);
    }

    /**
     * @param quantity
     */
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    // Getters

    /**
     * @return
     */
    public String getId() {
        return id.get();
    }

    /**
     * @return
     */
    public String getName() {
        return name.get();
    }

    /**
     * @return
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * @return
     */
    public String getImagePath() {
        return imagePath.get();
    }

    /**
     * @return
     */
    public ItemViewModel getItem() {
        return item.get();
    }

    /**
     * @return
     */
    public int getQuantity() {
        return quantity.get();

    }
}
