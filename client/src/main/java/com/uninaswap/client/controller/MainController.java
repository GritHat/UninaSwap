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
        statusLabel.setText(messageService.getMessage("dashboard.status.loaded"));
        connectionStatusLabel.setText(messageService.getMessage("dashboard.status.connected"));
    }
    
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
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
    
    /**
     * Updates the displayed username
     */
    public void setUsername(String username) {
        usernameLabel.setText("Welcome, " + username);
    }
}