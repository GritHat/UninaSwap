package com.uninaswap.client.controller;

import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.ValidationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ValidationService.ValidationResult;
import com.uninaswap.common.message.AuthMessage;
import com.uninaswap.client.service.UserSessionService;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Hyperlink registerLink;
    
    private final NavigationService navigationService;
    private final AuthenticationService authService;
    private final ValidationService validationService;
    private final LocaleService localeService;
    private final EventBusService eventBus = EventBusService.getInstance();
    
    public LoginController() {
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
            showMessage("login.info.logging", "message-info");
            authService.login(username, password).whenComplete((_, exception) -> {
                if (exception != null) {
                    Platform.runLater(() -> showMessage("login.error.connection", "message-error"));
                }
            });
        } catch (Exception e) {
            showMessage("login.error.connection", "message-error");
        }
    }
    
    @FXML
    public void handleRegisterLink(ActionEvent event) {
        System.out.println("Redirect alla pagina di registrazione");
        
        try {
            // Carica la schermata di registrazione
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/new/fxml_new/RegisterView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("UninaSwap - Registrazione");
            stage.show();
        } catch (IOException e) {
            System.err.println("Errore durante il caricamento della schermata di registrazione: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.LOGIN_RESPONSE) {
                if (response.isSuccess()) {
                    showMessage("login.success", "message-success");
                    
                    // Start user session
                    UserSessionService sessionService = UserSessionService.getInstance();
                    sessionService.startSession(response);
                    
                    // Navigate to main dashboard on successful login
                    try {
                        navigationService.navigateToMainDashboard(usernameField);
                    } catch (Exception e) {
                        System.err.println("Error navigating to main dashboard: " + e.getMessage());
                        e.printStackTrace();
                        showMessage("navigation.error.load.dashboard", "message-error");
                    }
                } else {
                    // Use server's message or fallback
                    String errorMessage = (response.getMessage() != null && !response.getMessage().isEmpty()) 
                        ? response.getMessage() 
                        : localeService.getMessage("login.error.failed");
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
}