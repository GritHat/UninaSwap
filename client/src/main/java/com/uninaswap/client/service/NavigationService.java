package com.uninaswap.client.service;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.stage.Window;
import javafx.stage.Modality;

import com.uninaswap.client.controller.LoginController;
import com.uninaswap.client.controller.ProfileController;
import com.uninaswap.client.controller.RegisterController;
import com.uninaswap.client.controller.MainController;
import com.uninaswap.client.controller.HomeController;
import com.uninaswap.client.controller.InventoryController;
import com.uninaswap.client.controller.AllertsController;
import com.uninaswap.client.controller.SavedController;
import com.uninaswap.client.controller.SettingsController;
import com.uninaswap.client.controller.ListingCreationController;
import com.uninaswap.client.controller.SupportController;

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
     * 
     * @param sourceNode The node that triggered the navigation
     * @param fxmlPath   The path to the FXML file
     * @param title      The title of the new window
     * @param width      The width of the new window
     * @param height     The height of the new window
     */
    private LoaderBundle loadView(String fxmlPath) throws IOException {
        // Check if the path is valid before attempting to load
        if (fxmlPath == null || fxmlPath.isEmpty()) {
            throw new IllegalArgumentException("FXML path cannot be null or empty");
        }

        // Get the resource URL and verify it exists
        java.net.URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("Resource not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
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

        Stage stage;
        if (sourceNode != null && sourceNode.getScene() != null) {
            stage = (Stage) sourceNode.getScene().getWindow();
        } else {
            // If source node is null or doesn't have a scene, try to find the active window
            Window activeWindow = null;
            for (Window window : Stage.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    activeWindow = window;
                    break;
                }
            }

            if (activeWindow != null) {
                stage = (Stage) activeWindow;
            } else {
                // Create a new stage as a fallback
                stage = new Stage();
            }
        }

        stage.setTitle("UninaSwap - Login");
        stage.setScene(new Scene(loginView, 800, 600));

        // Register the controller's message handler
        LoginController controller = loaderBundle.getLoader().getController();
        controller.registerMessageHandler();
    }

    /**
     * Navigate to the register screen
     * 
     * @param sourceNode The node that triggered the navigation
     */
    public void navigateToRegister(Node sourceNode) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/RegisterView.fxml");
        Parent registerView = loaderBundle.getView();

        Stage stage;
        if (sourceNode != null && sourceNode.getScene() != null) {
            // Get the stage from the source node if available
            stage = (Stage) sourceNode.getScene().getWindow();
        } else {
            // If source node is null or doesn't have a scene, try to find the active window
            Window activeWindow = null;
            for (Window window : Stage.getWindows()) {
                if (window instanceof Stage && window.isShowing()) {
                    activeWindow = window;
                    break;
                }
            }

            if (activeWindow != null) {
                stage = (Stage) activeWindow;
            } else {
                // Create a new stage as a fallback
                stage = new Stage();
            }
        }

        stage.setTitle("UninaSwap - Register");
        stage.setScene(new Scene(registerView, 800, 600)); // Using appropriate dimensions for RegisterView

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

        // Initialize the MainController if needed
        MainController mainController = loaderBundle.getLoader().getController();
        mainController.initialize();
    }

    public void navigateToItemDetails(String itemId) {
        // TODO: Implement navigation logic to item details view using itemId
        System.out.println("Navigating to item details for item: " + itemId);
    }

    /**
     * Navigate to the terms and conditions screen
     * 
     * @param sourceNode The node that triggered the navigation
     */
    public void openTermsAndConditions(Node sourceNode) throws IOException {
        // Define the correct path to the Terms and Conditions FXML file
        final String TERMS_FXML_PATH = "/fxml/TermsAndConditionsView.fxml";

        try {
            LoaderBundle loaderBundle = loadView(TERMS_FXML_PATH);
            Parent termsView = loaderBundle.getView();

            Stage termsStage = new Stage();
            termsStage.setTitle("UninaSwap - Termini e Condizioni");
            termsStage.initModality(Modality.APPLICATION_MODAL);

            if (sourceNode != null && sourceNode.getScene() != null && sourceNode.getScene().getWindow() != null) {
                termsStage.initOwner(sourceNode.getScene().getWindow());
            }

            Scene scene = new Scene(termsView, 600, 500);
            termsStage.setScene(scene);
            termsStage.setMinWidth(400);
            termsStage.setMinHeight(300);

            termsStage.show();
        } catch (IOException e) {
            System.err.println("Error loading Terms and Conditions view: " + e.getMessage());
            throw e; // Re-throw to let caller handle it
        }
    }

    /**
     * Load the home view
     */
    public Parent loadHomeView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/HomeView.fxml");
        Parent homeView = loaderBundle.getView();

        HomeController controller = loaderBundle.getLoader().getController();
        return homeView;
    }

    /**
     * Load the profile view
     */
    public Parent loadProfileView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/ProfileView.fxml");
        Parent profileView = loaderBundle.getView();

        ProfileController controller = loaderBundle.getLoader().getController();
        controller.registerMessageHandler();

        return profileView;
    }

    /**
     * Load the inventory view
     */
    public Parent loadInventoryView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/InventoryView.fxml");
        Parent inventoryView = loaderBundle.getView();

        InventoryController controller = loaderBundle.getLoader().getController();

        return inventoryView;
    }

    /**
     * Load the notifications view
     */
    public Parent loadAllertsView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/AllertsView.fxml");
        Parent allertsView = loaderBundle.getView();

        AllertsController controller = loaderBundle.getLoader().getController();

        return allertsView;
    }

    /**
     * Load the prefered view
     **/
    public Parent loadSavedView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SavedView.fxml");
        Parent savedView = loaderBundle.getView();

        SavedController controller = loaderBundle.getLoader().getController();

        return savedView;
    }

    /**
     * Load the listing creation view
     */
    public Parent loadListingCreationView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/ListingCreationView.fxml");
        Parent listingCreationView = loaderBundle.getView();

        ListingCreationController controller = loaderBundle.getLoader().getController();

        return listingCreationView;
    }

    /**
     * Load the support view
     */
    public Parent loadSupport() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SupportView.fxml");
        Parent supportView = loaderBundle.getView();

        SupportController controller = loaderBundle.getLoader().getController();

        return supportView;
    }

    public Parent loadSettings() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SettingsView.fxml");
        Parent settingsView = loaderBundle.getView();

        SettingsController controller = loaderBundle.getLoader().getController();

        return settingsView;
    }

    /**
     * Logout the current user and navigate to the login screen
     */
    public void logout() throws IOException {
        navigateToLogin(null);
        ;
    }

    /**
     * Convenience method to get Stage from an ActionEvent
     */
    public Stage getStageFromEvent(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}