package com.uninaswap.client.service;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import com.uninaswap.client.controller.LoginController;
import com.uninaswap.client.controller.RegisterController;
import com.uninaswap.client.websocket.WebSocketClient;

/**
 * Service class to handle navigation between screens.
 * This separates navigation concerns from controller business logic.
 */
public class NavigationService {
    
    private static NavigationService instance;
    
    // Singleton pattern
    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }
    
    private NavigationService() {
        // Private constructor to enforce singleton pattern
    }
    
    /**
     * Navigate to the login screen
     */
    public void navigateToLogin(Node sourceNode, WebSocketClient webSocketClient) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent loginView = loader.load();
        
        // Get the stage from the source node
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Login");
        stage.setScene(new Scene(loginView, 400, 300));
        
        // Pass the WebSocketClient to the controller
        LoginController controller = loader.getController();
        controller.setWebSocketClient(webSocketClient);
    }
    
    /**
     * Navigate to the register screen
     */
    public void navigateToRegister(Node sourceNode, WebSocketClient webSocketClient) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
        Parent registerView = loader.load();
        
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Register");
        stage.setScene(new Scene(registerView, 400, 350));
        
        // Pass the WebSocketClient to the controller
        RegisterController controller = loader.getController();
        controller.setWebSocketClient(webSocketClient);
    }
    
    /**
     * Navigate to the main dashboard screen
     */
    public void navigateToMainDashboard(Node sourceNode, WebSocketClient webSocketClient) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainView = loader.load();
        
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Dashboard");
        stage.setScene(new Scene(mainView, 800, 600));
        
        // If you have a dashboard controller, you can set the WebSocketClient here
        // DashboardController controller = loader.getController();
        // controller.setWebSocketClient(webSocketClient);
    }
    
    /**
     * Convenience method to get Stage from an ActionEvent
     */
    public Stage getStageFromEvent(ActionEvent event) {
        return (Stage)((Node)event.getSource()).getScene().getWindow();
    }
}