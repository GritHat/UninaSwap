package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;

import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GiftListingViewModel extends ListingViewModel {
    private final StringProperty giftType = new SimpleStringProperty();
    private final StringProperty giftDetails = new SimpleStringProperty();

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

}
