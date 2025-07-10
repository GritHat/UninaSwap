package com.uninaswap.client.controller;

import com.uninaswap.client.service.*;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.common.dto.*;
import com.uninaswap.common.enums.Currency;
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
    private TableView<ItemDTO> availableItemsTable;
    @FXML
    private TableColumn<ItemDTO, String> itemNameColumn;
    @FXML
    private TableColumn<ItemDTO, String> itemConditionColumn;
    @FXML
    private TableColumn<ItemDTO, Integer> itemAvailableColumn;
    @FXML
    private TableColumn<ItemDTO, Void> itemQuantityColumn;
    @FXML
    private TableColumn<ItemDTO, Void> itemActionColumn;

    @FXML
    private TableView<OfferItemDTO> selectedItemsTable;
    @FXML
    private TableColumn<OfferItemDTO, String> selectedNameColumn;
    @FXML
    private TableColumn<OfferItemDTO, String> selectedConditionColumn;
    @FXML
    private TableColumn<OfferItemDTO, Integer> selectedQuantityColumn;
    @FXML
    private TableColumn<OfferItemDTO, Void> selectedRemoveColumn;

    @FXML
    private TextArea messageArea;
    @FXML
    private VBox offerSummaryContent;

    // Services
    private final ItemService itemService = ItemService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final OfferService offerService = OfferService.getInstance();

    // State
    private ListingDTO currentListing;
    private final ObservableList<OfferItemDTO> selectedItems = FXCollections.observableArrayList();
    private final Map<String, Integer> tempReservedQuantities = new HashMap<>();

    @FXML
    public void initialize() {
        setupCurrencyComboBox();
        setupMoneyOfferHandlers();
        setupAvailableItemsTable();
        setupSelectedItemsTable();
        updateOfferSummary();
    }

    public void setListing(ListingDTO listing) {
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
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        itemConditionColumn.setCellValueFactory(cellData -> {
            ItemCondition condition = cellData.getValue().getCondition();
            return new SimpleStringProperty(condition != null ? condition.getDisplayName() : "");
        });

        itemAvailableColumn.setCellValueFactory(cellData -> {
            ItemDTO item = cellData.getValue();
            int actualAvailable = item.getAvailableQuantity();
            int tempReserved = tempReservedQuantities.getOrDefault(item.getId(), 0);
            int effectiveAvailable = Math.max(0, actualAvailable - tempReserved);
            return javafx.beans.binding.Bindings.createObjectBinding(() -> effectiveAvailable);
        });

        // Quantity spinner column
        itemQuantityColumn.setCellFactory(col -> new TableCell<ItemDTO, Void>() {
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
                } else {
                    ItemDTO currentItem = getTableView().getItems().get(getIndex());
                    int available = currentItem.getAvailableQuantity() -
                            tempReservedQuantities.getOrDefault(currentItem.getId(), 0);

                    SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner
                            .getValueFactory();
                    valueFactory.setMax(Math.max(1, available));
                    valueFactory.setValue(1);

                    setGraphic(available > 0 ? spinner : null);
                }
            }
        });

        // Add button column
        itemActionColumn.setCellFactory(col -> new TableCell<ItemDTO, Void>() {
            private final Button addButton = new Button("Aggiungi");

            {
                addButton.setOnAction(e -> {
                    ItemDTO item = getTableView().getItems().get(getIndex());
                    TableCell<ItemDTO, Void> quantityCell = (TableCell<ItemDTO, Void>) getTableView().getColumns()
                            .get(3).getCellFactory().call(null);

                    // Get quantity from the spinner in the same row
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
                    ItemDTO currentItem = getTableView().getItems().get(getIndex());
                    int available = currentItem.getAvailableQuantity() -
                            tempReservedQuantities.getOrDefault(currentItem.getId(), 0);
                    addButton.setDisable(available <= 0);
                    setGraphic(addButton);
                }
            }
        });
    }

    private int getQuantityFromRow(int rowIndex) {
        // Get the spinner from the quantity column
        TableRow<ItemDTO> row = availableItemsTable.getRowFactory().call(availableItemsTable);
        TableCell<ItemDTO, Void> cell = (TableCell<ItemDTO, Void>) availableItemsTable.getColumns().get(3)
                .getCellFactory().call(null);

        // For now, return 1 as default. In a real implementation,
        // you'd need to track the spinner values more carefully
        return 1;
    }

    private void setupSelectedItemsTable() {
        selectedNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        selectedConditionColumn.setCellValueFactory(cellData -> {
            ItemCondition condition = cellData.getValue().getCondition();
            return new SimpleStringProperty(condition != null ? condition.getDisplayName() : "");
        });

        selectedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Remove button column
        selectedRemoveColumn.setCellFactory(col -> new TableCell<OfferItemDTO, Void>() {
            private final Button removeButton = new Button("Rimuovi");

            {
                removeButton.setOnAction(e -> {
                    OfferItemDTO item = getTableView().getItems().get(getIndex());
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

    private void populateListingInfo() {
        listingTitle.setText(currentListing.getTitle());

        // Set listing price info
        if (currentListing instanceof SellListingDTO) {
            SellListingDTO sellListing = (SellListingDTO) currentListing;
            String currency = sellListing.getCurrency() != null ? sellListing.getCurrency().getSymbol() : "€";
            listingPrice.setText(currency + " " + sellListing.getPrice());
        } else if (currentListing instanceof TradeListingDTO) {
            TradeListingDTO tradeListing = (TradeListingDTO) currentListing;
            if (tradeListing.isAcceptMoneyOffers() && tradeListing.getReferencePrice() != null) {
                String currency = tradeListing.getCurrency() != null ? tradeListing.getCurrency().getSymbol() : "€";
                listingPrice.setText("Rif: " + currency + " " + tradeListing.getReferencePrice());
            } else {
                listingPrice.setText("Solo scambio");
            }
        }

        // Load listing image
        if (currentListing.getItems() != null && !currentListing.getItems().isEmpty()) {
            String imagePath = currentListing.getItems().get(0).getItemImagePath();
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
                SellListingDTO sellListing = (SellListingDTO) currentListing;
                String constraintText = localeService.getMessage("offer.constraint.sell.price");
                priceConstraintLabel.setText(constraintText);
                break;

            case "TRADE":
                TradeListingDTO tradeListing = (TradeListingDTO) currentListing;

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
        availableItemsTable.setItems(itemService.getUserItemsList());
    }

    private void addItemToOffer(ItemDTO item, int quantity) {
        // Check if item is already in the offer
        Optional<OfferItemDTO> existingItem = selectedItems.stream()
                .filter(offerItem -> offerItem.getItemId().equals(item.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            OfferItemDTO existing = existingItem.get();
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
                OfferItemDTO offerItem = new OfferItemDTO(
                        item.getId(),
                        item.getName(),
                        item.getImagePath(),
                        item.getCondition(),
                        quantity);
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

    private void removeItemFromOffer(OfferItemDTO offerItem) {
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

    @FXML
    private void handleAddNewItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemDialogView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            DialogPane dialogPane = loader.load();

            ButtonType confirmButtonType = new ButtonType(
                    localeService.getMessage("button.confirm"),
                    ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(
                    localeService.getMessage("button.cancel"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

            ItemDialogController controller = loader.getController();
            controller.setItem(new ItemDTO());

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(localeService.getMessage("item.add.title"));
            dialog.setHeaderText(localeService.getMessage("item.add.header"));
            dialog.setDialogPane(dialogPane);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
                ItemDTO newItem = controller.getUpdatedItem();
                File selectedImageFile = controller.getSelectedImageFile();

                // Upload image and save item
                if (selectedImageFile != null) {
                    imageService.uploadImageViaHttp(selectedImageFile)
                            .thenAccept(imagePath -> {
                                newItem.setImagePath(imagePath);
                                saveNewItemAndAddToOffer(newItem);
                            })
                            .exceptionally(ex -> {
                                AlertHelper.showErrorAlert(
                                        "Errore caricamento immagine",
                                        "Impossibile caricare l'immagine",
                                        ex.getMessage());
                                return null;
                            });
                } else {
                    saveNewItemAndAddToOffer(newItem);
                }
            }
        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    "Errore",
                    "Impossibile aprire il dialogo",
                    e.getMessage());
        }
    }

    private void saveNewItemAndAddToOffer(ItemDTO newItem) {
        itemService.addItem(newItem)
                .thenAccept(savedItem -> Platform.runLater(() -> {
                    // Refresh available items table
                    loadAvailableItems();

                    // Automatically add the new item to the offer
                    addItemToOffer(savedItem, 1);

                    AlertHelper.showInformationAlert(
                            "Oggetto aggiunto",
                            "Nuovo oggetto creato",
                            "L'oggetto è stato aggiunto al tuo inventario e all'offerta.");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                            "Errore salvataggio",
                            "Impossibile salvare l'oggetto",
                            ex.getMessage()));
                    return null;
                });
    }

    private void updateOfferSummary() {
        offerSummaryContent.getChildren().clear();

        // Money offer summary
        if (includeMoneyCheckBox.isSelected() && !moneyAmountField.getText().trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(moneyAmountField.getText().trim());
                Currency currency = currencyComboBox.getValue();

                // Use localized message
                String moneyOfferText = localeService.getMessage("offer.summary.money.offer",
                        currency.getSymbol(), amount);
                Text moneyText = new Text(moneyOfferText);
                moneyText.getStyleClass().add("summary-item");
                offerSummaryContent.getChildren().add(moneyText);

                // Validate money offer constraints
                if (currentListing instanceof SellListingDTO) {
                    SellListingDTO sellListing = (SellListingDTO) currentListing;
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

            for (OfferItemDTO item : selectedItems) {
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
                if (currentListing instanceof SellListingDTO) {
                    SellListingDTO sellListing = (SellListingDTO) currentListing;
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

    public OfferDTO createOffer() {
        if (!isValidOffer()) {
            return null;
        }

        OfferDTO offer = new OfferDTO();
        offer.setListingId(currentListing.getId());
        offer.setMessage(messageArea.getText().trim());

        // Set money offer
        if (includeMoneyCheckBox.isSelected() && !moneyAmountField.getText().trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(moneyAmountField.getText().trim());
                offer.setAmount(amount);
                offer.setCurrency(currencyComboBox.getValue());
            } catch (NumberFormatException e) {
                // This shouldn't happen if validation is correct
                return null;
            }
        }

        // Set item offers
        if (!selectedItems.isEmpty()) {
            List<OfferItemDTO> offerItems = new ArrayList<>(selectedItems);
            offer.setOfferItems(offerItems);
        }

        return offer;
    }

    public void cleanup() {
        // Reset temp reserved quantities
        tempReservedQuantities.clear();
    }

    public CheckBox getIncludeMoneyCheckBox() {
        return includeMoneyCheckBox;
    }

    public TextField getMoneyAmountField() {
        return moneyAmountField;
    }

    public ObservableList<OfferItemDTO> getSelectedItems() {
        return selectedItems;
    }
}
