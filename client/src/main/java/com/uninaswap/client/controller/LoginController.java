package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.message.AuthMessage;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    
    private WebSocketClient webSocketClient;
    private NavigationService navigationService;
    
    public LoginController() {
        this.navigationService = NavigationService.getInstance();
    }
    
    @FXML
    public void initialize() {
        // Only create a new WebSocketClient if we don't already have one
        if (webSocketClient == null) {
            webSocketClient = new WebSocketClient();
            try {
                webSocketClient.connect("ws://localhost:8080/auth");
            } catch (Exception e) {
                messageLabel.setText("Failed to connect to server");
                messageLabel.getStyleClass().add("message-error");
            }
        }
        
        // Always update the message handler
        webSocketClient.setMessageHandler(this::handleAuthResponse);
    }
    
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-error");
            return;
        }
        
        AuthMessage loginRequest = new AuthMessage();
        loginRequest.setType(AuthMessage.Type.LOGIN_REQUEST);
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        try {
            webSocketClient.sendMessage(loginRequest);
            messageLabel.setText("Logging in...");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-info");
        } catch (Exception e) {
            messageLabel.setText("Failed to send login request");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-error");
        }
    }
    
    @FXML
    public void showRegister(ActionEvent event) {
        try {
            navigationService.navigateToRegister(usernameField, webSocketClient);
        } catch (Exception e) {
            messageLabel.setText("Failed to load register view");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-error");
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            messageLabel.getStyleClass().clear();
            
            if (response.getType() == AuthMessage.Type.LOGIN_RESPONSE) {
                if (response.isSuccess()) {
                    messageLabel.setText("Login successful");
                    messageLabel.getStyleClass().add("message-success");
                    
                    // Navigate to main dashboard on successful login
                    try {
                        navigationService.navigateToMainDashboard(usernameField, webSocketClient);
                    } catch (Exception e) {
                        messageLabel.setText("Failed to load dashboard");
                        messageLabel.getStyleClass().add("message-error");
                    }
                } else {
                    messageLabel.setText(response.getMessage());
                    messageLabel.getStyleClass().add("message-error");
                }
            }
        });
    }
    
    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
        
        // When we get a WebSocketClient from elsewhere, always set our handler
        if (webSocketClient != null) {
            webSocketClient.setMessageHandler(this::handleAuthResponse);
            System.out.println("Login controller: Set handler on existing WebSocketClient");
        }
    }
}