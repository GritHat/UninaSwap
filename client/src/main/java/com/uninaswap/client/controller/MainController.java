package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;

import java.io.IOException;

public class MainController implements Refreshable {

    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Label contentAreaTitleLabel;
    @FXML private Label contentAreaSubtitleLabel;
    @FXML private StackPane contentArea;
    @FXML private ImageView headerProfileImageView;
    @FXML private Button dashboardMenuItem;
    @FXML private Button marketsMenuItem;
    @FXML private Button portfolioMenuItem;
    @FXML private Button tradeMenuItem;
    @FXML private Button settingsMenuItem;
    @FXML private Button profileMenuItem;
    @FXML private Button inventoryMenuItem;
    @FXML private Button createListingMenuItem;
    @FXML private Button logoutButton;
    @FXML private Button quickTradeButton;
    @FXML private Button viewMarketsButton;

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final ImageService imageService;
    private final EventBusService eventBus = EventBusService.getInstance();

    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.localeService = LocaleService.getInstance();
        this.sessionService = UserSessionService.getInstance();
        this.imageService = ImageService.getInstance();
    }

    @FXML
    public void initialize() {
        checkAuthentication();
        statusLabel.setText(localeService.getMessage("dashboard.status.loaded"));
        connectionStatusLabel.setText(localeService.getMessage("dashboard.status.connected"));

        String username = sessionService.getUser().getUsername();
        usernameLabel.setText(localeService.getMessage("dashboard.welcome.user", username));
    
        String profileImagePath = sessionService.getUser().getProfileImagePath();
        loadProfileImageInHeader(profileImagePath);
        
        // Subscribe to profile image change events
        EventBusService.getInstance().subscribe(EventTypes.PROFILE_IMAGE_CHANGED, data -> {
            if (data instanceof String) {
                updateProfileImage((String) data);
            }
        });

        // Subscribe to locale change events
        eventBus.subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshAllViews);
        });
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Clean up event subscriptions before ending the session
            eventBus.clearAllSubscriptions();
            // End the user session
            sessionService.endSession();

            navigationService.navigateToLogin(usernameLabel);
        } catch (Exception e) {
            statusLabel.setText(localeService.getMessage("dashboard.error.logout", e.getMessage()));
        }
    }

    // Navigation methods - these would load different content into the contentArea
    @FXML
    public void showDashboard(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.dashboard"));
        // TODO: Load dashboard content
    }

    @FXML
    public void showMarkets(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.markets"));
        // TODO: Load markets content
    }

    @FXML
    public void showPortfolio(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.portfolio"));
        // TODO: Load portfolio content
    }

    @FXML
    public void showTrade(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.trade"));
        // TODO: Load trade content
    }

    @FXML
    public void showSettings(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.settings"));
        // TODO: Load settings content

    }

    @FXML
    public void showProfile(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.profile"));
        try {
            Parent profileView = navigationService.loadProfileView();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(0,profileView);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText(localeService.getMessage("dashboard.error.load.profile"));
        }
    }

    @FXML
    public void showInventory(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.inventory"));
        try {
            Parent inventoryView = navigationService.loadInventoryView();
            inventoryView.setId("inventoryView");

            // Replace content area with inventory view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(inventoryView);
        } catch (IOException e) {
            statusLabel.setText(localeService.getMessage("dashboard.error.load.inventory"));
            e.printStackTrace();
        }
    }

    @FXML
    public void showCreateListing(ActionEvent event) {
        statusLabel.setText("Creating New Listing");
        try {
            Parent listingCreationView = navigationService.loadListingCreationView();
            listingCreationView.setId("createListingView");
            // Replace content area with listing creation view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(listingCreationView);
        } catch (IOException e) {
            statusLabel.setText("Failed to load listing creation view");
            e.printStackTrace();
        }
    }

    /**
     * Verify that a user is logged in, redirect to login if not
     * Call this from initialize() in protected controllers
     */
    private void checkAuthentication() {
        if (!sessionService.isLoggedIn()) {
            Platform.runLater(() -> {
                try {
                    navigationService.navigateToLogin(usernameLabel);
                } catch (Exception e) {
                    System.err.println("User is not logged in, redirecting to login screen");
                    // Log error
                }
            });
        }
    }

    /**
     * Loads user profile image in the header
     */
    private void loadProfileImageInHeader(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            setDefaultProfileImage();
            return;
        }
        imageService.fetchImage(imagePath)
            .thenAccept(image -> {
                Platform.runLater(() -> {
                    headerProfileImageView.setImage(image);

                    // Apply circular clip to the image
                    Circle clip = new Circle(16, 16, 16); // 32/2=16
                    headerProfileImageView.setClip(clip);
                });
            })
            .exceptionally(ex -> {
                // If loading fails, set default image
                System.err.println("Failed to load profile image: " + ex.getMessage());
                Platform.runLater(this::setDefaultProfileImage);
                return null;
            });
    }

    /**
     * Sets a default profile image when no custom image is available
     */
    private void setDefaultProfileImage() {
        // Load default image from resources
        Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
        headerProfileImageView.setImage(defaultImage);

        // Apply circular clip
        Circle clip = new Circle(16, 16, 16);
        headerProfileImageView.setClip(clip);
    }

    /**
     * Update profile image when profile is changed elsewhere
     */
    public void updateProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            loadProfileImageInHeader(imagePath);
        }
    }

    /**
     * Refreshes all UI elements after a language change
     */
    private void refreshAllViews() {
        // Update main view elements
        refreshUI();
        refreshCurrentContentView();
    }

    /**
     * Refreshes the current content view
     */
    private void refreshCurrentContentView() {
        if (contentArea.getChildren().isEmpty()) return;

        Node currentView = contentArea.getChildren().get(0);
        Object controller = null;

        // Get the controller for the current view
        if (currentView instanceof Parent) {
            controller = ((Parent) currentView).getProperties().get("controller");
        }
        // If the controller implements Refreshable, refresh it
        if (controller instanceof Refreshable) {
            ((Refreshable) controller).refreshUI();
        } else {
            // Otherwise, try to reload the current view
            reloadCurrentView();
        }
    }

    /**
     * Reloads the current view completely
     * TODO : This is a temporary solution, ideally we should have a refresh method in each controller
     */
    private void reloadCurrentView() {
        if (contentArea.getChildren().isEmpty()) return;

        Node currentView = contentArea.getChildren().get(0);
        String viewId = currentView.getId();
        System.out.println("Reloading view: " + viewId);

        if (viewId != null) {
            switch (viewId) {
                case "dashboardView":
                    showDashboard(null);
                    break;
                case "marketsView":
                    showMarkets(null);
                    break;
                case "portfolioView":
                    showPortfolio(null);
                    break;
                case "tradeView":
                    showTrade(null);
                    break;
                case "settingsView":
                    showSettings(null);
                    break;
                case "profileView":
                    showProfile(null);
                    break;
                case "inventoryView":
                    showInventory(null);
                    break;
                case "createListingView":
                    showCreateListing(null);
                    break;
            }
        }
    }

    /**
     * Refreshes the main view elements
     */
    public void refreshUI() {
        usernameLabel.setText(sessionService.getUser().getUsername());
        statusLabel.setText(localeService.getMessage("label.ready"));
        connectionStatusLabel.setText(localeService.getMessage("label.connected"));
        contentAreaSubtitleLabel.setText(localeService.getMessage("dashboard.contentaread.title"));
        contentAreaTitleLabel.setText(localeService.getMessage("dashboard.contentaread.subtitle"));
        // Update sidebar menu items
        dashboardMenuItem.setText(localeService.getMessage("dashboard.menu.dashboard"));
        marketsMenuItem.setText(localeService.getMessage("dashboard.menu.markets"));
        portfolioMenuItem.setText(localeService.getMessage("dashboard.menu.portfolio"));
        tradeMenuItem.setText(localeService.getMessage("dashboard.menu.trade"));
        settingsMenuItem.setText(localeService.getMessage("dashboard.menu.settings"));
        profileMenuItem.setText(localeService.getMessage("dashboard.menu.profile"));
        inventoryMenuItem.setText(localeService.getMessage("dashboard.menu.inventory"));
        createListingMenuItem.setText(localeService.getMessage("dashboard.menu.create.listing"));
        logoutButton.setText(localeService.getMessage("button.logout"));
        quickTradeButton.setText(localeService.getMessage("button.quicktrade"));
        viewMarketsButton.setText(localeService.getMessage("button.view.markets"));
    }
}