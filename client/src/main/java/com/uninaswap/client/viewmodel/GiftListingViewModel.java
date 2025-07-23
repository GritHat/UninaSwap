package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;

import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 
 */
public class GiftListingViewModel extends ListingViewModel {
    /**
     * 
     */
    private final StringProperty giftType = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty giftDetails = new SimpleStringProperty();
    /**
     * 
     */
    private final BooleanProperty pickupOnly = new SimpleBooleanProperty();
    /**
     * 
     */
    private final BooleanProperty allowThankYouOffers = new SimpleBooleanProperty();
    /**
     * 
     */
    private final StringProperty pickupLocation = new SimpleStringProperty(); 

    
    /**
     * 
     */
    public GiftListingViewModel() {
        super();
    }

    /**
     * @param id
     * @param title
     * @param description
     * @param user
     * @param createdAt
     * @param status
     * @param featured
     * @param giftType
     * @param giftDetails
     */
    public GiftListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured, String giftType, String giftDetails) {
        super(id, title, description, user, createdAt, status, featured);
        setGiftType(giftType);
        setGiftDetails(giftDetails);
    }

    
    /**
     * @return
     */
    public StringProperty giftTypeProperty() {
        return giftType;
    }

    /**
     * @return
     */
    public StringProperty giftDetailsProperty() {
        return giftDetails;
    }

    /**
     * @return
     */
    public BooleanProperty pickupOnlyProperty() {
        return pickupOnly;
    }

    /**
     * @return
     */
    public BooleanProperty allowThankYouOffersProperty() {
        return allowThankYouOffers;
    }

    /**
     * @return
     */
    public StringProperty pickupLocationProperty() {
        return pickupLocation;
    }

    
    /**
     * @return
     */
    public String getGiftType() {
        return giftType.get();
    }

    /**
     * @param giftType
     */
    public void setGiftType(String giftType) {
        this.giftType.set(giftType);
    }

    /**
     * @return
     */
    public String getGiftDetails() {
        return giftDetails.get();
    }

    /**
     * @param giftDetails
     */
    public void setGiftDetails(String giftDetails) {
        this.giftDetails.set(giftDetails);
    }

    /**
     * @return
     */
    public boolean isPickupOnly() {
        return pickupOnly.get();
    }

    /**
     * @param pickupOnly
     */
    public void setPickupOnly(boolean pickupOnly) {
        this.pickupOnly.set(pickupOnly);
    }

    /**
     * @return
     */
    public boolean isAllowThankYouOffers() {
        return allowThankYouOffers.get();
    }

    /**
     * @param allowThankYouOffer
     */
    public void setAllowThankYouOffers(boolean allowThankYouOffer) {
        this.allowThankYouOffers.set(allowThankYouOffer);
    }

    /**
     *
     */
    @Override
    public DeliveryType getDeliveryType(DeliveryType deliveryType) {
        if (pickupLocation.get() == null || !pickupLocation.get().isEmpty()) {
            return DeliveryType.SHIPPING;
        } else if (pickupOnly.get()) {
            return DeliveryType.PICKUP;
        }
        return deliveryType;
    }

    /**
     *
     */
    @Override
    public String getPickupLocation() {
        return pickupLocation.get();
    }

    /**
     *
     */
    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation.set(pickupLocation);
    }

    /**
     *
     */
    @Override
    public String getListingTypeValue() {
        return "GIFT";
    }

}
