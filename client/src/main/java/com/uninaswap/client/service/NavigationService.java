package com.uninaswap.client.service;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Window;
import javafx.stage.Modality;

import com.uninaswap.client.controller.ListingDetailsController;
import com.uninaswap.client.controller.LoginController;
import com.uninaswap.client.controller.ProfileController;
import com.uninaswap.client.controller.RegisterController;
import com.uninaswap.client.controller.MainController;

/**
 * Service class to handle navigation between screens.
 * This separates navigation concerns from controller business logic.
 */
public class NavigationService {
    private static NavigationService instance;
    private final LocaleService localeService = LocaleService.getInstance();
    private final ListingService listingService = ListingService.getInstance();

    // Add navigation history stack
    private final java.util.Stack<NavigationState> navigationHistory = new java.util.Stack<>();
    private MainController mainController; // Reference to main controller

    // Navigation state class to store view information
    private static class NavigationState {
        private final Parent view;
        private final String title;

        public NavigationState(Parent view, String title) {
            this.view = view;
            this.title = title;
        }

        public Parent getView() {
            return view;
        }

        public String getTitle() {
            return title;
        }
    }

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
        stage.setScene(new Scene(
                loginView,
                stage.getScene().getWidth(),
                stage.getScene().getHeight()));

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
        stage.setScene(
                new Scene(registerView,
                        stage.getScene().getWidth(),
                        stage.getScene().getHeight()));

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
        stage.setScene(new Scene(
                mainView,
                stage.getScene().getWidth(),
                stage.getScene().getHeight()));

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

        return inventoryView;
    }

    /**
     * Load the notifications view
     */
    public Parent loadAllertsView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/AllertsView.fxml");
        Parent allertsView = loaderBundle.getView();

        return allertsView;
    }

    /**
     * Load the prefered view
     **/
    public Parent loadSavedView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SavedView.fxml");
        Parent savedView = loaderBundle.getView();

        return savedView;
    }

    /**
     * Load the listing creation view
     */
    public Parent loadListingCreationView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/ListingCreationView.fxml");
        Parent listingCreationView = loaderBundle.getView();

        return listingCreationView;
    }

    /**
     * Load the support view
     */
    public Parent loadSupport() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SupportView.fxml");
        Parent supportView = loaderBundle.getView();

        return supportView;
    }

    public Parent loadSettings() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SettingsView.fxml");
        Parent settingsView = loaderBundle.getView();

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

    public void loadListingDetails(String listingId) {
        // Fetch listing details and navigate
        listingService.getListingDetails(listingId)
                .thenAccept(listing -> {
                    Platform.runLater(() -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingDetailsView.fxml"));
                            Parent detailsView = loader.load();

                            ListingDetailsController controller = loader.getController();
                            controller.setListing(listing);

                            loadView(detailsView, "Dettagli Inserzione");
                        } catch (Exception e) {
                            System.err.println("Error loading listing details: " + e.getMessage());
                        }
                    });
                });
    }

    /**
     * Set the main controller reference for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Navigate back to the previous view
     */
    public void goBack() {
        if (!navigationHistory.isEmpty()) {
            // Remove current view from history
            navigationHistory.pop();

            if (!navigationHistory.isEmpty()) {
                // Get previous view
                NavigationState previousState = navigationHistory.peek();

                if (mainController != null) {
                    Platform.runLater(() -> {
                        mainController.setContent(previousState.getView());
                        // Optionally update window title
                        updateWindowTitle(previousState.getTitle());
                    });
                } else {
                    System.err.println("MainController reference not set in NavigationService");
                }
            } else {
                // No more history, go to home
                try {
                    Parent homeView = loadHomeView();
                    loadView(homeView, "Home");
                } catch (IOException e) {
                    System.err.println("Error loading home view: " + e.getMessage());
                }
            }
        } else {
            // No navigation history, go to home
            try {
                Parent homeView = loadHomeView();
                loadView(homeView, "Home");
            } catch (IOException e) {
                System.err.println("Error loading home view: " + e.getMessage());
            }
        }
    }

    /**
     * Load a view and add it to navigation history
     */
    public void loadView(Parent view, String title) {
        if (mainController != null) {
            Platform.runLater(() -> {
                // Add current view to history before navigating
                addToHistory(view, title);

                // Set the new content
                mainController.setContent(view);

                // Update window title
                updateWindowTitle(title);
            });
        } else {
            System.err.println("MainController reference not set in NavigationService");
        }
    }

    /**
     * Add a view to navigation history
     */
    private void addToHistory(Parent view, String title) {
        NavigationState state = new NavigationState(view, title);
        navigationHistory.push(state);

        // Limit history size to prevent memory issues
        if (navigationHistory.size() > 10) {
            // Remove oldest entry
            navigationHistory.remove(0);
        }
    }

    /**
     * Clear navigation history
     */
    public void clearHistory() {
        navigationHistory.clear();
    }

    /**
     * Check if there's a previous view to go back to
     */
    public boolean canGoBack() {
        return navigationHistory.size() > 1;
    }

    /**
     * Update window title (if applicable)
     */
    private void updateWindowTitle(String title) {
        // This depends on your application structure
        // You might want to update the main window title or just ignore this
        System.out.println("Navigation: " + title);
    }
}