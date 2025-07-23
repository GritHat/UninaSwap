package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 */
public class FavoriteViewModel {
    /**
     * 
     */
    private final LongProperty id = new SimpleLongProperty();
    /**
     * 
     */
    private final LongProperty userId = new SimpleLongProperty();
    /**
     * 
     */
    private final StringProperty listingId = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> user = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<ListingViewModel> listing = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    /**
     * 
     */
    public FavoriteViewModel() {
    }

    /**
     * @param userId
     * @param listingId
     */
    public FavoriteViewModel(Long userId, String listingId) {
        setUserId(userId);
        setListingId(listingId);
        setCreatedAt(LocalDateTime.now());
    }

    /**
     * @return
     */
    public LongProperty idProperty() {
        return id;
    }

    /**
     * @return
     */
    public LongProperty userIdProperty() {
        return userId;
    }

    /**
     * @return
     */
    public StringProperty listingIdProperty() {
        return listingId;
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
    public ObjectProperty<ListingViewModel> listingProperty() {
        return listing;
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
    public Long getId() {
        return id.get();
    }

    /**
     * @param id
     */
    public void setId(Long id) {
        this.id.set(id);
    }

    /**
     * @return
     */
    public Long getUserId() {
        return userId.get();
    }

    /**
     * @param userId
     */
    public void setUserId(Long userId) {
        this.userId.set(userId);
    }

    /**
     * @return
     */
    public String getListingId() {
        return listingId.get();
    }

    /**
     * @param listingId
     */
    public void setListingId(String listingId) {
        this.listingId.set(listingId);
    }

    /**
     * @return
     */
    public UserViewModel getUser() {
        return user.get();
    }

    /**
     * @param user
     */
    public void setUser(UserViewModel user) {
        this.user.set(user);
    }

    /**
     * @return
     */
    public ListingViewModel getListing() {
        return listing.get();
    }

    /**
     * @param listing
     */
    public void setListing(ListingViewModel listing) {
        this.listing.set(listing);
    }

    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    /**
     * @return
     */
    public String getFormattedDate() {
        if (getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return getCreatedAt().format(formatter);
        }
        return "";
    }

    /**
     * @return
     */
    public String getUserName() {
        return user.get() != null ? user.get().getDisplayName() : "";
    }

    /**
     * @return
     */
    public String getListingTitle() {
        return listing.get() != null ? listing.get().getTitle() : "";
    }

    /**
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        FavoriteViewModel that = (FavoriteViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    /**
     *
     */
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return String.format("FavoriteViewModel{id=%d, userId=%d, listingId='%s'}",
                getId(), getUserId(), getListingId());
    }
}