package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class ListingViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<UserViewModel> user = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private final ObjectProperty<ListingStatus> status = new SimpleObjectProperty<>();
    private final BooleanProperty featured = new SimpleBooleanProperty();

    private final ObservableList<ListingItemViewModel> items = FXCollections.observableArrayList();

    // Constructors
    public ListingViewModel() {
    }

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
    public StringProperty idProperty() {
        return id;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<UserViewModel> userProperty() {
        return user;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    public ObjectProperty<ListingStatus> statusProperty() {
        return status;
    }

    public BooleanProperty featuredProperty() {
        return featured;
    }

    public ObservableList<ListingItemViewModel> getItems() {
        return items;
    }

    public String getId() {
        return id.get();
    }

    public String getTitle() {
        return title.get();
    }

    public String getDescription() {
        return description.get();
    }

    public UserViewModel getUser() {
        return user.get();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public ListingStatus getStatus() {
        return status.get();
    }

    public boolean isFeatured() {
        return featured.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setUser(UserViewModel user) {
        this.user.set(user);
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public void setStatus(ListingStatus status) {
        this.status.set(status);
    }

    public void setFeatured(boolean featured) {
        this.featured.set(featured);
    }

    public abstract String getListingTypeValue();
}
