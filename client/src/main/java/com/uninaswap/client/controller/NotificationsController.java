package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.NotificationService;
import com.uninaswap.client.viewmodel.NotificationViewModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class NotificationsController implements Initializable, Refreshable {

    @FXML
    private TabPane notificationTabPane;
    @FXML
    private Tab allNotificationsTab;
    @FXML
    private Tab purchasesSalesTab;
    @FXML
    private Tab auctionsTab;
    @FXML
    private Tab socialTab;
    @FXML
    private VBox allNotificationsContainer;
    @FXML
    private VBox purchasesSalesContainer;
    @FXML
    private VBox auctionsContainer;
    @FXML
    private VBox socialContainer;
    @FXML
    private Button markAllReadButton;
    @FXML
    private Text titleText;
    @FXML
    private ImageView notificationIcon;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final NotificationService notificationService = NotificationService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    // Data
    private ObservableList<NotificationViewModel> allNotifications;
    private FilteredList<NotificationViewModel> purchasesSalesNotifications;
    private FilteredList<NotificationViewModel> auctionNotifications;
    private FilteredList<NotificationViewModel> socialNotifications;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupLabels();
        setupObservableList();
        setupNotificationTabs();
        setupEventHandlers();
        loadNotifications();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("notifications.debug.initialized", "NotificationsController initialized"));
    }

    private void setupLabels() {
        if (titleText != null) {
            titleText.setText(localeService.getMessage("notification.center", "Notification Center"));
        }
        if (markAllReadButton != null) {
            markAllReadButton.setText(localeService.getMessage("notification.mark.all.read", "Mark All as Read"));
        }
        
        // Set tab texts
        if (allNotificationsTab != null) {
            allNotificationsTab.setText(localeService.getMessage("notification.tab.all", "All"));
        }
        if (purchasesSalesTab != null) {
            purchasesSalesTab.setText(localeService.getMessage("notification.tab.purchases.sales", "Purchases & Sales"));
        }
        if (auctionsTab != null) {
            auctionsTab.setText(localeService.getMessage("notification.tab.auctions", "Auctions"));
        }
        if (socialTab != null) {
            socialTab.setText(localeService.getMessage("notification.tab.social", "Social"));
        }
    }

    private void setupObservableList() {
        // Get the observable list from NotificationService
        allNotifications = notificationService.getAllNotifications();

        // Create filtered lists for different tabs
        purchasesSalesNotifications = new FilteredList<>(allNotifications, notification -> {
            String type = notification.getType();
            return type.startsWith("OFFER_") || type.equals("PICKUP_SCHEDULED") || type.equals("PICKUP_REMINDER");
        });

        auctionNotifications = new FilteredList<>(allNotifications, notification -> {
            String type = notification.getType();
            return type.startsWith("AUCTION_");
        });

        socialNotifications = new FilteredList<>(allNotifications, notification -> {
            String type = notification.getType();
            return type.equals("MESSAGE_RECEIVED") || type.equals("PROFILE_UPDATED") || 
                   type.equals("FAVORITE_LISTING_UPDATED");
        });

        // Set up listener for automatic updates
        allNotifications.addListener((ListChangeListener<NotificationViewModel>) change -> {
            Platform.runLater(() -> {
                updateNotificationDisplays();
            });
        });
    }

    private void setupNotificationTabs() {
        // Setup tab selection listeners
        if (notificationTabPane != null) {
            notificationTabPane.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldTab, newTab) -> {
                        if (newTab != null) {
                            loadNotificationsForTab(newTab);
                        }
                    });
        }
    }

    private void setupEventHandlers() {
        // Setup the "Mark All as Read" button
        if (markAllReadButton != null) {
            markAllReadButton.setOnAction(e -> handleMarkAllAsRead());
        }
    }

    private void loadNotifications() {
        // Refresh notifications from server if list is empty
        if (allNotifications.isEmpty()) {
            notificationService.getNotifications(0, 100)
                .thenAccept(notifications -> Platform.runLater(() -> {
                    updateNotificationDisplays();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println(localeService.getMessage("notifications.error.load.failed", "Failed to load notifications: {0}").replace("{0}", ex.getMessage()));
                        showErrorPlaceholder(localeService.getMessage("notifications.error.load.general", "Failed to load notifications"));
                    });
                    return null;
                });
        } else {
            updateNotificationDisplays();
        }
    }

    private void loadNotificationsForTab(Tab selectedTab) {
        // Update displays when tab changes - the filtered lists will automatically update
        updateNotificationDisplays();
    }

    private void updateNotificationDisplays() {
        // Update all tab contents
        updateTabContent(allNotificationsContainer, allNotifications);
        updateTabContent(purchasesSalesContainer, purchasesSalesNotifications);
        updateTabContent(auctionsContainer, auctionNotifications);
        updateTabContent(socialContainer, socialNotifications);
    }

    private void updateTabContent(VBox container, ObservableList<? extends NotificationViewModel> notifications) {
        if (container == null) return;

        container.getChildren().clear();

        if (notifications.isEmpty()) {
            addEmptyPlaceholder(container);
        } else {
            for (NotificationViewModel notification : notifications) {
                VBox notificationItem = createNotificationItem(notification);
                container.getChildren().add(notificationItem);
            }
        }
    }

    private VBox createNotificationItem(NotificationViewModel notification) {
        VBox item = new VBox(8);
        item.getStyleClass().addAll("notification-item", notification.isRead() ? "read" : "unread");
        item.setPadding(new Insets(15));
        item.setOnMouseClicked(e -> handleNotificationClick(notification));

        // Header with icon, title, and time
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Notification icon based on type
        ImageView icon = new ImageView(getNotificationIcon(notification.getType()));
        icon.setFitWidth(24);
        icon.setFitHeight(24);

        Text title = new Text(notification.getTitle());
        title.getStyleClass().add("notification-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text time = new Text(formatTime(notification.getCreatedAt()));
        time.getStyleClass().add("notification-time");

        // Unread indicator
        if (!notification.isRead()) {
            Region unreadDot = new Region();
            unreadDot.getStyleClass().add("unread-dot");
            unreadDot.setPrefSize(12, 12);
            unreadDot.setMaxSize(12, 12);
            header.getChildren().addAll(icon, title, spacer, time, unreadDot);
        } else {
            header.getChildren().addAll(icon, title, spacer, time);
        }

        // Message
        Text message = new Text(notification.getMessage());
        message.getStyleClass().add("notification-message");
        message.setWrappingWidth(450);

        // Action buttons (optional)
        HBox actionButtons = createActionButtons(notification);

        item.getChildren().addAll(header, message);
        if (actionButtons != null) {
            item.getChildren().add(actionButtons);
        }

        return item;
    }

    private HBox createActionButtons(NotificationViewModel notification) {
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setPadding(new Insets(10, 0, 0, 0));

        // Add context-specific action buttons based on notification type
        switch (notification.getType()) {
            case "OFFER_RECEIVED" -> {
                Button viewOfferBtn = new Button(localeService.getMessage("notification.action.view.offer", "View Offer"));
                viewOfferBtn.getStyleClass().add("primary-button");
                viewOfferBtn.setOnAction(_ -> handleViewOffer(notification));
                buttonContainer.getChildren().add(viewOfferBtn);
            }
            case "AUCTION_ENDING_SOON" -> {
                Button viewAuctionBtn = new Button(localeService.getMessage("notification.action.view.auction", "View Auction"));
                viewAuctionBtn.getStyleClass().add("primary-button");
                viewAuctionBtn.setOnAction(e -> handleViewAuction(notification));
                buttonContainer.getChildren().add(viewAuctionBtn);
            }
            case "PICKUP_SCHEDULED" -> {
                Button viewPickupBtn = new Button(localeService.getMessage("notification.action.view.pickup", "View Pickup"));
                viewPickupBtn.getStyleClass().add("primary-button");
                viewPickupBtn.setOnAction(e -> handleViewPickup(notification));
                buttonContainer.getChildren().add(viewPickupBtn);
            }
        }

        // Always add mark as read button if not read
        if (!notification.isRead()) {
            Button markReadBtn = new Button(localeService.getMessage("notification.action.mark.read", "Mark as Read"));
            markReadBtn.getStyleClass().add("secondary-button");
            markReadBtn.setOnAction(e -> markAsRead(notification));
            buttonContainer.getChildren().add(markReadBtn);
        }

        return buttonContainer.getChildren().isEmpty() ? null : buttonContainer;
    }

    private Image getNotificationIcon(String type) {
        String iconPath = switch (type) {
            case "OFFER_RECEIVED", "OFFER_ACCEPTED", "OFFER_REJECTED", "OFFER_WITHDRAWN" -> "/images/icons/offers.png";
            case "AUCTION_ENDING_SOON", "AUCTION_WON", "AUCTION_OUTBID" -> "/images/icons/auction.png";
            case "MESSAGE_RECEIVED" -> "/images/icons/message.png";
            case "PICKUP_SCHEDULED", "PICKUP_REMINDER" -> "/images/icons/pickup.png";
            case "SYSTEM_ANNOUNCEMENT", "PROFILE_UPDATED" -> "/images/icons/system.png";
            case "FAVORITE_LISTING_UPDATED" -> "/images/icons/favorites_add.png";
            default -> "/images/icons/notification.png";
        };

        try {
            return new Image(getClass().getResourceAsStream(iconPath));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("notifications.error.icon.load", "Could not load notification icon: {0}").replace("{0}", iconPath));
            return new Image(getClass().getResourceAsStream("/images/icons/notification.png"));
        }
    }

    private String formatTime(LocalDateTime timestamp) {
        if (timestamp == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(timestamp, now);

        if (duration.toDays() > 0) {
            return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (duration.toHours() > 0) {
            return localeService.getMessage("notifications.time.hours.ago", "{0}h ago").replace("{0}", String.valueOf(duration.toHours()));
        } else if (duration.toMinutes() > 0) {
            return localeService.getMessage("notifications.time.minutes.ago", "{0}m ago").replace("{0}", String.valueOf(duration.toMinutes()));
        } else {
            return localeService.getMessage("notifications.time.just.now", "Just now");
        }
    }

    private void addEmptyPlaceholder(VBox container) {
        VBox placeholder = new VBox(10);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setPadding(new Insets(50));

        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/icons/notification.png")));
        icon.setFitWidth(48);
        icon.setFitHeight(48);
        icon.setOpacity(0.5);

        Text emptyText = new Text(localeService.getMessage("notifications.empty", "No notifications"));
        emptyText.getStyleClass().add("placeholder-text");
        emptyText.setStyle("-fx-font-size: 16px; -fx-fill: #888888;");

        placeholder.getChildren().addAll(icon, emptyText);
        container.getChildren().add(placeholder);
    }

    private void showErrorPlaceholder(String errorMessage) {
        // Add error placeholder to all containers
        VBox errorPlaceholder = new VBox(10);
        errorPlaceholder.setAlignment(Pos.CENTER);
        errorPlaceholder.setPadding(new Insets(50));

        Text errorText = new Text(errorMessage);
        errorText.getStyleClass().add("error-text");
        errorText.setStyle("-fx-font-size: 16px; -fx-fill: #E74C3C;");

        Button retryButton = new Button(localeService.getMessage("notification.retry", "Retry"));
        retryButton.getStyleClass().add("primary-button");
        retryButton.setOnAction(e -> loadNotifications());

        errorPlaceholder.getChildren().addAll(errorText, retryButton);

        if (allNotificationsContainer != null) {
            allNotificationsContainer.getChildren().clear();
            allNotificationsContainer.getChildren().add(errorPlaceholder);
        }
    }

    private void markAsRead(NotificationViewModel notification) {
        if (!notification.isRead()) {
            notificationService.markAsRead(notification.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        notification.setRead(true);
                        updateNotificationDisplays();
                        // The unread count is automatically updated via the server response
                        System.out.println(localeService.getMessage("notifications.debug.marked.read", "Notification marked as read: {0}").replace("{0}", notification.getTitle()));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println(localeService.getMessage("notifications.error.mark.read", "Failed to mark notification as read: {0}").replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
        }
    }

    @FXML
    private void handleMarkAllAsRead() {
        notificationService.markAllAsRead()
            .thenAccept(success -> Platform.runLater(() -> {
                if (success) {
                    // The observable list will automatically update via the server response
                    updateNotificationDisplays();
                    System.out.println(localeService.getMessage("notifications.debug.marked.all.read", "All notifications marked as read"));
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println(localeService.getMessage("notifications.error.mark.all.read", "Failed to mark all as read: {0}").replace("{0}", ex.getMessage()));
                });
                return null;
            });
    }

    private void handleNotificationClick(NotificationViewModel notification) {
        markAsRead(notification);
        
        // Navigate based on notification type
        switch (notification.getType()) {
            case "OFFER_RECEIVED", "OFFER_ACCEPTED", "OFFER_REJECTED", "OFFER_WITHDRAWN" -> handleViewOffer(notification);
            case "AUCTION_ENDING_SOON", "AUCTION_WON", "AUCTION_OUTBID" -> handleViewAuction(notification);
            case "PICKUP_SCHEDULED", "PICKUP_REMINDER" -> handleViewPickup(notification);
            default -> System.out.println(localeService.getMessage("notifications.debug.clicked", "Clicked notification: {0}").replace("{0}", notification.getTitle()));
        }
    }

    private void handleViewOffer(NotificationViewModel notification) {
        try {
            navigationService.navigateToOffersView();
        } catch (Exception e) {
            System.err.println(localeService.getMessage("notifications.error.navigate.offers", "Failed to navigate to offers: {0}").replace("{0}", e.getMessage()));
        }
        System.out.println(localeService.getMessage("notifications.debug.navigate.offer", "Navigate to offer from notification: {0}").replace("{0}", notification.getTitle()));
        // TODO: Implement navigation to specific offer
    }

    private void handleViewAuction(NotificationViewModel notification) {
        // Extract auction ID from notification data if available
        System.out.println(localeService.getMessage("notifications.debug.navigate.auction", "Navigate to auction from notification: {0}").replace("{0}", notification.getTitle()));
        // TODO: Implement navigation to specific auction
    }

    private void handleViewPickup(NotificationViewModel notification) {
        // Extract pickup ID from notification data if available
        System.out.println(localeService.getMessage("notifications.debug.navigate.pickup", "Navigate to pickup from notification: {0}").replace("{0}", notification.getTitle()));
        // TODO: Implement navigation to specific pickup
    }

    @Override
    public void refreshUI() {
        // Update all labels and text elements
        setupLabels();
        
        // Refresh the notification displays to update any localized content
        updateNotificationDisplays();
    }

    public void updateNotificationBadge(int unreadCount) {
        Platform.runLater(() -> {
            // Update any badge indicators
            System.out.println(localeService.getMessage("notifications.debug.badge.update", "Unread notifications: {0}").replace("{0}", String.valueOf(unreadCount)));
        });
    }
}
