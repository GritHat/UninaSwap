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
import com.uninaswap.client.controller.ProfileController;

/**
 * Service class to handle navigation between screens.
 * This separates navigation concerns from controller business logic.
 */
public class NavigationService {
    
    private static NavigationService instance;
    private final LocaleService localeService = LocaleService.getInstance();
    
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
    public void navigateToLogin(Node sourceNode) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        loader.setResources(localeService.getResourceBundle());
        Parent loginView = loader.load();
        
        // Get the stage from the source node
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Login");
        stage.setScene(new Scene(loginView, 400, 300));
        
        // Register the controller's message handler
        LoginController controller = loader.getController();
        controller.registerMessageHandler();
    }
    
    /**
     * Navigate to the register screen
     */
    public void navigateToRegister(Node sourceNode) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
        loader.setResources(localeService.getResourceBundle());
        Parent registerView = loader.load();
        
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Register");
        stage.setScene(new Scene(registerView, 400, 350));
        
        // Register the controller's message handler
        RegisterController controller = loader.getController();
        controller.registerMessageHandler();
    }
    
    /**
     * Navigate to the main dashboard screen
     */
    public void navigateToMainDashboard(Node sourceNode) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setResources(localeService.getResourceBundle());
        Parent mainView = loader.load();
        
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Dashboard");
        stage.setScene(new Scene(mainView, 1600, 900));
    }
    
    /**
     * Navigate to the profile view within the content area of the main dashboard
     */
    public Parent loadProfileView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProfileView.fxml"));
        loader.setResources(localeService.getResourceBundle());
        Parent profileView = loader.load();
        
        // Register the controller's message handler
        ProfileController controller = loader.getController();
        controller.registerMessageHandler();
        
        return profileView;
    }
    
    /**
     * Load the inventory view
     */
    public Parent loadInventoryView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InventoryView.fxml"));
        loader.setResources(localeService.getResourceBundle());
        return loader.load();
    }

    /**
     * Load the listing creation view
     */
    public Parent loadListingCreationView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingCreationView.fxml"));
        loader.setResources(localeService.getResourceBundle());
        return loader.load();
    }

    /**
     * Convenience method to get Stage from an ActionEvent
     */
    public Stage getStageFromEvent(ActionEvent event) {
        return (Stage)((Node)event.getSource()).getScene().getWindow();
    }
}