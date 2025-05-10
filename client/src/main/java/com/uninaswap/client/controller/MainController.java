package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.MessageService;
import com.uninaswap.client.service.UserSessionService;

import java.io.IOException;

public class MainController {

    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private StackPane contentArea;
    
    private final NavigationService navigationService;
    private final MessageService messageService;
    private final UserSessionService sessionService;
    
    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.messageService = MessageService.getInstance();
        this.sessionService = UserSessionService.getInstance();
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
     * Updates the displayed username
     */
    public void setUsername(String username) {
        usernameLabel.setText("Welcome, " + username);
    }
}