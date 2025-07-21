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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListingCreationController implements Refreshable {

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
    private TableColumn<ItemDTO, Integer> availableQuantityColumn;

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
    private ComboBox<DurationOption> durationComboBox;

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
    private final Map<String, Integer> tempReservedQuantities = new HashMap<>();
    private List<DurationOption> durationOptions;

    public class DurationOption {
        private final int days;
        private final String messageKey;
        private final LocaleService localeService;

        public DurationOption(int days, String messageKey, LocaleService localeService) {
            this.days = days;
            this.messageKey = messageKey;
            this.localeService = localeService;
        }

        public int getDays() {
            return days;
        }

        @Override
        public String toString() {
            return localeService.getMessage(messageKey, String.valueOf(days) + " day(s)");
        }
    }

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
        durationOptions = Arrays.asList(
                new DurationOption(1, "listing.auction.time.1day", localeService),
                new DurationOption(3, "listing.auction.time.3days", localeService),
                new DurationOption(5, "listing.auction.time.5days", localeService),
                new DurationOption(7, "listing.auction.time.7days", localeService),
                new DurationOption(14, "listing.auction.time.14days", localeService),
                new DurationOption(30, "listing.auction.time.30days", localeService));

        durationComboBox.setItems(FXCollections.observableArrayList(durationOptions));
        durationComboBox.setValue(durationOptions.stream()
                .filter(option -> option.getDays() == 7)
                .findFirst()
                .orElse(durationOptions.get(3)));

        // Configure bindings for trade listing controls
        referencePriceField.disableProperty().bind(acceptMoneyOffersCheckBox.selectedProperty().not());
        tradeCurrencyComboBox.disableProperty().bind(acceptMoneyOffersCheckBox.selectedProperty().not());
        acceptMixedOffersCheckBox.disableProperty().bind(acceptMoneyOffersCheckBox.selectedProperty().not());
        acceptMoneyOffersCheckBox.selectedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                acceptMixedOffersCheckBox.setSelected(false);
            }
        });
        
        // Load user's items
        itemsTable.setItems(itemService.getUserItemsList());
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("listingcreation.debug.initialized", "ListingCreation controller initialized"));
    }

    private void setupItemsTable() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        conditionColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getCondition().getDisplayName()));

        availableQuantityColumn.setCellValueFactory(cellData -> {
            ItemDTO item = cellData.getValue();
            int actualAvailable = item.getAvailableQuantity();
            int tempReserved = tempReservedQuantities.getOrDefault(item.getId(), 0);
            int effectiveAvailable = Math.max(0, actualAvailable - tempReserved);

            return Bindings.createObjectBinding(() -> effectiveAvailable);
        });

        // Configure selection listener
        itemsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                // Enable the add button and set max quantity
                addItemButton.setDisable(false);

                // Set max spinner value to available quantity
                SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) quantitySpinner
                        .getValueFactory();

                if (valueFactory == null) {
                    valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,
                            newSelection.getAvailableQuantity(), 1);
                    quantitySpinner.setValueFactory(valueFactory);
                } else {
                    valueFactory.setMax(newSelection.getAvailableQuantity());
                    valueFactory.setValue(1); // Reset to 1 when selecting new item
                }
                
                System.out.println(localeService.getMessage("listingcreation.debug.item.selected", "Item selected: {0}").replace("{0}", newSelection.getName()));
            } else {
                addItemButton.setDisable(true);
            }
        });
    }

    private void setupSelectedItemsTable() {
        selectedNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getItemName()));

        selectedQuantityColumn
                .setCellValueFactory(cellData -> Bindings.createObjectBinding(() -> cellData.getValue().getQuantity()));

        selectedActionColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button removeButton = new Button(localeService.getMessage("button.remove", "Remove"));

            {
                removeButton.setOnAction(_ -> {
                    ListingItemDTO item = getTableView().getItems().get(getIndex());

                    // Return the quantity to available
                    String itemId = item.getItemId();
                    int currentReserved = tempReservedQuantities.getOrDefault(itemId, 0);
                    int newReserved = Math.max(0, currentReserved - item.getQuantity());

                    if (newReserved > 0) {
                        tempReservedQuantities.put(itemId, newReserved);
                    } else {
                        tempReservedQuantities.remove(itemId);
                    }

                    selectedItems.remove(item);
                    updateSelectedItemsTable();
                    refreshItemsTable(); // Refresh to show updated available quantities
                    
                    System.out.println(localeService.getMessage("listingcreation.debug.item.removed", "Item removed from selection: {0}").replace("{0}", item.getItemName()));
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

    private void refreshItemsTable() {
        // Get the current selection to restore it after refresh
        ItemDTO selectedItem = itemsTable.getSelectionModel().getSelectedItem();

        // Store current items
        List<ItemDTO> currentItems = new ArrayList<>(itemsTable.getItems());

        // Refresh the table by re-setting the same items
        // This forces the cell factories to recalculate their values
        itemsTable.setItems(null);
        itemsTable.setItems(FXCollections.observableArrayList(currentItems));

        // Force refresh of table cells
        itemsTable.refresh();

        // Restore selection if possible
        if (selectedItem != null) {
            for (ItemDTO item : itemsTable.getItems()) {
                if (item.getId().equals(selectedItem.getId())) {
                    itemsTable.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    @FXML
    private void handleAddItem() {
        ItemDTO selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int quantity = quantitySpinner.getValue();
            String itemId = selectedItem.getId();
            int effectivelyAvailable = selectedItem.getAvailableQuantity();

            if (quantity > effectivelyAvailable) {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("listing.create.error.title", "Error"),
                        localeService.getMessage("listing.create.error.header", "Invalid Input"),
                        localeService.getMessage("listing.create.error.quantity.exceeded", "Requested quantity exceeds available stock"));
                return;
            }

            // Check if already in the list
            boolean exists = false;
            for (ListingItemDTO listingItem : selectedItems) {
                if (listingItem.getItemId().equals(itemId)) {
                    // Update quantity instead
                    int newQuantity = listingItem.getQuantity() + quantity;
                    if (newQuantity <= effectivelyAvailable) {
                        listingItem.setQuantity(newQuantity);
                        exists = true;
                        updateSelectedItemsTable();
                        selectedItemsTable.refresh();
                        // Update temporary reservation
                        int currentReserved = tempReservedQuantities.getOrDefault(itemId, 0);
                        tempReservedQuantities.put(itemId, currentReserved + quantity);
                        
                        System.out.println(localeService.getMessage("listingcreation.debug.item.quantity.updated", "Updated item quantity: {0} (new quantity: {1})").replace("{0}", selectedItem.getName()).replace("{1}", String.valueOf(newQuantity)));
                    } else {
                        AlertHelper.showWarningAlert(
                                localeService.getMessage("listing.create.error.title", "Error"),
                                localeService.getMessage("listing.create.error.header", "Invalid Input"),
                                localeService.getMessage("listing.create.error.quantity.exceeded", "Requested quantity exceeds available stock"));
                        return;
                    }
                    break;
                }
            }

            if (!exists) {
                // Create new listing item
                ListingItemDTO listingItem = new ListingItemDTO();
                listingItem.setItemId(itemId);
                listingItem.setItemName(selectedItem.getName());
                listingItem.setItemImagePath(selectedItem.getImagePath());
                listingItem.setQuantity(quantity);
                selectedItems.add(listingItem);

                // Track temporary reservation
                int currentReserved = tempReservedQuantities.getOrDefault(itemId, 0);
                tempReservedQuantities.put(itemId, currentReserved + quantity);
                
                System.out.println(localeService.getMessage("listingcreation.debug.item.added", "Item added to listing: {0} (quantity: {1})").replace("{0}", selectedItem.getName()).replace("{1}", String.valueOf(quantity)));
            }

            updateSelectedItemsTable();
            refreshItemsTable(); // Refresh to show updated available quantities
        } else {
            System.out.println(localeService.getMessage("listingcreation.debug.no.item.selected", "No item selected for adding"));
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
                    localeService.getMessage("listing.create.error.title", "Error"),
                    localeService.getMessage("listing.create.error.header", "Invalid Input"),
                    localeService.getMessage("listing.create.error.unknown.type", "Unknown listing type selected"));
            return;
        }

        if (listing == null) {
            // Error already shown in create methods
            return;
        }

        // Save the listing
        System.out.println(localeService.getMessage("listingcreation.debug.creating.listing", "Creating listing: {0}").replace("{0}", listing.getTitle()));
        
        listingService.createListing(listing)
                .thenAccept(_ -> {
                    System.out.println(localeService.getMessage("listingcreation.debug.listing.created", "Listing created successfully"));
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("listing.create.success.title", "Success"),
                            localeService.getMessage("listing.create.success.header", "Listing Created"),
                            localeService.getMessage("listing.create.success.content", "Your listing has been created successfully"));

                    // Close the window
                    Stage stage = (Stage) createButton.getScene().getWindow();
                    stage.close();
                })
                .exceptionally(ex -> {
                    System.err.println(localeService.getMessage("listingcreation.error.creation.failed", "Failed to create listing: {0}").replace("{0}", ex.getMessage()));
                    AlertHelper.showErrorAlert(
                            localeService.getMessage("listing.create.error.title", "Error"),
                            localeService.getMessage("listing.create.error.header", "Creation Failed"),
                            localeService.getMessage("listingcreation.error.creation.message", "Failed to create listing: {0}").replace("{0}", ex.getMessage()));
                    return null;
                });
    }

    private boolean validateCommonFields() {
        if (titleField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.title.empty", "Title cannot be empty"));
            titleField.requestFocus();
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.description.empty", "Description cannot be empty"));
            descriptionArea.requestFocus();
            return false;
        }

        if (selectedItems.isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.items.empty", "You must add at least one item"));
            return false;
        }

        System.out.println(localeService.getMessage("listingcreation.debug.validation.passed", "Common fields validation passed"));
        return true;
    }

    private SellListingDTO createSellListing() {
        if (sellPriceField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.price.empty", "Price cannot be empty"));
            sellPriceField.requestFocus();
            return null;
        }

        try {
            BigDecimal price = new BigDecimal(sellPriceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException(localeService.getMessage("listingcreation.error.price.negative", "Price must be positive"));
            }

            SellListingDTO listing = new SellListingDTO();
            setupCommonFields(listing);
            listing.setPrice(price);
            listing.setCurrency(sellCurrencyComboBox.getValue());

            System.out.println(localeService.getMessage("listingcreation.debug.sell.listing.created", "Sell listing created with price: {0} {1}").replace("{0}", sellCurrencyComboBox.getValue().getSymbol()).replace("{1}", price.toString()));
            return listing;
        } catch (NumberFormatException e) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.price.invalid", "Invalid price format"));
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
                
                System.out.println(localeService.getMessage("listingcreation.debug.trade.reference.price", "Trade listing reference price set: {0} {1}").replace("{0}", tradeCurrencyComboBox.getValue().getSymbol()).replace("{1}", referencePrice.toString()));
            } catch (NumberFormatException e) {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("listing.create.validation.title", "Validation Error"),
                        localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                        localeService.getMessage("listing.create.validation.price.invalid", "Invalid price format"));
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
            
            System.out.println(localeService.getMessage("listingcreation.debug.trade.categories", "Trade listing categories set: {0}").replace("{0}", String.join(", ", trimmedCategories)));
        }

        System.out.println(localeService.getMessage("listingcreation.debug.trade.listing.created", "Trade listing created"));
        return listing;
    }

    private GiftListingDTO createGiftListing() {
        GiftListingDTO listing = new GiftListingDTO();
        setupCommonFields(listing);

        // Set gift-specific fields
        listing.setPickupOnly(pickupOnlyCheckBox.isSelected());
        listing.setAllowThankYouOffers(allowThankYouOffersCheckBox.isSelected());
        listing.setRestrictions(restrictionsArea.getText().trim());

        System.out.println(localeService.getMessage("listingcreation.debug.gift.listing.created", "Gift listing created (pickup only: {0}, thank you offers: {1})").replace("{0}", String.valueOf(pickupOnlyCheckBox.isSelected())).replace("{1}", String.valueOf(allowThankYouOffersCheckBox.isSelected())));
        return listing;
    }

    private AuctionListingDTO createAuctionListing() {
        // Validate auction-specific fields
        if (startingPriceField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.starting.price.empty", "Starting price cannot be empty"));
            startingPriceField.requestFocus();
            return null;
        }

        if (bidIncrementField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.bid.increment.empty", "Bid increment cannot be empty"));
            bidIncrementField.requestFocus();
            return null;
        }

        try {
            BigDecimal startingPrice = new BigDecimal(startingPriceField.getText().trim());
            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException(localeService.getMessage("listingcreation.error.starting.price.negative", "Starting price must be positive"));
            }

            BigDecimal reservePrice = null;
            if (!reservePriceField.getText().trim().isEmpty()) {
                reservePrice = new BigDecimal(reservePriceField.getText().trim());
                if (reservePrice.compareTo(startingPrice) < 0) {
                    AlertHelper.showWarningAlert(
                            localeService.getMessage("listing.create.validation.title", "Validation Error"),
                            localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                            localeService.getMessage("listing.create.validation.reserve.price.low", "Reserve price must be higher than starting price"));
                    reservePriceField.requestFocus();
                    return null;
                }
            }

            BigDecimal bidIncrement = new BigDecimal(bidIncrementField.getText().trim());
            if (bidIncrement.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException(localeService.getMessage("listingcreation.error.bid.increment.negative", "Bid increment must be positive"));
            }

            // Calculate end time based on selected duration
            int days = durationComboBox.getValue().getDays();

            AuctionListingDTO listing = new AuctionListingDTO();
            setupCommonFields(listing);

            listing.setStartingPrice(startingPrice);
            listing.setReservePrice(reservePrice);
            listing.setCurrency(auctionCurrencyComboBox.getValue());
            listing.setDurationInDays(days); // Set the duration in days
            listing.setMinimumBidIncrement(bidIncrement);
            LocalDateTime now = LocalDateTime.now();
            listing.setStartTime(now);
            listing.setEndTime(now.plusDays(days));

            System.out.println(localeService.getMessage("listingcreation.debug.auction.created", "Auction listing created: start={0}, end={1}, starting price={2} {3}").replace("{0}", now.toString()).replace("{1}", now.plusDays(days).toString()).replace("{2}", auctionCurrencyComboBox.getValue().getSymbol()).replace("{3}", startingPrice.toString()));

            return listing;

        } catch (NumberFormatException e) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.create.validation.title", "Validation Error"),
                    localeService.getMessage("listing.create.validation.header", "Invalid Input"),
                    localeService.getMessage("listing.create.validation.price.invalid", "Invalid price format"));
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
        
        System.out.println(localeService.getMessage("listingcreation.debug.common.fields.set", "Common listing fields set for: {0}").replace("{0}", listing.getTitle()));
    }

    @FXML
    private void handleCancel() {
        // Clear temporary reservations before closing
        tempReservedQuantities.clear();
        System.out.println(localeService.getMessage("listingcreation.debug.cancelled", "Listing creation cancelled by user"));
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void refreshUI() {
        // Update button labels
        if (addItemButton != null) {
            addItemButton.setText(localeService.getMessage("button.add", "Add"));
        }
        if (createButton != null) {
            createButton.setText(localeService.getMessage("button.create", "Create"));
        }
        if (cancelButton != null) {
            cancelButton.setText(localeService.getMessage("button.cancel", "Cancel"));
        }

        // Update table column headers
        if (nameColumn != null) {
            nameColumn.setText(localeService.getMessage("item.name.column", "Name"));
        }
        if (conditionColumn != null) {
            conditionColumn.setText(localeService.getMessage("item.condition.column", "Condition"));
        }
        if (availableQuantityColumn != null) {
            availableQuantityColumn.setText(localeService.getMessage("item.available.column", "Available"));
        }
        if (selectedNameColumn != null) {
            selectedNameColumn.setText(localeService.getMessage("item.name.column", "Name"));
        }
        if (selectedQuantityColumn != null) {
            selectedQuantityColumn.setText(localeService.getMessage("item.quantity.column", "Quantity"));
        }
        if (selectedActionColumn != null) {
            selectedActionColumn.setText(localeService.getMessage("item.action.column", "Action"));
        }

        // Update field prompt texts
        if (titleField != null) {
            titleField.setPromptText(localeService.getMessage("listing.title.prompt", "Enter listing title"));
        }
        if (descriptionArea != null) {
            descriptionArea.setPromptText(localeService.getMessage("listing.description.prompt", "Enter detailed description"));
        }
        if (sellPriceField != null) {
            sellPriceField.setPromptText(localeService.getMessage("listing.sell.price.prompt", "Enter price"));
        }
        if (referencePriceField != null) {
            referencePriceField.setPromptText(localeService.getMessage("listing.trade.reference.price.prompt", "Enter reference price"));
        }
        if (categoriesField != null) {
            categoriesField.setPromptText(localeService.getMessage("listing.trade.categories.prompt", "Enter desired categories (comma separated)"));
        }
        if (restrictionsArea != null) {
            restrictionsArea.setPromptText(localeService.getMessage("listing.gift.restrictions.prompt", "Enter any restrictions or requirements"));
        }
        if (startingPriceField != null) {
            startingPriceField.setPromptText(localeService.getMessage("listing.auction.starting.price.prompt", "Enter starting price"));
        }
        if (reservePriceField != null) {
            reservePriceField.setPromptText(localeService.getMessage("listing.auction.reserve.price.prompt", "Enter optional reserve price"));
        }
        if (bidIncrementField != null) {
            bidIncrementField.setPromptText(localeService.getMessage("listing.auction.bid.increment.prompt", "Enter minimum bid increment"));
        }

        // Update duration combo box items to refresh localized strings
        if (durationComboBox != null && durationOptions != null) {
            DurationOption currentSelection = durationComboBox.getValue();
            durationComboBox.setItems(FXCollections.observableArrayList(durationOptions));
            if (currentSelection != null) {
                // Find the equivalent option in the refreshed list
                durationComboBox.setValue(durationOptions.stream()
                        .filter(option -> option.getDays() == currentSelection.getDays())
                        .findFirst()
                        .orElse(currentSelection));
            }
        }

        // Update checkbox labels
        if (acceptMoneyOffersCheckBox != null) {
            acceptMoneyOffersCheckBox.setText(localeService.getMessage("listing.trade.money.offers", "Accept money offers"));
        }
        if (acceptMixedOffersCheckBox != null) {
            acceptMixedOffersCheckBox.setText(localeService.getMessage("listing.trade.mixed.offers", "Accept mixed offers (items + money)"));
        }
        if (acceptOtherOffersCheckBox != null) {
            acceptOtherOffersCheckBox.setText(localeService.getMessage("listing.trade.other.offers", "Accept other items not in desired categories"));
        }
        if (pickupOnlyCheckBox != null) {
            pickupOnlyCheckBox.setText(localeService.getMessage("listing.gift.pickup.only", "Pickup only"));
        }
        if (allowThankYouOffersCheckBox != null) {
            allowThankYouOffersCheckBox.setText(localeService.getMessage("listing.gift.thank.you.offers", "Allow thank you offers"));
        }

        // Refresh the selected items table to update remove button text
        if (selectedItemsTable != null) {
            selectedItemsTable.refresh();
        }
    }
}