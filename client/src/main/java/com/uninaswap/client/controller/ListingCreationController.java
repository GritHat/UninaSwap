package com.uninaswap.client.controller;

import com.uninaswap.client.service.ItemService;
import com.uninaswap.client.service.ListingService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.common.dto.*;
import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ListingCreationController {

    @FXML
    private TabPane listingTypeTabs;
    
    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TableView<ItemDTO> itemsTable;
    
    @FXML
    private TableColumn<ItemDTO, String> nameColumn;
    
    @FXML
    private TableColumn<ItemDTO, String> conditionColumn;
    
    @FXML
    private TableColumn<ItemDTO, Integer> stockColumn;
    
    @FXML
    private Spinner<Integer> quantitySpinner;
    
    @FXML
    private Button addItemButton;
    
    @FXML
    private TableView<ListingItemDTO> selectedItemsTable;
    
    @FXML
    private TableColumn<ListingItemDTO, String> selectedNameColumn;
    
    @FXML
    private TableColumn<ListingItemDTO, Integer> selectedQuantityColumn;
    
    @FXML
    private TableColumn<ListingItemDTO, String> selectedActionColumn;
    
    // Sell listing specific controls
    @FXML
    private TextField sellPriceField;
    
    @FXML
    private ComboBox<Currency> sellCurrencyComboBox;
    
    // Trade listing specific controls
    @FXML
    private CheckBox acceptMoneyOffersCheckBox;
    
    @FXML
    private TextField referencePriceField;
    
    @FXML
    private ComboBox<Currency> tradeCurrencyComboBox;
    
    @FXML
    private CheckBox acceptMixedOffersCheckBox;
    
    @FXML
    private CheckBox acceptOtherOffersCheckBox;
    
    @FXML
    private TextField categoriesField;
    
    // Gift listing specific controls
    @FXML
    private CheckBox pickupOnlyCheckBox;
    
    @FXML
    private CheckBox allowThankYouOffersCheckBox;
    
    @FXML
    private TextArea restrictionsArea;
    
    // Auction listing specific controls
    @FXML
    private TextField startingPriceField;
    
    @FXML
    private TextField reservePriceField;
    
    @FXML
    private ComboBox<Currency> auctionCurrencyComboBox;
    
    @FXML
    private ComboBox<String> durationComboBox;
    
    @FXML
    private TextField bidIncrementField;
    
    // Common buttons
    @FXML
    private Button createButton;
    
    @FXML
    private Button cancelButton;
    
    private final ItemService itemService = ItemService.getInstance();
    private final ListingService listingService = ListingService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();
    
    private final List<ListingItemDTO> selectedItems = new ArrayList<>();
    
    @FXML
    public void initialize() {
        // Initialize tables
        setupItemsTable();
        setupSelectedItemsTable();
        
        // Configure currency combo boxes
        sellCurrencyComboBox.setItems(FXCollections.observableArrayList(Currency.values()));
        sellCurrencyComboBox.setValue(Currency.EUR);
        
        tradeCurrencyComboBox.setItems(FXCollections.observableArrayList(Currency.values()));
        tradeCurrencyComboBox.setValue(Currency.EUR);
        
        auctionCurrencyComboBox.setItems(FXCollections.observableArrayList(Currency.values()));
        auctionCurrencyComboBox.setValue(Currency.EUR);
        
        // Configure auction duration options
        durationComboBox.setItems(FXCollections.observableArrayList(
            "1 day", "3 days", "5 days", "7 days", "14 days", "30 days"
        ));
        durationComboBox.setValue("7 days");
        
        // Configure bindings for trade listing controls
        referencePriceField.disableProperty().bind(acceptMoneyOffersCheckBox.selectedProperty().not());
        tradeCurrencyComboBox.disableProperty().bind(acceptMoneyOffersCheckBox.selectedProperty().not());
        
        // Load user's items
        itemsTable.setItems(itemService.getUserItemsList());
    }
    
    private void setupItemsTable() {
        nameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getName()));
        
        conditionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCondition().getDisplayName()));
        
        stockColumn.setCellValueFactory(cellData -> 
            Bindings.createObjectBinding(() -> cellData.getValue().getAvailableQuantity()));
        
        // Configure selection listener
        itemsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                // Enable the add button and set max quantity
                addItemButton.setDisable(false);
                
                // Set max spinner value to available quantity
                SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = 
                    (SpinnerValueFactory.IntegerSpinnerValueFactory) quantitySpinner.getValueFactory();
                    
                if (valueFactory == null) {
                    valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 
                        newSelection.getAvailableQuantity(), 1);
                    quantitySpinner.setValueFactory(valueFactory);
                } else {
                    valueFactory.setMax(newSelection.getAvailableQuantity());
                    valueFactory.setValue(1); // Reset to 1 when selecting new item
                }
            } else {
                addItemButton.setDisable(true);
            }
        });
    }
    
    private void setupSelectedItemsTable() {
        selectedNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getItemName()));
            
        selectedQuantityColumn.setCellValueFactory(cellData -> 
            Bindings.createObjectBinding(() -> cellData.getValue().getQuantity()));
            
        selectedActionColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button removeButton = new Button(localeService.getMessage("button.remove"));
            
            {
                removeButton.setOnAction(_ -> {
                    ListingItemDTO item = getTableView().getItems().get(getIndex());
                    selectedItems.remove(item);
                    updateSelectedItemsTable();
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        // Set initial data
        updateSelectedItemsTable();
    }
    
    private void updateSelectedItemsTable() {
        selectedItemsTable.setItems(FXCollections.observableArrayList(selectedItems));
    }
    
    @FXML
    private void handleAddItem() {
        ItemDTO selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int quantity = quantitySpinner.getValue();
            
            // Check if already in the list
            boolean exists = false;
            for (ListingItemDTO listingItem : selectedItems) {
                if (listingItem.getItemId().equals(selectedItem.getId())) {
                    // Update quantity instead
                    int newQuantity = listingItem.getQuantity() + quantity;
                    if (newQuantity <= selectedItem.getAvailableQuantity()) {
                        listingItem.setQuantity(newQuantity);
                        exists = true;
                    } else {
                        AlertHelper.showWarningAlert(
                            localeService.getMessage("listing.create.error.title"),
                            localeService.getMessage("listing.create.error.header"),
                            localeService.getMessage("listing.create.error.quantity.exceeded")
                        );
                        return;
                    }
                    break;
                }
            }
            
            if (!exists) {
                // Create new listing item
                ListingItemDTO listingItem = new ListingItemDTO();
                listingItem.setItemId(selectedItem.getId());
                listingItem.setItemName(selectedItem.getName());
                listingItem.setItemImagePath(selectedItem.getImagePath());
                listingItem.setQuantity(quantity);
                selectedItems.add(listingItem);
            }
            
            updateSelectedItemsTable();
        }
    }
    
    @FXML
    private void handleCreateListing() {
        if (!validateCommonFields()) {
            return;
        }
        
        // Create appropriate listing based on selected tab
        Tab selectedTab = listingTypeTabs.getSelectionModel().getSelectedItem();
        ListingDTO listing;
        
        if (selectedTab.getId().equals("sellTab")) {
            listing = createSellListing();
        } else if (selectedTab.getId().equals("tradeTab")) {
            listing = createTradeListing();
        } else if (selectedTab.getId().equals("giftTab")) {
            listing = createGiftListing();
        } else if (selectedTab.getId().equals("auctionTab")) {
            listing = createAuctionListing();
        } else {
            AlertHelper.showErrorAlert(
                localeService.getMessage("listing.create.error.title"),
                localeService.getMessage("listing.create.error.header"),
                localeService.getMessage("listing.create.error.unknown.type")
            );
            return;
        }
        
        // Save the listing
        listingService.createListing(listing)
            .thenAccept(_ -> {
                AlertHelper.showInformationAlert(
                    localeService.getMessage("listing.create.success.title"),
                    localeService.getMessage("listing.create.success.header"),
                    localeService.getMessage("listing.create.success.content")
                );
                
                // Close the window
                ((Stage) createButton.getScene().getWindow()).close();
            })
            .exceptionally(ex -> {
                AlertHelper.showErrorAlert(
                    localeService.getMessage("listing.create.error.title"),
                    localeService.getMessage("listing.create.error.header"),
                    ex.getMessage()
                );
                return null;
            });
    }
    
    private boolean validateCommonFields() {
        if (titleField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.title.empty")
            );
            titleField.requestFocus();
            return false;
        }
        
        if (descriptionArea.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.description.empty")
            );
            descriptionArea.requestFocus();
            return false;
        }
        
        if (selectedItems.isEmpty()) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.items.empty")
            );
            return false;
        }
        
        return true;
    }
    
    private SellListingDTO createSellListing() {
        if (sellPriceField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.price.empty")
            );
            sellPriceField.requestFocus();
            return null;
        }
        
        try {
            BigDecimal price = new BigDecimal(sellPriceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("Price must be positive");
            }
            
            SellListingDTO listing = new SellListingDTO();
            setupCommonFields(listing);
            listing.setPrice(price);
            listing.setCurrency(sellCurrencyComboBox.getValue());
            
            return listing;
        } catch (NumberFormatException e) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.price.invalid")
            );
            sellPriceField.requestFocus();
            return null;
        }
    }
    
    private TradeListingDTO createTradeListing() {
        TradeListingDTO listing = new TradeListingDTO();
        setupCommonFields(listing);
        
        // Set trade-specific fields
        listing.setAcceptMoneyOffers(acceptMoneyOffersCheckBox.isSelected());
        listing.setAcceptMixedOffers(acceptMixedOffersCheckBox.isSelected());
        listing.setAcceptOtherOffers(acceptOtherOffersCheckBox.isSelected());
        
        // Set reference price if accepting money offers
        if (acceptMoneyOffersCheckBox.isSelected() && !referencePriceField.getText().trim().isEmpty()) {
            try {
                BigDecimal referencePrice = new BigDecimal(referencePriceField.getText().trim());
                listing.setReferencePrice(referencePrice);
                listing.setCurrency(tradeCurrencyComboBox.getValue());
            } catch (NumberFormatException e) {
                AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title"),
                    localeService.getMessage("listing.create.validation.header"),
                    localeService.getMessage("listing.create.validation.price.invalid")
                );
                referencePriceField.requestFocus();
                return null;
            }
        }
        
        // Set desired categories
        if (!categoriesField.getText().trim().isEmpty()) {
            String[] categories = categoriesField.getText().split(",");
            List<String> trimmedCategories = new ArrayList<>();
            for (String category : categories) {
                trimmedCategories.add(category.trim());
            }
            listing.setDesiredCategories(trimmedCategories);
        }
        
        return listing;
    }
    
    private GiftListingDTO createGiftListing() {
        GiftListingDTO listing = new GiftListingDTO();
        setupCommonFields(listing);
        
        // Set gift-specific fields
        listing.setPickupOnly(pickupOnlyCheckBox.isSelected());
        listing.setAllowThankYouOffers(allowThankYouOffersCheckBox.isSelected());
        listing.setRestrictions(restrictionsArea.getText().trim());
        
        return listing;
    }
    
    private AuctionListingDTO createAuctionListing() {
        // Validate auction-specific fields
        if (startingPriceField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.starting.price.empty")
            );
            startingPriceField.requestFocus();
            return null;
        }
        
        if (bidIncrementField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.bid.increment.empty")
            );
            bidIncrementField.requestFocus();
            return null;
        }
        
        try {
            BigDecimal startingPrice = new BigDecimal(startingPriceField.getText().trim());
            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("Starting price must be positive");
            }
            
            BigDecimal reservePrice = null;
            if (!reservePriceField.getText().trim().isEmpty()) {
                reservePrice = new BigDecimal(reservePriceField.getText().trim());
                if (reservePrice.compareTo(startingPrice) < 0) {
                    AlertHelper.showWarningAlert(
                        localeService.getMessage("listing.create.validation.title"),
                        localeService.getMessage("listing.create.validation.header"),
                        localeService.getMessage("listing.create.validation.reserve.price.low")
                    );
                    reservePriceField.requestFocus();
                    return null;
                }
            }
            
            BigDecimal bidIncrement = new BigDecimal(bidIncrementField.getText().trim());
            if (bidIncrement.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException("Bid increment must be positive");
            }
            
            // Calculate end time based on selected duration
            int days;
            switch (durationComboBox.getValue()) {
                case "1 day": days = 1; break;
                case "3 days": days = 3; break;
                case "5 days": days = 5; break;
                case "7 days": days = 7; break;
                case "14 days": days = 14; break;
                case "30 days": days = 30; break;
                default: days = 7;
            }
            
            LocalDateTime endTime = LocalDateTime.now().plus(days, ChronoUnit.DAYS);
            
            AuctionListingDTO listing = new AuctionListingDTO();
            setupCommonFields(listing);
            
            listing.setStartingPrice(startingPrice);
            listing.setReservePrice(reservePrice);
            listing.setCurrency(auctionCurrencyComboBox.getValue());
            listing.setEndTime(endTime);
            listing.setMinimumBidIncrement(bidIncrement);
            
            return listing;
            
        } catch (NumberFormatException e) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("listing.create.validation.title"),
                localeService.getMessage("listing.create.validation.header"),
                localeService.getMessage("listing.create.validation.price.invalid")
            );
            return null;
        }
    }
    
    private void setupCommonFields(ListingDTO listing) {
        listing.setTitle(titleField.getText().trim());
        listing.setDescription(descriptionArea.getText().trim());
        listing.setCreator(sessionService.getUser());
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());
        
        // Add selected items
        listing.setItems(new ArrayList<>(selectedItems));
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}