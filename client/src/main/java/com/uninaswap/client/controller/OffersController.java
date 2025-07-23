package com.uninaswap.client.controller;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.OfferService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.OfferStatus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 
 */
public class OffersController {
    /**
     * 
     */
    @FXML
    private TextField searchField;
    /**
     * 
     */
    @FXML
    private ComboBox<String> statusFilterComboBox;
    /**
     * 
     */
    @FXML
    private ComboBox<String> typeFilterComboBox;
    /**
     * 
     */
    @FXML
    private Button clearFiltersButton;
    /**
     * 
     */
    @FXML
    private TabPane offersTabPane;
    /**
     * 
     */
    @FXML
    private Tab receivedTab;
    /**
     * 
     */
    @FXML
    private Tab sentTab;
    /**
     * 
     */
    @FXML
    private Tab historyTab;
    /**
     * 
     */
    @FXML
    private SplitPane receivedOffersContent;
    /**
     * 
     */
    @FXML
    private OffersTabContentController receivedOffersContentController;
    /**
     * 
     */
    @FXML
    private SplitPane sentOffersContent;
    /**
     * 
     */
    @FXML
    private OffersTabContentController sentOffersContentController;
    /**
     * 
     */
    @FXML
    private SplitPane historyOffersContent;
    /**
     * 
     */
    @FXML
    private OffersTabContentController historyOffersContentController;
    /**
     * 
     */
    @FXML
    private Button refreshButton;
    /**
     * 
     */
    @FXML
    private Button acceptOfferButton;
    /**
     * 
     */
    @FXML
    private Button rejectOfferButton;
    /**
     * 
     */
    @FXML
    private Button counterOfferButton;

    /**
     * 
     */
    private final OfferService offerService = OfferService.getInstance();
    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final EventBusService eventBus = EventBusService.getInstance();
    /**
     * 
     */
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();
    /**
     * 
     */
    private OfferViewModel selectedOffer;

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupFilters();
        setupTabControllers();
        setupTabChangeHandlers();
        eventBus.subscribe(EventTypes.OFFER_UPDATED, _ -> refreshOffers());
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            Platform.runLater(() -> {
                clearAllTabs();
                System.out.println("OffersController: Cleared view on logout");
            });
        });
        refreshOffers();
    }

    /**
     * 
     */
    private void setupFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("offers.filter.all.statuses", "All Statuses"),
                localeService.getMessage("offers.status.pending", "Pending"),
                localeService.getMessage("offers.status.accepted", "Accepted"),
                localeService.getMessage("offers.status.confirmed", "Confirmed"),
                localeService.getMessage("offers.status.sellerverified", "Seller Verified"),
                localeService.getMessage("offers.status.buyerverified", "Buyer Verified"),
                localeService.getMessage("offers.status.pickupscheduling", "Pickup Scheduling"),
                localeService.getMessage("offers.status.pickuprescheduling", "Pickup Rescheduling"),
                localeService.getMessage("offers.status.cancelled", "Cancelled"),
                localeService.getMessage("offers.status.rejected", "Rejected"),
                localeService.getMessage("offers.status.withdrawn", "Withdrawn"),
                localeService.getMessage("offers.status.completed", "Completed")
        ));
        statusFilterComboBox.setValue(localeService.getMessage("offers.filter.all.statuses", "All Statuses"));
        typeFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("offers.filter.all.types", "All Types"),
                localeService.getMessage("offers.type.money", "Money"),
                localeService.getMessage("offers.type.trade", "Trade"),
                localeService.getMessage("offers.type.mixed", "Mixed")
        ));
        typeFilterComboBox.setValue(localeService.getMessage("offers.filter.all.types", "All Types"));
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        typeFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        searchField.textProperty().addListener((_, _, _) -> updateFilters());
    }

    /**
     * @param offer
     */
    private void showOfferDetails(OfferViewModel offer) {
    selectedOffer = offer;
    OffersTabContentController currentController = getCurrentTabController();
        if (currentController != null) {
            currentController.showOfferDetails(offer);
        }
    }

    /**
     * @param actionsColumn
     */
    private void setupReceivedActionButtons(TableColumn<OfferViewModel, Void> actionsColumn) {
        actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button(localeService.getMessage("offers.button.view", "View"));
            private final Button acceptButton = new Button(localeService.getMessage("offers.button.accept", "Accept"));
            private final Button rejectButton = new Button(localeService.getMessage("offers.button.reject", "Reject"));
            private final HBox actionBox = new HBox(5, viewButton, acceptButton, rejectButton);

            {
                viewButton.getStyleClass().add("primary-button");
                acceptButton.getStyleClass().add("primary-button");
                rejectButton.getStyleClass().add("danger-button");

                viewButton.setOnAction(e -> {
                    OfferViewModel offer = getTableView().getItems().get(getIndex());
                    showOfferDetails(offer);
                });

                acceptButton.setOnAction(e -> {
                    selectedOffer = getTableView().getItems().get(getIndex());
                    handleAcceptOffer();
                });

                rejectButton.setOnAction(e -> {
                    selectedOffer = getTableView().getItems().get(getIndex());
                    handleRejectOffer();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OfferViewModel offer = getTableView().getItems().get(getIndex());
                    
                    boolean canAcceptReject = offer.getStatus() == OfferStatus.PENDING;
                    acceptButton.setVisible(canAcceptReject);
                    acceptButton.setManaged(canAcceptReject);
                    rejectButton.setVisible(canAcceptReject);
                    rejectButton.setManaged(canAcceptReject);
                    
                    setGraphic(actionBox);
                }
            }
        });
    }

    /**
     * @param actionsColumn
     */
    private void setupSentActionButtons(TableColumn<OfferViewModel, Void> actionsColumn) {
        actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button(localeService.getMessage("offers.button.view", "View"));
            private final Button withdrawButton = new Button(localeService.getMessage("offers.button.withdraw", "Withdraw"));
            private final HBox actionBox = new HBox(5, viewButton, withdrawButton);

            {
                viewButton.getStyleClass().add("primary-button");
                withdrawButton.getStyleClass().add("danger-button");

                viewButton.setOnAction(e -> {
                    OfferViewModel offer = getTableView().getItems().get(getIndex());
                    showOfferDetails(offer);
                });

                withdrawButton.setOnAction(e -> {
                    OfferViewModel offer = getTableView().getItems().get(getIndex());
                    handleWithdrawOffer(offer);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    OfferViewModel offer = getTableView().getItems().get(getIndex());
                    boolean canWithdraw = offer.getStatus() == OfferStatus.PENDING;
                    withdrawButton.setVisible(canWithdraw);
                    withdrawButton.setManaged(canWithdraw);
                    
                    setGraphic(actionBox);
                }
            }
        });
    }

    /**
     * 
     */
    private void setupTabControllers() {
        if (receivedOffersContentController != null) {
            receivedOffersContentController.initialize(
                offerService.getReceivedOffersList(), 
                "received",
                this::showOfferDetails,
                this::setupReceivedActionButtons,
                this::handleAcceptOffer,
                this::handleRejectOffer,
                this::handleCounterOffer,
                this::handleConfirmTransaction,
                this::handleCancelTransaction,
                this::handleWriteReview
            );
        }
        if (sentOffersContentController != null) {
            sentOffersContentController.initialize(
                offerService.getUserOffersList(), 
                "sent",
                this::showOfferDetails,
                this::setupSentActionButtons,
                this::handleAcceptOffer,
                this::handleRejectOffer,
                this::handleCounterOffer,
                this::handleConfirmTransaction,
                this::handleCancelTransaction,
                this::handleWriteReview
            );
        }
        if (historyOffersContentController != null) {
            historyOffersContentController.initialize(
                FXCollections.observableArrayList(), 
                "history",
                this::showOfferDetails,
                null,
                null, null, null, null, null, null // No action handlers for history
            );
        }
    }

    /**
     * 
     */
    private void setupTabChangeHandlers() {
        offersTabPane.getSelectionModel().selectedItemProperty().addListener((_, oldTab, newTab) -> {
            clearOfferDetails();
            updateFilters();
        });
    }

    /**
     * 
     */
    private void updateFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = statusFilterComboBox.getValue();
        String typeFilter = typeFilterComboBox.getValue();

        Predicate<OfferViewModel> predicate = offer -> {
            if (!searchText.isEmpty()) {
                boolean matchesSearch = 
                    (offer.getListingTitle() != null && offer.getListingTitle().toLowerCase().contains(searchText)) ||
                    (offer.getOfferingUserUsername() != null && offer.getOfferingUserUsername().toLowerCase().contains(searchText)) ||
                    (offer.getMessage() != null && offer.getMessage().toLowerCase().contains(searchText));
                if (!matchesSearch) return false;
            }

            if (!localeService.getMessage("offers.filter.all.statuses", "All Statuses").equals(statusFilter)) {
                OfferStatus selectedStatus = mapStatusFilterToEnum(statusFilter);
                if (selectedStatus != null && offer.getStatus() != selectedStatus) {
                    return false;
                }
            }

            if (!localeService.getMessage("offers.filter.all.types", "All Types").equals(typeFilter)) {
                String offerType = getOfferTypeDisplayName(offer);
                if (!typeFilter.equals(offerType)) {
                    return false;
                }
            }

            return true;
        };

        if (receivedOffersContentController != null) {
            receivedOffersContentController.applyFilter(predicate);
        }
        if (sentOffersContentController != null) {
            sentOffersContentController.applyFilter(predicate);
        }
        if (historyOffersContentController != null) {
            historyOffersContentController.applyFilter(predicate);
        }
    }

    /**
     * 
     */
    private void clearOfferDetails() {
        selectedOffer = null;
        OffersTabContentController currentController = getCurrentTabController();
        if (currentController != null) {
            currentController.clearDetails();
        }
    }

    /**
     * @return
     */
    private OffersTabContentController getCurrentTabController() {
        Tab selectedTab = offersTabPane.getSelectionModel().getSelectedItem();
        
        if (selectedTab == receivedTab) {
            return receivedOffersContentController;
        } else if (selectedTab == sentTab) {
            return sentOffersContentController;
        } else if (selectedTab == historyTab) {
            return historyOffersContentController;
        }
        
        return null;
    }

    /**
     * 
     */
    private void clearAllTabs() {
        if (receivedOffersContentController != null) {
            receivedOffersContentController.clearDetails();
        }
        if (sentOffersContentController != null) {
            sentOffersContentController.clearDetails();
        }
        if (historyOffersContentController != null) {
            historyOffersContentController.clearDetails();
        }
    }

    /**
     * 
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        statusFilterComboBox.setValue("Tutti gli stati");
        typeFilterComboBox.setValue("Tutti i tipi");
        updateFilters();
    }

    /**
     * 
     */
    @FXML
    private void handleRefreshOffers() {
        refreshOffers();
    }

    /**
     * 
     */
    @FXML
    private void handleAcceptOffer() {
        handleAcceptOffer(selectedOffer);
    }

    /**
     * @param offer
     */
    private void handleAcceptOffer(OfferViewModel offer) {
        if (offer == null) return;
        
        selectedOffer = offer;
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("offers.accept.title", "Accept Offer"),
                localeService.getMessage("offers.accept.header", "Accept Offer"),
                localeService.getMessage("offers.accept.content", "Are you sure you want to accept this offer?"));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            offerService.acceptOffer(offer.getId())
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("offers.accept.success.title", "Offer Accepted"),
                                    localeService.getMessage("offers.accept.success.header", "Success"),
                                    localeService.getMessage("offers.accept.success.message",
                                            "Offer accepted successfully"));

                            if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                                Alert pickupDialog = AlertHelper.createConfirmationDialog(
                                        localeService.getMessage("pickup.schedule.title", "Schedule Pickup"),
                                        localeService.getMessage("pickup.schedule.header",
                                                "Would you like to schedule a pickup now?"),
                                        localeService.getMessage("pickup.schedule.content",
                                                "You can schedule pickup times now, or do it later. The offer status will be 'Pickup Scheduling' until pickup is arranged."));

                                pickupDialog.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        handleSchedulePickup(offer);
                                    }
                                });
                            } else {
                                AlertHelper.showInformationAlert(
                                        localeService.getMessage("offers.confirmed.title", "Offer Confirmed"),
                                        localeService.getMessage("offers.confirmed.header", "Ready for Shipping"),
                                        localeService.getMessage("offers.confirmed.message",
                                                "The offer has been confirmed and is ready for shipping arrangement."));
                            }

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

    /**
     * 
     */
    @FXML
    private void handleRejectOffer() {
        handleRejectOffer(selectedOffer);
    }

    /**
     * @param offer
     */
    private void handleRejectOffer(OfferViewModel offer) {
        if (offer == null) return;
        
        selectedOffer = offer;

        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("offers.reject.title", "Reject Offer"),
                localeService.getMessage("offers.reject.header", "Reject Offer"),
                localeService.getMessage("offers.reject.content", "Are you sure you want to reject this offer?"));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            offerService.rejectOffer(offer.getId())
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

    /**
     * 
     */
    @FXML
    private void handleCounterOffer() {
        handleCounterOffer(selectedOffer);
    }

    /**
     * @param offer
     */
    private void handleCounterOffer(OfferViewModel offer) {
        if (offer == null) return;
        
        selectedOffer = offer;
        AlertHelper.showInformationAlert(
                localeService.getMessage("offers.counter.title", "Counter Offer"),
                localeService.getMessage("offers.counter.header", "Feature Coming Soon"),
                localeService.getMessage("offers.counter.message",
                        "Counter offer functionality will be available soon"));
    }

    /**
     * @param offer
     */
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

    /**
     * @param offer
     */
    private void handleSchedulePickup(OfferViewModel offer) {
        Stage stage = (Stage) offersTabPane.getScene().getWindow();
        navigationService.openPickupScheduling(offer, stage);
    }

    /**
     * @param offer
     */
    private void handleCreateReview(OfferViewModel offer) {
        Stage stage = (Stage) offersTabPane.getScene().getWindow();
        navigationService.openReviewCreate(offer, stage);
    }

    /**
     * 
     */
    private void refreshOffers() {
        offerService.getReceivedOffers()
            .thenCompose(_ -> offerService.getSentOffers())
            .thenCompose(_ -> offerService.getOfferHistory())
            .thenAccept(historyOffers -> Platform.runLater(() -> {
                if (historyOffersContentController != null) {
                    var historyViewModels = historyOffers.stream()
                            .map(viewModelMapper::toViewModel)
                            .collect(Collectors.toList());
                    historyOffersContentController.updateOffersList(historyViewModels);
                }
                updateFilters();
            }))
            .exceptionally(ex -> {
                System.err.println("Error loading offers: " + ex.getMessage());
                return null;
            });
    }

    /**
     * @param statusFilter
     * @return
     */
    private OfferStatus mapStatusFilterToEnum(String statusFilter) {
        String pendingText = localeService.getMessage("offers.status.pending", "Pending");
        String acceptedText = localeService.getMessage("offers.status.accepted", "Accepted");
        String rejectedText = localeService.getMessage("offers.status.rejected", "Rejected");
        String withdrawnText = localeService.getMessage("offers.status.withdrawn", "Withdrawn");
        String completedText = localeService.getMessage("offers.status.completed", "Completed");
        String confirmedText = localeService.getMessage("offers.status.confirmed", "Confirmed");
        String cancelledText = localeService.getMessage("offers.status.cancelled", "Cancelled");
        String pickupSchedulingText = localeService.getMessage("offers.status.pickupscheduling", "Pickup Scheduling");
        String pickupReschedulingText = localeService.getMessage("offers.status.pickuprescheduling", "Pickup Rescheduling");
        String sellerVerifiedText = localeService.getMessage("offers.status.sellerverified", "Seller Verified");
        String buyerVerifiedText = localeService.getMessage("offers.status.buyerverified", "Buyer Verified");
        
        if (statusFilter.equals(pendingText)) return OfferStatus.PENDING;
        if (statusFilter.equals(acceptedText)) return OfferStatus.ACCEPTED;
        if (statusFilter.equals(rejectedText)) return OfferStatus.REJECTED;
        if (statusFilter.equals(withdrawnText)) return OfferStatus.WITHDRAWN;
        if (statusFilter.equals(completedText)) return OfferStatus.COMPLETED;
        if (statusFilter.equals(confirmedText)) return OfferStatus.CONFIRMED;
        if (statusFilter.equals(cancelledText)) return OfferStatus.CANCELLED;
        if (statusFilter.equals(pickupSchedulingText)) return OfferStatus.PICKUPSCHEDULING;
        if (statusFilter.equals(pickupReschedulingText)) return OfferStatus.PICKUPRESCHEDULING;
        if (statusFilter.equals(sellerVerifiedText)) return OfferStatus.SELLERVERIFIED;
        if (statusFilter.equals(buyerVerifiedText)) return OfferStatus.BUYERVERIFIED;
        
        return null;
    }

    /**
     * @param offer
     * @return
     */
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

    /**
     * @param offer
     * @return
     */
    private String formatOfferAmount(OfferViewModel offer) {
        if (offer.getAmount() != null && offer.getAmount().compareTo(BigDecimal.valueOf(0)) > 0) {
            String currency = offer.getCurrency() != null ? offer.getCurrency().getSymbol() : "€";
            return String.format("%s %.2f", currency, offer.getAmount());
        }
        return "-";
    }

    /**
     * @param offer
     * @return
     */
    private String formatDate(OfferViewModel offer) {
        if (offer.getCreatedAt() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return offer.getCreatedAt().format(formatter);
        }
        return "-";
    }

    /**
     * 
     */
    public void refreshFilters() {
        String currentStatusValue = statusFilterComboBox.getValue();
        String currentTypeValue = typeFilterComboBox.getValue();
        setupFilters();
        statusFilterComboBox.setValue(localeService.getMessage("offers.filter.all.statuses", "All Statuses"));
        typeFilterComboBox.setValue(localeService.getMessage("offers.filter.all.types", "All Types"));
    }

    /**
     * 
     */
    @FXML
    private void handleCloseDetails() {
        clearOfferDetails();
    }

    /**
     * @param offer
     */
    private void handleConfirmTransaction(OfferViewModel offer) {
        if (offer == null) return;
        
        selectedOffer = offer;
        
        offerService.confirmTransaction(offer.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        AlertHelper.showInformationAlert(
                                localeService.getMessage("transaction.confirm.success.title", "Transaction Confirmed"),
                                localeService.getMessage("transaction.confirm.success.header", "Success"),
                                localeService.getMessage("transaction.confirm.success.message",
                                        "Transaction has been confirmed successfully."));
                        refreshOffers();
                    } else {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("offers.error.title", "Error"),
                                localeService.getMessage("offers.error.header", "Failed to confirm transaction"),
                                localeService.getMessage("offers.error.message",
                                        "Could not confirm the transaction"));
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

    /**
     * @param offer
     */
    private void handleCancelTransaction(OfferViewModel offer) {
        if (offer == null) return;
        
        selectedOffer = offer;
        
        offerService.cancelTransaction(offer.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        AlertHelper.showInformationAlert(
                                localeService.getMessage("transaction.cancel.success.title", "Transaction Cancelled"),
                                localeService.getMessage("transaction.cancel.success.header", "Success"),
                                localeService.getMessage("transaction.cancel.success.message",
                                        "Transaction has been cancelled."));
                        refreshOffers();
                    } else {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("offers.error.title", "Error"),
                                localeService.getMessage("offers.error.header", "Failed to cancel transaction"),
                                localeService.getMessage("offers.error.message",
                                        "Could not cancel the transaction"));
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

    /**
     * @param offer
     */
    private void handleWriteReview(OfferViewModel offer) {
        if (offer == null) return;
        
        selectedOffer = offer;
        if (!canOfferBeReviewed(offer)) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("review.error.title", "Cannot Review"),
                    localeService.getMessage("review.error.header", "Review Not Available"),
                    localeService.getMessage("review.error.message",
                            "This offer cannot be reviewed. Reviews are only available for completed non-gift transactions."));
            return;
        }
        Stage stage = (Stage) offersTabPane.getScene().getWindow();
        navigationService.openReviewCreate(offer, stage);
    }

    /**
     * @param offer
     * @return
     */
    private boolean canOfferBeReviewed(OfferViewModel offer) {
        if (offer == null || offer.getListing() == null) {
            return false;
        }
        String listingType = offer.getListing().getListingTypeValue();
        if ("GIFT".equalsIgnoreCase(listingType)) {
            return false;
        }
        return offer.getStatus() == OfferStatus.COMPLETED;
    }
}
