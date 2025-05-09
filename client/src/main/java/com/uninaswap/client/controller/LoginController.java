package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.message.AuthMessage;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    
    private WebSocketClient webSocketClient;
    
    @FXML
    public void initialize() {
        // Only create a new WebSocketClient if we don't already have one
        if (webSocketClient == null) {
            webSocketClient = new WebSocketClient();
            try {
                webSocketClient.connect("ws://localhost:8080/auth");
            } catch (Exception e) {
                messageLabel.setText("Failed to connect to server");
                messageLabel.setStyle("-fx-text-fill: red;");
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
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        AuthMessage loginRequest = new AuthMessage();
        loginRequest.setType(AuthMessage.Type.LOGIN_REQUEST);
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        
        try {
            webSocketClient.sendMessage(loginRequest);
            messageLabel.setText("Logging in...");
            messageLabel.setStyle("-fx-text-fill: blue;");
        } catch (Exception e) {
            messageLabel.setText("Failed to send login request");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    public void showRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
            Parent registerView = loader.load();
            
            Scene currentScene = usernameField.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("UninaSwap - Register");
            stage.setScene(new Scene(registerView, 400, 350));
            
            RegisterController controller = loader.getController();
            controller.setWebSocketClient(webSocketClient);
        } catch (Exception e) {
            messageLabel.setText("Failed to load register view");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.LOGIN_RESPONSE) {
                if (response.isSuccess()) {
                    messageLabel.setText("Login successful");
                    messageLabel.setStyle("-fx-text-fill: green;");
                    // TODO: Navigate to main application view
                } else {
                    messageLabel.setText(response.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red;");
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