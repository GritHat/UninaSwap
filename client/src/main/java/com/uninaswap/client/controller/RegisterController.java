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

public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    
    private WebSocketClient webSocketClient;
    
    @FXML
    public void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            messageLabel.setStyle("-fx-text-fill: red;");
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
            messageLabel.setStyle("-fx-text-fill: blue;");
            
            webSocketClient.setMessageHandler(this::handleAuthResponse);
        } catch (Exception e) {
            messageLabel.setText("Failed to send register request");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    public void showLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginView = loader.load();
            
            Scene currentScene = usernameField.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle("UninaSwap - Login");
            stage.setScene(new Scene(loginView, 400, 300));
            
            LoginController controller = loader.getController();
            controller.setWebSocketClient(webSocketClient);
        } catch (Exception e) {
            messageLabel.setText("Failed to load login view");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void handleAuthResponse(AuthMessage response) {
        Platform.runLater(() -> {
            if (response.getType() == AuthMessage.Type.REGISTER_RESPONSE) {
                if (response.isSuccess()) {
                    messageLabel.setText("Registration successful. You can now login.");
                    messageLabel.setStyle("-fx-text-fill: green;");
                } else {
                    messageLabel.setText(response.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red;");
                }
            }
        });
    }
    
    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }
}