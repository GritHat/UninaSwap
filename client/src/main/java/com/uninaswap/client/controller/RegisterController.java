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

public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    
    private WebSocketClient webSocketClient;
    private NavigationService navigationService;
    
    public RegisterController() {
        this.navigationService = NavigationService.getInstance();
    }
    
    @FXML
    public void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-error");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-error");
            return;
        }
        
        AuthMessage registerRequest = new AuthMessage();
        registerRequest.setType(AuthMessage.Type.REGISTER_REQUEST);
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        
        try {
            webSocketClient.sendMessage(registerRequest);
            messageLabel.setText("Registering...");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-info");
            
            webSocketClient.setMessageHandler(this::handleAuthResponse);
        } catch (Exception e) {
            messageLabel.setText("Failed to send register request");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-error");
        }
    }
    
    @FXML
    public void showLogin(ActionEvent event) {
        try {
            navigationService.navigateToLogin(usernameField, webSocketClient);
        } catch (Exception e) {
            messageLabel.setText("Failed to load login view");
            messageLabel.getStyleClass().clear();
            messageLabel.getStyleClass().add("message-error");
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.REGISTER_RESPONSE) {
                messageLabel.getStyleClass().clear();
                if (response.isSuccess()) {
                    messageLabel.setText("Registration successful. You can now login.");
                    messageLabel.getStyleClass().add("message-success");
                } else {
                    messageLabel.setText(response.getMessage());
                    messageLabel.getStyleClass().add("message-error");
                }
            }
        });
    }
    
    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }
}