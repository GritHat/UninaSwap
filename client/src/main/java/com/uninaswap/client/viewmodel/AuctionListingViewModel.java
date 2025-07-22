package com.uninaswap.client.viewmodel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AuctionListingViewModel extends ListingViewModel {

    private final ObjectProperty<BigDecimal> startingPrice = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> reservePrice = new SimpleObjectProperty<>();
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> auctionStartTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> auctionEndTime = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> highestBid = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> minimumBidIncrement = new SimpleObjectProperty<>();
    private final ObjectProperty<UserViewModel> highestBidder = new SimpleObjectProperty<>();
    private final IntegerProperty durationInDays = new SimpleIntegerProperty();
    private final StringProperty pickupLocation = new SimpleStringProperty();

    public AuctionListingViewModel() {
        super();
    }

    public AuctionListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured) {
        super(id, title, description, user, createdAt, status, featured);
    }

    public ObjectProperty<BigDecimal> startingPriceProperty() {
        return startingPrice;
    }

    public ObjectProperty<BigDecimal> reservePriceProperty() {
        return reservePrice;
    }

    public ObjectProperty<Currency> currencyProperty() {
        return currency;
    }

    public ObjectProperty<LocalDateTime> auctionStartTimeProperty() {
        return auctionStartTime;
    }

    public ObjectProperty<LocalDateTime> auctionEndTimeProperty() {
        return auctionEndTime;
    }

    public ObjectProperty<BigDecimal> highestBidProperty() {
        return highestBid;
    }

    public ObjectProperty<UserViewModel> highestBidderProperty() {
        return highestBidder;
    }

    public IntegerProperty durationInDaysProperty() {
        return durationInDays;
    }

    public ObjectProperty<BigDecimal> minimumBidIncrementProperty() {
        return minimumBidIncrement;
    }

    public StringProperty pickupLocationProperty() {
        return pickupLocation;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice.set(startingPrice);
    }

    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice.set(reservePrice);
    }

    public void setCurrency(Currency currency) {
        this.currency.set(currency);
    }

    public void setAuctionStartTime(LocalDateTime auctionStartTime) {
        this.auctionStartTime.set(auctionStartTime);
    }

    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime.set(auctionEndTime);
    }

    public void setHighestBid(BigDecimal highestBid) {
        this.highestBid.set(highestBid);
    }

    public void setHighestBidder(UserViewModel highestBidder) {
        this.highestBidder.set(highestBidder);
    }

    public void setDurationInDays(int durationInDays) {
        this.durationInDays.set(durationInDays);
    }

    public void setMinimumBidIncrement(BigDecimal minimumBidIncrement) {
        this.minimumBidIncrement.set(minimumBidIncrement);
    }

    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation.set(pickupLocation);
    }

    @Override
    public DeliveryType getDeliveryType(DeliveryType deliveryType) {
        if (pickupLocation.get() != null && !pickupLocation.get().isEmpty()) {
            return deliveryType;
        } else {
            return DeliveryType.SHIPPING;
        }
    }

    public BigDecimal getStartingPrice() {
        return startingPrice.get();
    }

    public BigDecimal getReservePrice() {
        return reservePrice.get();
    }

    public Currency getCurrency() {
        return currency.get();
    }

    public LocalDateTime getAuctionStartTime() {
        return auctionStartTime.get();
    }

    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime.get();
    }

    public BigDecimal getHighestBid() {
        return highestBid.get();
    }

    public UserViewModel getHighestBidder() {
        return highestBidder.get();
    }

    public int getDurationInDays() {
        return durationInDays.get();
    }

    public BigDecimal getMinimumBidIncrement() {
        return minimumBidIncrement.get();
    }

    @Override
    public String getPickupLocation() {
        return pickupLocation.get();
    }

    @Override
    public String getListingTypeValue() {
        return "AUCTION";
    }

    public BigDecimal getMinimumNextBid() {
        if (highestBid != null && highestBid.get().compareTo(BigDecimal.ZERO) > 0) {
            return highestBid.get().add(minimumBidIncrement.get());
        } else {
            return startingPrice.get();
        }
    }
}
