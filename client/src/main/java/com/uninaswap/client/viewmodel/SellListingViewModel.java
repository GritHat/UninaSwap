package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;

import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SellListingViewModel extends ListingViewModel {
    private final StringProperty price = new SimpleStringProperty();
    private final StringProperty currency = new SimpleStringProperty();

    // Constructors
    public SellListingViewModel() {
        super();
    }

    public SellListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured, String price, String currency) {
        super(id, title, description, user, createdAt, status, featured);
        setPrice(price);
        setCurrency(currency);
    }

    // Property getters
    public StringProperty priceProperty() {
        return price;
    }

    public StringProperty currencyProperty() {
        return currency;
    }

    // Getters and setters
    public String getPrice() {
        return price.get();
    }

    public void setPrice(String price) {
        this.price.set(price);
    }

    public String getCurrency() {
        return currency.get();
    }

    public void setCurrency(String currency) {
        this.currency.set(currency);
    }

}
