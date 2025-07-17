package com.uninaswap.client.viewmodel;

import java.time.LocalDateTime;

import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TradeListingViewModel extends ListingViewModel {
    private final StringProperty tradeType = new SimpleStringProperty();
    private final StringProperty tradeDetails = new SimpleStringProperty();
    private final BooleanProperty acceptMoneyOffers = new SimpleBooleanProperty();
    private final StringProperty referencePrice = new SimpleStringProperty();
    private final ObjectProperty<Currency> currency = new SimpleObjectProperty<>();
    private final BooleanProperty acceptMixedOffers = new SimpleBooleanProperty();
    private final BooleanProperty acceptOtherOffers = new SimpleBooleanProperty();
    private final ObservableList<String> desiredCategories = FXCollections.observableArrayList();
    private final ObservableList<ListingItemViewModel> desiredItems = FXCollections.observableArrayList();

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

    public BooleanProperty acceptMoneyOffersProperty() {
        return acceptMoneyOffers;
    }

    public StringProperty referencePriceProperty() {
        return referencePrice;
    }

    public ObjectProperty<Currency> currencyProperty() {
        return currency;
    }

    public BooleanProperty acceptMixedOffersProperty() {
        return acceptMixedOffers;
    }

    public BooleanProperty acceptOtherOffersProperty() {
        return acceptOtherOffers;
    }

    public ObservableList<String> desiredCategoriesProperty() {
        return desiredCategories;
    }

    public ObservableList<ListingItemViewModel> desiredItemsProperty() {
        return desiredItems;
    }

    public void setAcceptMoneyOffers(boolean acceptMoneyOffers) {
        this.acceptMoneyOffers.set(acceptMoneyOffers);
    }

    public void setReferencePrice(String referencePrice) {
        this.referencePrice.set(referencePrice);
    }

    public void setCurrency(Currency currency) {
        this.currency.set(currency);
    }

    public void setAcceptMixedOffers(boolean acceptMixedOffers) {
        this.acceptMixedOffers.set(acceptMixedOffers);
    }

    public void setAcceptOtherOffers(boolean acceptOtherOffers) {
        this.acceptOtherOffers.set(acceptOtherOffers);
    }

    public void setDesiredCategories(ObservableList<String> desiredCategories) {
        this.desiredCategories.setAll(desiredCategories);
    }

    public void setDesiredItems(ObservableList<ListingItemViewModel> desiredItems) {
        this.desiredItems.setAll(desiredItems);
    }

    public boolean isAcceptMoneyOffers() {
        return acceptMoneyOffers.get();
    }

    public String getReferencePrice() {
        return referencePrice.get();
    }

    public Currency getCurrency() {
        return currency.get();
    }

    public boolean isAcceptMixedOffers() {
        return acceptMixedOffers.get();
    }

    public boolean isAcceptOtherOffers() {
        return acceptOtherOffers.get();
    }

    public ObservableList<String> getDesiredCategories() {
        return desiredCategories;
    }

    public ObservableList<ListingItemViewModel> getDesiredItems() {
        return desiredItems;
    }

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
