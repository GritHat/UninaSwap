package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.NotificationService;
import com.uninaswap.client.viewmodel.NotificationViewModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class NotificationDropdownController {
    
    @FXML private Text titleText;
    @FXML private Button markAllReadBtn;
    @FXML private ScrollPane notificationsScrollPane;
    @FXML private VBox notificationsContainer;
    @FXML private Label noNotificationsLabel;
    @FXML private Button viewAllBtn;
    
    private final LocaleService localeService = LocaleService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final NotificationService notificationService = NotificationService.getInstance();
    
    private Consumer<Void> onCloseCallback;
    
    @FXML
    public void initialize() {
        setupRealtimeUpdates();
        loadNotifications();
    }
    
    private void setupRealtimeUpdates() {
        // Listen for changes to recent notifications
        notificationService.getRecentNotifications().addListener((ListChangeListener<NotificationViewModel>) change -> {
            Platform.runLater(this::updateNotificationsDisplay);
        });
        
        // Set up callback for new notifications
        notificationService.setNewNotificationCallback(notification -> {
            Platform.runLater(() -> {
                // Show brief highlight or animation for new notification
                updateNotificationsDisplay();
            });
        });
    }
    
    public void setOnCloseCallback(Consumer<Void> callback) {
        this.onCloseCallback = callback;
    }
    
    @FXML
    private void handleMarkAllAsRead() {
        notificationService.markAllAsRead()
            .thenAccept(success -> Platform.runLater(() -> {
                if (success) {
                    updateNotificationsDisplay();
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println("Failed to mark all as read: " + ex.getMessage());
                });
                return null;
            });
    }
    
    @FXML
    private void handleViewAll() {
        closeDropdown();
        navigateToNotificationCenter();
    }
    
    private void loadNotifications() {
        notificationService.refreshRecentNotifications();
        updateNotificationsDisplay();
    }
    
    private void updateNotificationsDisplay() {
        notificationsContainer.getChildren().clear();
        
        var recentNotifications = notificationService.getRecentNotifications();
        
        if (recentNotifications.isEmpty()) {
            noNotificationsLabel.setVisible(true);
            noNotificationsLabel.setManaged(true);
            notificationsContainer.getChildren().add(noNotificationsLabel);
        } else {
            noNotificationsLabel.setVisible(false);
            noNotificationsLabel.setManaged(false);
            
            for (NotificationViewModel notification : recentNotifications) {
                VBox notificationItem = createNotificationItem(notification);
                notificationsContainer.getChildren().add(notificationItem);
            }
        }
    }
    
    private VBox createNotificationItem(NotificationViewModel notification) {
        VBox item = new VBox(5);
        item.getStyleClass().addAll("notification-item", notification.isRead() ? "read" : "unread");
        item.setPadding(new Insets(10));
        item.setOnMouseClicked(e -> {
            markAsRead(notification);
            handleNotificationClick(notification);
        });
        
        // Header with title and time
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Notification icon based on type
        ImageView icon = new ImageView(getNotificationIcon(notification.getType()));
        icon.setFitWidth(16);
        icon.setFitHeight(16);
        
        Text title = new Text(notification.getTitle());
        title.getStyleClass().add("notification-title");
        title.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Text time = new Text(formatTime(notification.getCreatedAt()));
        time.getStyleClass().add("notification-time");
        
        // Unread indicator
        if (!notification.isRead()) {
            Region unreadDot = new Region();
            unreadDot.getStyleClass().add("unread-dot");
            unreadDot.setPrefSize(8, 8);
            unreadDot.setMaxSize(8, 8);
            header.getChildren().addAll(icon, title, spacer, time, unreadDot);
        } else {
            header.getChildren().addAll(icon, title, spacer, time);
        }
        
        // Message
        Text message = new Text(notification.getMessage());
        message.getStyleClass().add("notification-message");
        message.setWrappingWidth(300);
        
        item.getChildren().addAll(header, message);
        return item;
    }
    
    private Image getNotificationIcon(String type) {
        String iconPath = switch (type) {
            case "OFFER_RECEIVED", "OFFER_ACCEPTED", "OFFER_REJECTED", "OFFER_WITHDRAWN" -> "/images/icons/offer.png";
            case "AUCTION_ENDING_SOON", "AUCTION_WON", "AUCTION_OUTBID" -> "/images/icons/auction.png";
            case "MESSAGE_RECEIVED" -> "/images/icons/message.png";
            case "PICKUP_SCHEDULED", "PICKUP_REMINDER" -> "/images/icons/pickup.png";
            case "SYSTEM_ANNOUNCEMENT", "PROFILE_UPDATED" -> "/images/icons/system.png";
            default -> "/images/icons/notification.png";
        };
        
        try {
            return new Image(getClass().getResourceAsStream(iconPath));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/images/icons/notification.png"));
        }
    }
    
    private String formatTime(LocalDateTime timestamp) {
        if (timestamp == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        if (timestamp.toLocalDate().equals(now.toLocalDate())) {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return timestamp.format(DateTimeFormatter.ofPattern("dd/MM"));
        }
    }
    
    private void markAsRead(NotificationViewModel notification) {
        if (!notification.isRead()) {
            notificationService.markAsRead(notification.getId())
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        notification.setRead(true);
                    }
                }));
        }
    }
    
    private void handleNotificationClick(NotificationViewModel notification) {
        closeDropdown();
        
        // Navigate based on notification type
        switch (notification.getType()) {
            case "OFFER_RECEIVED", "OFFER_ACCEPTED", "OFFER_REJECTED", "OFFER_WITHDRAWN" -> {
                try {
                    navigationService.navigateToOffersView();
                } catch (Exception e) {
                    System.err.println("Failed to navigate to offers: " + e.getMessage());
                }
            }
            case "AUCTION_ENDING_SOON", "AUCTION_WON", "AUCTION_OUTBID" -> {
                // Navigate to specific auction or user's auction history
                System.out.println("Navigate to auction: " + notification.getTitle());
            }
            case "PICKUP_SCHEDULED", "PICKUP_REMINDER" -> {
                // Navigate to pickup management
                System.out.println("Navigate to pickup: " + notification.getTitle());
            }
            default -> {
                // For system notifications, just mark as read
                System.out.println("Clicked notification: " + notification.getTitle());
            }
        }
    }
    
    private void navigateToNotificationCenter() {
        try {
            navigationService.navigateToNotificationsView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to notification center: " + e.getMessage());
        }
    }
    
    private void closeDropdown() {
        if (onCloseCallback != null) {
            onCloseCallback.accept(null);
        }
    }
}