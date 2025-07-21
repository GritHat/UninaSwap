package com.uninaswap.client.controller;

import com.uninaswap.client.service.ItemService;
import com.uninaswap.client.service.ListingService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.common.dto.*;
import com.uninaswap.common.enums.Category;
import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.ListingStatus;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @FXML
    private TextField sellLocationField; // Add location field for sell listings

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
    private TextField tradeLocationField; // Add location field for trade listings (MANDATORY)

    // Gift listing specific controls
    @FXML
    private CheckBox pickupOnlyCheckBox;

    @FXML
    private CheckBox allowThankYouOffersCheckBox;

    @FXML
    private TextArea restrictionsArea;

    @FXML
    private ComboBox<Category> availableCategoriesComboBox;

    @FXML
    private Button addCategoryButton;
    

    @FXML
    private TextField giftLocationField; // Add location field for gift listings

    // Auction listing specific controls
    @FXML
    private TextField startingPriceField;

    @FXML
    private TextField reservePriceField;

    @FXML
    private VBox selectedCategoriesContainer;



    @FXML
    private ComboBox<Currency> auctionCurrencyComboBox;

    @FXML
    private ComboBox<DurationOption> durationComboBox;

    @FXML
    private TextField bidIncrementField;

    @FXML
    private TextField auctionLocationField; // Add location field for auction listings

    // Common buttons
    @FXML
    private Button createButton;

    @FXML
    private Button cancelButton;

    private final List<Category> selectedCategories = new ArrayList<>();


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
            return localeService.getMessage(messageKey);
        }
    }

    @FXML
    public void initialize() {
        // Initialize tables
        setupItemsTable();
        setupSelectedItemsTable();

        setupCategorySelection();

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
    }

    private void setupCategorySelection() {
        // Populate available categories (exclude ALL category for selection)
        List<Category> availableCategories = Arrays.stream(Category.values())
                .filter(category -> category != Category.ALL)
                .collect(Collectors.toList());

        availableCategoriesComboBox.setItems(FXCollections.observableArrayList(availableCategories));

        // Configure category combo box display
        availableCategoriesComboBox.setCellFactory(_ -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(localeService.getMessage(category.getMessageKey()));
                }
            }
        });
        
        availableCategoriesComboBox.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(localeService.getMessage(category.getMessageKey()));
                }
            }
        });

        // Enable/disable add button based on selection
        addCategoryButton.disableProperty().bind(
            availableCategoriesComboBox.valueProperty().isNull()
        );

        // Update selected categories display
        updateSelectedCategoriesDisplay();
    }

    @FXML
    private void handleAddCategory() {
        Category selectedCategory = availableCategoriesComboBox.getValue();
        if (selectedCategory != null && !selectedCategories.contains(selectedCategory)) {
            selectedCategories.add(selectedCategory);
            updateSelectedCategoriesDisplay();
            availableCategoriesComboBox.setValue(null); // Clear selection
        }
    }

    private void updateSelectedCategoriesDisplay() {
        selectedCategoriesContainer.getChildren().clear();
        
        if (selectedCategories.isEmpty()) {
            // Show empty state
            Label emptyLabel = new Label(localeService.getMessage("listing.trade.categories.none.selected"));
            emptyLabel.getStyleClass().add("empty-categories-label");
            selectedCategoriesContainer.getChildren().add(emptyLabel);
        } else {
            // Create category chips
            for (Category category : selectedCategories) {
                HBox categoryChip = createCategoryChip(category);
                selectedCategoriesContainer.getChildren().add(categoryChip);
            }
        }
    }

    private HBox createCategoryChip(Category category) {
        HBox chip = new HBox(8.0);
        chip.getStyleClass().add("category-chip");
        chip.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Category label
        Label categoryLabel = new Label(localeService.getMessage(category.getMessageKey()));
        categoryLabel.getStyleClass().add("category-chip-label");

        // Remove button
        Button removeButton = new Button("×");
        removeButton.getStyleClass().addAll("category-chip-remove", "small-button");
        removeButton.setOnAction(_ -> {
            selectedCategories.remove(category);
            updateSelectedCategoriesDisplay();
        });

        chip.getChildren().addAll(categoryLabel, removeButton);
        return chip;
    }

    // Update the createTradeListing method to use the new category selection
    private TradeListingDTO createTradeListing() {
        // Validate mandatory location for trade listings
        if (tradeLocationField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.trade.validation.location.required"));
            return null;
        }

        TradeListingDTO listing = new TradeListingDTO();
        setupCommonFields(listing);

        // Set trade-specific fields
        listing.setAcceptMoneyOffers(acceptMoneyOffersCheckBox.isSelected());
        listing.setAcceptMixedOffers(acceptMixedOffersCheckBox.isSelected());
        listing.setAcceptOtherOffers(acceptOtherOffersCheckBox.isSelected());
        listing.setPickupLocation(tradeLocationField.getText().trim());
        // Set reference price if accepting money offers
        if (acceptMoneyOffersCheckBox.isSelected() && !referencePriceField.getText().trim().isEmpty()) {
            try {
                BigDecimal referencePrice = new BigDecimal(referencePriceField.getText().trim());
                listing.setReferencePrice(referencePrice);
                listing.setCurrency(tradeCurrencyComboBox.getValue());
            } catch (NumberFormatException e) {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("listing.validation.title"),
                        localeService.getMessage("listing.validation.header"),
                        localeService.getMessage("listing.trade.validation.reference.price.invalid"));
                return null;
            }
        }

        // Set desired categories using the new category selection
        if (!selectedCategories.isEmpty()) {
            List<String> categoryNames = selectedCategories.stream()
                    .map(category -> localeService.getMessage(category.getMessageKey()))
                    .collect(Collectors.toList());
            listing.setDesiredCategories(categoryNames);
        }

        return listing;
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
                    int maxQuantity = Math.max(1, newSelection.getAvailableQuantity()
                            - tempReservedQuantities.getOrDefault(newSelection.getId(), 0));
                    valueFactory.setMax(maxQuantity);
                    valueFactory.setValue(Math.min(valueFactory.getValue(), maxQuantity));
                }
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
            private final Button removeButton = new Button(localeService.getMessage("button.remove"));

            {
                removeButton.setOnAction(_ -> {
                    ListingItemDTO item = getTableView().getItems().get(getIndex());
                    selectedItems.remove(item);

                    // Update temp reserved quantities
                    int currentReserved = tempReservedQuantities.getOrDefault(item.getItemId(), 0);
                    int newReserved = Math.max(0, currentReserved - item.getQuantity());
                    if (newReserved == 0) {
                        tempReservedQuantities.remove(item.getItemId());
                    } else {
                        tempReservedQuantities.put(item.getItemId(), newReserved);
                    }

                    updateSelectedItemsTable();
                    refreshItemsTable();
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
            itemsTable.getSelectionModel().select(selectedItem);
        }
    }

    @FXML
    private void handleAddItem() {
        ItemDTO selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            int quantity = quantitySpinner.getValue();

            // Create listing item
            ListingItemDTO listingItem = new ListingItemDTO();
            listingItem.setItemId(selectedItem.getId());
            listingItem.setItemName(selectedItem.getName());
            listingItem.setQuantity(quantity);

            selectedItems.add(listingItem);

            // Update temp reserved quantities
            String itemId = selectedItem.getId();
            int currentReserved = tempReservedQuantities.getOrDefault(itemId, 0);
            tempReservedQuantities.put(itemId, currentReserved + quantity);

            updateSelectedItemsTable();
            refreshItemsTable(); // Refresh to show updated available quantities
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
                    localeService.getMessage("listing.create.error.no.type"));
            return;
        }

        if (listing == null) {
            return; // Validation failed in specific listing creation
        }

        // Save the listing
        listingService.createListing(listing)
                .thenAccept(_ -> {
                    // Clear temp reservations
                    tempReservedQuantities.clear();

                    AlertHelper.showInformationAlert(
                            localeService.getMessage("listing.create.success.title"),
                            localeService.getMessage("listing.create.success.header"),
                            localeService.getMessage("listing.create.success.content"));

                    // Clear the form instead of closing
                    clearForm();
                })
                .exceptionally(ex -> {
                    AlertHelper.showErrorAlert(
                            localeService.getMessage("listing.create.error.title"),
                            localeService.getMessage("listing.create.error.header"),
                            ex.getMessage());
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        // Clear temporary reservations and form
        tempReservedQuantities.clear();
        clearForm();
    }

    private void clearForm() {
        // Clear basic fields
        titleField.clear();
        descriptionArea.clear();
        
        // Clear selected items
        selectedItems.clear();
        updateSelectedItemsTable();
        refreshItemsTable();
        
        // Clear sell listing fields
        sellPriceField.clear();
        sellCurrencyComboBox.setValue(Currency.EUR);
        sellLocationField.clear();
        
        // Clear trade listing fields
        acceptMoneyOffersCheckBox.setSelected(false);
        referencePriceField.clear();
        tradeCurrencyComboBox.setValue(Currency.EUR);
        acceptMixedOffersCheckBox.setSelected(false);
        acceptOtherOffersCheckBox.setSelected(false);
        tradeLocationField.clear();
        
        // Clear selected categories
        selectedCategories.clear();
        updateSelectedCategoriesDisplay();
        availableCategoriesComboBox.setValue(null);
        
        // Clear gift listing fields
        pickupOnlyCheckBox.setSelected(false);
        allowThankYouOffersCheckBox.setSelected(false);
        restrictionsArea.clear();
        giftLocationField.clear();
        
        // Clear auction listing fields
        startingPriceField.clear();
        reservePriceField.clear();
        auctionCurrencyComboBox.setValue(Currency.EUR);
        bidIncrementField.clear();
        auctionLocationField.clear();
        
        // Reset duration to default
        if (durationComboBox.getItems() != null && !durationComboBox.getItems().isEmpty()) {
            durationComboBox.setValue(durationOptions.stream()
                    .filter(option -> option.getDays() == 7)
                    .findFirst()
                    .orElse(durationOptions.get(0)));
        }
        
        // Reset to the first tab (Sell)
        if (listingTypeTabs != null && !listingTypeTabs.getTabs().isEmpty()) {
            listingTypeTabs.getSelectionModel().selectFirst();
        }
        
        // Clear any table selections
        itemsTable.getSelectionModel().clearSelection();
        selectedItemsTable.getSelectionModel().clearSelection();
        
        // Disable add item button
        addItemButton.setDisable(true);
        
        // Reset quantity spinner
        if (quantitySpinner.getValueFactory() != null) {
            quantitySpinner.getValueFactory().setValue(1);
        }
    }

    private boolean validateCommonFields() {
        if (titleField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.validation.title.required"));
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.validation.description.required"));
            return false;
        }

        if (selectedItems.isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.validation.items.required"));
            return false;
        }

        return true;
    }

    private SellListingDTO createSellListing() {
        if (sellPriceField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.sell.validation.price.required"));
            return null;
        }

        try {
            BigDecimal price = new BigDecimal(sellPriceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("listing.validation.title"),
                        localeService.getMessage("listing.validation.header"),
                        localeService.getMessage("listing.sell.validation.price.positive"));
                return null;
            }

            SellListingDTO listing = new SellListingDTO();
            setupCommonFields(listing);
            listing.setPrice(price);
            listing.setCurrency(sellCurrencyComboBox.getValue());
            listing.setPickupLocation(sellLocationField.getText().trim());

            return listing;
        } catch (NumberFormatException e) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.sell.validation.price.invalid"));
            return null;
        }
    }

    private GiftListingDTO createGiftListing() {
        GiftListingDTO listing = new GiftListingDTO();
        setupCommonFields(listing);

        listing.setPickupOnly(pickupOnlyCheckBox.isSelected());
        listing.setAllowThankYouOffers(allowThankYouOffersCheckBox.isSelected());
        listing.setRestrictions(restrictionsArea.getText().trim());
        listing.setPickupLocation(giftLocationField.getText().trim());

        return listing;
    }

    private AuctionListingDTO createAuctionListing() {
        if (startingPriceField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.auction.validation.starting.price.required"));
            return null;
        }

        try {
            BigDecimal startingPrice = new BigDecimal(startingPriceField.getText().trim());
            if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("listing.validation.title"),
                        localeService.getMessage("listing.validation.header"),
                        localeService.getMessage("listing.auction.validation.starting.price.positive"));
                return null;
            }

            AuctionListingDTO listing = new AuctionListingDTO();
            setupCommonFields(listing);
            listing.setStartingPrice(startingPrice);
            listing.setCurrency(auctionCurrencyComboBox.getValue());
            listing.setDurationInDays(durationComboBox.getValue().getDays());
            listing.setPickupLocation(auctionLocationField.getText().trim());

            // Set optional reserve price
            if (!reservePriceField.getText().trim().isEmpty()) {
                BigDecimal reservePrice = new BigDecimal(reservePriceField.getText().trim());
                if (reservePrice.compareTo(startingPrice) < 0) {
                    AlertHelper.showWarningAlert(
                            localeService.getMessage("listing.validation.title"),
                            localeService.getMessage("listing.validation.header"),
                            localeService.getMessage("listing.auction.validation.reserve.price.lower"));
                    return null;
                }
                listing.setReservePrice(reservePrice);
            }

            // Set optional bid increment
            if (!bidIncrementField.getText().trim().isEmpty()) {
                BigDecimal bidIncrement = new BigDecimal(bidIncrementField.getText().trim());
                if (bidIncrement.compareTo(BigDecimal.ZERO) <= 0) {
                    AlertHelper.showWarningAlert(
                            localeService.getMessage("listing.validation.title"),
                            localeService.getMessage("listing.validation.header"),
                            localeService.getMessage("listing.auction.validation.bid.increment.positive"));
                    return null;
                }
                listing.setMinimumBidIncrement(bidIncrement);
            }

            return listing;
        } catch (NumberFormatException e) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listing.validation.title"),
                    localeService.getMessage("listing.validation.header"),
                    localeService.getMessage("listing.auction.validation.price.invalid"));
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

    public void refreshUI() {
        durationComboBox.setItems(FXCollections.observableArrayList(durationOptions));
    }
}