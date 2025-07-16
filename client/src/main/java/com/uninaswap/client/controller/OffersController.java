package com.uninaswap.client.controller;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.OfferService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.OfferItemViewModel;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.common.enums.OfferStatus;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OffersController {

    // Received offers tab
    @FXML
    private TableView<OfferViewModel> receivedOffersTable;
    @FXML
    private TableColumn<OfferViewModel, String> listingTitleColumn;
    @FXML
    private TableColumn<OfferViewModel, String> offerFromColumn;
    @FXML
    private TableColumn<OfferViewModel, String> offerTypeColumn;
    @FXML
    private TableColumn<OfferViewModel, String> offerAmountColumn;
    @FXML
    private TableColumn<OfferViewModel, String> offerStatusColumn;
    @FXML
    private TableColumn<OfferViewModel, String> offerDateColumn;
    @FXML
    private TableColumn<OfferViewModel, Void> offerActionsColumn;

    // Sent offers tab
    @FXML
    private TableView<OfferViewModel> sentOffersTable;
    @FXML
    private TableColumn<OfferViewModel, String> sentListingTitleColumn;
    @FXML
    private TableColumn<OfferViewModel, String> sentOfferToColumn;
    @FXML
    private TableColumn<OfferViewModel, String> sentOfferTypeColumn;
    @FXML
    private TableColumn<OfferViewModel, String> sentOfferAmountColumn;
    @FXML
    private TableColumn<OfferViewModel, String> sentOfferStatusColumn;
    @FXML
    private TableColumn<OfferViewModel, String> sentOfferDateColumn;
    @FXML
    private TableColumn<OfferViewModel, Void> sentOfferActionsColumn;

    // History tab
    @FXML
    private TableView<OfferViewModel> offerHistoryTable;
    @FXML
    private TableColumn<OfferViewModel, String> historyListingTitleColumn;
    @FXML
    private TableColumn<OfferViewModel, String> historyOfferFromColumn;
    @FXML
    private TableColumn<OfferViewModel, String> historyOfferTypeColumn;
    @FXML
    private TableColumn<OfferViewModel, String> historyOfferAmountColumn;
    @FXML
    private TableColumn<OfferViewModel, String> historyOfferStatusColumn;
    @FXML
    private TableColumn<OfferViewModel, String> historyOfferDateColumn;

    // Offer details section
    @FXML
    private VBox offerDetailsSection;
    @FXML
    private Label offerFromLabel;
    @FXML
    private Label offerListingLabel;
    @FXML
    private Label offerTypeLabel;
    @FXML
    private Label offerAmountLabel;
    @FXML
    private TextArea offerMessageArea;
    @FXML
    private VBox offeredItemsSection;
    @FXML
    private TableView<OfferItemViewModel> offeredItemsTable;
    @FXML
    private TableColumn<OfferItemViewModel, String> itemNameColumn;
    @FXML
    private TableColumn<OfferItemViewModel, String> itemConditionColumn;
    @FXML
    private TableColumn<OfferItemViewModel, Integer> itemQuantityColumn;

    // Action buttons
    @FXML
    private Button refreshButton;
    @FXML
    private Button acceptOfferButton;
    @FXML
    private Button rejectOfferButton;
    @FXML
    private Button counterOfferButton;

    // Services
    private final OfferService offerService = OfferService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final EventBusService eventBus = EventBusService.getInstance();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    // Current selected offer
    private OfferViewModel selectedOffer;

    @FXML
    public void initialize() {
        setupReceivedOffersTable();
        setupSentOffersTable();
        setupHistoryTable();
        setupOfferedItemsTable();
        setupSelectionHandlers();

        // Bind tables directly to service observable lists
        receivedOffersTable.setItems(offerService.getReceivedOffersList());
        sentOffersTable.setItems(offerService.getUserOffersList());

        // Load offers
        refreshOffers();

        // Subscribe to events
        eventBus.subscribe(EventTypes.OFFER_UPDATED, _ -> refreshOffers());
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            Platform.runLater(() -> {
                clearAllTables();
                clearOfferDetails();
                System.out.println("OffersController: Cleared view on logout");
            });
        });
    }

    private void setupReceivedOffersTable() {
        listingTitleColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getListingTitle()));

        offerFromColumn
                .setCellValueFactory(
                        cellData -> new SimpleStringProperty(cellData.getValue().getOfferingUserUsername()));

        offerTypeColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(getOfferTypeDisplayName(cellData.getValue())));

        offerAmountColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(formatOfferAmount(cellData.getValue())));

        offerStatusColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        offerDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatDate(cellData.getValue())));

        // Setup action buttons in table
        setupActionButtons(offerActionsColumn, true);
    }

    private void setupSentOffersTable() {
        sentListingTitleColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getListingTitle()));

        sentOfferToColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getListingOwnerUsername()));

        sentOfferTypeColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(getOfferTypeDisplayName(cellData.getValue())));

        sentOfferAmountColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(formatOfferAmount(cellData.getValue())));

        sentOfferStatusColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        sentOfferDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatDate(cellData.getValue())));

        // Setup action buttons for sent offers (limited actions)
        setupActionButtons(sentOfferActionsColumn, false);
    }

    private void setupHistoryTable() {
        historyListingTitleColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getListingTitle()));

        historyOfferFromColumn
                .setCellValueFactory(
                        cellData -> new SimpleStringProperty(cellData.getValue().getOfferingUserUsername()));

        historyOfferTypeColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(getOfferTypeDisplayName(cellData.getValue())));

        historyOfferAmountColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(formatOfferAmount(cellData.getValue())));

        historyOfferStatusColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        historyOfferDateColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(formatDate(cellData.getValue())));
    }

    private void setupOfferedItemsTable() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemConditionColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getCondition().getDisplayName()));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    }

    private void setupActionButtons(TableColumn<OfferViewModel, Void> column, boolean isReceived) {
        column.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button(localeService.getMessage("offers.button.view", "View"));
            private final Button withdrawButton = new Button(
                    localeService.getMessage("offers.button.withdraw", "Withdraw"));

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    OfferViewModel offer = getTableView().getItems().get(getIndex());

                    if (isReceived) {
                        // For received offers, show view button
                        viewButton.setOnAction(_ -> showOfferDetails(offer));
                        setGraphic(viewButton);
                    } else {
                        // For sent offers, show withdraw button (if pending)
                        if (offer.getStatus() == OfferStatus.PENDING) {
                            withdrawButton.setOnAction(_ -> handleWithdrawOffer(offer));
                            setGraphic(withdrawButton);
                        } else {
                            viewButton.setOnAction(_ -> showOfferDetails(offer));
                            setGraphic(viewButton);
                        }
                    }
                }
            }
        });
    }

    private void setupSelectionHandlers() {
        // Add selection listeners to show offer details
        receivedOffersTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                showOfferDetails(newSelection);
            }
        });
    }

    @FXML
    private void handleRefreshOffers() {
        refreshOffers();
    }

    @FXML
    private void handleAcceptOffer() {
        if (selectedOffer == null)
            return;

        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("offers.accept.title", "Accept Offer"),
                localeService.getMessage("offers.accept.header", "Accept Offer"),
                localeService.getMessage("offers.accept.content", "Are you sure you want to accept this offer?"));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            offerService.acceptOffer(selectedOffer.getId())
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("offers.accept.success.title", "Offer Accepted"),
                                    localeService.getMessage("offers.accept.success.header", "Success"),
                                    localeService.getMessage("offers.accept.success.message",
                                            "Offer accepted successfully"));

                            // Ask if user wants to schedule pickup
                            Alert pickupDialog = AlertHelper.createConfirmationDialog(
                                    localeService.getMessage("pickup.schedule.title", "Schedule Pickup"),
                                    localeService.getMessage("pickup.schedule.header",
                                            "Would you like to schedule a pickup?"),
                                    localeService.getMessage("pickup.schedule.content",
                                            "You can schedule a pickup time now or do it later from your offers page."));

                            pickupDialog.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    handleSchedulePickup(selectedOffer);
                                }
                            });

                            refreshOffers();
                        } else {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("offers.accept.error.title", "Error"),
                                    localeService.getMessage("offers.accept.error.header", "Failed to accept offer"),
                                    localeService.getMessage("offers.accept.error.message",
                                            "Could not accept the offer"));
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> AlertHelper.showErrorAlert(
                                localeService.getMessage("offers.error.title", "Error"),
                                localeService.getMessage("offers.error.header", "Connection Error"),
                                ex.getMessage()));
                        return null;
                    });
        }
    }

    @FXML
    private void handleRejectOffer() {
        if (selectedOffer == null)
            return;

        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("offers.reject.title", "Reject Offer"),
                localeService.getMessage("offers.reject.header", "Reject Offer"),
                localeService.getMessage("offers.reject.content", "Are you sure you want to reject this offer?"));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            offerService.rejectOffer(selectedOffer.getId())
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("offers.reject.success.title", "Offer Rejected"),
                                    localeService.getMessage("offers.reject.success.header", "Success"),
                                    localeService.getMessage("offers.reject.success.message",
                                            "Offer rejected successfully"));
                            refreshOffers();
                        } else {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("offers.reject.error.title", "Error"),
                                    localeService.getMessage("offers.reject.error.header", "Failed to reject offer"),
                                    localeService.getMessage("offers.reject.error.message",
                                            "Could not reject the offer"));
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> AlertHelper.showErrorAlert(
                                localeService.getMessage("offers.error.title", "Error"),
                                localeService.getMessage("offers.error.header", "Connection Error"),
                                ex.getMessage()));
                        return null;
                    });
        }
    }

    @FXML
    private void handleCounterOffer() {
        if (selectedOffer == null)
            return;

        // TODO: Implement counter offer functionality
        // This would open a dialog similar to the offer dialog but pre-filled with
        // current offer data
        AlertHelper.showInformationAlert(
                localeService.getMessage("offers.counter.title", "Counter Offer"),
                localeService.getMessage("offers.counter.header", "Feature Coming Soon"),
                localeService.getMessage("offers.counter.message",
                        "Counter offer functionality will be available soon"));
    }

    private void handleWithdrawOffer(OfferViewModel offer) {
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("offers.withdraw.title", "Withdraw Offer"),
                localeService.getMessage("offers.withdraw.header", "Withdraw Offer"),
                localeService.getMessage("offers.withdraw.content", "Are you sure you want to withdraw this offer?"));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            offerService.withdrawOffer(offer.getId())
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("offers.withdraw.success.title", "Offer Withdrawn"),
                                    localeService.getMessage("offers.withdraw.success.header", "Success"),
                                    localeService.getMessage("offers.withdraw.success.message",
                                            "Offer withdrawn successfully"));
                            refreshOffers();
                        } else {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("offers.withdraw.error.title", "Error"),
                                    localeService.getMessage("offers.withdraw.error.header",
                                            "Failed to withdraw offer"),
                                    localeService.getMessage("offers.withdraw.error.message",
                                            "Could not withdraw the offer"));
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> AlertHelper.showErrorAlert(
                                localeService.getMessage("offers.error.title", "Error"),
                                localeService.getMessage("offers.error.header", "Connection Error"),
                                ex.getMessage()));
                        return null;
                    });
        }
    }

    private void handleSchedulePickup(OfferViewModel offer) {
        navigationService.openPickupScheduling(offer, (Stage) acceptOfferButton.getScene().getWindow());
    }

    private void refreshOffers() {
        offerService.getReceivedOffers()
                .thenCompose(_ -> offerService.getSentOffers())
                .thenCompose(_ -> offerService.getOfferHistory())
                .thenAccept(historyOffers -> Platform.runLater(() -> {
                    // Handle history offers manually
                    offerHistoryTable.getItems().clear();
                    historyOffers.forEach(offer -> {
                        OfferViewModel offerViewModel = viewModelMapper.toViewModel(offer);
                        offerHistoryTable.getItems().add(offerViewModel);
                    });
                }))
                .exceptionally(ex -> {
                    System.err.println("Error loading offers: " + ex.getMessage());
                    return null;
                });
    }

    private void showOfferDetails(OfferViewModel offer) {
        selectedOffer = offer;

        // Show offer details
        offerFromLabel.setText(offer.getOfferingUserUsername());
        offerListingLabel.setText(offer.getListingTitle());
        offerTypeLabel.setText(getOfferTypeDisplayName(offer));
        offerAmountLabel.setText(formatOfferAmount(offer));
        offerMessageArea.setText(offer.getMessage() != null ? offer.getMessage() : "");

        // Show offered items if trade offer
        if (offer.getOfferItems() != null && !offer.getOfferItems().isEmpty()) {
            offeredItemsSection.setVisible(true);
            offeredItemsTable.getItems().clear();
            offeredItemsTable.getItems().addAll(offer.getOfferItems());
        } else {
            offeredItemsSection.setVisible(false);
        }

        // Enable/disable action buttons based on offer status and type
        boolean canAcceptReject = offer.getStatus() == OfferStatus.PENDING &&
                receivedOffersTable.getSelectionModel().getSelectedItem() != null;
        acceptOfferButton.setDisable(!canAcceptReject);
        rejectOfferButton.setDisable(!canAcceptReject);
        counterOfferButton.setDisable(!canAcceptReject);

        offerDetailsSection.setVisible(true);
    }

    private void clearOfferDetails() {
        selectedOffer = null;
        offerDetailsSection.setVisible(false);
        offeredItemsSection.setVisible(false);
    }

    private void clearAllTables() {
        // Clear the service's observable lists instead of table items directly
        offerService.getReceivedOffersList().clear();
        offerService.getUserOffersList().clear();
        offerHistoryTable.getItems().clear();
        offeredItemsTable.getItems().clear();
    }

    private String getOfferTypeDisplayName(OfferViewModel offer) {
        if (offer.getAmount() != null && offer.getAmount().compareTo(BigDecimal.valueOf(0)) > 0) {
            if (offer.getOfferItems() != null && !offer.getOfferItems().isEmpty()) {
                return localeService.getMessage("offers.type.mixed", "Mixed");
            } else {
                return localeService.getMessage("offers.type.money", "Money");
            }
        } else if (offer.getOfferItems() != null && !offer.getOfferItems().isEmpty()) {
            return localeService.getMessage("offers.type.trade", "Trade");
        } else {
            return localeService.getMessage("offers.type.unknown", "Unknown");
        }
    }

    private String formatOfferAmount(OfferViewModel offer) {
        if (offer.getAmount() != null && offer.getAmount().compareTo(BigDecimal.valueOf(0)) > 0) {
            String currency = offer.getCurrency() != null ? offer.getCurrency().getSymbol() : "€";
            return String.format("%s %.2f", currency, offer.getAmount());
        }
        return "-";
    }

    private String formatDate(OfferViewModel offer) {
        if (offer.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return offer.getCreatedAt().format(formatter);
        }
        return "-";
    }
}
