package com.uninaswap.client.controller;

import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ItemViewModel;
import com.uninaswap.client.viewmodel.OfferItemViewModel;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.OfferStatus;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

public class OffersTabContentController implements Refreshable {

    // Main split pane
    @FXML
    private SplitPane mainSplitPane;
    
    // Left side - offers list
    @FXML
    private TableView<OfferViewModel> offersTable;
    @FXML
    private TableColumn<OfferViewModel, String> listingTitleColumn;
    @FXML
    private TableColumn<OfferViewModel, String> fromToColumn;
    @FXML
    private TableColumn<OfferViewModel, String> typeColumn;
    @FXML
    private TableColumn<OfferViewModel, String> amountColumn;
    @FXML
    private TableColumn<OfferViewModel, String> statusColumn;
    @FXML
    private TableColumn<OfferViewModel, String> dateColumn;
    @FXML
    private TableColumn<OfferViewModel, Void> actionsColumn;
    
    // Right side - offer details
    @FXML
    private VBox offerDetailsPanel;
    @FXML
    private VBox detailsHeader;
    @FXML
    private ScrollPane detailsScrollPane;
    @FXML
    private VBox detailsContent;
    @FXML
    private VBox emptyDetailsState;
    
    // Details content elements
    @FXML
    private VBox offerSummaryCard;
    @FXML
    private Label offerFromLabel;
    @FXML
    private Label offerListingLabel;
    @FXML
    private Label offerTypeLabel;
    @FXML
    private Label offerAmountLabel;
    @FXML
    private VBox messageSection;
    @FXML
    private TextArea offerMessageArea;
    @FXML
    private VBox offeredItemsSection;
    @FXML
    private VBox offeredItemsList;
    @FXML
    private VBox actionButtonsSection;
    @FXML
    private Button acceptOfferButton;
    @FXML
    private Button rejectOfferButton;
    @FXML
    private Button counterOfferButton;
    @FXML
    private Button closeDetailsButton;
    
    // New button fields for pickup scheduling
    @FXML
    private Button schedulePickupButton;
    @FXML
    private Button selectPickupTimeButton;
    @FXML
    private Button reschedulePickupButton;
    
    // New button fields for transaction confirmation
    @FXML
    private Button confirmTransactionButton;
    @FXML
    private Button cancelTransactionButton;
    
    // New button field for writing reviews
    @FXML
    private Button writeReviewButton;
    
    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    
    // State
    private FilteredList<OfferViewModel> filteredOffers;
    private String tabType; // "received", "sent", "history"
    private Consumer<OfferViewModel> onOfferSelected;
    private Consumer<TableColumn<OfferViewModel, Void>> actionButtonsSetup;
    private OfferViewModel currentOffer;
    private Consumer<OfferViewModel> onAcceptOffer;
    private Consumer<OfferViewModel> onRejectOffer;
    private Consumer<OfferViewModel> onCounterOffer;
    private Consumer<OfferViewModel> onConfirmTransaction;
    private Consumer<OfferViewModel> onCancelTransaction;
    private Consumer<OfferViewModel> onWriteReview;
    
    private ObservableList<OfferViewModel> sourceOffersList;

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
        this.sourceOffersList = offersList; // Store reference
        
        // Create filtered list
        filteredOffers = new FilteredList<>(offersList);
        offersTable.setItems(filteredOffers);
        
        setupTable();
        setupDetailsPanel();
        setupResponsiveLayout();
        
        // Initially show empty state
        showEmptyState();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("offerstab.debug.initialized", "OffersTabContent initialized for tab: {0}").replace("{0}", tabType));
    }

    @Override
    public void refreshUI() {
        // Update table column headers based on tab type
        if (listingTitleColumn != null) {
            listingTitleColumn.setText(localeService.getMessage("offers.column.listing", "Listing"));
        }
        if (fromToColumn != null) {
            if ("received".equals(tabType)) {
                fromToColumn.setText(localeService.getMessage("offers.column.from", "From"));
            } else {
                fromToColumn.setText(localeService.getMessage("offers.column.to", "To"));
            }
        }
        if (typeColumn != null) {
            typeColumn.setText(localeService.getMessage("offers.column.type", "Type"));
        }
        if (amountColumn != null) {
            amountColumn.setText(localeService.getMessage("offers.column.amount", "Amount"));
        }
        if (statusColumn != null) {
            statusColumn.setText(localeService.getMessage("offers.column.status", "Status"));
        }
        if (dateColumn != null) {
            dateColumn.setText(localeService.getMessage("offers.column.date", "Date"));
        }
        if (actionsColumn != null) {
            actionsColumn.setText(localeService.getMessage("offers.column.actions", "Actions"));
        }

        // Update button texts
        if (acceptOfferButton != null) {
            acceptOfferButton.setText(localeService.getMessage("offers.button.accept", "Accept"));
        }
        if (rejectOfferButton != null) {
            rejectOfferButton.setText(localeService.getMessage("offers.button.reject", "Reject"));
        }
        if (counterOfferButton != null) {
            counterOfferButton.setText(localeService.getMessage("offers.button.counter", "Counter Offer"));
        }
        if (closeDetailsButton != null) {
            closeDetailsButton.setText(localeService.getMessage("button.close", "✕"));
        }
        if (schedulePickupButton != null) {
            schedulePickupButton.setText(localeService.getMessage("pickup.button.schedule", "Schedule Pickup"));
        }
        if (selectPickupTimeButton != null) {
            selectPickupTimeButton.setText(localeService.getMessage("pickup.button.select.time", "Select Time"));
        }
        if (reschedulePickupButton != null) {
            reschedulePickupButton.setText(localeService.getMessage("pickup.button.reschedule", "Reschedule"));
        }
        if (confirmTransactionButton != null) {
            confirmTransactionButton.setText(localeService.getMessage("transaction.button.confirm", "Confirm"));
        }
        if (cancelTransactionButton != null) {
            cancelTransactionButton.setText(localeService.getMessage("transaction.button.cancel", "Cancel"));
        }
        if (writeReviewButton != null) {
            writeReviewButton.setText(localeService.getMessage("review.button.write", "Write Review"));
        }

        // Update table placeholder
        if (offersTable != null) {
            VBox placeholder = createTablePlaceholder();
            offersTable.setPlaceholder(placeholder);
        }

        // Refresh current offer details display if one is selected
        if (currentOffer != null) {
            refreshCurrentOfferDetails();
        }
    }

    private VBox createTablePlaceholder() {
        VBox placeholder = new VBox(15);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.getStyleClass().add("table-placeholder");

        Text titleText = new Text();
        titleText.getStyleClass().add("placeholder-title");
        
        Text subtitleText = new Text();
        subtitleText.getStyleClass().add("placeholder-subtitle");

        // Set appropriate placeholder text based on tab type
        switch (tabType) {
            case "received" -> {
                titleText.setText(localeService.getMessage("offers.empty.received.title", "No Received Offers"));
                subtitleText.setText(localeService.getMessage("offers.empty.received.subtitle", "You haven't received any offers yet"));
            }
            case "sent" -> {
                titleText.setText(localeService.getMessage("offers.empty.sent.title", "No Sent Offers"));
                subtitleText.setText(localeService.getMessage("offers.empty.sent.subtitle", "You haven't made any offers yet"));
            }
            case "history" -> {
                titleText.setText(localeService.getMessage("offers.empty.history.title", "No Offer History"));
                subtitleText.setText(localeService.getMessage("offers.empty.history.subtitle", "No completed offers to display"));
            }
            default -> {
                titleText.setText(localeService.getMessage("offers.empty.title", "No Offers"));
                subtitleText.setText(localeService.getMessage("offers.empty.subtitle", "No offers to display"));
            }
        }

        placeholder.getChildren().addAll(titleText, subtitleText);
        return placeholder;
    }

    private void refreshCurrentOfferDetails() {
        // Refresh the details display with current locale
        if (currentOffer != null) {
            // Update type display
            offerTypeLabel.setText(getOfferTypeDisplayName(currentOffer));
            
            // Update amount display
            offerAmountLabel.setText(formatOfferAmount(currentOffer));
            
            // Update date display (though this shouldn't change with locale for this format)
            // But other formats might need updating in the future
        }
    }

    private void setupTable() {
        // Setup table columns
        listingTitleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getListingTitle()));
        
        // From/To column depends on tab type
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
            new SimpleStringProperty(getLocalizedStatus(cellData.getValue().getStatus())));
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(formatDate(cellData.getValue())));
        
        // Setup action buttons if provided
        if (actionButtonsSetup != null && !"history".equals(tabType)) {
            actionButtonsSetup.accept(actionsColumn);
        } else {
            // Hide actions column for history
            actionsColumn.setVisible(false);
            if (actionsColumn.getTableView() != null) {
                // Only set managed if the column is part of a table
                Platform.runLater(() -> {
                    if (actionsColumn.getTableView() != null) {
                        actionsColumn.getTableView().getColumns().remove(actionsColumn);
                    }
                });
            }
        }
        
        // Setup selection handler
        offersTable.getSelectionModel().selectedItemProperty().addListener((_, _, newOffer) -> {
            if (newOffer != null) {
                showOfferDetails(newOffer);
                if (onOfferSelected != null) {
                    onOfferSelected.accept(newOffer);
                }
            }
        });
        
        // Setup row click handler for better UX
        offersTable.setRowFactory(tv -> {
            TableRow<OfferViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    offersTable.getSelectionModel().select(row.getItem());
                }
            });
            return row;
        });
    }

    private String getLocalizedStatus(OfferStatus status) {
        return switch (status) {
            case PENDING -> localeService.getMessage("offers.status.pending", "Pending");
            case ACCEPTED -> localeService.getMessage("offers.status.accepted", "Accepted");
            case CONFIRMED -> localeService.getMessage("offers.status.confirmed", "Confirmed");
            case SELLERVERIFIED -> localeService.getMessage("offers.status.sellerverified", "Seller Verified");
            case BUYERVERIFIED -> localeService.getMessage("offers.status.buyerverified", "Buyer Verified");
            case PICKUPSCHEDULING -> localeService.getMessage("offers.status.pickupscheduling", "Pickup Scheduling");
            case PICKUPRESCHEDULING -> localeService.getMessage("offers.status.pickuprescheduling", "Pickup Rescheduling");
            case CANCELLED -> localeService.getMessage("offers.status.cancelled", "Cancelled");
            case REJECTED -> localeService.getMessage("offers.status.rejected", "Rejected");
            case WITHDRAWN -> localeService.getMessage("offers.status.withdrawn", "Withdrawn");
            case COMPLETED -> localeService.getMessage("offers.status.completed", "Completed");
            case REVIEWED -> localeService.getMessage("offers.status.reviewed", "Reviewed");
            case EXPIRED -> localeService.getMessage("offers.status.expired", "Expired");
            default -> status.name();
        };
    }
    
    private void setupDetailsPanel() {
        // Setup close button
        closeDetailsButton.setOnAction(e -> clearDetails());
        
        // Action buttons should be visible for both received and sent offers
        // The visibility of individual buttons will be controlled by updateActionButtons()
        actionButtonsSection.setVisible(true);
        actionButtonsSection.setManaged(true);
        
        // Only hide action buttons for history tab
        if ("history".equals(tabType)) {
            actionButtonsSection.setVisible(false);
            actionButtonsSection.setManaged(false);
        }
    }
    
    private void setupResponsiveLayout() {
        // Set initial divider position
        Platform.runLater(() -> {
            mainSplitPane.setDividerPositions(0.6);
        });
        
        // Make the split pane responsive
        mainSplitPane.widthProperty().addListener((_, _, newWidth) -> {
            double width = newWidth.doubleValue();
            
            if (width < 800) {
                // On smaller screens, give more space to details when shown
                if (currentOffer != null) {
                    mainSplitPane.setDividerPositions(0.35);
                } else {
                    mainSplitPane.setDividerPositions(1.0); // Hide details panel
                }
            } else if (width < 1200) {
                // Medium screens
                mainSplitPane.setDividerPositions(0.5);
            } else {
                // Large screens
                mainSplitPane.setDividerPositions(0.6);
            }
        });
    }
    
    public void showOfferDetails(OfferViewModel offer) {
        this.currentOffer = offer;
        
        // Hide empty state and show details
        emptyDetailsState.setVisible(false);
        emptyDetailsState.setManaged(false);
        detailsContent.setVisible(true);
        detailsContent.setManaged(true);
        
        // Populate offer details
        offerFromLabel.setText(offer.getOfferingUserUsername());
        offerListingLabel.setText(offer.getListingTitle());
        offerTypeLabel.setText(getOfferTypeDisplayName(offer));
        offerAmountLabel.setText(formatOfferAmount(offer));
        
        // Handle message
        String message = offer.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            offerMessageArea.setText(message);
            messageSection.setVisible(true);
            messageSection.setManaged(true);
        } else {
            messageSection.setVisible(false);
            messageSection.setManaged(false);
        }
        
        // Handle offered items
        if (offer.getOfferItems() != null && !offer.getOfferItems().isEmpty()) {
            populateOfferedItemsList(offer.getOfferItems());
            offeredItemsSection.setVisible(true);
            offeredItemsSection.setManaged(true);
        } else {
            offeredItemsSection.setVisible(false);
            offeredItemsSection.setManaged(false);
        }
        
        // Update action buttons state
        updateActionButtons(offer);
        
        // Scroll to top of details
        Platform.runLater(() -> {
            detailsScrollPane.setVvalue(0.0);
        });
        
        // Adjust split pane for responsive layout
        adjustSplitPaneForDetails();
    }
    
    private void populateOfferedItemsList(ObservableList<OfferItemViewModel> offerItems) {
        offeredItemsList.getChildren().clear();
        
        for (OfferItemViewModel item : offerItems) {
            VBox itemRow = createOfferItemRow(item);
            offeredItemsList.getChildren().add(itemRow);
        }
    }
    
    private VBox createOfferItemRow(OfferItemViewModel item) {
        // Use the same implementation as in the main OffersController
        VBox itemContainer = new VBox(8);
        itemContainer.getStyleClass().add("offer-item-row");
        
        // Main item header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("offer-item-header-row");
        
        // Item image thumbnail
        ImageView itemImage = new ImageView();
        itemImage.setFitHeight(40);
        itemImage.setFitWidth(40);
        itemImage.setPreserveRatio(true);
        itemImage.getStyleClass().add("offer-item-thumbnail");
        
        // Load item image
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
        HBox nameQuantityRow = new HBox(8);
        nameQuantityRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Text itemName = new Text(item.getItemName());
        itemName.getStyleClass().add("offer-item-name");
        
        // Quantity badge
        Label quantityBadge = new Label(localeService.getMessage("offers.item.quantity.badge", "x{0}").replace("{0}", String.valueOf(item.getQuantity())));
        quantityBadge.getStyleClass().add("offer-quantity-badge");
        
        nameQuantityRow.getChildren().addAll(itemName, quantityBadge);
        itemMainInfo.getChildren().add(nameQuantityRow);
        
        // Add condition if available
        if (item.getCondition() != null) {
            Label conditionLabel = new Label(getLocalizedCondition(item.getCondition()));
            conditionLabel.getStyleClass().add("offer-item-condition");
            itemMainInfo.getChildren().add(conditionLabel);
        }
        
        headerRow.getChildren().addAll(itemImage, itemMainInfo);
        itemContainer.getChildren().add(headerRow);
        
        return itemContainer;
    }

    private String getLocalizedCondition(com.uninaswap.common.enums.ItemCondition condition) {
        return switch (condition) {
            case NEW -> localeService.getMessage("condition.new", "New");
            case LIKE_NEW -> localeService.getMessage("condition.like.new", "Like New");
            case VERY_GOOD -> localeService.getMessage("condition.very.good", "Very Good");
            case GOOD -> localeService.getMessage("condition.good", "Good");
            case ACCEPTABLE -> localeService.getMessage("condition.acceptable", "Acceptable");
            case FOR_PARTS -> localeService.getMessage("condition.for.parts", "For Parts");
            default -> condition.getDisplayName();
        };
    }
    
    private void setDefaultOfferItemImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println(localeService.getMessage("offerstab.error.image.load", "Could not load default offer item image: {0}").replace("{0}", e.getMessage()));
        }
    }
    
    private void updateActionButtons(OfferViewModel offer) {
        
        if ("received".equals(tabType)) {
            // For received offers, show buttons based on status and user role
            updateReceivedOfferButtons(offer);
        } else if ("sent".equals(tabType)) {
            // For sent offers, show buttons based on status
            updateSentOfferButtons(offer);
        } else if ("history".equals(tabType)) {
            // For history, typically no action buttons
            hideAllActionButtons();
        } else {
            // Unknown tab type, hide all buttons
            hideAllActionButtons();
        }
    }
    
    private void updateReceivedOfferButtons(OfferViewModel offer) {
        // Get current user to determine role
        Long currentUserId = UserSessionService.getInstance().getUser().getId();
        boolean isListingOwner = offer.getListing() != null && 
            offer.getListing().getUser() != null && 
            offer.getListing().getUser().getId().equals(currentUserId);
        
        // Hide all buttons first
        hideAllActionButtons();
        
        System.out.println(localeService.getMessage("offerstab.debug.received.buttons", "=== RECEIVED OFFER BUTTONS DEBUG ==="));
        System.out.println(localeService.getMessage("offerstab.debug.offer.id", "Offer ID: {0}").replace("{0}", String.valueOf(offer.getId())));
        System.out.println(localeService.getMessage("offerstab.debug.offer.status", "Offer Status: {0}").replace("{0}", offer.getStatus().toString()));
        System.out.println(localeService.getMessage("offerstab.debug.delivery.type", "Delivery Type: {0}").replace("{0}", String.valueOf(offer.getDeliveryType())));
        System.out.println(localeService.getMessage("offerstab.debug.is.listing.owner", "Is Listing Owner: {0}").replace("{0}", String.valueOf(isListingOwner)));
        System.out.println(localeService.getMessage("offerstab.debug.tab.type", "Tab Type: {0}").replace("{0}", tabType));
        
        switch (offer.getStatus()) {
            case PENDING:
                if (isListingOwner) {
                    // Listing owner can accept/reject pending offers
                    showActionButtons(true, true, true, false, false, false, false, false, false);
                    System.out.println(localeService.getMessage("offerstab.debug.pending.owner", "PENDING - showing accept/reject/counter buttons for listing owner"));
                }
                break;
                
            case ACCEPTED:
                if (isListingOwner && offer.getDeliveryType() == DeliveryType.PICKUP) {
                    // Show schedule pickup button for listing owner after accepting pickup offer
                    showActionButtons(false, false, false, true, false, false, false, false, false);
                    System.out.println(localeService.getMessage("offerstab.debug.accepted.pickup", "ACCEPTED - showing schedule pickup button for listing owner"));
                }
                break;
                
            case PICKUPSCHEDULING:
                if (!isListingOwner && offer.getDeliveryType() == DeliveryType.PICKUP) {
                    // Show select pickup time button for offer creator when pickup is being scheduled
                    showActionButtons(false, false, false, false, true, false, false, false, false);
                    System.out.println(localeService.getMessage("offerstab.debug.pickup.scheduling", "PICKUPSCHEDULING - showing select time button for offer creator"));
                }
                break;
                
            case PICKUPRESCHEDULING:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    if (isListingOwner) {
                        // FIXED: Listing owner should SELECT from the proposed times, not reschedule
                        showActionButtons(false, false, false, false, true, false, false, false, false);
                        System.out.println(localeService.getMessage("offerstab.debug.pickup.rescheduling.owner", "PICKUPRESCHEDULING - showing SELECT TIME button for listing owner"));
                    } else {
                        // Offer creator can propose new times (reschedule)
                        showActionButtons(false, false, false, false, false, true, false, false, false);
                        System.out.println(localeService.getMessage("offerstab.debug.pickup.rescheduling.creator", "PICKUPRESCHEDULING - showing RESCHEDULE button for offer creator"));
                    }
                }
                break;
                
            case CONFIRMED:
                // Both parties can verify transaction for PICKUP, only buyer for SHIPPING
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    // Both seller (listing owner) and buyer (offer creator) can confirm/cancel
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println(localeService.getMessage("offerstab.debug.confirmed.pickup", "CONFIRMED - showing transaction buttons for both parties (pickup)"));
                } else if (offer.getDeliveryType() == DeliveryType.SHIPPING && !isListingOwner) {
                    // Only buyer can confirm/cancel for shipping
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println(localeService.getMessage("offerstab.debug.confirmed.shipping", "CONFIRMED - showing transaction buttons for buyer only (shipping)"));
                }
                break;
                
            case SELLERVERIFIED:
                // Only buyer can confirm now (to complete transaction)
                if (!isListingOwner) {
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println(localeService.getMessage("offerstab.debug.seller.verified", "SELLERVERIFIED - showing transaction buttons for buyer to complete"));
                } else {
                    hideAllActionButtons();
                    System.out.println(localeService.getMessage("offerstab.debug.seller.verified.waiting", "SELLERVERIFIED - seller already verified, waiting for buyer"));
                }
                break;
                
            case BUYERVERIFIED:
                // Only seller can confirm now (to complete transaction)
                if (isListingOwner) {
                    showActionButtons(false, false, false, false, false, false, true, true, false);
                    System.out.println(localeService.getMessage("offerstab.debug.buyer.verified", "BUYERVERIFIED - showing transaction buttons for seller to complete"));
                } else {
                    hideAllActionButtons();
                    System.out.println(localeService.getMessage("offerstab.debug.buyer.verified.waiting", "BUYERVERIFIED - buyer already verified, waiting for seller"));
                }
                break;
                
            case COMPLETED:
                // NO review button for sellers in received offers
                // Only buyers can review sellers, not the other way around
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.completed.no.review", "COMPLETED - no review option for sellers (only buyers can review sellers)"));
                break;
                
            case REVIEWED:
                // Review has been submitted - no more actions available
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.reviewed", "REVIEWED - review already submitted, no actions available"));
                break;
                
            case CANCELLED:
            case REJECTED:
            case WITHDRAWN:
            case EXPIRED:
                // Terminal states - no actions available
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.terminal.status", "Terminal status - hiding all buttons"));
                break;
                
            default:
                // Unknown status
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.unknown.status", "Unknown status - hiding all buttons"));
                break;
        }
    }
    
    private void updateSentOfferButtons(OfferViewModel offer) {
        // For sent offers (offers the current user made)
        hideAllActionButtons();
        
        System.out.println(localeService.getMessage("offerstab.debug.sent.buttons", "=== SENT OFFER BUTTONS DEBUG ==="));
        System.out.println(localeService.getMessage("offerstab.debug.offer.id", "Offer ID: {0}").replace("{0}", String.valueOf(offer.getId())));
        System.out.println(localeService.getMessage("offerstab.debug.offer.status", "Offer Status: {0}").replace("{0}", offer.getStatus().toString()));
        System.out.println(localeService.getMessage("offerstab.debug.delivery.type", "Delivery Type: {0}").replace("{0}", String.valueOf(offer.getDeliveryType())));
        System.out.println(localeService.getMessage("offerstab.debug.tab.type", "Tab Type: {0}").replace("{0}", tabType));
        
        switch (offer.getStatus()) {
            case PENDING:
                // User can withdraw their pending offer
                System.out.println(localeService.getMessage("offerstab.debug.pending.sent", "PENDING sent offer - no special actions"));
                break;
                
            case ACCEPTED:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    // For accepted pickup offers, the seller will schedule first
                    System.out.println(localeService.getMessage("offerstab.debug.accepted.pickup.sent", "ACCEPTED pickup offer - waiting for seller to schedule"));
                }
                break;
                
            case PICKUPSCHEDULING:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    // Show select pickup time button for the offer creator (buyer)
                    showActionButtons(false, false, false, false, true, false, false, false, false);
                    System.out.println(localeService.getMessage("offerstab.debug.pickup.scheduling.buyer", "PICKUPSCHEDULING - showing SELECT TIME button for buyer"));
                }
                break;
                
            case PICKUPRESCHEDULING:
                if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                    // Offer creator (buyer) can propose new times or select from existing ones
                    showActionButtons(false, false, false, false, true, true, false, false, false);
                    System.out.println(localeService.getMessage("offerstab.debug.pickup.rescheduling.buyer", "PICKUPRESCHEDULING - showing SELECT TIME and RESCHEDULE buttons for buyer"));
                }
                break;
                
            case CONFIRMED:
                // Buyer can verify transaction for both PICKUP and SHIPPING
                showActionButtons(false, false, false, false, false, false, true, true, false);
                System.out.println(localeService.getMessage("offerstab.debug.confirmed.buyer", "CONFIRMED - showing transaction verification buttons for buyer"));
                break;
                
            case SELLERVERIFIED:
                // Only buyer can confirm now (to complete transaction)
                showActionButtons(false, false, false, false, false, false, true, true, false);
                System.out.println(localeService.getMessage("offerstab.debug.seller.verified.buyer", "SELLERVERIFIED - showing transaction verification buttons for buyer to complete"));
                break;
                
            case BUYERVERIFIED:
                // Buyer already verified, waiting for seller - NO BUTTONS FOR BUYER
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.buyer.verified.waiting.sent", "BUYERVERIFIED - buyer already verified, waiting for seller"));
                break;
                
            case COMPLETED:
                // ONLY the buyer (offer creator) can write a review about the seller
                // Show write review button for completed offers in SENT tab (buyer's perspective)
                boolean canReview = canOfferBeReviewed(offer);
                if (canReview) {
                    showActionButtons(false, false, false, false, false, false, false, false, true);
                    System.out.println(localeService.getMessage("offerstab.debug.completed.review", "COMPLETED - showing WRITE REVIEW button for buyer to review seller"));
                } else {
                    hideAllActionButtons();
                    System.out.println(localeService.getMessage("offerstab.debug.completed.no.review.gift", "COMPLETED - no review option for gift listing"));
                }
                break;
                
            case REVIEWED:
                // Review has been submitted - no more actions available
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.reviewed", "REVIEWED - review already submitted, no actions available"));
                break;
                
            case CANCELLED:
            case REJECTED:
            case WITHDRAWN:
            case EXPIRED:
                // Terminal states - no actions available
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.terminal.status", "Terminal status - hiding all buttons"));
                break;
                
            default:
                // Other statuses don't need special actions
                hideAllActionButtons();
                System.out.println(localeService.getMessage("offerstab.debug.unknown.status", "Unknown status - hiding all buttons"));
                break;
        }
    }
    
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
        
        System.out.println(localeService.getMessage("offerstab.debug.showing.buttons", "Showing buttons - review: {0}").replace("{0}", String.valueOf(showWriteReview)));
    }
    
    private void setButtonVisibility(Button button, boolean visible) {
        if (button != null) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
    }
    
    // Add these new action handlers
    @FXML
    private void handleSchedulePickup() {
        if (currentOffer != null) {
            Stage stage = (Stage) schedulePickupButton.getScene().getWindow();
            NavigationService.getInstance().openPickupScheduling(currentOffer, stage);
        }
    }

    @FXML
    private void handleSelectPickupTime() {
        if (currentOffer != null) {
            Stage stage = (Stage) selectPickupTimeButton.getScene().getWindow();
            NavigationService.getInstance().openPickupSelection(currentOffer, stage);
        }
    }

    @FXML
    private void handleReschedulePickup() {
        if (currentOffer != null) {
            Stage stage = (Stage) reschedulePickupButton.getScene().getWindow();
            NavigationService.getInstance().openPickupRescheduling(currentOffer.getId(), stage);
        }
    }
    
    @FXML
    private void handleCloseDetails() {
        clearDetails();
    }

    @FXML
    private void handleRejectOffer() {
        if (currentOffer != null && onRejectOffer != null) {
            onRejectOffer.accept(currentOffer);
        }
    }

    @FXML
    private void handleCounterOffer() {
        if (currentOffer != null && onCounterOffer != null) {
            onCounterOffer.accept(currentOffer);
        }
    }

    @FXML
    private void handleAcceptOffer() {
        if (currentOffer != null && onAcceptOffer != null) {
            onAcceptOffer.accept(currentOffer);
        }
    }
    
    @FXML
    private void handleConfirmTransaction() {
        System.out.println(localeService.getMessage("offerstab.debug.confirm.clicked", "=== CONFIRM TRANSACTION CLICKED ==="));
        System.out.println(localeService.getMessage("offerstab.debug.current.offer", "Current offer: {0}").replace("{0}", currentOffer != null ? String.valueOf(currentOffer.getId()) : "null"));
        
        if (currentOffer != null) {
            Alert confirmation = AlertHelper.createConfirmationDialog(
                    localeService.getMessage("transaction.confirm.title", "Confirm Transaction"),
                    localeService.getMessage("transaction.confirm.header", "Confirm Successful Transaction"),
                    localeService.getMessage("transaction.confirm.content", 
                            "Are you sure you want to confirm that this transaction was completed successfully?"));

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Call the service to confirm transaction
                    if (onConfirmTransaction != null) {
                        onConfirmTransaction.accept(currentOffer);
                    }
                }
            });
        } else {
            System.out.println(localeService.getMessage("offerstab.error.no.offer", "ERROR: No current offer set!"));
        }
    }

    @FXML
    private void handleCancelTransaction() {
        System.out.println(localeService.getMessage("offerstab.debug.cancel.clicked", "=== CANCEL TRANSACTION CLICKED ==="));
        System.out.println(localeService.getMessage("offerstab.debug.current.offer", "Current offer: {0}").replace("{0}", currentOffer != null ? String.valueOf(currentOffer.getId()) : "null"));
        
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
            System.out.println(localeService.getMessage("offerstab.error.no.offer", "ERROR: No current offer set!"));
        }
    }
    
    @FXML
    private void handleWriteReview() {
        System.out.println(localeService.getMessage("offerstab.debug.review.clicked", "=== WRITE REVIEW CLICKED ==="));
        System.out.println(localeService.getMessage("offerstab.debug.current.offer", "Current offer: {0}").replace("{0}", currentOffer != null ? String.valueOf(currentOffer.getId()) : "null"));
        
        if (currentOffer != null && onWriteReview != null) {
            onWriteReview.accept(currentOffer);
        } else {
            System.out.println(localeService.getMessage("offerstab.error.no.offer.or.callback", "ERROR: No current offer set or callback null!"));
        }
    }
    
    public void showEmptyState() {
        // Hide details content and show empty state
        detailsContent.setVisible(false);
        detailsContent.setManaged(false);
        emptyDetailsState.setVisible(true);
        emptyDetailsState.setManaged(true);
        
        // Clear current offer
        currentOffer = null;
    }

    public void clearDetails() {
        showEmptyState();
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

    private void adjustSplitPaneForDetails() {
        if (mainSplitPane != null) {
            double width = mainSplitPane.getWidth();
            
            // Adjust divider position based on content and screen size
            if (width < 800) {
                // On smaller screens, give more space to details when shown
                mainSplitPane.setDividerPositions(0.3);
            } else if (width < 1200) {
                // Medium screens
                mainSplitPane.setDividerPositions(0.4);
            } else {
                // Large screens
                mainSplitPane.setDividerPositions(0.5);
            }
        }
    }

    public void updateOffersList(List<OfferViewModel> offers) {
        if (sourceOffersList != null) {
            // Clear current list and add new offers
            sourceOffersList.clear();
            sourceOffersList.addAll(offers);
            
            // If a table is empty, show empty state
            if (offers.isEmpty()) {
                showEmptyState();
            }
        }
    }

    public void applyFilter(Predicate<OfferViewModel> predicate) {
        if (filteredOffers != null) {
            filteredOffers.setPredicate(predicate);
            
            // If filter results in empty list, show empty state
            if (filteredOffers.isEmpty()) {
                showEmptyState();
            }
        }
    }

    private boolean canOfferBeReviewed(OfferViewModel offer) {
        if (offer == null || offer.getListing() == null) {
            return false;
        }
        
        // Don't allow reviews for gift listings
        String listingType = offer.getListing().getListingTypeValue();
        if ("GIFT".equalsIgnoreCase(listingType)) {
            return false;
        }
        
        // Only allow reviews for completed offers (not reviewed yet)
        return offer.getStatus() == OfferStatus.COMPLETED;
    }

    public SplitPane getMainSplitPane() {
        return mainSplitPane;
    }

    private boolean isCurrentUserListingOwner(OfferViewModel offer) {
        if (offer == null || offer.getListing() == null || offer.getListing().getUser() == null) {
            return false;
        }
        
        Long currentUserId = UserSessionService.getInstance().getUser().getId();
        return currentUserId != null && currentUserId.equals(offer.getListing().getUser().getId());
    }

    // Helper method to check if current user is the offer creator
    private boolean isCurrentUserOfferCreator(OfferViewModel offer) {
        if (offer == null || offer.getOfferingUser() == null) {
            return false;
        }
        
        Long currentUserId = UserSessionService.getInstance().getUser().getId();
        return currentUserId != null && currentUserId.equals(offer.getOfferingUser().getId());
    }
}
