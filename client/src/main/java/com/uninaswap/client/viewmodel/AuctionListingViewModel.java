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

/**
 * 
 */
public class AuctionListingViewModel extends ListingViewModel {

    /**
     * 
     */
    private final ObjectProperty<BigDecimal> startingPrice = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<BigDecimal> reservePrice = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> auctionStartTime = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> auctionEndTime = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<BigDecimal> highestBid = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<BigDecimal> minimumBidIncrement = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> highestBidder = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final IntegerProperty durationInDays = new SimpleIntegerProperty();
    /**
     * 
     */
    private final StringProperty pickupLocation = new SimpleStringProperty();

    /**
     * 
     */
    public AuctionListingViewModel() {
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
     */
    public AuctionListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured) {
        super(id, title, description, user, createdAt, status, featured);
    }

    /**
     * @return
     */
    public ObjectProperty<BigDecimal> startingPriceProperty() {
        return startingPrice;
    }

    /**
     * @return
     */
    public ObjectProperty<BigDecimal> reservePriceProperty() {
        return reservePrice;
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
    public ObjectProperty<LocalDateTime> auctionStartTimeProperty() {
        return auctionStartTime;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> auctionEndTimeProperty() {
        return auctionEndTime;
    }

    /**
     * @return
     */
    public ObjectProperty<BigDecimal> highestBidProperty() {
        return highestBid;
    }

    /**
     * @return
     */
    public ObjectProperty<UserViewModel> highestBidderProperty() {
        return highestBidder;
    }

    /**
     * @return
     */
    public IntegerProperty durationInDaysProperty() {
        return durationInDays;
    }

    /**
     * @return
     */
    public ObjectProperty<BigDecimal> minimumBidIncrementProperty() {
        return minimumBidIncrement;
    }

    /**
     * @return
     */
    public StringProperty pickupLocationProperty() {
        return pickupLocation;
    }

    /**
     * @param startingPrice
     */
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice.set(startingPrice);
    }

    /**
     * @param reservePrice
     */
    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice.set(reservePrice);
    }

    /**
     * @param currency
     */
    public void setCurrency(Currency currency) {
        this.currency.set(currency);
    }

    /**
     * @param auctionStartTime
     */
    public void setAuctionStartTime(LocalDateTime auctionStartTime) {
        this.auctionStartTime.set(auctionStartTime);
    }

    /**
     * @param auctionEndTime
     */
    public void setAuctionEndTime(LocalDateTime auctionEndTime) {
        this.auctionEndTime.set(auctionEndTime);
    }

    /**
     * @param highestBid
     */
    public void setHighestBid(BigDecimal highestBid) {
        this.highestBid.set(highestBid);
    }

    /**
     * @param highestBidder
     */
    public void setHighestBidder(UserViewModel highestBidder) {
        this.highestBidder.set(highestBidder);
    }

    /**
     * @param durationInDays
     */
    public void setDurationInDays(int durationInDays) {
        this.durationInDays.set(durationInDays);
    }

    /**
     * @param minimumBidIncrement
     */
    public void setMinimumBidIncrement(BigDecimal minimumBidIncrement) {
        this.minimumBidIncrement.set(minimumBidIncrement);
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
    public DeliveryType getDeliveryType(DeliveryType deliveryType) {
        if (pickupLocation.get() != null && !pickupLocation.get().isEmpty()) {
            return deliveryType;
        } else {
            return DeliveryType.SHIPPING;
        }
    }

    /**
     * @return
     */
    public BigDecimal getStartingPrice() {
        return startingPrice.get();
    }

    /**
     * @return
     */
    public BigDecimal getReservePrice() {
        return reservePrice.get();
    }

    /**
     * @return
     */
    public Currency getCurrency() {
        return currency.get();
    }

    /**
     * @return
     */
    public LocalDateTime getAuctionStartTime() {
        return auctionStartTime.get();
    }

    /**
     * @return
     */
    public LocalDateTime getAuctionEndTime() {
        return auctionEndTime.get();
    }

    /**
     * @return
     */
    public BigDecimal getHighestBid() {
        return highestBid.get();
    }

    /**
     * @return
     */
    public UserViewModel getHighestBidder() {
        return highestBidder.get();
    }

    /**
     * @return
     */
    public int getDurationInDays() {
        return durationInDays.get();
    }

    /**
     * @return
     */
    public BigDecimal getMinimumBidIncrement() {
        return minimumBidIncrement.get();
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
    public String getListingTypeValue() {
        return "AUCTION";
    }

    /**
     * @return
     */
    public BigDecimal getMinimumNextBid() {
        if (highestBid != null && highestBid.get().compareTo(BigDecimal.ZERO) > 0) {
            return highestBid.get().add(minimumBidIncrement.get());
        } else {
            return startingPrice.get();
        }
    }
}
