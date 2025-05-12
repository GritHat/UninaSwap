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
    
    private class LoaderBundle {
        private FXMLLoader loader;
        private Parent view;
        public LoaderBundle(FXMLLoader loader, Parent view) {
            this.loader = loader;
            this.view = view;
        }
        public FXMLLoader getLoader() {
            return loader;
        }
        public Parent getView() {
            return view;
        }
    }

    /**
     * Navigate to the specified view
     * @param sourceNode The node that triggered the navigation
     * @param fxmlPath The path to the FXML file
     * @param title The title of the new window
     * @param width The width of the new window
     * @param height The height of the new window
     */
    private LoaderBundle loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.setResources(localeService.getResourceBundle());
        Parent root = loader.load();
        
        // Store the controller in the view's properties
        Object controller = loader.getController();
        root.getProperties().put("controller", controller);
        
        // Also store the ID for easy identification
        String viewName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1, fxmlPath.lastIndexOf('.'));
        root.setId(viewName);
        
        return new LoaderBundle(loader, root);
    }

    /**
     * Navigate to the login screen
     */
    public void navigateToLogin(Node sourceNode) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/LoginView.fxml");
        Parent loginView = loaderBundle.getView();
        // Get the stage from the source node
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Login");
        stage.setScene(new Scene(loginView, 400, 300));
        
        // Register the controller's message handler
        LoginController controller = loaderBundle.getLoader().getController();
        controller.registerMessageHandler();
    }
    
    /**
     * Navigate to the register screen
     */
    public void navigateToRegister(Node sourceNode) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/RegisterView.fxml");
        Parent registerView = loaderBundle.getView();
        
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Register");
        stage.setScene(new Scene(registerView, 400, 350));
        
        // Register the controller's message handler
        RegisterController controller = loaderBundle.getLoader().getController();
        controller.registerMessageHandler();
    }
    
    /**
     * Navigate to the main dashboard screen
     */
    public void navigateToMainDashboard(Node sourceNode) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/MainView.fxml");
        Parent mainView = loaderBundle.getView();
        
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setTitle("UninaSwap - Dashboard");
        stage.setScene(new Scene(mainView, 1600, 900));
    }
    
    /**
     * Navigate to the profile view within the content area of the main dashboard
     */
    public Parent loadProfileView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/ProfileView.fxml");
        Parent profileView = loaderBundle.getView();
        
        // Register the controller's message handler
        ProfileController controller = loaderBundle.getLoader().getController();
        controller.registerMessageHandler();
        
        return profileView;
    }
    
    /**
     * Load the inventory view
     */
    public Parent loadInventoryView() throws IOException {
        return loadView("/fxml/InventoryView.fxml").getView();
    }

    /**
     * Load the listing creation view
     */
    public Parent loadListingCreationView() throws IOException {
        return loadView("/fxml/ListingCreationView.fxml").getView();
    }

    /**
     * Convenience method to get Stage from an ActionEvent
     */
    public Stage getStageFromEvent(ActionEvent event) {
        return (Stage)((Node)event.getSource()).getScene().getWindow();
    }
}