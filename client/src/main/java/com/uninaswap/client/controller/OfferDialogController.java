package com.uninaswap.client.controller;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.*;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.*;
import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ItemCondition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OfferDialogController implements Refreshable {

    // FXML Elements
    @FXML
    private ImageView listingImage;
    @FXML
    private Text listingTitle;
    @FXML
    private Text listingPrice;
    @FXML
    private Text offerForLabel;
    @FXML
    private Text itemsOfferSectionTitle;
    @FXML
    private Text selectedItemsSubtitle;
    @FXML
    private Text offerSummaryTitle;

    @FXML
    private VBox moneyOfferSection;
    @FXML
    private CheckBox includeMoneyCheckBox;
    @FXML
    private VBox moneyInputSection;
    @FXML
    private TextField moneyAmountField;
    @FXML
    private ComboBox<Currency> currencyComboBox;
    @FXML
    private Label priceConstraintLabel;
    @FXML
    private Label amountLabel;

    @FXML
    private VBox itemsOfferSection;
    @FXML
    private Button addNewItemButton;
    @FXML
    private TableView<ItemViewModel> availableItemsTable;
    @FXML
    private TableColumn<ItemViewModel, String> itemNameColumn;
    @FXML
    private TableColumn<ItemViewModel, String> itemConditionColumn;
    @FXML
    private TableColumn<ItemViewModel, Integer> itemAvailableColumn;
    @FXML
    private TableColumn<ItemViewModel, Void> itemQuantityColumn;
    @FXML
    private TableColumn<ItemViewModel, Void> itemActionColumn;
    @FXML
    private Label selectItemsLabel;

    @FXML
    private TableView<OfferItemViewModel> selectedItemsTable;
    @FXML
    private TableColumn<OfferItemViewModel, String> selectedNameColumn;
    @FXML
    private TableColumn<OfferItemViewModel, String> selectedConditionColumn;
    @FXML
    private TableColumn<OfferItemViewModel, Integer> selectedQuantityColumn;
    @FXML
    private TableColumn<OfferItemViewModel, Void> selectedRemoveColumn;

    @FXML
    private TextArea messageArea;
    @FXML
    private VBox offerSummaryContent;
    @FXML
    private ComboBox<DeliveryType> deliveryMethodComboBox;
    @FXML
    private Label messageLabel;

    // Services
    private final ItemService itemService = ItemService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final OfferService offerService = OfferService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    private final EventBusService eventBus = EventBusService.getInstance();

    // State
    private ListingViewModel currentListing;
    private final ObservableList<OfferItemViewModel> selectedItems = FXCollections.observableArrayList();
    private final Map<String, Integer> tempReservedQuantities = new HashMap<>();
    private final Map<Integer, Spinner<Integer>> rowSpinners = new HashMap<>();

    @FXML
    public void initialize() {
        setupCurrencyComboBox();
        setupMoneyOfferHandlers();
        setupAvailableItemsTable();
        setupSelectedItemsTable();
        updateOfferSummary();

        eventBus.subscribe(EventTypes.ITEM_UPDATED, itemViewModel -> {
            if (itemViewModel instanceof ItemViewModel) {
                addItemToOffer((ItemViewModel) itemViewModel, 1);
            }
        });

        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("offerdialog.debug.initialized", "OfferDialog initialized"));
    }

    @Override
    public void refreshUI() {
        // Update static labels and text elements
        if (offerForLabel != null) {
            offerForLabel.setText(localeService.getMessage("offerdialog.label.offer.for", "Offer for:"));
        }
        if (includeMoneyCheckBox != null) {
            includeMoneyCheckBox.setText(localeService.getMessage("offerdialog.checkbox.include.money", "Include money offer"));
        }
        if (amountLabel != null) {
            amountLabel.setText(localeService.getMessage("offerdialog.label.amount", "Amount:"));
        }
        if (itemsOfferSectionTitle != null) {
            itemsOfferSectionTitle.setText(localeService.getMessage("offerdialog.section.items.title", "Items to offer"));
        }
        if (selectItemsLabel != null) {
            selectItemsLabel.setText(localeService.getMessage("offerdialog.label.select.items", "Select from your items:"));
        }
        if (addNewItemButton != null) {
            addNewItemButton.setText(localeService.getMessage("offerdialog.button.add.new.item", "+ Add new item"));
        }
        if (selectedItemsSubtitle != null) {
            selectedItemsSubtitle.setText(localeService.getMessage("offerdialog.subtitle.selected.items", "Selected items for offer:"));
        }
        if (messageLabel != null) {
            messageLabel.setText(localeService.getMessage("offerdialog.label.message", "Message (optional):"));
        }
        if (offerSummaryTitle != null) {
            offerSummaryTitle.setText(localeService.getMessage("offerdialog.title.summary", "Offer Summary"));
        }

        // Update field prompts
        if (moneyAmountField != null) {
            moneyAmountField.setPromptText(localeService.getMessage("offerdialog.prompt.amount", "0.00"));
        }
        if (messageArea != null) {
            messageArea.setPromptText(localeService.getMessage("offerdialog.prompt.message", "Add a message for the seller..."));
        }

        // Update table column headers
        if (itemNameColumn != null) {
            itemNameColumn.setText(localeService.getMessage("offerdialog.column.name", "Name"));
        }
        if (itemConditionColumn != null) {
            itemConditionColumn.setText(localeService.getMessage("offerdialog.column.condition", "Condition"));
        }
        if (itemAvailableColumn != null) {
            itemAvailableColumn.setText(localeService.getMessage("offerdialog.column.available", "Available"));
        }
        if (itemQuantityColumn != null) {
            itemQuantityColumn.setText(localeService.getMessage("offerdialog.column.quantity", "Quantity"));
        }
        if (itemActionColumn != null) {
            itemActionColumn.setText(localeService.getMessage("offerdialog.column.action", "Action"));
        }
        if (selectedNameColumn != null) {
            selectedNameColumn.setText(localeService.getMessage("offerdialog.column.name", "Name"));
        }
        if (selectedConditionColumn != null) {
            selectedConditionColumn.setText(localeService.getMessage("offerdialog.column.condition", "Condition"));
        }
        if (selectedQuantityColumn != null) {
            selectedQuantityColumn.setText(localeService.getMessage("offerdialog.column.quantity", "Quantity"));
        }
        if (selectedRemoveColumn != null) {
            selectedRemoveColumn.setText(localeService.getMessage("offerdialog.column.remove", "Remove"));
        }

        // Update table placeholders
        if (availableItemsTable != null) {
            Label placeholder = new Label(localeService.getMessage("offerdialog.placeholder.no.items", "No items available in your inventory"));
            availableItemsTable.setPlaceholder(placeholder);
        }
        if (selectedItemsTable != null) {
            Label placeholder = new Label(localeService.getMessage("offerdialog.placeholder.no.selected", "No items selected for offer"));
            selectedItemsTable.setPlaceholder(placeholder);
        }

        // Update price constraint if listing is set
        if (currentListing != null) {
            updatePriceConstraintLabel();
        }

        // Update listing price display
        updateListingPriceDisplay();

        // Refresh the offer summary to update any localized content
        updateOfferSummary();
    }

    public void setListing(ListingViewModel listing) {
        this.currentListing = listing;

        if (listing != null) {
            Platform.runLater(() -> {
                populateListingInfo();
                configureOfferSections();
                loadAvailableItems();
                refreshUI(); // Refresh UI after setting listing
            });
        }
    }

    private void setupCurrencyComboBox() {
        List<Currency> currencies = List.of(Currency.EUR, Currency.USD, Currency.GBP);
        currencyComboBox.setItems(FXCollections.observableArrayList(currencies));
        currencyComboBox.setValue(Currency.EUR);
    }

    private void setupMoneyOfferHandlers() {
        includeMoneyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            moneyInputSection.setVisible(newVal);
            updateOfferSummary();
        });

        moneyAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateOfferSummary();
        });

        currencyComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateOfferSummary();
        });
    }

    private void setupAvailableItemsTable() {
        itemNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        itemConditionColumn.setCellValueFactory(cellData -> {
            ItemCondition condition = cellData.getValue().getCondition();
            return new SimpleStringProperty(condition != null ? condition.getDisplayName() : "");
        });

        itemAvailableColumn.setCellValueFactory(cellData -> {
            ItemViewModel item = cellData.getValue();
            int actualAvailable = item.getAvailableQuantity();
            int tempReserved = tempReservedQuantities.getOrDefault(item.getId(), 0);
            int effectiveAvailable = Math.max(0, actualAvailable - tempReserved);
            return javafx.beans.binding.Bindings.createObjectBinding(() -> effectiveAvailable);
        });

        // Quantity spinner column
        itemQuantityColumn.setCellFactory(col -> new TableCell<ItemViewModel, Void>() {
            private final Spinner<Integer> spinner = new Spinner<>();

            {
                spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
                spinner.setPrefWidth(70);
                spinner.setEditable(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    rowSpinners.remove(getIndex());
                } else {
                    ItemViewModel currentItem = getTableView().getItems().get(getIndex());
                    int available = currentItem.getAvailableQuantity() -
                            tempReservedQuantities.getOrDefault(currentItem.getId(), 0);

                    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner
                            .getValueFactory();
                    valueFactory.setMax(Math.max(1, available));
                    valueFactory.setValue(1);

                    if (available > 0) {
                        setGraphic(spinner);
                        rowSpinners.put(getIndex(), spinner);
                    } else {
                        setGraphic(null);
                        rowSpinners.remove(getIndex());
                    }
                }
            }
        });

        // Add button column
        itemActionColumn.setCellFactory(col -> new TableCell<ItemViewModel, Void>() {
            private final Button addButton = new Button();

            {
                addButton.setOnAction(e -> {
                    ItemViewModel item = getTableView().getItems().get(getIndex());
                    int quantity = getQuantityFromRow(getIndex());
                    addItemToOffer(item, quantity);
                });
                addButton.getStyleClass().add("add-to-offer-button");
                addButton.setText(localeService.getMessage("offerdialog.button.add", "Add"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ItemViewModel currentItem = getTableView().getItems().get(getIndex());
                    int available = currentItem.getAvailableQuantity() -
                            tempReservedQuantities.getOrDefault(currentItem.getId(), 0);
                    addButton.setDisable(available <= 0);
                    addButton.setText(localeService.getMessage("offerdialog.button.add", "Add"));
                    setGraphic(addButton);
                }
            }
        });
    }

    private int getQuantityFromRow(int rowIndex) {
        Spinner<Integer> spinner = rowSpinners.get(rowIndex);
        return spinner != null ? spinner.getValue() : 1;
    }

    private void setupSelectedItemsTable() {
        selectedNameColumn.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());

        selectedConditionColumn.setCellValueFactory(cellData -> {
            ItemCondition condition = cellData.getValue().getCondition();
            return new SimpleStringProperty(condition != null ? condition.getDisplayName() : "");
        });

        selectedQuantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());

        // Remove button column
        selectedRemoveColumn.setCellFactory(col -> new TableCell<OfferItemViewModel, Void>() {
            private final Button removeButton = new Button();

            {
                removeButton.setOnAction(e -> {
                    OfferItemViewModel item = getTableView().getItems().get(getIndex());
                    removeItemFromOffer(item);
                });
                removeButton.getStyleClass().add("remove-from-offer-button");
                removeButton.setText(localeService.getMessage("offerdialog.button.remove", "Remove"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    removeButton.setText(localeService.getMessage("offerdialog.button.remove", "Remove"));
                    setGraphic(removeButton);
                }
            }
        });

        selectedItemsTable.setItems(selectedItems);
    }

    public void handleAddNewItem() {
        navigationService.openItemDialog(new ItemViewModel());
    }

    private void populateListingInfo() {
        listingTitle.setText(currentListing.getTitle());
        updateListingPriceDisplay();

        // Load listing image
        if (currentListing.getItems() != null && !currentListing.getItems().isEmpty()) {
            String imagePath = currentListing.getItems().get(0).getItem().getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                imageService.fetchImage(imagePath)
                        .thenAccept(image -> Platform.runLater(() -> {
                            if (image != null && !image.isError()) {
                                listingImage.setImage(image);
                            } else {
                                setDefaultListingImage();
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                System.err.println(localeService.getMessage("offerdialog.error.image.load", "Failed to load listing image: {0}").replace("{0}", ex.getMessage()));
                                setDefaultListingImage();
                            });
                            return null;
                        });
            } else {
                setDefaultListingImage();
            }
        } else {
            setDefaultListingImage();
        }
    }

    private void updateListingPriceDisplay() {
        if (currentListing == null) return;

        // Set listing price info
        if (currentListing instanceof SellListingViewModel) {
            SellListingViewModel sellListing = (SellListingViewModel) currentListing;
            String currency = sellListing.getCurrency() != null ? sellListing.getCurrency().getSymbol() : "€";
            listingPrice.setText(currency + " " + sellListing.getPrice());
        } else if (currentListing instanceof TradeListingViewModel) {
            TradeListingViewModel tradeListing = (TradeListingViewModel) currentListing;
            if (tradeListing.isAcceptMoneyOffers() && tradeListing.getReferencePrice() != null) {
                String currency = tradeListing.getCurrency() != null ? tradeListing.getCurrency().getSymbol() : "€";
                listingPrice.setText(localeService.getMessage("offerdialog.price.reference", "Ref: {0} {1}")
                    .replace("{0}", currency)
                    .replace("{1}", tradeListing.getReferencePrice().toString()));
            } else {
                listingPrice.setText(localeService.getMessage("offerdialog.price.trade.only", "Trade only"));
            }
        }
    }

    private void setDefaultListingImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/immagine_generica.png"));
            listingImage.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println(localeService.getMessage("offerdialog.error.default.image", "Could not load default listing image: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void configureOfferSections() {
        String listingType = currentListing.getListingTypeValue().toUpperCase();

        switch (listingType) {
            case "SELL":
                // For sell listings, only money offers are allowed
                moneyOfferSection.setVisible(true);
                itemsOfferSection.setVisible(false);
                includeMoneyCheckBox.setSelected(true);
                includeMoneyCheckBox.setDisable(true); // Force money offer for sell listings
                updatePriceConstraintLabel();
                break;

            case "TRADE":
                TradeListingViewModel tradeListing = (TradeListingViewModel) currentListing;

                // Show money section only if trade accepts money offers
                moneyOfferSection.setVisible(tradeListing.isAcceptMoneyOffers());

                // Always show items section for trade listings
                itemsOfferSection.setVisible(true);
                updatePriceConstraintLabel();
                break;

            default:
                // For other listing types, hide offer sections
                moneyOfferSection.setVisible(false);
                itemsOfferSection.setVisible(false);
                break;
        }
    }

    private void updatePriceConstraintLabel() {
        if (currentListing instanceof SellListingViewModel) {
            String constraintText = localeService.getMessage("offerdialog.constraint.sell.price", "Must be less than the asking price");
            priceConstraintLabel.setText(constraintText);
        } else if (currentListing instanceof TradeListingViewModel) {
            TradeListingViewModel tradeListing = (TradeListingViewModel) currentListing;
            if (tradeListing.isAcceptMoneyOffers() && tradeListing.getReferencePrice() != null) {
                String refPriceText = localeService.getMessage("offerdialog.constraint.trade.reference", "Reference price: {0}")
                    .replace("{0}", tradeListing.getCurrency().getSymbol() + " " + tradeListing.getReferencePrice());
                priceConstraintLabel.setText(refPriceText);
            } else {
                priceConstraintLabel.setText(localeService.getMessage("offerdialog.constraint.trade.no.limit", "No price limit"));
            }
        }
    }

    private void loadAvailableItems() {
        // Convert ItemDTOs from service to ItemViewModels
        ObservableList<ItemViewModel> itemViewModels = FXCollections.observableArrayList();
        itemService.getUserItemsList().forEach(itemDTO -> {
            itemViewModels.add(viewModelMapper.toViewModel(itemDTO));
        });
        availableItemsTable.setItems(itemViewModels);
    }

    private void addItemToOffer(ItemViewModel item, int quantity) {
        // Check if item is already in the offer
        Optional<OfferItemViewModel> existingItem = selectedItems.stream()
                .filter(offerItem -> offerItem.getItemId().equals(item.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            OfferItemViewModel existing = existingItem.get();
            int newQuantity = existing.getQuantity() + quantity;
            int available = item.getAvailableQuantity() - tempReservedQuantities.getOrDefault(item.getId(), 0);

            if (newQuantity <= available) {
                existing.setQuantity(newQuantity);
                tempReservedQuantities.put(item.getId(),
                        tempReservedQuantities.getOrDefault(item.getId(), 0) + quantity);
            } else {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("offerdialog.warning.quantity.title", "Quantity not available"),
                        localeService.getMessage("offerdialog.warning.quantity.header", "Insufficient quantity"),
                        localeService.getMessage("offerdialog.warning.quantity.message", "You cannot add more items than available."));
                return;
            }
        } else {
            // Add new item
            int available = item.getAvailableQuantity() - tempReservedQuantities.getOrDefault(item.getId(), 0);

            if (quantity <= available) {
                OfferItemViewModel offerItem = new OfferItemViewModel(
                        item.getId(),
                        item.getName(),
                        item.getImagePath(),
                        item.getCondition(),
                        quantity,
                        item);
                selectedItems.add(offerItem);
                tempReservedQuantities.put(item.getId(),
                        tempReservedQuantities.getOrDefault(item.getId(), 0) + quantity);
            } else {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("offerdialog.warning.quantity.title", "Quantity not available"),
                        localeService.getMessage("offerdialog.warning.quantity.header", "Insufficient quantity"),
                        localeService.getMessage("offerdialog.warning.quantity.message", "You cannot add more items than available."));
                return;
            }
        }

        availableItemsTable.refresh();
        updateOfferSummary();
    }

    private void removeItemFromOffer(OfferItemViewModel offerItem) {
        selectedItems.remove(offerItem);

        // Update temp reserved quantities
        int currentReserved = tempReservedQuantities.getOrDefault(offerItem.getItemId(), 0);
        int newReserved = Math.max(0, currentReserved - offerItem.getQuantity());
        if (newReserved == 0) {
            tempReservedQuantities.remove(offerItem.getItemId());
        } else {
            tempReservedQuantities.put(offerItem.getItemId(), newReserved);
        }

        availableItemsTable.refresh();
        updateOfferSummary();
    }

    private void updateOfferSummary() {
        offerSummaryContent.getChildren().clear();

        // Money offer summary
        if (includeMoneyCheckBox.isSelected() && !moneyAmountField.getText().trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(moneyAmountField.getText().trim());
                Currency currency = currencyComboBox.getValue();

                String moneyOfferText = localeService.getMessage("offerdialog.summary.money.offer", "Money offer: {0} {1}")
                        .replace("{0}", currency.getSymbol())
                        .replace("{1}", amount.toString());
                Text moneyText = new Text(moneyOfferText);
                moneyText.getStyleClass().add("summary-item");
                offerSummaryContent.getChildren().add(moneyText);

                // Validate money offer constraints
                if (currentListing instanceof SellListingViewModel) {
                    SellListingViewModel sellListing = (SellListingViewModel) currentListing;
                    if (amount.compareTo(sellListing.getPrice()) >= 0) {
                        Text warningText = new Text("⚠ " + localeService.getMessage("offerdialog.validation.amount.too.high", "Offer must be less than asking price"));
                        warningText.getStyleClass().add("warning-text");
                        offerSummaryContent.getChildren().add(warningText);
                    }
                }
            } catch (NumberFormatException e) {
                Text errorText = new Text("⚠ " + localeService.getMessage("offerdialog.validation.amount.invalid", "Invalid amount format"));
                errorText.getStyleClass().add("error-text");
                offerSummaryContent.getChildren().add(errorText);
            }
        }

        // Items offer summary
        if (!selectedItems.isEmpty()) {
            Text itemsTitle = new Text(localeService.getMessage("offerdialog.summary.items.title", "Offered items:"));
            itemsTitle.getStyleClass().add("summary-title");
            offerSummaryContent.getChildren().add(itemsTitle);

            for (OfferItemViewModel item : selectedItems) {
                String itemText = localeService.getMessage("offerdialog.summary.item.entry", "{0} (x{1})")
                        .replace("{0}", item.getItemName())
                        .replace("{1}", String.valueOf(item.getQuantity()));
                Text itemEntry = new Text("• " + itemText);
                itemEntry.getStyleClass().add("summary-item");
                offerSummaryContent.getChildren().add(itemEntry);
            }
        }

        // Empty offer warning
        if (!includeMoneyCheckBox.isSelected() && selectedItems.isEmpty()) {
            Text warningText = new Text("⚠ " + localeService.getMessage("offerdialog.validation.empty", "Select at least a money offer or items"));
            warningText.getStyleClass().add("warning-text");
            offerSummaryContent.getChildren().add(warningText);
        }
    }

    public boolean isValidOffer() {
        boolean hasMoneyOffer = includeMoneyCheckBox.isSelected() &&
                !moneyAmountField.getText().trim().isEmpty();
        boolean hasItemOffer = !selectedItems.isEmpty();

        if (!hasMoneyOffer && !hasItemOffer) {
            return false;
        }

        // Validate money offer amount
        if (hasMoneyOffer) {
            try {
                BigDecimal amount = new BigDecimal(moneyAmountField.getText().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }

                // Check constraints for sell listings
                if (currentListing instanceof SellListingViewModel) {
                    SellListingViewModel sellListing = (SellListingViewModel) currentListing;
                    if (amount.compareTo(sellListing.getPrice()) >= 0) {
                        return false;
                    }
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    public CompletableFuture<Boolean> createOffer() {
        if (!isValidOffer()) {
            return CompletableFuture.completedFuture(false);
        }

        // Create OfferViewModel instead of OfferDTO
        OfferViewModel offer = new OfferViewModel();
        offer.setListingId(currentListing.getId());
        offer.setMessage(messageArea.getText().trim());
        System.out.println(localeService.getMessage("offerdialog.debug.delivery.type", "delivery type {0}")
            .replace("{0}", String.valueOf(currentListing.getDeliveryType(deliveryMethodComboBox != null ? deliveryMethodComboBox.getValue() : null))));
        offer.setDeliveryType(currentListing.getDeliveryType(deliveryMethodComboBox != null ? deliveryMethodComboBox.getValue() : null));

        // Set money offer
        if (includeMoneyCheckBox.isSelected() && !moneyAmountField.getText().trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(moneyAmountField.getText().trim());
                offer.setAmount(amount);
                offer.setCurrency(currencyComboBox.getValue());
            } catch (NumberFormatException e) {
                return CompletableFuture.completedFuture(false);
            }
        }

        // Set item offers
        if (!selectedItems.isEmpty()) {
            offer.getOfferItems().setAll(selectedItems);
        }

        return offerService.createOffer(offer)
                .thenApply(createdOffer -> {
                    System.out.println(localeService.getMessage("offerdialog.debug.offer.created", "Offer created successfully"));
                    return true;
                })
                .exceptionally(ex -> {
                    System.err.println(localeService.getMessage("offerdialog.error.offer.creation", "Failed to create offer: {0}").replace("{0}", ex.getMessage()));
                    return false;
                });
    }

    public void cleanup() {
        // Reset temp reserved quantities
        tempReservedQuantities.clear();
        // Clear spinner references
        rowSpinners.clear();
    }

    public CheckBox getIncludeMoneyCheckBox() {
        return includeMoneyCheckBox;
    }

    public TextField getMoneyAmountField() {
        return moneyAmountField;
    }

    public ObservableList<OfferItemViewModel> getSelectedItems() {
        return selectedItems;
    }
}
