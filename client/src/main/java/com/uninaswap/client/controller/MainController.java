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
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.MessageService;
import com.uninaswap.client.service.UserSessionService;

import java.io.IOException;

public class MainController {

    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private StackPane contentArea;
    @FXML private ImageView headerProfileImageView;

    
    private final NavigationService navigationService;
    private final MessageService messageService;
    private final UserSessionService sessionService;
    private final ImageService imageService;
    
    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.messageService = MessageService.getInstance();
        this.sessionService = UserSessionService.getInstance();
        this.imageService = ImageService.getInstance();
    }
    
    @FXML
    public void initialize() {
        checkAuthentication();
        statusLabel.setText(messageService.getMessage("dashboard.status.loaded"));
        connectionStatusLabel.setText(messageService.getMessage("dashboard.status.connected"));
        
        // Display username from session
        if (sessionService.isLoggedIn()) {
            String username = sessionService.getUsername();
            usernameLabel.setText(messageService.getMessage("dashboard.welcome.user", username));
        } else {
            usernameLabel.setText(messageService.getMessage("dashboard.welcome"));
        }
        String profileImagePath = sessionService.get("profileImagePath");
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            loadProfileImageInHeader(profileImagePath);
        } else {
            // Set default profile image
            setDefaultProfileImage();
        }
    }
    
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // End the user session
            sessionService.endSession();
            
            navigationService.navigateToLogin(usernameLabel);
        } catch (Exception e) {
            statusLabel.setText(messageService.getMessage("dashboard.error.logout", e.getMessage()));
        }
    }
    
    // Navigation methods - these would load different content into the contentArea
    @FXML
    public void showDashboard(ActionEvent event) {
        statusLabel.setText(messageService.getMessage("dashboard.view.dashboard"));
        // TODO: Load dashboard content
    }
    
    @FXML
    public void showMarkets(ActionEvent event) {
        statusLabel.setText(messageService.getMessage("dashboard.view.markets"));
        // TODO: Load markets content
    }
    
    @FXML
    public void showPortfolio(ActionEvent event) {
        statusLabel.setText(messageService.getMessage("dashboard.view.portfolio"));
        // TODO: Load portfolio content
    }
    
    @FXML
    public void showTrade(ActionEvent event) {
        statusLabel.setText(messageService.getMessage("dashboard.view.trade"));
        // TODO: Load trade content
    }
    
    @FXML
    public void showSettings(ActionEvent event) {
        statusLabel.setText(messageService.getMessage("dashboard.view.settings"));
        // TODO: Load settings content
    }
    
    @FXML
    public void showProfile(ActionEvent event) {
        statusLabel.setText(messageService.getMessage("dashboard.view.profile"));
        try {
            Parent profileView = navigationService.loadProfileView();
            
            // Replace content area with profile view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(profileView);
        } catch (IOException e) {
            statusLabel.setText(messageService.getMessage("dashboard.error.load.profile"));
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