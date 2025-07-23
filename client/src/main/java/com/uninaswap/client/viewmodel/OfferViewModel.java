package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.OfferStatus;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 
 */
public class OfferViewModel {
    /**
     * 
     */
    private final StringProperty id = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty listingId = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<ListingViewModel> listing = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> offeringUser = new SimpleObjectProperty<>();
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
    private final ObjectProperty<OfferStatus> status = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<DeliveryType> deliveryType = new SimpleObjectProperty<>();

    // Money component
    /**
     * 
     */
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();

    // Items component
    /**
     * 
     */
    private final ObservableList<OfferItemViewModel> offerItems = FXCollections.observableArrayList();

    // Message
    /**
     * 
     */
    private final StringProperty message = new SimpleStringProperty();

    // Constructors
    /**
     * 
     */
    public OfferViewModel() {
    }

    /**
     * @param id
     * @param listingId
     * @param user
     * @param createdAt
     * @param status
     * @param amount
     * @param currency
     * @param message
     */
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
    /**
     * @return
     */
    public StringProperty idProperty() {
        return id;
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
        return offeringUser;
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
    public ObjectProperty<OfferStatus> statusProperty() {
        return status;
    }

    /**
     * @return
     */
    public ObjectProperty<BigDecimal> amountProperty() {
        return amount;
    }

    /**
     * @return
     */
    public ObjectProperty<Currency> currencyProperty() {
        return currency;
    }

    /**
     * @return
     */
    public StringProperty messageProperty() {
        return message;
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
    public ObjectProperty<DeliveryType> deliveryTypeProperty() {
        return deliveryType;
    }

    // Getters and setters
    /**
     * @return
     */
    public String getId() {
        return id.get();
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id.set(id);
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
    public UserViewModel getOfferingUser() {
        return offeringUser.get();
    }

    /**
     * @param user
     */
    public void setUser(UserViewModel user) {
        this.offeringUser.set(user);
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
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    /**
     * @return
     */
    public OfferStatus getStatus() {
        return status.get();
    }

    /**
     * @param status
     */
    public void setStatus(OfferStatus status) {
        this.status.set(status);
    }

    /**
     * @return
     */
    public BigDecimal getAmount() {
        return amount.get();
    }

    /**
     * @param amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount.set(amount);
    }

    /**
     * @return
     */
    public Currency getCurrency() {
        return currency.get();
    }

    /**
     * @param currency
     */
    public void setCurrency(Currency currency) {
        this.currency.set(currency);
    }

    /**
     * @return
     */
    public ObservableList<OfferItemViewModel> getOfferItems() {
        return offerItems;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message.get();
    }

    /**
     * @param message
     */
    public void setMessage(String message) {
        this.message.set(message);
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
    public ListingViewModel getListing() {
        return listing.get();
    }

    // Utility methods
    /**
     * @return
     */
    public boolean hasMoneyOffer() {
        return getAmount() != null && getAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * @return
     */
    public boolean hasItemOffer() {
        return !offerItems.isEmpty();
    }

    /**
     * @return
     */
    public int getTotalItemCount() {
        return offerItems.stream().mapToInt(OfferItemViewModel::getQuantity).sum();
    }

    /**
     * @return
     */
    public String getListingTitle() {
        return listing.get().getTitle();
    }

    /**
     * @return
     */
    public String getListingDescription() {
        return listing.get().getDescription();
    }

    /**
     * @return
     */
    public String getOfferingUserUsername() {
        return offeringUser.get().getUsername();
    }

    /**
     * @return
     */
    public String getListingOwnerUsername() {
        return listing.get().getUser().getUsername();
    }

    /**
     * @return
     */
    public DeliveryType getDeliveryType() {
        return deliveryType.get();
    }

    /**
     * @param deliveryType
     */
    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType.set(deliveryType);
    }
}