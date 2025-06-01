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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;

import java.io.IOException;

public class MainController implements Refreshable {
    // NON CI SONO
    @FXML
    private Label usernameLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label connectionStatusLabel;
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

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final EventBusService eventBus = EventBusService.getInstance();

    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.localeService = LocaleService.getInstance();
        this.sessionService = UserSessionService.getInstance();
    }

    @FXML
    private HBox articoliPreferitiBox;
    @FXML
    private HBox utentiPreferitiBox;
    @FXML
    private HBox astePreferiteBox;

    @FXML
    public void initialize() {
        try {
            // Inizializza le componenti UI
            // statusLabel.setText(localeService.getMessage("dashboard.status.loaded"));
            connectionStatusLabel.setText(localeService.getMessage("dashboard.status.connected"));

            String username = sessionService.getUser().getUsername();
            usernameLabel.setText(localeService.getMessage("dashboard.welcome.user", username));

            // Subscribe to locale change events
            eventBus.subscribe(EventTypes.LOCALE_CHANGED, _ -> {
                Platform.runLater(this::refreshAllViews);
            });

            // load cards
            // Esempio: popola articoli preferiti con dati fittizi
            articoliPreferitiBox.getChildren().clear();
            for (int i = 1; i <= 3; i++) {
                VBox card = new VBox();
                ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/images/spermatozoi.png")));
                img.setFitWidth(100);
                img.setFitHeight(100);
                Text titolo = new Text("Articolo " + i);
                Text prezzo = new Text((10 * i) + "€");
                card.getChildren().addAll(img, titolo, prezzo);
                articoliPreferitiBox.getChildren().add(card);
            }

            // Esempio: popola utenti preferiti con dati fittizi
            utentiPreferitiBox.getChildren().clear();
            for (int i = 1; i <= 2; i++) {
                VBox userBox = new VBox();
                ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/images/spermatozoi.png")));
                img.setFitWidth(100);
                img.setFitHeight(100);
                Text nome = new Text("Utente " + i);
                userBox.getChildren().addAll(img, nome);
                utentiPreferitiBox.getChildren().add(userBox);
            }

            // Esempio: popola aste preferite con dati fittizi
            astePreferiteBox.getChildren().clear();
            for (int i = 1; i <= 2; i++) {
                VBox astaBox = new VBox();
                Text titolo = new Text("Asta " + i);
                Text offerta = new Text("Offerta: " + (i * 50) + "€");
                astaBox.getChildren().addAll(titolo, offerta);
                astePreferiteBox.getChildren().add(astaBox);
            }

            // Se stai caricando immagini da risorse, assicurati di gestire correttamente le eccezioni
            // e di verificare che i percorsi siano corretti

            // Esempio di come gestire un'immagine in modo sicuro:
            if (ProfileImageView != null) {
                String imagePath = "/images/icons/user_profile.png";
                try {
                    Image img = new Image(getClass().getResourceAsStream(imagePath));
                    if (img.isError()) {
                        System.err.println("Errore nel caricamento dell'immagine: " + img.getException().getMessage());
                    } else {
                        ProfileImageView.setImage(img);
                    }
                } catch (Exception e) {
                    System.err.println("Eccezione durante il caricamento dell'immagine: " + e.getMessage());
                }
            }

            // Altre inizializzazioni

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
    public void showSaved(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.saved"));
        // TODO: Load saved content
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
    public void addItem(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.add.item"));
        // TODO: Load add item content
        // apre l'inventario e poi il popup per aggiungere un nuovo item
    }

    @FXML
    public void showProfile(ActionEvent event) {
        statusLabel.setText(localeService.getMessage("dashboard.view.profile"));
        try {
            Parent profileView = navigationService.loadProfileView();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(0, profileView);
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
        connectionStatusLabel.setText(localeService.getMessage("label.connected"));
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

    @FXML
    public void openNotifications(ActionEvent event) {

    }

    @FXML
    public void openAste(ActionEvent event) {

    }
}