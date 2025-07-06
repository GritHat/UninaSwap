package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import com.uninaswap.client.service.NavigationService;

import java.util.List;

import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.controller.UserCardController;

public class MainController implements Refreshable {
    @FXML
    private Label usernameLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label contentAreaTitleLabel;
    @FXML
    private Label contentAreaSubtitleLabel;

    @FXML
    private StackPane contentArea;
    @FXML
    private Button dashboardMenuItem;
    @FXML
    private Button marketsMenuItem;
    @FXML
    private Button portfolioMenuItem;
    @FXML
    private Button tradeMenuItem;
    @FXML
    private Button settingsMenuItem;
    @FXML
    private Button profileMenuItem;
    @FXML
    private Button inventoryMenuItem;
    @FXML
    private Button createListingMenuItem;
    @FXML
    private Button logoutButton;
    @FXML
    private Button quickTradeButton;
    @FXML
    private Button viewMarketsButton;

    @FXML
    private VBox sidebarInclude; // Change from SidebarController to VBox
    @FXML
    private SidebarController sidebarIncludeController; // Optional: only if you need the controller

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final EventBusService eventBus = EventBusService.getInstance();
    private final UserCardController userCard= new UserCardController();

    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.localeService = LocaleService.getInstance();
        this.sessionService = UserSessionService.getInstance();
    }
    /**
     * Constructor with dependencies injected
     * 
     * @param navigationService Navigation service instance
     * @param localeService Locale service instance
     * @param sessionService User session service instance
     */

    @FXML
    private HBox articoliPreferitiBox;
    @FXML
    private HBox utentiPreferitiBox;
    @FXML
    private HBox astePreferiteBox;

  

    @FXML
    public void initialize() {
        try {
             checkAuthentication();

            Parent homeView = navigationService.loadHomeView();
            setContent(homeView);
            String username = sessionService.getUser().getUsername();
            usernameLabel.setText(localeService.getMessage("dashboard.welcome.user", username));

            userCard.loadUserCardsIntoTab(null, List<UserDTO>);

            // Subscribe to locale change events
            eventBus.subscribe(EventTypes.LOCALE_CHANGED, _ -> {
                Platform.runLater(this::refreshAllViews);
            });

            // Recupera il controller della sidebar
            sidebarIncludeController.setMainController(this);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Errore durante l'inizializzazione del controller: " + e.getMessage());
        }
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
                }
            });
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
     * Refreshes the main view elements
     */
    public void refreshUI() {
        statusLabel.setText(localeService.getMessage("label.ready"));
        contentAreaSubtitleLabel.setText(localeService.getMessage("dashboard.contentaread.title"));
        contentAreaTitleLabel.setText(localeService.getMessage("dashboard.contentared.subtitle"));
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

    /**
     * Refreshes the current content view
     */
    private void refreshCurrentContentView() {
        if (contentArea.getChildren().isEmpty())
            return;

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
     */
    private void reloadCurrentView() {
        if (contentArea.getChildren().isEmpty())
            return;

        Node currentView = contentArea.getChildren().get(0);
        String viewId = currentView.getId();
        System.out.println("Reloading view: " + viewId);

        if (viewId != null) {
        }
    }

    /**
     * Sets the content of the main area
     * 
     * @param newContent The new content to display
     */
    public void setContent(Parent newContent) {
        contentArea.getChildren().setAll(newContent);
    }

}