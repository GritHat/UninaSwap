package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import com.uninaswap.client.service.CategoryService;
import com.uninaswap.client.service.SearchService;
import com.uninaswap.common.enums.Category;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.fxml.FXMLLoader;

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
    private Button tradesButton;
    @FXML
    private Button salesButton;
    @FXML
    private Button giftsButton;

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
    @FXML
    private HBox favoritesDrawerInclude;
    @FXML
    private FavoritesDrawerController favoritesDrawerIncludeController;
    @FXML
    private Button favoritesButton;

    // Add this field
    @FXML
    private ComboBox<Category> categoryComboBox;

    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final EventBusService eventBus = EventBusService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    // Add the CategoryService
    private final CategoryService categoryService = CategoryService.getInstance();
    private final SearchService searchService = SearchService.getInstance();

    private String currentFilter = "all";
    private boolean isInSearchMode = false;

    private Popup notificationPopup;
    private Popup userMenuPopup;

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
            favoritesDrawerIncludeController.setMainController(this);

            // Initialize category combo box
            setupCategoryComboBox();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error during controller initialization: " + e.getMessage());
        }
    }

    private void initializeHeader() {
        loadUserAvatar();

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

    private void setupCategoryComboBox() {
        if (categoryComboBox != null) {
            // Set up the items
            categoryComboBox.setItems(FXCollections.observableArrayList(categoryService.getCategories()));
            
            // Set default value
            categoryComboBox.setValue(Category.ALL);
            
            // Set up string converter - this is crucial for display
            categoryComboBox.setConverter(new StringConverter<Category>() {
                @Override
                public String toString(Category category) {
                    if (category == null) {
                        return "Tutte le categorie";
                    }
                    return categoryService.getLocalizedCategoryName(category);
                }

                @Override
                public Category fromString(String string) {
                    return categoryService.getCategoryByDisplayName(string);
                }
            });

            // Add change listener to trigger search
            categoryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != oldValue && newValue != null) {
                    System.out.println("Category changed to: " + newValue);
                    if (isInSearchMode) {
                        performCurrentSearch();
                    } else {
                        triggerSearch();
                    }
                }
            });
        } else {
            System.err.println("categoryComboBox is null - check FXML fx:id");
        }
    }

    // Header event handlers
    @FXML
    public void handleSearch(ActionEvent event) {
        String searchQuery = searchField.getText().trim();
        Category selectedCategory = categoryComboBox.getValue();
        
        // Perform search
        performSearch(searchQuery, currentFilter, selectedCategory);
    }

    @FXML
    public void showNotifications(ActionEvent event) {
        // Hide user menu if open
        if (userMenuPopup != null && userMenuPopup.isShowing()) {
            userMenuPopup.hide();
        }
        
        // Toggle notification popup
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
            return;
        }
        
        try {
            // Load the notification dropdown FXML
            Parent notificationDropdown = navigationService.loadNotificationDropdownMenu(notificationPopup);

            // Create and show popup
            notificationPopup = new Popup();
            notificationPopup.setAutoHide(true);
            notificationPopup.setHideOnEscape(true);
            notificationPopup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_RIGHT);
            notificationPopup.getContent().add(notificationDropdown);
            
            // Position popup relative to notification button
            notificationPopup.show(notificationsButton, 
                notificationsButton.localToScreen(notificationsButton.getBoundsInLocal()).getMaxX(),
                notificationsButton.localToScreen(notificationsButton.getBoundsInLocal()).getMaxY() + 5);
                
        } catch (Exception e) {
            System.err.println("Failed to show notifications dropdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void showUserMenu(ActionEvent event) {
        // Hide notification popup if open
        if (notificationPopup != null && notificationPopup.isShowing()) {
            notificationPopup.hide();
        }
        
        // Toggle user menu popup
        if (userMenuPopup != null && userMenuPopup.isShowing()) {
            userMenuPopup.hide();
            return;
        }
        
        try {
            // Load the user menu dropdown FXML
            Parent userMenuDropdown = navigationService.loadUserDropdownMenu(userMenuPopup);
            
            // Create and show popup
            userMenuPopup = new Popup();
            userMenuPopup.setAutoHide(true);
            userMenuPopup.setHideOnEscape(true);
            userMenuPopup.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_RIGHT);
            userMenuPopup.getContent().add(userMenuDropdown);
            
            // Position popup relative to user menu button
            userMenuPopup.show(userMenuButton,
                userMenuButton.localToScreen(userMenuButton.getBoundsInLocal()).getMaxX(),
                userMenuButton.localToScreen(userMenuButton.getBoundsInLocal()).getMaxY() + 5);
                
        } catch (Exception e) {
            System.err.println("Failed to show user menu dropdown: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void filterAll(ActionEvent event) {
        setCurrentFilter("all");
        if (isInSearchMode) {
            performCurrentSearch();
        } else {
            triggerSearch();
        }
    }

    @FXML
    public void filterAuctions(ActionEvent event) {
        setCurrentFilter("auctions");
        if (isInSearchMode) {
            performCurrentSearch();
        } else {
            triggerSearch();
        }
    }

    @FXML
    public void filterTrades(ActionEvent event) {
        setCurrentFilter("trades");
        if (isInSearchMode) {
            performCurrentSearch();
        } else {
            triggerSearch();
        }
    }

    @FXML
    public void filterSales(ActionEvent event) {
        setCurrentFilter("sales");
        if (isInSearchMode) {
            performCurrentSearch();
        } else {
            triggerSearch();
        }
    }

    @FXML
    public void filterGifts(ActionEvent event) {
        setCurrentFilter("gifts");
        if (isInSearchMode) {
            performCurrentSearch();
        } else {
            triggerSearch();
        }
    }
    
    private void setCurrentFilter(String filter) {
        currentFilter = filter;
        updateFilterButtons();
    }

    private void updateFilterButtons() {
        // Remove active class from all buttons
        allItemsButton.getStyleClass().removeAll("active");
        auctionsButton.getStyleClass().removeAll("active");
        tradesButton.getStyleClass().removeAll("active");
        giftsButton.getStyleClass().removeAll("active");
        salesButton.getStyleClass().removeAll("active");

        // Add active class to current filter button
        switch (currentFilter) {
            case "all" -> allItemsButton.getStyleClass().add("active");
            case "auctions" -> auctionsButton.getStyleClass().add("active");
            case "trades" -> tradesButton.getStyleClass().add("active");
            case "sales" -> salesButton.getStyleClass().add("active");
            case "gifts" -> giftsButton.getStyleClass().add("active");
        }
    }

    // Update the triggerSearch method to include category
    private void triggerSearch() {
        String searchQuery = searchField.getText().trim();
        Category selectedCategory = categoryComboBox.getValue();

        // Create enhanced search data
        SearchData searchData = new SearchData(searchQuery, currentFilter, selectedCategory);
        eventBus.publishEvent(EventTypes.SEARCH_REQUESTED, searchData);
    }

    private void performSearch(String query, String listingType, Category category) {
        System.out.println("Performing search: query='" + query + "', type='" + listingType + "', category=" + category);
        
        searchService.search(query, listingType, category)
            .thenAccept(searchResult -> Platform.runLater(() -> {
                isInSearchMode = true;
                updateContentWithSearchResults(searchResult);
                showSearchResultsMessage(searchResult);
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println("Search failed: " + ex.getMessage());
                    showSearchError(ex.getMessage());
                });
                return null;
            });
    }
    
    // Perform search with current parameters
    private void performCurrentSearch() {
        String query = searchField.getText().trim();
        Category selectedCategory = categoryComboBox.getValue();
        performSearch(query, currentFilter, selectedCategory);
    }
    
    // Add method to clear search and return to normal view
    public void clearSearch() {
        searchService.clearSearch();
        isInSearchMode = false;
        searchField.clear();
        categoryComboBox.setValue(Category.ALL);
        setCurrentFilter("all");
        
        // Load normal listings
        try {
            Parent homeView = navigationService.loadHomeView();
            setContent(homeView);
        } catch (Exception e) {
            System.err.println("Error returning to home view: " + e.getMessage());
        }
    }
    
    private void updateContentWithSearchResults(SearchService.SearchResult searchResult) {
        try {
            // Load the home view to get access to its containers
            Parent homeView = navigationService.loadHomeView();
            setContent(homeView);
            
            // Get the HomeController and update it with search results
            Object controller = homeView.getProperties().get("controller");
            if (controller instanceof HomeController) {
                HomeController homeController = (HomeController) controller;
                homeController.displaySearchResults(searchResult.getResults());
            }
            
        } catch (Exception e) {
            System.err.println("Error updating search results: " + e.getMessage());
        }
    }
    
    private void showSearchResultsMessage(SearchService.SearchResult searchResult) {
        String query = searchService.getLastQuery();
        String message;
        
        if (query.isEmpty()) {
            message = "Filtri applicati: " + searchResult.getTotalResults() + " risultati trovati";
        } else {
            message = "Risultati per '" + query + "': " + searchResult.getTotalResults() + " inserzioni trovate";
        }
        
        // You can update a status label or show a temporary message
        System.out.println(message);
    }
    
    private void showSearchError(String error) {
        // Handle search error - could show an alert or update UI
        System.err.println("Search error: " + error);
    }
    
    // Update the existing handleSearchRequest method
    private void handleSearchRequest(Object data) {
        if (data instanceof SearchData searchData) {
            performSearch(searchData.getQuery(), searchData.getFilter(), searchData.getCategory());
        }
    }
    
    // Add method to check if in search mode
    public boolean isInSearchMode() {
        return isInSearchMode;
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
        tradesButton.setText(localeService.getMessage("header.filter.trades"));
        salesButton.setText(localeService.getMessage("header.filter.sales"));
        giftsButton.setText(localeService.getMessage("header.filter.gifts"));

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

    public void toggleFavoritesDrawer(ActionEvent e) {
        favoritesDrawerIncludeController.drawerVisibleProperty()
                .set(!favoritesDrawerIncludeController.drawerVisibleProperty().get());
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
        private final Category category;

        public SearchData(String query, String filter, Category category) {
            this.query = query;
            this.filter = filter;
            this.category = category;
        }

        public String getQuery() {
            return query;
        }

        public String getFilter() {
            return filter;
        }

        public Category getCategory() {
            return category;
        }
        
        @Override
        public String toString() {
            return "SearchData{" +
                    "query='" + query + '\'' +
                    ", filter='" + filter + '\'' +
                    ", category=" + category +
                    '}';
        }
    }
}