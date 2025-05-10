package com.uninaswap.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.MessageService;

public class MainController {

    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private StackPane contentArea;
    
    private final NavigationService navigationService;
    private final AuthenticationService authService;
    private final MessageService messageService;
    
    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.authService = AuthenticationService.getInstance();
        this.messageService = MessageService.getInstance();
    }
    
    @FXML
    public void initialize() {
        // TODO: Set username
        statusLabel.setText("Dashboard loaded");
    }
    
    @FXML
    public void handleLogout(ActionEvent event) {
        // TODO: Implement logout functionality
        try {
            navigationService.navigateToLogin(usernameLabel);
        } catch (Exception e) {
            statusLabel.setText("Error logging out: " + e.getMessage());
        }
    }
    
    // Navigation methods - these would load different content into the contentArea
    @FXML
    public void showDashboard(ActionEvent event) {
        statusLabel.setText("Dashboard view selected");
        // TODO: Load dashboard content
    }
    
    @FXML
    public void showMarkets(ActionEvent event) {
        statusLabel.setText("Markets view selected");
        // TODO: Load markets content
    }
    
    @FXML
    public void showPortfolio(ActionEvent event) {
        statusLabel.setText("Portfolio view selected");
        // TODO: Load portfolio content
    }
    
    @FXML
    public void showTrade(ActionEvent event) {
        statusLabel.setText("Trade view selected");
        // TODO: Load trade content
    }
    
    @FXML
    public void showSettings(ActionEvent event) {
        statusLabel.setText("Settings view selected");
        // TODO: Load settings content
    }
    
    /**
     * Updates the displayed username
     */
    public void setUsername(String username) {
        usernameLabel.setText("Welcome, " + username);
    }
}