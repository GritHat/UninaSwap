package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 */
public class TradeListingViewModel extends ListingViewModel {
    /**
     * 
     */
    private final StringProperty tradeType = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty tradeDetails = new SimpleStringProperty();
    /**
     * 
     */
    private final BooleanProperty acceptMoneyOffers = new SimpleBooleanProperty();
    /**
     * 
     */
    private final StringProperty referencePrice = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final BooleanProperty acceptMixedOffers = new SimpleBooleanProperty();
    /**
     * 
     */
    private final BooleanProperty acceptOtherOffers = new SimpleBooleanProperty();
    /**
     * 
     */
    private final ObservableList<String> desiredCategories = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<ListingItemViewModel> desiredItems = FXCollections.observableArrayList();
    /**
     * 
     */
    private final SimpleStringProperty pickupLocation = new SimpleStringProperty();

    
    /**
     * 
     */
    public TradeListingViewModel() {
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
     * @param tradeType
     * @param tradeDetails
     */
    public TradeListingViewModel(String id, String title, String description, UserViewModel user,
            LocalDateTime createdAt, ListingStatus status, boolean featured, String tradeType, String tradeDetails) {
        super(id, title, description, user, createdAt, status, featured);
        setTradeType(tradeType);
        setTradeDetails(tradeDetails);
    }

    
    /**
     * @return
     */
    public StringProperty tradeTypeProperty() {
        return tradeType;
    }

    /**
     * @return
     */
    public StringProperty tradeDetailsProperty() {
        return tradeDetails;
    }

    /**
     * @return
     */
    public BooleanProperty acceptMoneyOffersProperty() {
        return acceptMoneyOffers;
    }

    /**
     * @return
     */
    public StringProperty referencePriceProperty() {
        return referencePrice;
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
    public BooleanProperty acceptMixedOffersProperty() {
        return acceptMixedOffers;
    }

    /**
     * @return
     */
    public BooleanProperty acceptOtherOffersProperty() {
        return acceptOtherOffers;
    }

    /**
     * @return
     */
    public ObservableList<String> desiredCategoriesProperty() {
        return desiredCategories;
    }

    /**
     * @return
     */
    public ObservableList<ListingItemViewModel> desiredItemsProperty() {
        return desiredItems;
    }

    /**
     * @return
     */
    public StringProperty pickupLocationProperty() {
        return pickupLocation;
    }

    /**
     * @param acceptMoneyOffers
     */
    public void setAcceptMoneyOffers(boolean acceptMoneyOffers) {
        this.acceptMoneyOffers.set(acceptMoneyOffers);
    }

    /**
     * @param referencePrice
     */
    public void setReferencePrice(String referencePrice) {
        this.referencePrice.set(referencePrice);
    }

    /**
     * @param currency
     */
    public void setCurrency(Currency currency) {
        this.currency.set(currency);
    }

    /**
     * @param acceptMixedOffers
     */
    public void setAcceptMixedOffers(boolean acceptMixedOffers) {
        this.acceptMixedOffers.set(acceptMixedOffers);
    }

    /**
     * @param acceptOtherOffers
     */
    public void setAcceptOtherOffers(boolean acceptOtherOffers) {
        this.acceptOtherOffers.set(acceptOtherOffers);
    }

    /**
     * @param desiredCategories
     */
    public void setDesiredCategories(ObservableList<String> desiredCategories) {
        this.desiredCategories.setAll(desiredCategories);
    }

    /**
     * @param desiredItems
     */
    public void setDesiredItems(ObservableList<ListingItemViewModel> desiredItems) {
        this.desiredItems.setAll(desiredItems);
    }

    /**
     * @return
     */
    public boolean isAcceptMoneyOffers() {
        return acceptMoneyOffers.get();
    }

    /**
     * @return
     */
    public String getReferencePrice() {
        return referencePrice.get();
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
    public boolean isAcceptMixedOffers() {
        return acceptMixedOffers.get();
    }

    /**
     * @return
     */
    public boolean isAcceptOtherOffers() {
        return acceptOtherOffers.get();
    }

    /**
     * @return
     */
    public ObservableList<String> getDesiredCategories() {
        return desiredCategories;
    }

    /**
     * @return
     */
    public ObservableList<ListingItemViewModel> getDesiredItems() {
        return desiredItems;
    }

    /**
     * @return
     */
    public String getTradeType() {
        return tradeType.get();
    }

    /**
     * @param tradeType
     */
    public void setTradeType(String tradeType) {
        this.tradeType.set(tradeType);
    }

    /**
     * @return
     */
    public String getTradeDetails() {
        return tradeDetails.get();
    }

    /**
     * @param tradeDetails
     */
    public void setTradeDetails(String tradeDetails) {
        this.tradeDetails.set(tradeDetails);
    }

    /**
     *
     */
    @Override
    public DeliveryType getDeliveryType(DeliveryType deliveryType) {
        return DeliveryType.PICKUP;
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
        return "TRADE";
    }

}
