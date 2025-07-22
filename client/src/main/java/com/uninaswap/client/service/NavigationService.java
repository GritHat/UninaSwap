package com.uninaswap.client.service;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import com.uninaswap.client.controller.LoginController;
import com.uninaswap.client.controller.ProfileController;
import com.uninaswap.client.controller.RegisterController;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.client.viewmodel.PickupViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.client.controller.MainController;
import com.uninaswap.client.controller.NotificationDropdownController;
import com.uninaswap.client.controller.PickupSchedulingController;
import com.uninaswap.client.controller.PickupSelectionController;
import com.uninaswap.client.controller.ReviewCreateController;
import com.uninaswap.client.controller.UserFavoritesController;
import com.uninaswap.client.controller.UserFollowersController;
import com.uninaswap.client.controller.UserMenuDropdownController;
import com.uninaswap.client.controller.UserReviewsController;
import com.uninaswap.client.controller.ReportDialogController;
import com.uninaswap.client.controller.HomeController;

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
    private final java.util.Stack<NavigationState> navigationHistory = new java.util.Stack<>();
    private MainController mainController;

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

    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    private NavigationService() {
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
     * @param fxmlPath The path to the FXML file
     * @return A LoaderBundle containing the FXMLLoader and the loaded view
     * @throws IOException If an error occurs while loading the view
     */
    private LoaderBundle loadView(String fxmlPath) throws IOException {
        if (fxmlPath == null || fxmlPath.isEmpty()) {
            throw new IllegalArgumentException("FXML path cannot be null or empty");
        }
        java.net.URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            throw new IOException("Resource not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        loader.setResources(localeService.getResourceBundle());
        Parent root = loader.load();
        Object controller = loader.getController();
        root.getProperties().put("controller", controller);
        String viewName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1, fxmlPath.lastIndexOf('.'));
        root.setId(viewName);

        return new LoaderBundle(loader, root);
    }

    /**
     * Navigate to the login screen
     * 
     * @param sourceNode The node that triggered the navigation
     * @throws IOException If an error occurs while loading the view
     */
    public void navigateToLogin(Node sourceNode) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/LoginView.fxml");
        Parent loginView = loaderBundle.getView();

        Stage stage;
        if (sourceNode != null && sourceNode.getScene() != null) {
            stage = (Stage) sourceNode.getScene().getWindow();
        } else {
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
                stage = new Stage();
            }
        }

        stage.setTitle("UninaSwap - Login");
        stage.setScene(new Scene(
                loginView,
                stage.getScene().getWidth(),
                stage.getScene().getHeight()));

        LoginController controller = loaderBundle.getLoader().getController();
        controller.registerMessageHandler();
    }

    /**
     * Navigate to the register screen
     * 
     * @param sourceNode The node that triggered the navigation
     * @throws IOException If an error occurs while loading the view
     */
    public void navigateToRegister(Node sourceNode) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/RegisterView.fxml");
        Parent registerView = loaderBundle.getView();

        Stage stage;
        if (sourceNode != null && sourceNode.getScene() != null) {
            stage = (Stage) sourceNode.getScene().getWindow();
        } else {
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
                stage = new Stage();
            }
        }

        stage.setTitle("UninaSwap - Register");
        stage.setScene(
                new Scene(registerView,
                        stage.getScene().getWidth(),
                        stage.getScene().getHeight()));

        RegisterController controller = loaderBundle.getLoader().getController();
        controller.registerMessageHandler();
    }

    /**
     * Navigate to the main dashboard screen
     * 
     * @param sourceNode The node that triggered the navigation
     * @throws IOException If an error occurs while loading the view
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
        MainController mainController = loaderBundle.getLoader().getController();
        mainController.initialize();
    }

    public void navigateToItemDetails(String itemId) {
        System.out.println("Navigating to item details for item: " + itemId);
    }

    /**
     * Navigate to the terms and conditions screen
     * 
     * @param sourceNode The node that triggered the navigation
     * @throws IOException If an error occurs while loading the view
     */
    public void openTermsAndConditions(Node sourceNode) throws IOException {
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
            throw e;
        }
    }

    /**
     * Load the home view
     * 
     * @throws IOException If an error occurs while loading the view
     */
    public Parent loadHomeView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/HomeView.fxml");
        Parent homeView = loaderBundle.getView();

        return homeView;
    }

    public void navigateToHomeView() throws IOException {
        Parent homeView = loadHomeView();
        mainController.setContent(homeView);
        mainController.updateSidebarButtonSelection("home");
        mainController.sidebarClearAllSelection();
    }

    /**
     * Load the profile view
     *
     * @param user The user to load the profile for
     * @throws IOException If an error occurs while loading the view
     * @return The loaded profile view as a Parent object
     */
    public Parent loadProfileView(UserViewModel user) throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/ProfileView.fxml");
        Parent profileView = loaderBundle.getView();

        ProfileController controller = loaderBundle.getLoader().getController();
        controller.loadProfile(user);
        controller.registerMessageHandler();

        return profileView;
    }

    /**
     * Load the inventory view
     * 
     * @throws IOException If an error occurs while loading the view
     * @return The loaded inventory view as a Parent object
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
     * 
     * @throws IOException If an error occurs while loading the view
     * @return The loaded notifications view as a Parent object
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
     * 
     * @throws IOException If an error occurs while loading the view
     * @return The loaded listing creation view as a Parent object
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

    public Parent loadAnalyticsView() throws IOException {
        LoaderBundle loaderBundle = loadView("/fxml/AnalyticsView.fxml");
        Parent analyticsView = loaderBundle.getView();

        return analyticsView;
    }

    public void navigateToAnalyticsView() throws IOException {
    if (mainController != null) {
        Parent analyticsView = loadAnalyticsView();
        mainController.setContent(analyticsView);
        mainController.sidebarClearAllSelection();
        
        System.out.println("Navigated to Analytics view");
    }
}

    /**
     * Load the support view
     * 
     * @throws IOException If an error occurs while loading the view
     * @return The loaded support view as a Parent object
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
     * 
     * @throws IOException If an error occurs while logging out
     */
    public void logout() throws IOException {
        sessionService.endSession();
        eventBus.clearAllSubscriptions();
        navigateToLogin(null);
    }

    /**
     * Convenience method to get Stage from an ActionEvent
     * 
     * @param event The ActionEvent from which to get the Stage
     * @return The Stage associated with the ActionEvent    
     */
    public Stage getStageFromEvent(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }

    /**
     * Load offers view
     * 
     * @throws IOException If an error occurs while loading the view
     * @return The loaded offers view as a Parent object
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

    public void navigateToProfileView(UserViewModel user) throws IOException {
        mainController.setContent(loadProfileView(user));
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
     * 
     * @param mainController The main controller to set
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void openItemDialog(ItemViewModel itemViewModel) {
        try {
            ItemDTO item = ViewModelMapper.getInstance().toDTO(itemViewModel);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemDialogView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            DialogPane dialogPane = loader.load();
            ButtonType confirmButtonType = new ButtonType(
                    localeService.getMessage("button.confirm"),
                    ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(
                    localeService.getMessage("button.cancel"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);
            ItemDialogController controller = loader.getController();
            controller.setItem(item);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(localeService.getMessage(item.getId() == null ? "item.add.title" : "item.edit.title"));
            dialog.setHeaderText(
                    localeService.getMessage(item.getId() == null ? "item.add.header" : "item.edit.header"));
            dialog.setDialogPane(dialogPane);
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
                ItemDTO updatedItem = controller.getUpdatedItem();
                File selectedImageFile = controller.getSelectedImageFile();
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
            loader.setResources(localeService.getResourceBundle());
            Parent root = loader.load();

            PickupSchedulingController controller = loader.getController();
            controller.setOfferId(offer.getId());
            controller.setOffer(offer);

            Stage stage = new Stage();
            stage.setTitle(localeService.getMessage("pickup.scheduling.window.title", "Schedule Pickup"));
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
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

    /**
     * Open pickup selection dialog for accepting/selecting pickup times
     * 
     * @param offer The offer for which to select a pickup time
     * @param parentStage The parent stage for the dialog
     */
    public void openPickupSelection(OfferViewModel offer, Stage parentStage) {
        try {
            PickupService pickupService = PickupService.getInstance();
            pickupService.getPickupByOfferId(offer.getId())
                .thenAccept(pickup -> Platform.runLater(() -> {
                    if (pickup != null) {
                        openPickupSelectionDialog(pickup, parentStage);
                    } else {
                        AlertHelper.showErrorAlert(
                            localeService.getMessage("error.title", "Error"),
                            localeService.getMessage("pickup.not.found.header", "Pickup Not Found"),
                            localeService.getMessage("pickup.not.found.message", 
                                "No pickup arrangement found for this offer."));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                        localeService.getMessage("error.title", "Error"),
                        localeService.getMessage("pickup.error.header", "Pickup Error"),
                        ex.getMessage()));
                    return null;
                });
            
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                localeService.getMessage("error.title", "Error"),
                localeService.getMessage("pickup.selection.error.header", "Failed to Open Pickup Selection"),
                "Could not open pickup selection dialog: " + e.getMessage()
            );
        }
    }

    private void openPickupSelectionDialog(PickupViewModel pickup, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PickupSelectionView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            Parent root = loader.load();
            PickupSelectionController controller = loader.getController();
            controller.setPickup(pickup);
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(parentStage);
            dialog.setTitle(localeService.getMessage("pickup.selection.title", "Select Pickup Time"));
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.setOnShown(e -> {
                dialog.setX(parentStage.getX() + (parentStage.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(parentStage.getY() + (parentStage.getHeight() - dialog.getHeight()) / 2);
            });
            
            dialog.showAndWait();
            
        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                localeService.getMessage("error.title", "Error"),
                localeService.getMessage("pickup.selection.error.header", "Failed to Open Pickup Selection"),
                "Could not open pickup selection dialog: " + e.getMessage()
            );
        }
    }

    public void openReviewCreate(OfferViewModel offer, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReviewCreateView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            
            Parent reviewContent = loader.load();
            ReviewCreateController controller = loader.getController();
            controller.setOffer(offer);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(parentStage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(localeService.getMessage("review.create.title", "Write a Review"));
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(reviewContent);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);
            Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
            if (closeButton != null) {
                closeButton.setVisible(false);
                closeButton.setManaged(false);
            }
            dialog.showAndWait();
            
        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    "Failed to open review creation dialog: " + e.getMessage());
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
            navigationHistory.pop();

            if (!navigationHistory.isEmpty()) {
                NavigationState previousState = navigationHistory.peek();

                if (mainController != null) {
                    Platform.runLater(() -> {
                        mainController.setContent(previousState.getView());
                        updateWindowTitle(previousState.getTitle());
                    });
                } else {
                    System.err.println("MainController reference not set in NavigationService");
                }
            } else {
                try {
                    Parent homeView = loadHomeView();
                    loadView(homeView, "Home");
                } catch (IOException e) {
                    System.err.println("Error loading home view: " + e.getMessage());
                }
            }
        } else {
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
     * 
     * @param view The view to load
     * @param title The title for the view
     */
    public void loadView(Parent view, String title) {
        if (mainController != null) {
            Platform.runLater(() -> {
                addToHistory(view, title);
                mainController.setContent(view);
                updateWindowTitle(title);
            });
        } else {
            System.err.println("MainController reference not set in NavigationService");
        }
    }

    /**
     * Add a view to navigation history
     * 
     * @param view The view to add
     * @param title The title for the view
     */
    private void addToHistory(Parent view, String title) {
        NavigationState state = new NavigationState(view, title);
        navigationHistory.push(state);
        if (navigationHistory.size() > 10) {
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
     * 
     * @param title The new title for the window
     */
    private void updateWindowTitle(String title) {
        System.out.println("Navigation: " + title);
    }

    /**
     * Open pickup rescheduling dialog for an existing offer
     * 
     * @param offerId The ID of the offer to reschedule
     * @param parentStage The parent stage for the dialog
     */
    public void openPickupRescheduling(String offerId, Stage parentStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PickupSchedulingView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            Parent root = loader.load();
            PickupSchedulingController controller = loader.getController();
            controller.setOfferId(offerId);
            controller.setReschedulingMode(true);
            Stage dialog = new Stage();
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(parentStage);
            dialog.setTitle(localeService.getMessage("pickup.reschedule.title", "Reschedule Pickup"));
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.setOnShown(_ -> {
                dialog.setX(parentStage.getX() + (parentStage.getWidth() - dialog.getWidth()) / 2);
                dialog.setY(parentStage.getY() + (parentStage.getHeight() - dialog.getHeight()) / 2);
            });
            
            dialog.showAndWait();
            
        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                localeService.getMessage("error.title", "Error"),
                localeService.getMessage("pickup.reschedule.error.header", "Failed to Open Rescheduling"),
                "Could not open pickup rescheduling dialog: " + e.getMessage()
            );
        }
    }

    /**
     * Navigate to home view and display listings for a specific user
     * 
     * @param userId The ID of the user whose listings to display
     * @throws IOException If an error occurs while loading the home view
     */
    public void navigateToHomeViewAndSearchListingsByUserId(Long userId) throws IOException {
        Parent homeView = loadHomeView();
        mainController.setContent(homeView);
        mainController.updateSidebarButtonSelection("home");
        Object controller = homeView.getProperties().get("controller");
        if (controller instanceof HomeController homeController) {
            Platform.runLater(() -> {
                homeController.displayUserListings(userId);
            });
        } else {
            System.err.println("Could not get HomeController instance to display user listings");
        }
    }
}