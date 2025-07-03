package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class AllertsController implements Initializable {

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
    private VBox notificationsContainer;
    @FXML
    private Button markAllReadButton;
    @FXML
    private Text titleText;
    @FXML
    private ImageView notificationIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupNotificationTabs();
        loadNotifications();
        setupEventHandlers();
    }
//TODO: all
    private void setupNotificationTabs() {
        // Setup tab selection listeners if needed
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
        // Load all notifications
        Platform.runLater(() -> {
            // Mock implementation - replace with actual service calls
            System.out.println("Loading notifications...");
            // Here you would call your notification service
            // notificationService.getAllNotifications()
            // .thenAccept(notifications -> {
            // Platform.runLater(() -> displayNotifications(notifications));
            // });
        });
    }

    private void loadNotificationsForTab(Tab selectedTab) {
        String tabText = selectedTab.getText();

        // Load specific notifications based on tab
        switch (tabText) {
            case "All": // or use the resource bundle key
                loadAllNotifications();
                break;
            case "Purchases & Sales":
                loadPurchaseSaleNotifications();
                break;
            case "Auctions":
                loadAuctionNotifications();
                break;
            case "Social":
                loadSocialNotifications();
                break;
            default:
                loadAllNotifications();
        }
    }

    private void loadAllNotifications() {
        // Implementation for loading all notifications
        System.out.println("Loading all notifications");
    }

    private void loadPurchaseSaleNotifications() {
        // Implementation for loading purchase/sale notifications
        System.out.println("Loading purchase/sale notifications");
    }

    private void loadAuctionNotifications() {
        // Implementation for loading auction notifications
        System.out.println("Loading auction notifications");
    }

    private void loadSocialNotifications() {
        // Implementation for loading social notifications
        System.out.println("Loading social notifications");
    }

    @FXML
    private void handleMarkAllAsRead() {
        // Mark all notifications as read
        System.out.println("Marking all notifications as read");

        // Here you would call your notification service
        // notificationService.markAllAsRead()
        // .thenRun(() -> {
        // Platform.runLater(() -> {
        // // Update UI to reflect read status
        // refreshNotifications();
        // });
        // });
    }

    private void refreshNotifications() {
        // Refresh the notification display
        loadNotifications();
    }

    @FXML
    private void handleNotificationClick() {
        // Handle when a notification is clicked
        System.out.println("Notification clicked");
    }

    // Method to add notification badge updates
    public void updateNotificationBadge(int unreadCount) {
        Platform.runLater(() -> {
            // Update any badge indicators
            if (unreadCount > 0) {
                // Show badge with count
                System.out.println("Unread notifications: " + unreadCount);
            } else {
                // Hide badge
                System.out.println("No unread notifications");
            }
        });
    }
}
