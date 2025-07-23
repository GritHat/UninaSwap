package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;
import java.util.Optional;

import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 */
public abstract class ListingViewModel {
    /**
     * 
     */
    private final StringProperty id = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty title = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty description = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> user = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<ListingStatus> status = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final BooleanProperty featured = new SimpleBooleanProperty();

    /**
     * 
     */
    private final ObservableList<ListingItemViewModel> items = FXCollections.observableArrayList();

    // Constructors
    /**
     * 
     */
    public ListingViewModel() {
    }

    /**
     * @param id
     * @param title
     * @param description
     * @param user
     * @param createdAt
     * @param status
     * @param featured
     */
    public ListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured) {
        setId(id);
        setTitle(title);
        setDescription(description);
        setUser(user);
        setCreatedAt(createdAt);
        setStatus(status);
        setFeatured(featured);
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
    public StringProperty titleProperty() {
        return title;
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
    public ObjectProperty<UserViewModel> userProperty() {
        return user;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    /**
     * @return
     */
    public ObjectProperty<ListingStatus> statusProperty() {
        return status;
    }

    /**
     * @return
     */
    public BooleanProperty featuredProperty() {
        return featured;
    }

    /**
     * @return
     */
    public ObservableList<ListingItemViewModel> getItems() {
        return items;
    }

    /**
     * @return
     */
    public String getId() {
        return id.get();
    }

    /**
     * @return
     */
    public String getTitle() {
        return title.get();
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
    public UserViewModel getUser() {
        return user.get();
    }

    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    /**
     * @return
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    /**
     * @return
     */
    public ListingStatus getStatus() {
        return status.get();
    }

    /**
     * @return
     */
    public boolean isFeatured() {
        return featured.get();
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id.set(id);
    }

    /**
     * @param title
     */
    public void setTitle(String title) {
        this.title.set(title);
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description.set(description);
    }

    /**
     * @param user
     */
    public void setUser(UserViewModel user) {
        this.user.set(user);
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    /**
     * @param status
     */
    public void setStatus(ListingStatus status) {
        this.status.set(status);
    }

    /**
     * @param featured
     */
    public void setFeatured(boolean featured) {
        this.featured.set(featured);
    }

    /**
     * @param pickupLocation
     */
    public abstract void setPickupLocation(String pickupLocation);
    /**
     * @return
     */
    public abstract String getPickupLocation();

    /**
     * @return
     */
    public abstract String getListingTypeValue();

    /**
     * @param deliveryType
     * @return
     */
    public abstract DeliveryType getDeliveryType(DeliveryType deliveryType);
}
