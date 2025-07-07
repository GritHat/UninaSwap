package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.Node;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.common.message.AuthMessage;

public class LoginController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final NavigationService navigationService;
    private final AuthenticationService authService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;

    public LoginController() {
        this.navigationService = NavigationService.getInstance();
        this.authService = AuthenticationService.getInstance();
        this.localeService = LocaleService.getInstance();
        this.sessionService = UserSessionService.getInstance();
    }

    @FXML
    public void initialize() {
        // Set message handler
        registerMessageHandler();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        Node source = (Node) event.getSource();

        Platform.runLater(() -> {
            source.setDisable(true);
        });
        source.getScene().getWindow().requestFocus();

        String usernameOrEmail = loginField.getText();
        String password = passwordField.getText();

        if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
            Platform.runLater(() -> {
                source.setDisable(false);
            });
            showMessage("login.error.username.email.required", "message-error");
            return;
        }

        if (password == null || password.isEmpty()) {
            Platform.runLater(() -> {
                source.setDisable(false);
            });
            showMessage("login.error.password.required", "message-error");
            return;
        }

        showMessage("login.info.logging", "message-info");

        authService.login(usernameOrEmail, password)
                .thenRun(() -> {
                    // The actual authentication result will be handled in handleAuthResponse
                })
                .exceptionally(_ -> {
                    Platform.runLater(() -> {
                        showMessage("login.error.connection", "message-error");
                    });
                    return null;
                })
                .whenComplete((_, _) -> {
                    Platform.runLater(() -> {
                        source.setDisable(false);
                    });
                });
    }

    @FXML
    public void showRegister(ActionEvent event) {
        try {
            // Get the source node from the event
            Node sourceNode = (Node) event.getSource();
            navigationService.navigateToRegister(sourceNode);
        } catch (java.io.IOException e) {
            System.err.println("Error navigating to register: " + e.getMessage());
            e.printStackTrace();
            showMessage("navigation.error.load.register", "message-error");
        }
    }

    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.LOGIN_RESPONSE) {
                if (response.isSuccess()) {
                    showMessage("login.success", "message-success");

                    // Start user session
                    sessionService.startSession(response);

                    // Navigate to main dashboard on successful login
                    try {
                        navigationService.navigateToMainDashboard(loginField);
                        navigationService.loadHomeView();
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

        messageLabel.setText(localeService.getMessage(messageKey)); // Uso il localeService diretto
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