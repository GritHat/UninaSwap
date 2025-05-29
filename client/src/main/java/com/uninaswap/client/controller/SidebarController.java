package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import com.uninaswap.client.service.NavigationService;
import javafx.scene.image.ImageView;

public class SidebarController {
    
    @FXML private VBox sidebar;
    @FXML private ImageView profileIcon;
    @FXML private ImageView homeIcon;
    @FXML private ImageView inventoryIcon;
    @FXML private ImageView notificationsIcon;
    @FXML private ImageView settingsIcon;
    @FXML private ImageView supportIcon;
    @FXML private ImageView logoutIcon;
    
    private final NavigationService navigationService = NavigationService.getInstance();
    
    @FXML
    public void initialize() {
        // Initialization code if needed
    }
    
    @FXML
    public void showProfile(MouseEvent event) { 
        try {
            navigationService.loadProfileView();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }
    
    @FXML
    public void showHome(MouseEvent event) {
        try {
            navigationService.navigateToMainDashboard((javafx.scene.Node) event.getSource());
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }
    
    @FXML
    public void showInventory(MouseEvent event) {
        try {
            navigationService.loadInventoryView();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }
    
    @FXML
    public void showNotifications(MouseEvent event) {
        try {
            navigationService.loadNotificationsView();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }
    
    @FXML
    public void showSettings(MouseEvent event) {
        try {
            navigationService.navigateToSettings();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }
    
    @FXML
    public void showSupport(MouseEvent event) {
        try {
            navigationService.navigateToSupport();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }
    
    @FXML
    public void logout(MouseEvent event) {
        try {
            navigationService.logout();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Optionally, show an error dialog to the user
        }
    }
    
    @FXML
    public void handleMouseMove(MouseEvent event) {
        // Get the source of the event (the ImageView that was hovered)
        ImageView source = (ImageView) event.getSource();
        
        // Apply a visual effect to indicate hover state
        source.setScaleX(1.2);
        source.setScaleY(1.2);
        source.setOpacity(0.8);
    }
    
    @FXML
    public void handleMouseExit(MouseEvent event) {
        // Reset the visual effect when mouse exits
        ImageView source = (ImageView) event.getSource();
        
        source.setScaleX(1.0);
        source.setScaleY(1.0);
        source.setOpacity(1.0);
    }
}
