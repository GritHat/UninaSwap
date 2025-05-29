package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.PasswordField;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.ValidationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ValidationService.ValidationResult;
import com.uninaswap.common.message.AuthMessage;

public class RegisterController {
    
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    
    private final NavigationService navigationService;
    private final AuthenticationService authService;
    private final ValidationService validationService;
    private final LocaleService localeService;
    
    public RegisterController() {
        this.navigationService = NavigationService.getInstance();
        this.authService = AuthenticationService.getInstance();
        this.validationService = ValidationService.getInstance();
        this.localeService = LocaleService.getInstance();
    }
    
    @FXML
    public void initialize() {
        // Set message handler
        registerMessageHandler();
    }
    
    @FXML
    public void handleRegister(ActionEvent event) {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate input
        ValidationResult validationResult = validationService.validateRegistration(
            username, email, password, confirmPassword);
        
        if (!validationResult.isValid()) {
            showMessage(validationResult.getMessageKey(), "message-error");
            return;
        }
        
        try {
            authService.register(firstName, lastName, username, email, password);
            showMessage("register.info.registering", "message-info");
        } catch (Exception e) {
            showMessage("register.error.connection", "message-error");
        }
    }
    
    @FXML
    public void showLogin(ActionEvent event) {
        try {
            navigationService.navigateToLogin(usernameField);
        } catch (Exception e) {
            showMessage("navigation.error.load.login", "message-error");
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.REGISTER_RESPONSE) {
                if (response.isSuccess()) {
                    showMessage("register.success", "message-success");
                } else {
                    // Use server's message or fallback
                    String errorMessage = (response.getMessage() != null && !response.getMessage().isEmpty()) 
                        ? response.getMessage() 
                        : localeService.getMessage("register.error.failed");
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
        messageLabel.setText(localeService.getMessage(messageKey));
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

    @FXML
    public void openTermsAndConditions(MouseEvent event) {
        try {
            // Pass the source of the event to the NavigationService
            navigationService.navigateToTermsAndConditions((javafx.scene.Node) event.getSource());
        } catch (Exception e) {
            System.err.println("Error opening Terms and Conditions: " + e.getMessage());
            e.printStackTrace();
            showMessage("navigation.error.terms", "message-error");
        }
    }
}