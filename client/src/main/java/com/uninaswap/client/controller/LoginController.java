package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Hyperlink;
import javafx.scene.Node;
import javafx.scene.text.Text;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.AuthenticationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.Refreshable;
import com.uninaswap.common.message.AuthMessage;

public class LoginController implements Refreshable {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;
    @FXML
    private Text titleText;
    @FXML
    private Hyperlink forgotPasswordLink;
    @FXML
    private Hyperlink registerLink;
    @FXML
    private Text noAccountText;

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
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("login.debug.initialized", "Login controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update form placeholders
        if (loginField != null) {
            loginField.setPromptText(localeService.getMessage("login.username.email.prompt", "Username or Email"));
        }
        if (passwordField != null) {
            passwordField.setPromptText(localeService.getMessage("login.password.prompt", "Password"));
        }
        
        // Update static text elements
        if (titleText != null) {
            titleText.setText(localeService.getMessage("login.title", "Login"));
        }
        if (forgotPasswordLink != null) {
            forgotPasswordLink.setText(localeService.getMessage("login.forgot.password", "Forgot password?"));
        }
        if (noAccountText != null) {
            noAccountText.setText(localeService.getMessage("login.no.account", "Don't have an account?"));
        }
        if (registerLink != null) {
            registerLink.setText(localeService.getMessage("button.register", "Register"));
        }
        
        // Clear any existing message
        if (messageLabel != null) {
            messageLabel.setText("");
        }
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
            System.err.println(localeService.getMessage("login.error.navigate.register", "Error navigating to register: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
            showMessage("navigation.error.load.register", "message-error");
        }
    }

    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.AUTH_ERROR_RESPONSE && !response.isSuccess()) {
                try {
                    navigationService.logout();
                    return;
                } catch (Exception e) {
                    System.err.println(localeService.getMessage("login.error.logout", "Error during logout: {0}").replace("{0}", e.getMessage()));
                }
            }
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
                        System.err.println(localeService.getMessage("login.error.navigate.dashboard", "Error navigating to main dashboard: {0}").replace("{0}", e.getMessage()));
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