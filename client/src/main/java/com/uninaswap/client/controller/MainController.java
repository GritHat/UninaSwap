package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;

import java.io.IOException;

public class MainController {

    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private StackPane contentArea;
    @FXML private ImageView headerProfileImageView;

    
    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final ImageService imageService;
    
    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.localeService = LocaleService.getInstance();
        this.sessionService = UserSessionService.getInstance();
        this.imageService = ImageService.getInstance();
    }
    
    @FXML
    public void initialize() {
        checkAuthentication();
        statusLabel.setText(localeService.getMessage("dashboard.status.loaded"));
        connectionStatusLabel.setText(localeService.getMessage("dashboard.status.connected"));
        
        // Display username from session
        if (sessionService.isLoggedIn()) {
            String username = sessionService.getUser().getUsername();
            usernameLabel.setText(localeService.getMessage("dashboard.welcome.user", username));
        } else {
            usernameLabel.setText(localeService.getMessage("dashboard.welcome"));
        }
        String profileImagePath = sessionService.getUser().getProfileImagePath();
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            loadProfileImageInHeader(profileImagePath);
        } else {
            // Set default profile image
            setDefaultProfileImage();
        }
        // Subscribe to profile image change events
        EventBusService.getInstance().subscribe("PROFILE_IMAGE_CHANGED", data -> {
            if (data instanceof String) {
                updateProfileImage((String) data);
            }
        });
    }
    
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // End the user session
            sessionService.endSession();
            
            navigationService.navigateToLogin(usernameLabel);
        } catch (Exception e) {
            statusLabel.setText(localeService.getMessage("dashboard.error.logout", e.getMessage()));
        }
    }
    
    // Navigation methods - these would load different content into the contentArea
    @FXML
    public void showDashboard(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.dashboard"));
        // TODO: Load dashboard content
    }
    
    @FXML
    public void showMarkets(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.markets"));
        // TODO: Load markets content
    }
    
    @FXML
    public void showPortfolio(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.portfolio"));
        // TODO: Load portfolio content
    }
    
    @FXML
    public void showTrade(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.trade"));
        // TODO: Load trade content
    }
    
    @FXML
    public void showSettings(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.settings"));
        // TODO: Load settings content
        
    }
    
    @FXML
    public void showProfile(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.profile"));
        try {
            Parent profileView = navigationService.loadProfileView();
            
            // Replace content area with profile view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(profileView);
        } catch (IOException e) {
            statusLabel.setText(localeService.getMessage("dashboard.error.load.profile"));
        }
    }
    
    @FXML
    public void showInventory(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.inventory"));
        try {
            Parent inventoryView = navigationService.loadInventoryView();
            
            // Replace content area with inventory view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(inventoryView);
        } catch (IOException e) {
            statusLabel.setText(localeService.getMessage("dashboard.error.load.inventory"));
            e.printStackTrace();
        }
    }

    @FXML
    public void showCreateListing(ActionEvent event) {
        statusLabel.setText("Creating New Listing");
        try {
            Parent listingCreationView = navigationService.loadListingCreationView();
            
            // Replace content area with listing creation view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(listingCreationView);
        } catch (IOException e) {
            statusLabel.setText("Failed to load listing creation view");
            e.printStackTrace();
        }
    }

    /**
     * Verify that a user is logged in, redirect to login if not
     * Call this from initialize() in protected controllers
     */
    private void checkAuthentication() {
        if (!UserSessionService.getInstance().isLoggedIn()) {
            Platform.runLater(() -> {
                try {
                    navigationService.navigateToLogin(usernameLabel);
                } catch (Exception e) {
                    // Log error
                }
            });
        }
    }

    /**
     * Loads user profile image in the header
     */
    private void loadProfileImageInHeader(String imagePath) {
        imageService.fetchImage(imagePath)
            .thenAccept(image -> {
                Platform.runLater(() -> {
                    headerProfileImageView.setImage(image);
                    
                    // Apply circular clip to the image
                    Circle clip = new Circle(16, 16, 16); // 32/2=16
                    headerProfileImageView.setClip(clip);
                });
            })
            .exceptionally(ex -> {
                // If loading fails, set default image
                System.err.println("Failed to load profile image: " + ex.getMessage());
                Platform.runLater(this::setDefaultProfileImage);
                return null;
            });
    }

    /**
     * Sets a default profile image when no custom image is available
     */
    private void setDefaultProfileImage() {
        // Load default image from resources
        Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
        headerProfileImageView.setImage(defaultImage);
        
        // Apply circular clip
        Circle clip = new Circle(16, 16, 16);
        headerProfileImageView.setClip(clip);
    }

    /**
     * Update profile image when profile is changed elsewhere
     */
    public void updateProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            loadProfileImageInHeader(imagePath);
        }
    }
}