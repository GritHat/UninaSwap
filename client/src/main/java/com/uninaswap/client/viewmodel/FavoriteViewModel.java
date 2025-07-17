package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FavoriteViewModel {
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty userId = new SimpleLongProperty();
    private final StringProperty listingId = new SimpleStringProperty();
    private final ObjectProperty<UserViewModel> user = new SimpleObjectProperty<>();
    private final ObjectProperty<ListingViewModel> listing = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    // Default constructor
    public FavoriteViewModel() {
    }

    // Constructor with required fields
    public FavoriteViewModel(Long userId, String listingId) {
        setUserId(userId);
        setListingId(listingId);
        setCreatedAt(LocalDateTime.now());
    }

    // Property getters
    public LongProperty idProperty() {
        return id;
    }

    public LongProperty userIdProperty() {
        return userId;
    }

    public StringProperty listingIdProperty() {
        return listingId;
    }

    public ObjectProperty<UserViewModel> userProperty() {
        return user;
    }

    public ObjectProperty<ListingViewModel> listingProperty() {
        return listing;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    // Getters and setters
    public Long getId() {
        return id.get();
    }

    public void setId(Long id) {
        this.id.set(id);
    }

    public Long getUserId() {
        return userId.get();
    }

    public void setUserId(Long userId) {
        this.userId.set(userId);
    }

    public String getListingId() {
        return listingId.get();
    }

    public void setListingId(String listingId) {
        this.listingId.set(listingId);
    }

    public UserViewModel getUser() {
        return user.get();
    }

    public void setUser(UserViewModel user) {
        this.user.set(user);
    }

    public ListingViewModel getListing() {
        return listing.get();
    }

    public void setListing(ListingViewModel listing) {
        this.listing.set(listing);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    // Utility methods
    public String getFormattedDate() {
        if (getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getCreatedAt().format(formatter);
        }
        return "";
    }

    public String getUserName() {
        return user.get() != null ? user.get().getDisplayName() : "";
    }

    public String getListingTitle() {
        return listing.get() != null ? listing.get().getTitle() : "";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        FavoriteViewModel that = (FavoriteViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("FavoriteViewModel{id=%d, userId=%d, listingId='%s'}",
                getId(), getUserId(), getListingId());
    }
}