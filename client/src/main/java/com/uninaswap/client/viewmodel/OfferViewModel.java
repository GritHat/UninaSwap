package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.OfferStatus;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OfferViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty listingId = new SimpleStringProperty();
    private final ObjectProperty<ListingViewModel> listing = new SimpleObjectProperty<>();
    private final ObjectProperty<UserViewModel> offeringUser = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private final ObjectProperty<OfferStatus> status = new SimpleObjectProperty<>();

    // Money component
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();

    // Items component
    private final ObservableList<OfferItemViewModel> offerItems = FXCollections.observableArrayList();

    // Message
    private final StringProperty message = new SimpleStringProperty();

    // Constructors
    public OfferViewModel() {
    }

    public OfferViewModel(String id, String listingId, UserViewModel user, LocalDateTime createdAt,
            OfferStatus status, BigDecimal amount, Currency currency, String message) {
        setId(id);
        setListingId(listingId);
        setUser(user);
        setCreatedAt(createdAt);
        setStatus(status);
        setAmount(amount);
        setCurrency(currency);
        setMessage(message);
    }

    // Property getters
    public StringProperty idProperty() {
        return id;
    }

    public StringProperty listingIdProperty() {
        return listingId;
    }

    public ObjectProperty<UserViewModel> userProperty() {
        return offeringUser;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    public ObjectProperty<OfferStatus> statusProperty() {
        return status;
    }

    public ObjectProperty<BigDecimal> amountProperty() {
        return amount;
    }

    public ObjectProperty<Currency> currencyProperty() {
        return currency;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public ObjectProperty<ListingViewModel> listingProperty() {
        return listing;
    }

    // Getters and setters
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getListingId() {
        return listingId.get();
    }

    public void setListingId(String listingId) {
        this.listingId.set(listingId);
    }

    public UserViewModel getOfferingUser() {
        return offeringUser.get();
    }

    public void setUser(UserViewModel user) {
        this.offeringUser.set(user);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public OfferStatus getStatus() {
        return status.get();
    }

    public void setStatus(OfferStatus status) {
        this.status.set(status);
    }

    public BigDecimal getAmount() {
        return amount.get();
    }

    public void setAmount(BigDecimal amount) {
        this.amount.set(amount);
    }

    public Currency getCurrency() {
        return currency.get();
    }

    public void setCurrency(Currency currency) {
        this.currency.set(currency);
    }

    public ObservableList<OfferItemViewModel> getOfferItems() {
        return offerItems;
    }

    public String getMessage() {
        return message.get();
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public void setListing(ListingViewModel listing) {
        this.listing.set(listing);
    }

    public ListingViewModel getListing() {
        return listing.get();
    }

    // Utility methods
    public boolean hasMoneyOffer() {
        return getAmount() != null && getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasItemOffer() {
        return !offerItems.isEmpty();
    }

    public int getTotalItemCount() {
        return offerItems.stream().mapToInt(OfferItemViewModel::getQuantity).sum();
    }

    public String getListingTitle() {
        return listing.get().getTitle();
    }

    public String getListingDescription() {
        return listing.get().getDescription();
    }

    public String getOfferingUserUsername() {
        return offeringUser.get().getUsername();
    }

    public String getListingOwnerUsername() {
        return listing.get().getUser().getUsername();
    }
}