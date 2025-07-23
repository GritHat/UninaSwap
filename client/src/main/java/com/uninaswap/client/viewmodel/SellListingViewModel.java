package com.uninaswap.client.viewmodel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * 
 */
public class SellListingViewModel extends ListingViewModel {
    /**
     * 
     */
    private final ObjectProperty<BigDecimal> price = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<String> pickupLocation = new SimpleObjectProperty<>(); 

    
    /**
     * 
     */
    public SellListingViewModel() {
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
     * @param price
     * @param currency
     */
    public SellListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured, BigDecimal price, Currency currency) {
        super(id, title, description, user, createdAt, status, featured);
        setPrice(price);
        setCurrency(currency);
    }

    
    /**
     * @return
     */
    public ObjectProperty<BigDecimal> priceProperty() {
        return price;
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
    public ObjectProperty<String> pickupLocationProperty() {
        return pickupLocation;
    }

    
    /**
     * @return
     */
    public BigDecimal getPrice() {
        return price.get();
    }

    /**
     * @param price
     */
    public void setPrice(BigDecimal price) {
        this.price.set(price);
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
     *
     */
    @Override
    public DeliveryType getDeliveryType(DeliveryType deliveryType) {
        if (pickupLocation.get() != null && !pickupLocation.get().isEmpty()) {
            return deliveryType;
        } else {
            return DeliveryType.SHIPPING;
        }
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
        return "SELL";
    }

}
