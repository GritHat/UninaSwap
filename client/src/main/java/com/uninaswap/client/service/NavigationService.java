package com.uninaswap.client.service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Window;
import javafx.stage.Modality;
import javafx.stage.Popup;

import com.uninaswap.client.controller.ItemDialogController;
import com.uninaswap.client.controller.ListingDetailsController;
import com.uninaswap.client.controller.ListingsController;
import com.uninaswap.client.controller.LoginController;
import com.uninaswap.client.controller.ProfileController;
import com.uninaswap.client.controller.RegisterController;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.client.controller.MainController;
import com.uninaswap.client.controller.NotificationDropdownController;
import com.uninaswap.client.controller.PickupSchedulingController;
import com.uninaswap.client.controller.PickupSelectionController;
import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.client.controller.ReviewCreateController;
import com.uninaswap.client.controller.UserFavoritesController;
import com.uninaswap.client.controller.UserFollowersController;
import com.uninaswap.client.controller.UserMenuDropdownController;
import com.uninaswap.client.controller.UserReviewsController;
import com.uninaswap.client.controller.ReportDialogController;

/**
 * Service class to handle navigation between screens.
 * This separates navigation concerns from controller business logic.
 */
public class NavigationService {
    private static NavigationService instance;
    private final LocaleService localeService = LocaleService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final ItemService itemService = ItemService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();
    private final EventBusService eventBus = EventBusService.getInstance();
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

    public void navigateToInventoryView() throws IOException {
        Parent inventoryView = loadInventoryView();
        mainController.setContent(inventoryView);
        mainController.updateSidebarButtonSelection("inventory");
    }

    /**
     * Load the notifications view
     */
    public Parent loadNotificationsView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/NotificationsView.fxml");
        Parent notificationsView = loaderBundle.getView();

        return notificationsView;
    }

    public void navigateToNotificationsView() throws IOException {
        mainController.setContent(loadNotificationsView());
        mainController.updateSidebarButtonSelection("notifications");
    }

    /**
     * Load the listing creation view
     */
    public Parent loadListingCreationView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/ListingCreationView.fxml");
        Parent listingCreationView = loaderBundle.getView();

        return listingCreationView;
    }

    public void navigateToListingCreationView() throws IOException {
        mainController.setContent(loadListingCreationView());
        mainController.sidebarClearAllSelection();
    }

    /**
     * Load the support view
     */
    public Parent loadSupportView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SupportView.fxml");
        Parent supportView = loaderBundle.getView();

        return supportView;
    }

    public void navigateToSupportView() throws IOException {
        Parent supportView = loadSupportView();
        mainController.setContent(supportView);
        mainController.updateSidebarButtonSelection("support");
        mainController.sidebarClearAllSelection();
    }

    public Parent loadSettingsView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/SettingsView.fxml");
        Parent settingsView = loaderBundle.getView();

        return settingsView;
    }

    public void navigateToSettingsView() throws IOException {
        Parent settingsView = loadSettingsView();
        mainController.setContent(settingsView);
        mainController.updateSidebarButtonSelection("settings");
        mainController.sidebarClearAllSelection();
    }

    public Parent loadUserFollowersView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/UserFollowersView.fxml");
        Parent userFollowersView = loaderBundle.getView();

        return userFollowersView;
    }

    public void navigateToUserFollowersView() throws IOException {
        Parent userFollowersView = loadUserFollowersView();
        mainController.setContent(userFollowersView);
        mainController.sidebarClearAllSelection();
    }

    public Parent loadUserFavoritesView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/UserFavoritesView.fxml");
        Parent userFavoritesView = loaderBundle.getView();

        return userFavoritesView;
    }

    public void navigateToUserFavoritesView() throws IOException {
        Parent userFavoritesView = loadUserFavoritesView();
        mainController.setContent(userFavoritesView);
        mainController.sidebarClearAllSelection();
    }


    /**
     * Logout the current user and navigate to the login screen
     */
    public void logout() throws IOException {
        sessionService.endSession();
        eventBus.clearAllSubscriptions();
        navigateToLogin(null);
    }

    /**
     * Convenience method to get Stage from an ActionEvent
     */
    public Stage getStageFromEvent(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    /**
     * Load offers view
     */
    public Parent loadOffersView() throws IOException {

        LoaderBundle loaderBundle = loadView("/fxml/OffersView.fxml");
        Parent offersView = loaderBundle.getView();

        return offersView;
    }

    public void navigateToOffersView() throws IOException {
        Parent offersView = loadOffersView();
        mainController.setContent(offersView);
        mainController.updateSidebarButtonSelection("offers");
    }

    public void navigateToProfileView() throws IOException {
        mainController.setContent(loadProfileView());
        mainController.updateSidebarButtonSelection("profile");
    }

    public Parent loadListingDetails(ListingViewModel listing) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/ListingDetailsView.fxml");
        Parent detailsView = loaderBundle.getView();

        ListingDetailsController controller = loaderBundle.getLoader().getController();
        controller.setListing(listing);
        return detailsView;
    }

    public void navigateToListingDetails(ListingViewModel listing) throws IOException {
        mainController.setContent(loadListingDetails(listing));
        mainController.sidebarClearAllSelection();
    }

    public Parent loadListingsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingsView.fxml"));
        Parent listingsView = loader.load();
        
        return listingsView;
    }

    public void navigateToListingsView() throws IOException {
        Parent listingsView = loadListingsView();
        mainController.setContent(listingsView);
        mainController.updateSidebarButtonSelection("listings");
    }

    public Parent loadUserDropdownMenu(Popup userMenuPopup) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/UserMenuDropdownView.fxml");
        Parent userMenuDropdown = loaderBundle.getView();
        UserMenuDropdownController controller = loaderBundle.getLoader().getController();
        controller.setOnCloseCallback(_ -> {
            if (userMenuPopup != null) {
                userMenuPopup.hide();
            }
        });
            
        return userMenuDropdown;
    }

    public Parent loadNotificationDropdownMenu(Popup notificationPopup) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/NotificationDropdownView.fxml");
        Parent notificationDropdown = loaderBundle.getView();
        NotificationDropdownController controller = loaderBundle.getLoader().getController();
        controller.setOnCloseCallback(_ -> {
            if (notificationPopup != null) {
                notificationPopup.hide();
            }
        });
        return notificationDropdown;
    }
    
    /**
     * Set the main controller reference for navigation
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void openItemDialog(ItemViewModel itemViewModel) {
        try {
            ItemDTO item = ViewModelMapper.getInstance().toDTO(itemViewModel);
            // Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemDialogView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            DialogPane dialogPane = loader.load();
            // Create custom button types with localized text
            ButtonType confirmButtonType = new ButtonType(
                    localeService.getMessage("button.confirm"),
                    ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(
                    localeService.getMessage("button.cancel"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);
            // Get the controller
            ItemDialogController controller = loader.getController();

            // Set up the item in the controller
            controller.setItem(item);

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(localeService.getMessage(item.getId() == null ? "item.add.title" : "item.edit.title"));
            dialog.setHeaderText(
                    localeService.getMessage(item.getId() == null ? "item.add.header" : "item.edit.header"));
            dialog.setDialogPane(dialogPane);

            // Show the dialog and handle result
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
                ItemDTO updatedItem = controller.getUpdatedItem();
                File selectedImageFile = controller.getSelectedImageFile();

                // If we have a new image, upload it first using HTTP
                if (selectedImageFile != null) {
                    imageService.uploadImageViaHttp(selectedImageFile)
                            .thenAccept(imagePath -> {
                                updatedItem.setImagePath(imagePath);
                                itemService.saveItem(updatedItem);
                            })
                            .exceptionally(ex -> {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("item.image.upload.error.title"),
                                        localeService.getMessage("item.image.upload.error.header"),
                                        ex.getMessage());
                                return null;
                            });
                } else {
                    // No new image, just save the item
                    itemService.saveItem(updatedItem);
                }
            }
        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("item.dialog.error.title"),
                    localeService.getMessage("item.dialog.error.header"),
                    e.getMessage());
        }
    }

    public void openPickupScheduling(OfferViewModel offer, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PickupSchedulingView.fxml"));
            Parent root = loader.load();

            PickupSchedulingController controller = loader.getController();
            controller.setOfferId(offer.getId());

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("pickup.scheduling.window.title", "Schedule Pickup"));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);

            // Center on parent window
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open pickup scheduling"),
                    e.getMessage());
        }
    }

    public void openPickupSelection(PickupDTO pickup, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PickupSelectionView.fxml"));
            Parent root = loader.load();

            PickupSelectionController controller = loader.getController();
            controller.setPickup(pickup);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("pickup.selection.window.title", "Select Pickup Time"));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open pickup selection"),
                    e.getMessage());
        }
    }

    public void openReviewCreate(OfferViewModel offer, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReviewCreateView.fxml"));
            Parent root = loader.load();

            ReviewCreateController controller = loader.getController();
            controller.setOffer(offer);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("review.create.window.title", "Write Review"));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open review creation"),
                    e.getMessage());
        }
    }

    public void openUserReviews(UserViewModel user, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserReviewsView.fxml"));
            Parent root = loader.load();

            UserReviewsController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("reviews.window.title", "User Reviews"));
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open user reviews"),
                    e.getMessage());
        }
    }

    public void openReportDialog(UserViewModel user, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportDialogView.fxml"));
            Parent root = loader.load();

            ReportDialogController controller = loader.getController();
            controller.setReportedUser(user);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("report.dialog.window.title", "Report User"));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open report dialog"),
                    e.getMessage());
        }
    }

    public void openReportDialog(ListingViewModel listing, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportDialogView.fxml"));
            Parent root = loader.load();

            ReportDialogController controller = loader.getController();
            controller.setReportedListing(listing);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("report.dialog.window.title", "Report Listing"));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open report dialog"),
                    e.getMessage());
        }
    }

    public void openUserFollowers(UserViewModel user, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserFollowersView.fxml"));
            Parent root = loader.load();

            UserFollowersController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("followers.window.title", "Followers & Following"));
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open followers view"),
                    e.getMessage());
        }
    }

    public void openUserFavorites(UserViewModel user, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserFavoritesView.fxml"));
            Parent root = loader.load();

            UserFavoritesController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("favorites.window.title", "Favorite Listings"));
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.header", "Failed to open favorites view"),
                    e.getMessage());
        }
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