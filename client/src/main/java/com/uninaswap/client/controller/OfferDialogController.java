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

public class OfferDialogController {

    // FXML Elements
    @FXML
    private ImageView listingImage;
    @FXML
    private Text listingTitle;
    @FXML
    private Text listingPrice;

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
    }

    public void setListing(ListingViewModel listing) {
        this.currentListing = listing;

        if (listing != null) {
            Platform.runLater(() -> {
                populateListingInfo();
                configureOfferSections();
                loadAvailableItems();
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
            private final Button addButton = new Button("Aggiungi");

            {
                addButton.setOnAction(e -> {
                    ItemViewModel item = getTableView().getItems().get(getIndex());
                    int quantity = getQuantityFromRow(getIndex());
                    addItemToOffer(item, quantity);
                });
                addButton.getStyleClass().add("add-to-offer-button");
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
            private final Button removeButton = new Button("Rimuovi");

            {
                removeButton.setOnAction(e -> {
                    OfferItemViewModel item = getTableView().getItems().get(getIndex());
                    removeItemFromOffer(item);
                });
                removeButton.getStyleClass().add("remove-from-offer-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        selectedItemsTable.setItems(selectedItems);
    }

    public void handleAddNewItem() {
        navigationService.openItemDialog(new ItemViewModel());
    }

    private void populateListingInfo() {
        listingTitle.setText(currentListing.getTitle());

        // Set listing price info
        if (currentListing instanceof SellListingViewModel) {
            SellListingViewModel sellListing = (SellListingViewModel) currentListing;
            String currency = sellListing.getCurrency() != null ? sellListing.getCurrency().getSymbol() : "€";
            listingPrice.setText(currency + " " + sellListing.getPrice());
        } else if (currentListing instanceof TradeListingViewModel) {
            TradeListingViewModel tradeListing = (TradeListingViewModel) currentListing;
            if (tradeListing.isAcceptMoneyOffers() && tradeListing.getReferencePrice() != null) {
                String currency = tradeListing.getCurrency() != null ? tradeListing.getCurrency().getSymbol() : "€";
                listingPrice.setText("Rif: " + currency + " " + tradeListing.getReferencePrice());
            } else {
                listingPrice.setText("Solo scambio");
            }
        }

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
                        }));
            } else {
                setDefaultListingImage();
            }
        } else {
            setDefaultListingImage();
        }
    }

    private void setDefaultListingImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/immagine_generica.png"));
            listingImage.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default listing image: " + e.getMessage());
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

                // Set constraint label for sell listings
                SellListingViewModel sellListing = (SellListingViewModel) currentListing;
                String constraintText = localeService.getMessage("offer.constraint.sell.price");
                priceConstraintLabel.setText(constraintText);
                break;

            case "TRADE":
                TradeListingViewModel tradeListing = (TradeListingViewModel) currentListing;

                // Show money section only if trade accepts money offers
                moneyOfferSection.setVisible(tradeListing.isAcceptMoneyOffers());

                // Always show items section for trade listings
                itemsOfferSection.setVisible(true);

                if (tradeListing.isAcceptMoneyOffers() && tradeListing.getReferencePrice() != null) {
                    String refPriceText = localeService.getMessage("offer.constraint.trade.reference",
                            tradeListing.getCurrency().getSymbol() + " " + tradeListing.getReferencePrice());
                    priceConstraintLabel.setText(refPriceText);
                } else {
                    priceConstraintLabel.setText(localeService.getMessage("offer.constraint.trade.no.limit"));
                }
                break;

            default:
                // For other listing types, hide offer sections
                moneyOfferSection.setVisible(false);
                itemsOfferSection.setVisible(false);
                break;
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
                        "Quantità non disponibile",
                        "Quantità insufficiente",
                        "Non puoi aggiungere più oggetti di quelli disponibili.");
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
                        "Quantità non disponibile",
                        "Quantità insufficiente",
                        "Non puoi aggiungere più oggetti di quelli disponibili.");
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

                String moneyOfferText = localeService.getMessage("offer.summary.money.offer",
                        currency.getSymbol(), amount);
                Text moneyText = new Text(moneyOfferText);
                moneyText.getStyleClass().add("summary-item");
                offerSummaryContent.getChildren().add(moneyText);

                // Validate money offer constraints
                if (currentListing instanceof SellListingViewModel) {
                    SellListingViewModel sellListing = (SellListingViewModel) currentListing;
                    if (amount.compareTo(sellListing.getPrice()) >= 0) {
                        Text warningText = new Text(
                                "⚠ " + localeService.getMessage("offer.validation.amount.too.high"));
                        warningText.getStyleClass().add("warning-text");
                        offerSummaryContent.getChildren().add(warningText);
                    }
                }
            } catch (NumberFormatException e) {
                Text errorText = new Text("⚠ " + localeService.getMessage("offer.validation.amount.invalid"));
                errorText.getStyleClass().add("error-text");
                offerSummaryContent.getChildren().add(errorText);
            }
        }

        // Items offer summary
        if (!selectedItems.isEmpty()) {
            Text itemsTitle = new Text(localeService.getMessage("offer.summary.items.title"));
            itemsTitle.getStyleClass().add("summary-title");
            offerSummaryContent.getChildren().add(itemsTitle);

            for (OfferItemViewModel item : selectedItems) {
                String itemText = localeService.getMessage("offer.summary.item.entry",
                        item.getItemName(), item.getQuantity());
                Text itemEntry = new Text("• " + itemText);
                itemEntry.getStyleClass().add("summary-item");
                offerSummaryContent.getChildren().add(itemEntry);
            }
        }

        // Empty offer warning
        if (!includeMoneyCheckBox.isSelected() && selectedItems.isEmpty()) {
            Text warningText = new Text("⚠ " + localeService.getMessage("offer.validation.empty"));
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
        System.out.println("delivery type " + currentListing.getDeliveryType(deliveryMethodComboBox != null ? deliveryMethodComboBox.getValue() : null));
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
                .thenApply(createdOffer -> true)
                .exceptionally(ex -> false);
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
