package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;

public class MainController implements Refreshable {
    // Existing FXML fields
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

    // Header FXML fields
    // @FXML
    // private Label welcomeLabel;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button notificationsButton;
    @FXML
    private Button userMenuButton;
    @FXML
    private ImageView userAvatarImage;
    @FXML
    private Button allItemsButton;
    @FXML
    private Button auctionsButton;
    @FXML
    private Button tradeButton;

    // Existing fields
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
    private VBox sidebarInclude;
    @FXML
    private SidebarController sidebarIncludeController;
    @FXML
    private HBox favoriteListingsContainer;
    @FXML
    private HBox favoriteUsersContainer;
    @FXML
    private HBox favoriteAuctionsContainer;

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final EventBusService eventBus = EventBusService.getInstance();
    private final ImageService imageService = ImageService.getInstance();

    private String currentFilter = "all";

    public MainController() {
        this.navigationService = NavigationService.getInstance();
        this.localeService = LocaleService.getInstance();
        this.sessionService = UserSessionService.getInstance();
    }

    @FXML
    public void initialize() {
        try {
            checkAuthentication();

            // Register this controller with NavigationService
            navigationService.setMainController(this);

            Parent homeView = navigationService.loadHomeView();
            setContent(homeView);

            // Set up header
            initializeHeader();

            // Subscribe to locale change events
            eventBus.subscribe(EventTypes.LOCALE_CHANGED, _ -> {
                Platform.runLater(this::refreshAllViews);
            });

            // Subscribe to search events
            eventBus.subscribe(EventTypes.SEARCH_REQUESTED, data -> {
                Platform.runLater(() -> handleSearchRequest(data));
            });

            // Set up sidebar
            sidebarIncludeController.setMainController(this);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error during controller initialization: " + e.getMessage());
        }
    }

    private void initializeHeader() {
        // Set welcome message with username
        if (sessionService.isLoggedIn()) {
            String username = sessionService.getUser().getUsername();
            // welcomeLabel.setText(localeService.getMessage("dashboard.welcome.user",
            // username));
            // usernameLabel.setText(localeService.getMessage("dashboard.welcome.user",
            // username));

            // Load user avatar
            loadUserAvatar();
        }

        // Subscribe to profile image changes
        eventBus.subscribe(EventTypes.PROFILE_IMAGE_CHANGED, data -> {
            if (data instanceof String) {
                Platform.runLater(() -> loadUserAvatar((String) data));
            }
        });

        // Set up search field enter key handler
        searchField.setOnAction(this::handleSearch);

        // Initialize filter button states
        updateFilterButtons();
    }

    // Header event handlers
    @FXML
    public void handleSearch(ActionEvent event) {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            System.out.println("Searching for: " + searchQuery);

            // Publish search event with current filter
            SearchData searchData = new SearchData(searchQuery, currentFilter);
            eventBus.publishEvent(EventTypes.SEARCH_REQUESTED, searchData);
        }
    }

    @FXML
    public void showNotifications(ActionEvent event) {
        System.out.println("Show notifications clicked");
        // TODO: Implement notifications view
    }

    @FXML
    public void showUserMenu(ActionEvent event) {
        System.out.println("Show user menu clicked");
        // TODO: Implement user menu dropdown
    }

    @FXML
    public void filterAll(ActionEvent event) {
        setCurrentFilter("all");
        triggerSearch();
    }

    @FXML
    public void filterAuctions(ActionEvent event) {
        setCurrentFilter("auctions");
        triggerSearch();
    }

    @FXML
    public void filterTrade(ActionEvent event) {
        setCurrentFilter("trade");
        triggerSearch();
    }

    private void setCurrentFilter(String filter) {
        currentFilter = filter;
        updateFilterButtons();
    }

    private void updateFilterButtons() {
        // Remove active class from all buttons
        allItemsButton.getStyleClass().removeAll("active");
        auctionsButton.getStyleClass().removeAll("active");
        tradeButton.getStyleClass().removeAll("active");

        // Add active class to current filter button
        switch (currentFilter) {
            case "all" -> allItemsButton.getStyleClass().add("active");
            case "auctions" -> auctionsButton.getStyleClass().add("active");
            case "trade" -> tradeButton.getStyleClass().add("active");
        }
    }

    private void triggerSearch() {
        String searchQuery = searchField.getText().trim();
        if (!searchQuery.isEmpty()) {
            SearchData searchData = new SearchData(searchQuery, currentFilter);
            eventBus.publishEvent(EventTypes.SEARCH_REQUESTED, searchData);
        }
    }

    private void handleSearchRequest(Object data) {
        if (data instanceof SearchData searchData) {
            System.out
                    .println("Search requested: " + searchData.getQuery() + " with filter: " + searchData.getFilter());
            // Handle search logic here or forward to appropriate view controller
        }
    }

    private void loadUserAvatar() {
        String profileImagePath = sessionService.getUser().getProfileImagePath();
        loadUserAvatar(profileImagePath);
    }

    private void loadUserAvatar(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            // Use default avatar
            return;
        }

        imageService.fetchImage(imagePath)
                .thenAccept(image -> {
                    Platform.runLater(() -> {
                        userAvatarImage.setImage(image);
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Failed to load user avatar: " + ex.getMessage());
                    return null;
                });
    }

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

    private void refreshAllViews() {
        refreshUI();
        refreshCurrentContentView();
    }

    public void refreshUI() {
        // Update existing labels
        statusLabel.setText(localeService.getMessage("label.ready"));
        contentAreaSubtitleLabel.setText(localeService.getMessage("dashboard.contentared.title"));
        contentAreaTitleLabel.setText(localeService.getMessage("dashboard.contentared.subtitle"));

        // Update header elements
        if (sessionService.isLoggedIn()) {
            // String username = sessionService.getUser().getUsername();
            // welcomeLabel.setText(localeService.getMessage("dashboard.welcome.user",
            // username));
        }

        // Update filter button text
        allItemsButton.setText(localeService.getMessage("header.filter.all"));
        auctionsButton.setText(localeService.getMessage("header.filter.auctions"));
        tradeButton.setText(localeService.getMessage("header.filter.trade"));

        // Update search placeholder
        searchField.setPromptText(localeService.getMessage("header.search.placeholder"));

        // Update existing menu items (if they exist)
        if (dashboardMenuItem != null) {
            dashboardMenuItem.setText(localeService.getMessage("dashboard.menu.dashboard"));
        }
        if (marketsMenuItem != null) {
            marketsMenuItem.setText(localeService.getMessage("dashboard.menu.markets"));
        }
        if (portfolioMenuItem != null) {
            portfolioMenuItem.setText(localeService.getMessage("dashboard.menu.portfolio"));
        }
        if (tradeMenuItem != null) {
            tradeMenuItem.setText(localeService.getMessage("dashboard.menu.trade"));
        }
        if (settingsMenuItem != null) {
            settingsMenuItem.setText(localeService.getMessage("dashboard.menu.settings"));
        }
        if (profileMenuItem != null) {
            profileMenuItem.setText(localeService.getMessage("dashboard.menu.profile"));
        }
        if (inventoryMenuItem != null) {
            inventoryMenuItem.setText(localeService.getMessage("dashboard.menu.inventory"));
        }
        if (createListingMenuItem != null) {
            createListingMenuItem.setText(localeService.getMessage("dashboard.menu.create.listing"));
        }
        if (logoutButton != null) {
            logoutButton.setText(localeService.getMessage("button.logout"));
        }
        if (quickTradeButton != null) {
            quickTradeButton.setText(localeService.getMessage("button.quicktrade"));
        }
        if (viewMarketsButton != null) {
            viewMarketsButton.setText(localeService.getMessage("button.view.markets"));
        }
    }

    private void refreshCurrentContentView() {
        if (contentArea.getChildren().isEmpty())
            return;

        Node currentView = contentArea.getChildren().get(0);
        Object controller = null;

        if (currentView instanceof Parent) {
            controller = ((Parent) currentView).getProperties().get("controller");
        }

        if (controller instanceof Refreshable) {
            ((Refreshable) controller).refreshUI();
        } else {
            reloadCurrentView();
        }
    }

    private void reloadCurrentView() {
        if (contentArea.getChildren().isEmpty())
            return;

        Node currentView = contentArea.getChildren().get(0);
        String viewId = currentView.getId();
        System.out.println("Reloading view: " + viewId);
    }

    public void setContent(Parent newContent) {
        contentArea.getChildren().setAll(newContent);
    }

    // Helper class for search data
    public static class SearchData {
        private final String query;
        private final String filter;

        public SearchData(String query, String filter) {
            this.query = query;
            this.filter = filter;
        }

        public String getQuery() {
            return query;
        }

        public String getFilter() {
            return filter;
        }
    }
}