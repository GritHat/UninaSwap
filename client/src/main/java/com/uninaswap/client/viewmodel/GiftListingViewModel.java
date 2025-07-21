package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;

import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GiftListingViewModel extends ListingViewModel {
    private final StringProperty giftType = new SimpleStringProperty();
    private final StringProperty giftDetails = new SimpleStringProperty();
    private final BooleanProperty pickupOnly = new SimpleBooleanProperty();
    private final BooleanProperty allowThankYouOffers = new SimpleBooleanProperty();
    private final StringProperty pickupLocation = new SimpleStringProperty(); // Added pickup location

    // Constructors
    public GiftListingViewModel() {
        super();
    }

    public GiftListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured, String giftType, String giftDetails) {
        super(id, title, description, user, createdAt, status, featured);
        setGiftType(giftType);
        setGiftDetails(giftDetails);
    }

    // Property getters
    public StringProperty giftTypeProperty() {
        return giftType;
    }

    public StringProperty giftDetailsProperty() {
        return giftDetails;
    }

    public BooleanProperty pickupOnlyProperty() {
        return pickupOnly;
    }

    public BooleanProperty allowThankYouOffersProperty() {
        return allowThankYouOffers;
    }

    public StringProperty pickupLocationProperty() {
        return pickupLocation;
    }

    // Getters and setters
    public String getGiftType() {
        return giftType.get();
    }

    public void setGiftType(String giftType) {
        this.giftType.set(giftType);
    }

    public String getGiftDetails() {
        return giftDetails.get();
    }

    public void setGiftDetails(String giftDetails) {
        this.giftDetails.set(giftDetails);
    }

    public boolean isPickupOnly() {
        return pickupOnly.get();
    }

    public void setPickupOnly(boolean pickupOnly) {
        this.pickupOnly.set(pickupOnly);
    }

    public boolean isAllowThankYouOffers() {
        return allowThankYouOffers.get();
    }

    public void setAllowThankYouOffers(boolean allowThankYouOffer) {
        this.allowThankYouOffers.set(allowThankYouOffer);
    }

    @Override
    public DeliveryType getDeliveryType(DeliveryType deliveryType) {
        if (pickupLocation.get() == null || !pickupLocation.get().isEmpty()) {
            return DeliveryType.SHIPPING;
        } else if (pickupOnly.get()) {
            return DeliveryType.PICKUP;
        }
        return deliveryType;
    }

    @Override
    public String getPickupLocation() {
        return pickupLocation.get();
    }

    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation.set(pickupLocation);
    }

    @Override
    public String getListingTypeValue() {
        return "GIFT";
    }

}
