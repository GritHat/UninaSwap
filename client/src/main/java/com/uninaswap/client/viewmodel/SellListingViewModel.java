package com.uninaswap.client.viewmodel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SellListingViewModel extends ListingViewModel {
    private final ObjectProperty<BigDecimal> price = new SimpleObjectProperty<>();
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();

    // Constructors
    public SellListingViewModel() {
        super();
    }

    public SellListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured, BigDecimal price, Currency currency) {
        super(id, title, description, user, createdAt, status, featured);
        setPrice(price);
        setCurrency(currency);
    }

    // Property getters
    public ObjectProperty<BigDecimal> priceProperty() {
        return price;
    }

    public ObjectProperty<Currency> currencyProperty() {
        return currency;
    }

    // Getters and setters
    public BigDecimal getPrice() {
        return price.get();
    }

    public void setPrice(BigDecimal price) {
        this.price.set(price);
    }

    public Currency getCurrency() {
        return currency.get();
    }

    public void setCurrency(Currency currency) {
        this.currency.set(currency);
    }

    @Override
    public String getListingTypeValue() {
        return "SELL";
    }

}
