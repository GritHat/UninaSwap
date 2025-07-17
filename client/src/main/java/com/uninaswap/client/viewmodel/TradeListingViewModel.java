package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;

import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TradeListingViewModel extends ListingViewModel {
    private final StringProperty tradeType = new SimpleStringProperty();
    private final StringProperty tradeDetails = new SimpleStringProperty();

    // Constructors
    public TradeListingViewModel() {
        super();
    }

    public TradeListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured, String tradeType, String tradeDetails) {
        super(id, title, description, user, createdAt, status, featured);
        setTradeType(tradeType);
        setTradeDetails(tradeDetails);
    }

    // Property getters
    public StringProperty tradeTypeProperty() {
        return tradeType;
    }

    public StringProperty tradeDetailsProperty() {
        return tradeDetails;
    }

    // Getters and setters
    public String getTradeType() {
        return tradeType.get();
    }

    public void setTradeType(String tradeType) {
        this.tradeType.set(tradeType);
    }

    public String getTradeDetails() {
        return tradeDetails.get();
    }

    public void setTradeDetails(String tradeDetails) {
        this.tradeDetails.set(tradeDetails);
    }

    @Override
    public String getListingTypeValue() {
        return "TRADE";
    }

}
