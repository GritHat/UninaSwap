package com.uninaswap.client.controller;

import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.OfferItemViewModel;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.OfferStatus;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 
 */
public class OffersTabContentController {
    /**
     * 
     */
    @FXML
    private SplitPane mainSplitPane;
    /**
     * 
     */
    @FXML
    private TableView<OfferViewModel> offersTable;
    /**
     * 
     */
    @FXML
    private TableColumn<OfferViewModel, String> listingTitleColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<OfferViewModel, String> fromToColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<OfferViewModel, String> typeColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<OfferViewModel, String> amountColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<OfferViewModel, String> statusColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<OfferViewModel, String> dateColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<OfferViewModel, Void> actionsColumn;
    /**
     * 
     */
    @FXML
    private VBox offerDetailsPanel;
    /**
     * 
     */
    @FXML
    private VBox detailsHeader;
    /**
     * 
     */
    @FXML
    private ScrollPane detailsScrollPane;
    /**
     * 
     */
    @FXML
    private VBox detailsContent;
    /**
     * 
     */
    @FXML
    private VBox emptyDetailsState;
    /**
     * 
     */
    @FXML
    private VBox offerSummaryCard;
    /**
     * 
     */
    @FXML
    private Label offerFromLabel;
    /**
     * 
     */
    @FXML
    private Label offerListingLabel;
    /**
     * 
     */
    @FXML
    private Label offerTypeLabel;
    /**
     * 
     */
    @FXML
    private Label offerAmountLabel;
    /**
     * 
     */
    @FXML
    private VBox messageSection;
    /**
     * 
     */
    @FXML
    private TextArea offerMessageArea;
    /**
     * 
     */
    @FXML
    private VBox offeredItemsSection;
    /**
     * 
     */
    @FXML
    private VBox offeredItemsList;
    /**
     * 
     */
    @FXML
    private VBox actionButtonsSection;
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
    @FXML
    private Button closeDetailsButton;
    /**
     * 
     */
    @FXML
    private Button schedulePickupButton;
    /**
     * 
     */
    @FXML
    private Button selectPickupTimeButton;
    /**
     * 
     */
    @FXML
    private Button reschedulePickupButton;
    /**
     * 
     */
    @FXML
    private Button confirmTransactionButton;
    /**
     * 
     */
    @FXML
    private Button cancelTransactionButton;
    /**
     * 
     */
    @FXML
    private Button writeReviewButton;
    
    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final ImageService imageService = ImageService.getInstance();
    
    /**
     * 
     */
    private FilteredList<OfferViewModel> filteredOffers;
    /**
     * 
     */
    private String tabType;
    /**
     * 
     */
    private Consumer<OfferViewModel> onOfferSelected;
    /**
     * 
     */
    private Consumer<TableColumn<OfferViewModel, Void>> actionButtonsSetup;
    /**
     * 
     */
    private OfferViewModel currentOffer;
    /**
     * 
     */
    private Consumer<OfferViewModel> onAcceptOffer;
    /**
     * 
     */
    private Consumer<OfferViewModel> onRejectOffer;
    /**
     * 
     */
    private Consumer<OfferViewModel> onCounterOffer;
    /**
     * 
     */
    private Consumer<OfferViewModel> onConfirmTransaction;
    /**
     * 
     */
    private Consumer<OfferViewModel> onCancelTransaction;
    /**
     * 
     */
    private Consumer<OfferViewModel> onWriteReview;
    
    /**
     * 
     */
    private ObservableList<OfferViewModel> sourceOffersList;

    /**
     * @param offersList
     * @param tabType
     * @param onOfferSelected
     * @param actionButtonsSetup
     * @param onAcceptOffer
     * @param onRejectOffer
     * @param onCounterOffer
     * @param onConfirmTransaction
     * @param onCancelTransaction
     * @param onWriteReview
     */
    public void initialize(ObservableList<OfferViewModel> offersList, String tabType, 
                          Consumer<OfferViewModel> onOfferSelected, 
                          Consumer<TableColumn<OfferViewModel, Void>> actionButtonsSetup,
                          Consumer<OfferViewModel> onAcceptOffer,
                          Consumer<OfferViewModel> onRejectOffer,
                          Consumer<OfferViewModel> onCounterOffer,
                          Consumer<OfferViewModel> onConfirmTransaction,
                          Consumer<OfferViewModel> onCancelTransaction,
                          Consumer<OfferViewModel> onWriteReview) {
        
        this.tabType = tabType;
        this.onOfferSelected = onOfferSelected;
        this.actionButtonsSetup = actionButtonsSetup;
        this.onAcceptOffer = onAcceptOffer;
        this.onRejectOffer = onRejectOffer;
        this.onCounterOffer = onCounterOffer;
        this.onConfirmTransaction = onConfirmTransaction;
        this.onCancelTransaction = onCancelTransaction;
        this.onWriteReview = onWriteReview;
        this.sourceOffersList = offersList;
        
        filteredOffers = new FilteredList<>(offersList);
        offersTable.setItems(filteredOffers);
        
        setupTable();
        setupDetailsPanel();
        setupResponsiveLayout();
        showEmptyState();
    }

    /**
     * 
     */
    private void setupTable() {
        listingTitleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getListingTitle()));
        
        if ("received".equals(tabType)) {
            fromToColumn.setText(localeService.getMessage("offers.column.from", "From"));
            fromToColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getOfferingUserUsername()));
        } else {
            fromToColumn.setText(localeService.getMessage("offers.column.to", "To"));
            fromToColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getListingOwnerUsername()));
        }
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(getOfferTypeDisplayName(cellData.getValue())));
        
        amountColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatOfferAmount(cellData.getValue())));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(localeService.getMessage(cellData.getValue().getStatus().getDisplayName())));
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatDate(cellData.getValue())));
        if (actionButtonsSetup != null && !"history".equals(tabType)) {
            actionButtonsSetup.accept(actionsColumn);
        } else {
            actionsColumn.setVisible(false);
            if (actionsColumn.getTableView() != null) {
                Platform.runLater(() -> {
                    if (actionsColumn.getTableView() != null) {
                        actionsColumn.getTableView().getColumns().remove(actionsColumn);
                    }
                });
            }
        }
        offersTable.getSelectionModel().selectedItemProperty().addListener((_, _, newOffer) -> {
            if (newOffer != null) {
                showOfferDetails(newOffer);
                if (onOfferSelected != null) {
                    onOfferSelected.accept(newOffer);
                }
            }
        });
        offersTable.setRowFactory(_ -> {
            TableRow<OfferViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    offersTable.getSelectionModel().select(row.getItem());
                }
            });
            return row;
        });
    }
    
    /**
     * 
     */
    private void setupDetailsPanel() {
        closeDetailsButton.setOnAction(_ -> clearDetails());
        actionButtonsSection.setVisible(true);
        actionButtonsSection.setManaged(true);
        if ("history".equals(tabType)) {
            actionButtonsSection.setVisible(false);
            actionButtonsSection.setManaged(false);
        }
    }
    
    /**
     * 
     */
    private void setupResponsiveLayout() {
        Platform.runLater(() -> {
            mainSplitPane.setDividerPositions(0.6);
        });
        mainSplitPane.widthProperty().addListener((_, _, newWidth) -> {
            double width = newWidth.doubleValue();
            
            if (width < 800) {
                if (currentOffer != null) {
                    mainSplitPane.setDividerPositions(0.35);
                } else {
                    mainSplitPane.setDividerPositions(1.0);
                }
            } else if (width < 1200) {
                mainSplitPane.setDividerPositions(0.5);
            } else {
                mainSplitPane.setDividerPositions(0.6);
            }
        });
    }
    
    /**
     * @param offer
     */
    public void showOfferDetails(OfferViewModel offer) {
        this.currentOffer = offer;
        emptyDetailsState.setVisible(false);
        emptyDetailsState.setManaged(false);
        detailsContent.setVisible(true);
        detailsContent.setManaged(true);
        offerFromLabel.setText(offer.getOfferingUserUsername());
        offerListingLabel.setText(offer.getListingTitle());
        offerTypeLabel.setText(getOfferTypeDisplayName(offer));
        offerAmountLabel.setText(formatOfferAmount(offer));
        String message = offer.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            offerMessageArea.setText(message);
            messageSection.setVisible(true);
            messageSection.setManaged(true);
        } else {
            messageSection.setVisible(false);
            messageSection.setManaged(false);
        }
        if (offer.getOfferItems() != null && !offer.getOfferItems().isEmpty()) {
            populateOfferedItemsList(offer.getOfferItems());
            offeredItemsSection.setVisible(true);
            offeredItemsSection.setManaged(true);
        } else {
            offeredItemsSection.setVisible(false);
            offeredItemsSection.setManaged(false);
        }
        updateActionButtons(offer);
        Platform.runLater(() -> {
            detailsScrollPane.setVvalue(0.0);
        });
        adjustSplitPaneForDetails();
    }
    
    /**
     * @param offerItems
     */
    private void populateOfferedItemsList(ObservableList<OfferItemViewModel> offerItems) {
        offeredItemsList.getChildren().clear();
        
        for (OfferItemViewModel item : offerItems) {
            VBox itemRow = createOfferItemRow(item);
            offeredItemsList.getChildren().add(itemRow);
        }
    }
    
    /**
     * @param item
     * @return
     */
    private VBox createOfferItemRow(OfferItemViewModel item) {
        VBox itemContainer = new VBox(8);
        itemContainer.getStyleClass().add("offer-item-row");
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("offer-item-header-row");
        ImageView itemImage = new ImageView();
        itemImage.setFitHeight(40);
        itemImage.setFitWidth(40);
        itemImage.setPreserveRatio(true);
        itemImage.getStyleClass().add("offer-item-thumbnail");
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
        VBox itemMainInfo = new VBox(3);
        itemMainInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(itemMainInfo, javafx.scene.layout.Priority.ALWAYS);
        HBox nameQuantityRow = new HBox(8);
        nameQuantityRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Text itemName = new Text(item.getItemName());
        itemName.getStyleClass().add("offer-item-name");
        Label quantityBadge = new Label("x" + item.getQuantity());
        quantityBadge.getStyleClass().add("offer-quantity-badge");
        nameQuantityRow.getChildren().addAll(itemName, quantityBadge);
        itemMainInfo.getChildren().add(nameQuantityRow);
        if (item.getCondition() != null) {
            Label conditionLabel = new Label(item.getCondition().getDisplayName());
            conditionLabel.getStyleClass().add("offer-item-condition");
            itemMainInfo.getChildren().add(conditionLabel);
        }
        
        headerRow.getChildren().addAll(itemImage, itemMainInfo);
        itemContainer.getChildren().add(headerRow);
        
        return itemContainer;
    }
    
    /**
     * @param imageView
     */
    private void setDefaultOfferItemImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default offer item image: " + e.getMessage());
        }
    }
    
    /**
     * @param offer
     */
    private void updateActionButtons(OfferViewModel offer) {
        
        if ("received".equals(tabType)) {
            updateReceivedOfferButtons(offer);
        } else if ("sent".equals(tabType)) {
            updateSentOfferButtons(offer);
        } else if ("history".equals(tabType)) {
            hideAllActionButtons();
        } else {
            hideAllActionButtons();
        }
    }
    
    /**
     * @param offer
     */
    private void updateReceivedOfferButtons(OfferViewModel offer) {
        Long currentUserId = UserSessionService.getInstance().getUser().getId();
        boolean isListingOwner = offer.getListing() != null && 
            offer.getListing().getUser() != null && 
            offer.getListing().getUser().getId().equals(currentUserId);
        hideAllActionButtons();
        
        System.out.println("=== RECEIVED OFFER BUTTONS DEBUG ===");
        System.out.println("Offer ID: " + offer.getId());
        System.out.println("Offer Status: " + offer.getStatus());
        System.out.println("Delivery Type: " + offer.getDeliveryType());
        System.out.println("Is Listing Owner: " + isListingOwner);
        System.out.println("Tab Type: " + tabType);
        System.out.println("=====================================");
        
        switch (offer.getStatus()) {
            case PENDING:
                if (isListingOwner) {
                    showActionButtons(true, true, true, false, false, false, false, false, false);
                    System.out.println("PENDING - showing accept/reject/counter buttons for listing owner");
                }
                break;
                
            case ACCEPTED:
                if (isListingOwner && offer.getDeliveryType() == DeliveryType.PICKUP) {
                    showActionButtons(false, false, false, true, false, false, false, false, false);
                    System.out.println("ACCEPTED - showing schedule pickup button for listing owner");
                }
                break;
                
            case PICKUPSCHEDULING:
                if (!isListingOwner && offer.getDeliveryType() == DeliveryType.PICKUP) {
                    showActionButtons(false, false, false, false, true, false, false, false, false);
                    System.out.println("PICKUPSCHEDULING - showing select time button for offer creator");
                }
                break;
                
            case PICKUPRESCHEDULING:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    if (isListingOwner) {
                        showActionButtons(false, false, false, false, true, false, false, false, false);
                        System.out.println("PICKUPRESCHEDULING - showing SELECT TIME button for listing owner");
                    } else {
                        showActionButtons(false, false, false, false, false, true, false, false, false);
                        System.out.println("PICKUPRESCHEDULING - showing RESCHEDULE button for offer creator");
                    }
                }
                break;
                
            case CONFIRMED:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println("CONFIRMED - showing transaction buttons for both parties (pickup)");
                } else if (offer.getDeliveryType() == DeliveryType.SHIPPING && !isListingOwner) {
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println("CONFIRMED - showing transaction buttons for buyer only (shipping)");
                }
                break;
                
            case SELLERVERIFIED:
                if (!isListingOwner) {
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println("SELLERVERIFIED - showing transaction buttons for buyer to complete");
                } else {
                    hideAllActionButtons();
                    System.out.println("SELLERVERIFIED - seller already verified, waiting for buyer");
                }
                break;
                
            case BUYERVERIFIED:
                if (isListingOwner) {
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println("BUYERVERIFIED - showing transaction buttons for seller to complete");
                } else {
                    hideAllActionButtons();
                    System.out.println("BUYERVERIFIED - buyer already verified, waiting for seller");
                }
                break;
                
            case COMPLETED:
                hideAllActionButtons();
                System.out.println("COMPLETED - no review option for sellers (only buyers can review sellers)");
                break;
                
            case REVIEWED:
                hideAllActionButtons();
                System.out.println("REVIEWED - review already submitted, no actions available");
                break;
                
            case CANCELLED:
            case REJECTED:
            case WITHDRAWN:
            case EXPIRED:
                hideAllActionButtons();
                System.out.println("Terminal status - hiding all buttons");
                break;
                
            default:
                hideAllActionButtons();
                System.out.println("Unknown status - hiding all buttons");
                break;
        }
    }
    
    /**
     * @param offer
     */
    private void updateSentOfferButtons(OfferViewModel offer) {
        hideAllActionButtons();
        
        System.out.println("=== SENT OFFER BUTTONS DEBUG ===");
        System.out.println("Offer ID: " + offer.getId());
        System.out.println("Offer Status: " + offer.getStatus());
        System.out.println("Delivery Type: " + offer.getDeliveryType());
        System.out.println("Tab Type: " + tabType);
        System.out.println("================================");
        
        switch (offer.getStatus()) {
            case PENDING:
                System.out.println("PENDING sent offer - no special actions");
                break;
                
            case ACCEPTED:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    System.out.println("ACCEPTED pickup offer - waiting for seller to schedule");
                }
                break;
                
            case PICKUPSCHEDULING:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    showActionButtons(false, false, false, false, true, false, false, false, false);
                    System.out.println("PICKUPSCHEDULING - showing SELECT TIME button for buyer");
                }
                break;
                
            case PICKUPRESCHEDULING:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    showActionButtons(false, false, false, false, true, true, false, false, false);
                    System.out.println("PICKUPRESCHEDULING - showing SELECT TIME and RESCHEDULE buttons for buyer");
                }
                break;
                
            case CONFIRMED:
                showActionButtons(false, false, false, false, false, false, true, true, false);
                System.out.println("CONFIRMED - showing transaction verification buttons for buyer");
                break;
                
            case SELLERVERIFIED:
                showActionButtons(false, false, false, false, false, false, true, true, false);
                System.out.println("SELLERVERIFIED - showing transaction verification buttons for buyer to complete");
                break;
                
            case BUYERVERIFIED:
                hideAllActionButtons();
                System.out.println("BUYERVERIFIED - buyer already verified, waiting for seller");
                break;
                
            case COMPLETED:
                boolean canReview = canOfferBeReviewed(offer);
                if (canReview) {
                    showActionButtons(false, false, false, false, false, false, false, false, true);
                    System.out.println("COMPLETED - showing WRITE REVIEW button for buyer to review seller");
                } else {
                    hideAllActionButtons();
                    System.out.println("COMPLETED - no review option for gift listing");
                }
                break;
                
            case REVIEWED:
                hideAllActionButtons();
                System.out.println("REVIEWED - review already submitted, no actions available");
                break;
                
            case CANCELLED:
            case REJECTED:
            case WITHDRAWN:
            case EXPIRED:
                hideAllActionButtons();
                System.out.println("Terminal status - hiding all buttons");
                break;
                
            default:
                hideAllActionButtons();
                System.out.println("Unknown status - hiding all buttons");
                break;
        }
    }
    
    /**
     * 
     */
    private void hideAllActionButtons() {
        setButtonVisibility(acceptOfferButton, false);
        setButtonVisibility(rejectOfferButton, false);
        setButtonVisibility(counterOfferButton, false);
        setButtonVisibility(schedulePickupButton, false);
        setButtonVisibility(selectPickupTimeButton, false);
        setButtonVisibility(reschedulePickupButton, false);
        setButtonVisibility(confirmTransactionButton, false);
        setButtonVisibility(cancelTransactionButton, false);
        setButtonVisibility(writeReviewButton, false);
    }
    
    /**
     * @param showAccept
     * @param showReject
     * @param showCounter
     * @param showSchedule
     * @param showSelectTime
     * @param showReschedule
     * @param showConfirmTransaction
     * @param showCancelTransaction
     * @param showWriteReview
     */
    private void showActionButtons(boolean showAccept, boolean showReject, boolean showCounter,
                                  boolean showSchedule, boolean showSelectTime, boolean showReschedule,
                                  boolean showConfirmTransaction, boolean showCancelTransaction,
                                  boolean showWriteReview) {
        setButtonVisibility(acceptOfferButton, showAccept);
        setButtonVisibility(rejectOfferButton, showReject);
        setButtonVisibility(counterOfferButton, showCounter);
        setButtonVisibility(schedulePickupButton, showSchedule);
        setButtonVisibility(selectPickupTimeButton, showSelectTime);
        setButtonVisibility(reschedulePickupButton, showReschedule);
        setButtonVisibility(confirmTransactionButton, showConfirmTransaction);
        setButtonVisibility(cancelTransactionButton, showCancelTransaction);
        setButtonVisibility(writeReviewButton, showWriteReview);
        
        System.out.println("Showing buttons - review: " + showWriteReview);
    }
    
    /**
     * @param button
     * @param visible
     */
    private void setButtonVisibility(Button button, boolean visible) {
        if (button != null) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleSchedulePickup() {
        if (currentOffer != null) {
            Stage stage = (Stage) schedulePickupButton.getScene().getWindow();
            NavigationService.getInstance().openPickupScheduling(currentOffer, stage);
        }
    }

    /**
     * 
     */
    @FXML
    private void handleSelectPickupTime() {
        if (currentOffer != null) {
            Stage stage = (Stage) selectPickupTimeButton.getScene().getWindow();
            NavigationService.getInstance().openPickupSelection(currentOffer, stage);
        }
    }

    /**
     * 
     */
    @FXML
    private void handleReschedulePickup() {
        if (currentOffer != null) {
            Stage stage = (Stage) reschedulePickupButton.getScene().getWindow();
            NavigationService.getInstance().openPickupRescheduling(currentOffer.getId(), stage);
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleCloseDetails() {
        clearDetails();
    }

    /**
     * 
     */
    @FXML
    private void handleRejectOffer() {
        if (currentOffer != null && onRejectOffer != null) {
            onRejectOffer.accept(currentOffer);
        }
    }

    /**
     * 
     */
    @FXML
    private void handleCounterOffer() {
        if (currentOffer != null && onCounterOffer != null) {
            onCounterOffer.accept(currentOffer);
        }
    }

    /**
     * 
     */
    @FXML
    private void handleAcceptOffer() {
        if (currentOffer != null && onAcceptOffer != null) {
            onAcceptOffer.accept(currentOffer);
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleConfirmTransaction() {
        System.out.println("=== CONFIRM TRANSACTION CLICKED ===");
        System.out.println("Current offer: " + (currentOffer != null ? currentOffer.getId() : "null"));
        
        if (currentOffer != null) {
            Alert confirmation = AlertHelper.createConfirmationDialog(
                    localeService.getMessage("transaction.confirm.title", "Confirm Transaction"),
                    localeService.getMessage("transaction.confirm.header", "Confirm Successful Transaction"),
                    localeService.getMessage("transaction.confirm.content", 
                            "Are you sure you want to confirm that this transaction was completed successfully?"));

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (onConfirmTransaction != null) {
                        onConfirmTransaction.accept(currentOffer);
                    }
                }
            });
        } else {
            System.out.println("ERROR: No current offer set!");
        }
    }

    /**
     * 
     */
    @FXML
    private void handleCancelTransaction() {
        System.out.println("=== CANCEL TRANSACTION CLICKED ===");
        System.out.println("Current offer: " + (currentOffer != null ? currentOffer.getId() : "null"));
        
        if (currentOffer != null) {
            Alert confirmation = AlertHelper.createConfirmationDialog(
                    localeService.getMessage("transaction.cancel.title", "Cancel Transaction"),
                    localeService.getMessage("transaction.cancel.header", "Cancel Transaction"),
                    localeService.getMessage("transaction.cancel.content", 
                            "Are you sure you want to cancel this transaction? This action cannot be undone."));

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Call the service to cancel transaction
                    if (onCancelTransaction != null) {
                        onCancelTransaction.accept(currentOffer);
                    }
                }
            });
        } else {
            System.out.println("ERROR: No current offer set!");
        }
    }
    
    /**
     * 
     */
    @FXML
    private void handleWriteReview() {
        System.out.println("=== WRITE REVIEW CLICKED ===");
        System.out.println("Current offer: " + (currentOffer != null ? currentOffer.getId() : "null"));
        
        if (currentOffer != null && onWriteReview != null) {
            onWriteReview.accept(currentOffer);
        } else {
            System.out.println("ERROR: No current offer set or callback null!");
        }
    }
    
    /**
     * 
     */
    public void showEmptyState() {
        detailsContent.setVisible(false);
        detailsContent.setManaged(false);
        emptyDetailsState.setVisible(true);
        emptyDetailsState.setManaged(true);
        currentOffer = null;
    }

    /**
     * 
     */
    public void clearDetails() {
        showEmptyState();
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
    private void adjustSplitPaneForDetails() {
        if (mainSplitPane != null) {
            double width = mainSplitPane.getWidth();
            if (width < 800) {
                mainSplitPane.setDividerPositions(0.3);
            } else if (width < 1200) {
                mainSplitPane.setDividerPositions(0.4);
            } else {
                mainSplitPane.setDividerPositions(0.5);
            }
        }
    }

    /**
     * @param offers
     */
    public void updateOffersList(List<OfferViewModel> offers) {
        if (sourceOffersList != null) {
            sourceOffersList.clear();
            sourceOffersList.addAll(offers);
            if (offers.isEmpty()) {
                showEmptyState();
            }
        }
    }

    /**
     * @param predicate
     */
    public void applyFilter(Predicate<OfferViewModel> predicate) {
        if (filteredOffers != null) {
            filteredOffers.setPredicate(predicate);
            if (filteredOffers.isEmpty()) {
                showEmptyState();
            }
        }
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

    /**
     * @return
     */
    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    /**
     * @param offer
     * @return
     */
    private boolean isCurrentUserListingOwner(OfferViewModel offer) {
        if (offer == null || offer.getListing() == null || offer.getListing().getUser() == null) {
            return false;
        }
        
        Long currentUserId = UserSessionService.getInstance().getUser().getId();
        return currentUserId != null && currentUserId.equals(offer.getListing().getUser().getId());
    }

    /**
     * @param offer
     * @return
     */
    private boolean isCurrentUserOfferCreator(OfferViewModel offer) {
        if (offer == null || offer.getOfferingUser() == null) {
            return false;
        }
        Long currentUserId = UserSessionService.getInstance().getUser().getId();
        return currentUserId != null && currentUserId.equals(offer.getOfferingUser().getId());
    }
}
