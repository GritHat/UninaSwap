package com.uninaswap.client.controller;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.OfferService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ItemViewModel;
import com.uninaswap.client.viewmodel.OfferItemViewModel;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.common.enums.OfferStatus;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class OffersController {

    // Filter controls
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private ComboBox<String> typeFilterComboBox;
    @FXML
    private Button clearFiltersButton;

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
    private VBox offeredItemsList;

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
    private final ImageService imageService = ImageService.getInstance();

    // Current selected offer
    private OfferViewModel selectedOffer;
    
    // Filtered lists for tables
    private FilteredList<OfferViewModel> filteredReceivedOffers;
    private FilteredList<OfferViewModel> filteredSentOffers;
    private FilteredList<OfferViewModel> filteredHistoryOffers;

    @FXML
    public void initialize() {
        setupFilters();
        setupReceivedOffersTable();
        setupSentOffersTable();
        setupHistoryTable();
        setupSelectionHandlers();
        setupFilteredLists();

        // Subscribe to events
        eventBus.subscribe(EventTypes.OFFER_UPDATED, _ -> refreshOffers());
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            Platform.runLater(() -> {
                clearAllTables();
                clearOfferDetails();
                System.out.println("OffersController: Cleared view on logout");
            });
        });

        // Load offers
        refreshOffers();
    }

    private void setupFilters() {
        // Status filter with localized values
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("offers.filter.all.statuses", "All Statuses"),
                localeService.getMessage("offers.status.pending", "Pending"),
                localeService.getMessage("offers.status.accepted", "Accepted"),
                localeService.getMessage("offers.status.rejected", "Rejected"),
                localeService.getMessage("offers.status.withdrawn", "Withdrawn"),
                localeService.getMessage("offers.status.completed", "Completed")
        ));
        statusFilterComboBox.setValue(localeService.getMessage("offers.filter.all.statuses", "All Statuses"));

        // Type filter with localized values
        typeFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("offers.filter.all.types", "All Types"),
                localeService.getMessage("offers.type.money", "Money"),
                localeService.getMessage("offers.type.trade", "Trade"),
                localeService.getMessage("offers.type.mixed", "Mixed")
        ));
        typeFilterComboBox.setValue(localeService.getMessage("offers.filter.all.types", "All Types"));

        // Add listeners for filter changes
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        typeFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        searchField.textProperty().addListener((_, _, _) -> updateFilters());
    }

    private void setupFilteredLists() {
        // Create filtered lists
        filteredReceivedOffers = new FilteredList<>(offerService.getReceivedOffersList());
        filteredSentOffers = new FilteredList<>(offerService.getUserOffersList());
        
        // For history, we'll manage it separately since it's not an observable list from service
        // We'll update the filter when we load history data

        // Bind filtered lists to tables
        receivedOffersTable.setItems(filteredReceivedOffers);
        sentOffersTable.setItems(filteredSentOffers);
    }

    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = statusFilterComboBox.getValue();
        String typeFilter = typeFilterComboBox.getValue();

        // Create predicate for filtering
        javafx.util.Callback<OfferViewModel, Boolean> predicate = offer -> {
            // Search filter
            if (!searchText.isEmpty()) {
                boolean matchesSearch = 
                    (offer.getListingTitle() != null && offer.getListingTitle().toLowerCase().contains(searchText)) ||
                    (offer.getOfferingUserUsername() != null && offer.getOfferingUserUsername().toLowerCase().contains(searchText)) ||
                    (offer.getMessage() != null && offer.getMessage().toLowerCase().contains(searchText));
                if (!matchesSearch) return false;
            }

            // Status filter
            if (!"Tutti gli stati".equals(statusFilter)) {
                OfferStatus selectedStatus = mapStatusFilterToEnum(statusFilter);
                if (selectedStatus != null && offer.getStatus() != selectedStatus) {
                    return false;
                }
            }

            // Type filter
            if (!"Tutti i tipi".equals(typeFilter)) {
                String offerType = getOfferTypeDisplayName(offer);
                if (!typeFilter.equals(offerType)) {
                    return false;
                }
            }

            return true;
        };

        // Apply filter to received offers
        filteredReceivedOffers.setPredicate(predicate::call);
        
        // Apply filter to sent offers
        filteredSentOffers.setPredicate(predicate::call);
        
        // Apply filter to history table manually since it's not using a filtered list
        filterHistoryTable(predicate);
    }

    private void filterHistoryTable(javafx.util.Callback<OfferViewModel, Boolean> predicate) {
        // Get all items and filter them
        var allHistoryOffers = FXCollections.observableArrayList(offerHistoryTable.getItems());
        var filteredHistory = allHistoryOffers.filtered(predicate::call);
        
        // Update table with filtered items
        offerHistoryTable.getItems().setAll(filteredHistory);
    }

    private OfferStatus mapStatusFilterToEnum(String statusFilter) {
        // Get localized strings for comparison
        String pendingText = localeService.getMessage("offers.status.pending", "Pending");
        String acceptedText = localeService.getMessage("offers.status.accepted", "Accepted");
        String rejectedText = localeService.getMessage("offers.status.rejected", "Rejected");
        String withdrawnText = localeService.getMessage("offers.status.withdrawn", "Withdrawn");
        String completedText = localeService.getMessage("offers.status.completed", "Completed");
        
        if (statusFilter.equals(pendingText)) return OfferStatus.PENDING;
        if (statusFilter.equals(acceptedText)) return OfferStatus.ACCEPTED;
        if (statusFilter.equals(rejectedText)) return OfferStatus.REJECTED;
        if (statusFilter.equals(withdrawnText)) return OfferStatus.WITHDRAWN;
        if (statusFilter.equals(completedText)) return OfferStatus.COMPLETED;
        
        return null; // "All Statuses" or unknown
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterComboBox.setValue("Tutti gli stati");
        typeFilterComboBox.setValue("Tutti i tipi");
        updateFilters();
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

    private void setupActionButtons(TableColumn<OfferViewModel, Void> column, boolean isReceived) {
        column.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button(localeService.getMessage("offers.button.view", "View"));
            private final Button withdrawButton = new Button(
                    localeService.getMessage("offers.button.withdraw", "Withdraw"));
            private final Button reviewButton = new Button(
                    localeService.getMessage("offers.button.review", "Review"));

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getIndex() < 0) {
                    setGraphic(null);
                } else {
                    OfferViewModel offer = getTableView().getItems().get(getIndex());

                    if (offer.getStatus() == OfferStatus.COMPLETED) {
                        // For completed offers, show review button
                        reviewButton.setOnAction(_ -> handleCreateReview(offer));
                        setGraphic(reviewButton);
                    } else if (isReceived) {
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

    private void handleCreateReview(OfferViewModel offer) {
        navigationService.openReviewCreate(offer, (Stage) acceptOfferButton.getScene().getWindow());
    }

    private void refreshOffers() {
        offerService.getReceivedOffers()
                .thenCompose(_ -> offerService.getSentOffers())
                .thenCompose(_ -> offerService.getOfferHistory())
                .thenAccept(historyOffers -> Platform.runLater(() -> {
                    // Handle history offers manually and apply current filters
                    offerHistoryTable.getItems().clear();
                    var historyViewModels = historyOffers.stream()
                            .map(viewModelMapper::toViewModel)
                            .toList();
                    offerHistoryTable.getItems().addAll(historyViewModels);
                    
                    // Reapply filters after loading data
                    updateFilters();
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
            populateOfferedItemsList(offer.getOfferItems());
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

    private void populateOfferedItemsList(ObservableList<OfferItemViewModel> offerItems) {
        offeredItemsList.getChildren().clear();
        
        for (OfferItemViewModel item : offerItems) {
            VBox itemRow = createOfferItemRow(item);
            offeredItemsList.getChildren().add(itemRow);
        }
    }

    private VBox createOfferItemDetailsSection(OfferItemViewModel item) {
        // Check if we have any additional details to show from the referenced ItemViewModel
        ItemViewModel itemData = item.getItem();
        if (itemData == null) {
            return null; // No item data available
        }
        
        boolean hasDescription = itemData.getDescription() != null && !itemData.getDescription().trim().isEmpty();
        boolean hasBrand = itemData.getBrand() != null && !itemData.getBrand().trim().isEmpty();
        boolean hasModel = itemData.getModel() != null && !itemData.getModel().trim().isEmpty();
        boolean hasYear = itemData.getYear() > 0;
        boolean hasCategory = itemData.getItemCategory() != null && !itemData.getItemCategory().trim().isEmpty();
        boolean hasCondition = item.getCondition() != null;
        
        if (!hasDescription && !hasBrand && !hasModel && !hasYear && !hasCategory && !hasCondition) {
            return null; // No additional details to show
        }
        
        VBox detailsContainer = new VBox(5);
        detailsContainer.getStyleClass().add("offer-item-details-section");
        
        // Product information grid
        VBox productInfo = new VBox(3);
        productInfo.getStyleClass().add("offer-item-info-grid");
        
        // Brand and Model row
        if (hasBrand || hasModel) {
            HBox brandModelRow = new HBox(15);
            brandModelRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            if (hasBrand) {
                VBox brandBox = new VBox(2);
                Label brandLabel = new Label(localeService.getMessage("offers.details.brand", "Brand:"));
                brandLabel.getStyleClass().add("offer-detail-label");
                Text brandValue = new Text(itemData.getBrand());
                brandValue.getStyleClass().add("offer-detail-value");
                brandBox.getChildren().addAll(brandLabel, brandValue);
                brandModelRow.getChildren().add(brandBox);
            }
            
            if (hasModel) {
                VBox modelBox = new VBox(2);
                Label modelLabel = new Label(localeService.getMessage("offers.details.model", "Model:"));
                modelLabel.getStyleClass().add("offer-detail-label");
                Text modelValue = new Text(itemData.getModel());
                modelValue.getStyleClass().add("offer-detail-value");
                modelBox.getChildren().addAll(modelLabel, modelValue);
                brandModelRow.getChildren().add(modelBox);
            }
            
            productInfo.getChildren().add(brandModelRow);
        }
        
        // Category and Year row
        if (hasCategory || hasYear) {
            HBox categoryYearRow = new HBox(15);
            categoryYearRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            if (hasCategory) {
                VBox categoryBox = new VBox(2);
                Label categoryLabel = new Label(localeService.getMessage("offers.details.category", "Category:"));
                categoryLabel.getStyleClass().add("offer-detail-label");
                Text categoryValue = new Text(itemData.getItemCategory());
                categoryValue.getStyleClass().add("offer-detail-value");
                categoryBox.getChildren().addAll(categoryLabel, categoryValue);
                categoryYearRow.getChildren().add(categoryBox);
            }
            
            if (hasYear) {
                VBox yearBox = new VBox(2);
                Label yearLabel = new Label(localeService.getMessage("offers.details.year", "Year:"));
                yearLabel.getStyleClass().add("offer-detail-label");
                Text yearValue = new Text(String.valueOf(itemData.getYear()));
                yearValue.getStyleClass().add("offer-detail-value");
                yearBox.getChildren().addAll(yearLabel, yearValue);
                categoryYearRow.getChildren().add(yearBox);
            }
            
            productInfo.getChildren().add(categoryYearRow);
        }
        
        // Condition row
        if (hasCondition) {
            HBox conditionRow = new HBox(10);
            conditionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            VBox conditionBox = new VBox(2);
            Label conditionLabel = new Label(localeService.getMessage("offers.details.condition", "Condition:"));
            conditionLabel.getStyleClass().add("offer-detail-label");
            Text conditionValue = new Text(item.getCondition().getDisplayName());
            conditionValue.getStyleClass().add("offer-detail-value");
            conditionBox.getChildren().addAll(conditionLabel, conditionValue);
            conditionRow.getChildren().add(conditionBox);
            
            productInfo.getChildren().add(conditionRow);
        }
        
        if (!productInfo.getChildren().isEmpty()) {
            detailsContainer.getChildren().add(productInfo);
        }
        
        // Description section
        if (hasDescription) {
            VBox descriptionBox = new VBox(5);
            descriptionBox.getStyleClass().add("item-description-section");
            
            Label descLabel = new Label(localeService.getMessage("offers.details.description", "Description:"));
            descLabel.getStyleClass().add("offer-detail-label");
            
            Text descriptionText = new Text(itemData.getDescription());
            descriptionText.getStyleClass().add("item-description-text");
            descriptionText.setWrappingWidth(300); // Adjust based on your layout
            
            descriptionBox.getChildren().addAll(descLabel, descriptionText);
            detailsContainer.getChildren().add(descriptionBox);
        }
        
        return detailsContainer;
    }

    // Update the createOfferItemRow method to use condition CSS class
    private VBox createOfferItemRow(OfferItemViewModel item) {
        VBox itemContainer = new VBox(8);
        itemContainer.getStyleClass().add("offer-item-row");
        
        // Main item header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("offer-item-header-row");
        
        // Item image thumbnail
        ImageView itemImage = new ImageView();
        itemImage.setFitHeight(50);
        itemImage.setFitWidth(50);
        itemImage.setPreserveRatio(true);
        itemImage.getStyleClass().add("offer-item-thumbnail");
        
        // Load item image - prioritize item's image from ItemViewModel
        String imagePath = null;
        if (item.getItem() != null && item.getItem().getImagePath() != null && !item.getItem().getImagePath().isEmpty()) {
            imagePath = item.getItem().getImagePath();
        } else if (item.getItemImagePath() != null && !item.getItemImagePath().isEmpty()) {
            imagePath = item.getItemImagePath();
        }
        
        if (imagePath != null && !imagePath.isEmpty()) {
            imageService.fetchImage(imagePath)
                    .thenAccept(image -> Platform.runLater(() -> {
                        if (image != null && !image.isError()) {
                            itemImage.setImage(image);
                        } else {
                            setDefaultOfferItemImage(itemImage);
                        }
                    }));
        } else {
            setDefaultOfferItemImage(itemImage);
        }
        
        // Main item info
        VBox itemMainInfo = new VBox(3);
        itemMainInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(itemMainInfo, javafx.scene.layout.Priority.ALWAYS);
        
        // Item name and quantity row
        HBox nameQuantityRow = new HBox(10);
        nameQuantityRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Text itemName = new Text(item.getItemName());
        itemName.getStyleClass().add("offer-item-name");
        
        // Quantity badge
        Label quantityBadge = new Label("x" + item.getQuantity());
        quantityBadge.getStyleClass().add("offer-quantity-badge");
        
        nameQuantityRow.getChildren().addAll(itemName, quantityBadge);
        
        // Category and condition row
        HBox categoryConditionRow = new HBox(10);
        categoryConditionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Add category if available from ItemViewModel
        if (item.getItem() != null && item.getItem().getItemCategory() != null && !item.getItem().getItemCategory().trim().isEmpty()) {
            Label categoryLabel = new Label(item.getItem().getItemCategory());
            categoryLabel.getStyleClass().add("offer-item-category");
            categoryConditionRow.getChildren().add(categoryLabel);
        }
        
        // Add condition with proper CSS class
        if (item.getCondition() != null) {
            Label conditionLabel = new Label(item.getCondition().getDisplayName());
            conditionLabel.getStyleClass().addAll("offer-item-condition", getConditionCssClass(item.getCondition()));
            categoryConditionRow.getChildren().add(conditionLabel);
        }
        
        itemMainInfo.getChildren().addAll(nameQuantityRow);
        if (!categoryConditionRow.getChildren().isEmpty()) {
            itemMainInfo.getChildren().add(categoryConditionRow);
        }
        
        headerRow.getChildren().addAll(itemImage, itemMainInfo);
        itemContainer.getChildren().add(headerRow);
        
        // Additional details section (expandable)
        VBox detailsSection = createOfferItemDetailsSection(item);
        if (detailsSection != null) {
            // Make details initially hidden
            detailsSection.setVisible(false);
            detailsSection.setManaged(false);
            
            // Add expand/collapse functionality
            Button expandButton = new Button("▼");
            expandButton.getStyleClass().add("offer-expand-button");
            expandButton.setOnAction(e -> {
                boolean isExpanded = detailsSection.isVisible();
                detailsSection.setVisible(!isExpanded);
                detailsSection.setManaged(!isExpanded);
                expandButton.setText(isExpanded ? "▼" : "▲");
            });
            
            // Add expand button to main row
            headerRow.getChildren().add(expandButton);
            
            itemContainer.getChildren().add(detailsSection);
        }
        
        return itemContainer;
    }

    // Add helper method for condition CSS classes
    private String getConditionCssClass(com.uninaswap.common.enums.ItemCondition condition) {
        if (condition == null) return "";
        
        return switch (condition) {
            case NEW -> "new";
            case LIKE_NEW -> "like-new";
            case VERY_GOOD -> "very-good";
            case GOOD -> "good";
            case ACCEPTABLE -> "acceptable";
            case FOR_PARTS -> "for-parts";
        };
    }

    private void setDefaultOfferItemImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default offer item image: " + e.getMessage());
        }
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
        
        // Clear offered items list instead of table
        if (offeredItemsList != null) {
            offeredItemsList.getChildren().clear();
        }
        
        clearOfferDetails();
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

    // Update the refreshFilters method
    public void refreshFilters() {
        String currentStatusValue = statusFilterComboBox.getValue();
        String currentTypeValue = typeFilterComboBox.getValue();
        
        // Refresh items
        setupFilters();
        
        // Try to maintain selection if possible, otherwise default to "All"
        statusFilterComboBox.setValue(localeService.getMessage("offers.filter.all.statuses", "All Statuses"));
        typeFilterComboBox.setValue(localeService.getMessage("offers.filter.all.types", "All Types"));
    }
}
