package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.ValidationService;
import com.uninaswap.client.service.MessageService;
import com.uninaswap.client.service.ValidationService.ValidationResult;
import com.uninaswap.common.message.AuthMessage;
import com.uninaswap.client.service.UserSessionService;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    
    private final NavigationService navigationService;
    private final AuthenticationService authService;
    private final ValidationService validationService;
    private final MessageService messageService;
    
    public LoginController() {
        this.navigationService = NavigationService.getInstance();
        this.authService = AuthenticationService.getInstance();
        this.validationService = ValidationService.getInstance();
        this.messageService = MessageService.getInstance();
    }
    
    @FXML
    public void initialize() {
        // Set message handler
        authService.setAuthResponseHandler(this::handleAuthResponse);
    }
    
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Validate input
        ValidationResult validationResult = validationService.validateLogin(username, password);
        if (!validationResult.isValid()) {
            showMessage(validationResult.getMessageKey(), "message-error");
            return;
        }
        
        try {
            authService.login(username, password);
            showMessage("login.info.logging", "message-info");
        } catch (Exception e) {
            showMessage("login.error.connection", "message-error");
        }
    }
    
    @FXML
    public void showRegister(ActionEvent event) {
        try {
            navigationService.navigateToRegister(usernameField);
        } catch (Exception e) {
            showMessage("navigation.error.load.register", "message-error");
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.LOGIN_RESPONSE) {
                if (response.isSuccess()) {
                    showMessage("login.success", "message-success");
                    
                    // Start user session
                    UserSessionService sessionService = UserSessionService.getInstance();
                    sessionService.startSession(response.getUsername());
                    
                    // Navigate to main dashboard on successful login
                    try {
                        navigationService.navigateToMainDashboard(usernameField);
                    } catch (Exception e) {
                        showMessage("navigation.error.load.dashboard", "message-error");
                    }
                } else {
                    // Use server's message or fallback
                    String errorMessage = (response.getMessage() != null && !response.getMessage().isEmpty()) 
                        ? response.getMessage() 
                        : messageService.getMessage("login.error.failed");
                    messageLabel.setText(errorMessage);
                    messageLabel.getStyleClass().clear();
                    messageLabel.getStyleClass().add("message-error");
                }
            }
        });
    }
    
    /**
     * Helper method to display messages
     */
    private void showMessage(String messageKey, String styleClass) {
        messageLabel.setText(messageService.getMessage(messageKey));
        messageLabel.getStyleClass().clear();
        messageLabel.getStyleClass().add(styleClass);
    }

    /**
     * Registers this controller's message handler with the AuthenticationService.
     * Called by NavigationService when this view is loaded.
     */
    public void registerMessageHandler() {
        authService.setAuthResponseHandler(this::handleAuthResponse);
    }
}