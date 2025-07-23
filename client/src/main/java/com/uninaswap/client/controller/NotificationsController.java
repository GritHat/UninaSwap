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
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 
 */
public class NotificationsController implements Initializable, Refreshable {

    /**
     * 
     */
    @FXML
    private TabPane notificationTabPane;
    /**
     * 
     */
    @FXML
    private Tab allNotificationsTab;
    /**
     * 
     */
    @FXML
    private Tab purchasesSalesTab;
    /**
     * 
     */
    @FXML
    private Tab auctionsTab;
    /**
     * 
     */
    @FXML
    private Tab socialTab;
    /**
     * 
     */
    @FXML
    private VBox allNotificationsContainer;
    /**
     * 
     */
    @FXML
    private VBox purchasesSalesContainer;
    /**
     * 
     */
    @FXML
    private VBox auctionsContainer;
    /**
     * 
     */
    @FXML
    private VBox socialContainer;
    /**
     * 
     */
    @FXML
    private Button markAllReadButton;
    /**
     * 
     */
    @FXML
    private Text titleText;
    /**
     * 
     */
    @FXML
    private ImageView notificationIcon;

    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final NotificationService notificationService = NotificationService.getInstance();
    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();

    /**
     * 
     */
    private ObservableList<NotificationViewModel> allNotifications;
    /**
     * 
     */
    private FilteredList<NotificationViewModel> purchasesSalesNotifications;
    /**
     * 
     */
    private FilteredList<NotificationViewModel> auctionNotifications;
    /**
     * 
     */
    private FilteredList<NotificationViewModel> socialNotifications;

    /**
     *
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupLabels();
        setupObservableList();
        setupNotificationTabs();
        setupEventHandlers();
        loadNotifications();
    }

    /**
     * 
     */
    private void setupLabels() {
        titleText.setText(localeService.getMessage("notification.center", "Notification Center"));
        markAllReadButton.setText(localeService.getMessage("notification.mark.all.read", "Mark All as Read"));
        allNotificationsTab.setText(localeService.getMessage("notification.tab.all", "All"));
        purchasesSalesTab.setText(localeService.getMessage("notification.tab.purchases.sales", "Purchases & Sales"));
        auctionsTab.setText(localeService.getMessage("notification.tab.auctions", "Auctions"));
        socialTab.setText(localeService.getMessage("notification.tab.social", "Social"));
    }

    /**
     * 
     */
    private void setupObservableList() {
        allNotifications = notificationService.getAllNotifications();
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
        allNotifications.addListener((ListChangeListener<NotificationViewModel>) _ -> {
            Platform.runLater(() -> {
                updateNotificationDisplays();
            });
        });
    }

    /**
     * 
     */
    private void setupNotificationTabs() {
        if (notificationTabPane != null) {
            notificationTabPane.getSelectionModel().selectedItemProperty().addListener(
                    (_, _, newTab) -> {
                        if (newTab != null) {
                            loadNotificationsForTab(newTab);
                        }
                    });
        }
    }

    /**
     * 
     */
    private void setupEventHandlers() {
        if (markAllReadButton != null) {
            markAllReadButton.setOnAction(_ -> handleMarkAllAsRead());
        }
    }

    /**
     * 
     */
    private void loadNotifications() {
        if (allNotifications.isEmpty()) {
            notificationService.getNotifications(0, 100)
                .thenAccept(_ -> Platform.runLater(() -> {
                    updateNotificationDisplays();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Failed to load notifications: " + ex.getMessage());
                        showErrorPlaceholder("Failed to load notifications");
                    });
                    return null;
                });
        } else {
            updateNotificationDisplays();
        }
    }

    /**
     * @param selectedTab
     */
    private void loadNotificationsForTab(Tab selectedTab) {
        updateNotificationDisplays();
    }

    /**
     * 
     */
    private void updateNotificationDisplays() {
        updateTabContent(allNotificationsContainer, allNotifications);
        updateTabContent(purchasesSalesContainer, purchasesSalesNotifications);
        updateTabContent(auctionsContainer, auctionNotifications);
        updateTabContent(socialContainer, socialNotifications);
    }

    /**
     * @param container
     * @param notifications
     */
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

    /**
     * @param notification
     * @return
     */
    private VBox createNotificationItem(NotificationViewModel notification) {
        VBox item = new VBox(8);
        item.getStyleClass().addAll("notification-item", notification.isRead() ? "read" : "unread");
        item.setPadding(new Insets(15));
        item.setOnMouseClicked(_ -> handleNotificationClick(notification));
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
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
        if (!notification.isRead()) {
            Region unreadDot = new Region();
            unreadDot.getStyleClass().add("unread-dot");
            unreadDot.setPrefSize(12, 12);
            unreadDot.setMaxSize(12, 12);
            header.getChildren().addAll(icon, title, spacer, time, unreadDot);
        } else {
            header.getChildren().addAll(icon, title, spacer, time);
        }
        Text message = new Text(notification.getMessage());
        message.getStyleClass().add("notification-message");
        message.setWrappingWidth(450);
        HBox actionButtons = createActionButtons(notification);

        item.getChildren().addAll(header, message);
        if (actionButtons != null) {
            item.getChildren().add(actionButtons);
        }

        return item;
    }

    /**
     * @param notification
     * @return
     */
    private HBox createActionButtons(NotificationViewModel notification) {
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setPadding(new Insets(10, 0, 0, 0));
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
                viewAuctionBtn.setOnAction(_ -> handleViewAuction(notification));
                buttonContainer.getChildren().add(viewAuctionBtn);
            }
            case "PICKUP_SCHEDULED" -> {
                Button viewPickupBtn = new Button(localeService.getMessage("notification.action.view.pickup", "View Pickup"));
                viewPickupBtn.getStyleClass().add("primary-button");
                viewPickupBtn.setOnAction(_ -> handleViewPickup(notification));
                buttonContainer.getChildren().add(viewPickupBtn);
            }
        }
        if (!notification.isRead()) {
            Button markReadBtn = new Button(localeService.getMessage("notification.action.mark.read", "Mark as Read"));
            markReadBtn.getStyleClass().add("secondary-button");
            markReadBtn.setOnAction(_ -> markAsRead(notification));
            buttonContainer.getChildren().add(markReadBtn);
        }

        return buttonContainer.getChildren().isEmpty() ? null : buttonContainer;
    }

    /**
     * @param type
     * @return
     */
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
            return new Image(getClass().getResourceAsStream("/images/icons/notification.png"));
        }
    }

    /**
     * @param timestamp
     * @return
     */
    private String formatTime(java.time.LocalDateTime timestamp) {
        if (timestamp == null) return "";

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(timestamp, now);

        if (duration.toDays() > 0) {
            return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (duration.toHours() > 0) {
            return duration.toHours() + "h ago";
        } else if (duration.toMinutes() > 0) {
            return duration.toMinutes() + "m ago";
        } else {
            return "Just now";
        }
    }

    /**
     * @param container
     */
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

    /**
     * @param errorMessage
     */
    private void showErrorPlaceholder(String errorMessage) {
        VBox errorPlaceholder = new VBox(10);
        errorPlaceholder.setAlignment(Pos.CENTER);
        errorPlaceholder.setPadding(new Insets(50));

        Text errorText = new Text(errorMessage);
        errorText.getStyleClass().add("error-text");
        errorText.setStyle("-fx-font-size: 16px; -fx-fill: #E74C3C;");

        Button retryButton = new Button(localeService.getMessage("notification.retry", "Retry"));
        retryButton.getStyleClass().add("primary-button");
        retryButton.setOnAction(_ -> loadNotifications());

        errorPlaceholder.getChildren().addAll(errorText, retryButton);

        allNotificationsContainer.getChildren().clear();
        allNotificationsContainer.getChildren().add(errorPlaceholder);
    }

    /**
     * @param notification
     */
    private void markAsRead(NotificationViewModel notification) {
        if (!notification.isRead()) {
            notificationService.markAsRead(notification.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        notification.setRead(true);
                        updateNotificationDisplays();
                        System.out.println("Notification marked as read: " + notification.getTitle());
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.err.println("Failed to mark notification as read: " + ex.getMessage());
                    });
                    return null;
                });
        }
    }

    /**
     * 
     */
    @FXML
    private void handleMarkAllAsRead() {
        notificationService.markAllAsRead()
            .thenAccept(success -> Platform.runLater(() -> {
                if (success) {
                    updateNotificationDisplays();
                    System.out.println("All notifications marked as read");
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println("Failed to mark all as read: " + ex.getMessage());
                });
                return null;
            });
    }

    /**
     * @param notification
     */
    private void handleNotificationClick(NotificationViewModel notification) {
        markAsRead(notification);
        switch (notification.getType()) {
            case "OFFER_RECEIVED", "OFFER_ACCEPTED", "OFFER_REJECTED", "OFFER_WITHDRAWN" -> handleViewOffer(notification);
            case "AUCTION_ENDING_SOON", "AUCTION_WON", "AUCTION_OUTBID" -> handleViewAuction(notification);
            case "PICKUP_SCHEDULED", "PICKUP_REMINDER" -> handleViewPickup(notification);
            default -> System.out.println("Clicked notification: " + notification.getTitle());
        }
    }

    /**
     * @param notification
     */
    private void handleViewOffer(NotificationViewModel notification) {
        try {
            navigationService.navigateToOffersView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to offers: " + e.getMessage());
        }
        System.out.println("Navigate to offer from notification: " + notification.getTitle());
    }

    /**
     * @param notification
     */
    private void handleViewAuction(NotificationViewModel notification) {
        System.out.println("Navigate to auction from notification: " + notification.getTitle());
    }

    /**
     * @param notification
     */
    private void handleViewPickup(NotificationViewModel notification) {
        System.out.println("Navigate to pickup from notification: " + notification.getTitle());
    }

    /**
     *
     */
    @Override
    public void refreshUI() {
        setupLabels();
        updateNotificationDisplays();
    }

    /**
     * @param unreadCount
     */
    public void updateNotificationBadge(int unreadCount) {
        Platform.runLater(() -> {
            System.out.println("Unread notifications: " + unreadCount);
        });
    }
}
