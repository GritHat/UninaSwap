package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import javafx.application.Platform;
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
import java.util.List;
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
    
    private Consumer<Void> onCloseCallback;
    
    @FXML
    public void initialize() {
        loadNotifications();
    }
    
    public void setOnCloseCallback(Consumer<Void> callback) {
        this.onCloseCallback = callback;
    }
    
    @FXML
    private void handleMarkAllAsRead() {
        // TODO: Implement mark all as read
        System.out.println("Marking all notifications as read");
        // Refresh after marking as read
        loadNotifications();
    }
    
    @FXML
    private void handleViewAll() {
        closeDropdown();
        navigateToNotificationCenter();
    }
    
    private void loadNotifications() {
        // TODO: Replace with actual notification service call
        Platform.runLater(() -> {
            List<NotificationItem> notifications = getMockNotifications();
            updateNotifications(notifications);
        });
    }
    
    private void updateNotifications(List<NotificationItem> notifications) {
        notificationsContainer.getChildren().clear();
        
        if (notifications.isEmpty()) {
            noNotificationsLabel.setVisible(true);
            noNotificationsLabel.setManaged(true);
            notificationsContainer.getChildren().add(noNotificationsLabel);
        } else {
            noNotificationsLabel.setVisible(false);
            noNotificationsLabel.setManaged(false);
            
            for (NotificationItem notification : notifications) {
                VBox notificationItem = createNotificationItem(notification);
                notificationsContainer.getChildren().add(notificationItem);
            }
        }
    }
    
    private VBox createNotificationItem(NotificationItem notification) {
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
        
        Text time = new Text(formatTime(notification.getTimestamp()));
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
            case "OFFER" -> "/images/icons/offer.png";
            case "AUCTION" -> "/images/icons/auction.png";
            case "MESSAGE" -> "/images/icons/message.png";
            case "SYSTEM" -> "/images/icons/system.png";
            default -> "/images/icons/notification.png";
        };
        
        try {
            return new Image(getClass().getResourceAsStream(iconPath));
        } catch (Exception e) {
            return new Image(getClass().getResourceAsStream("/images/icons/notification.png"));
        }
    }
    
    private String formatTime(LocalDateTime timestamp) {
        LocalDateTime now = LocalDateTime.now();
        if (timestamp.toLocalDate().equals(now.toLocalDate())) {
            return timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
        } else {
            return timestamp.format(DateTimeFormatter.ofPattern("dd/MM"));
        }
    }
    
    private void markAsRead(NotificationItem notification) {
        notification.setRead(true);
        // TODO: Update notification status on server
    }
    
    private void handleNotificationClick(NotificationItem notification) {
        closeDropdown();
        // TODO: Navigate to relevant content based on notification type
        System.out.println("Clicked notification: " + notification.getTitle());
    }
    
    private void navigateToNotificationCenter() {
        try {
            // TODO: Implement navigation to notification center
            System.out.println("Navigating to notification center");
        } catch (Exception e) {
            System.err.println("Failed to navigate to notification center: " + e.getMessage());
        }
    }
    
    private void closeDropdown() {
        if (onCloseCallback != null) {
            onCloseCallback.accept(null);
        }
    }
    
    // Mock data - replace with actual service
    private List<NotificationItem> getMockNotifications() {
        return List.of(
            new NotificationItem("OFFER", "New Offer Received", "You received a new offer for 'Vintage Camera'", LocalDateTime.now().minusMinutes(5), false),
            new NotificationItem("AUCTION", "Auction Ending Soon", "Your auction for 'Antique Watch' ends in 2 hours", LocalDateTime.now().minusHours(1), false),
            new NotificationItem("MESSAGE", "New Message", "You have a new message from @johndoe", LocalDateTime.now().minusHours(3), true),
            new NotificationItem("SYSTEM", "Profile Updated", "Your profile information has been successfully updated", LocalDateTime.now().minusDays(1), true)
        );
    }
    
    // Notification data class
    public static class NotificationItem {
        private final String type;
        private final String title;
        private final String message;
        private final LocalDateTime timestamp;
        private boolean read;
        
        public NotificationItem(String type, String title, String message, LocalDateTime timestamp, boolean read) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.read = read;
        }
        
        // Getters and setters
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }
}