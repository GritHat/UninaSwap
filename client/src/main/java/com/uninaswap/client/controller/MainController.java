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
import javafx.scene.image.Image;
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
import com.uninaswap.client.service.NotificationService;
import com.uninaswap.common.enums.Category;
import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;

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
    private ImageView addListingIcon;
    @FXML
    private ImageView favoritesIcon;
    @FXML
    private ImageView notificationsIcon;
    @FXML
    private ImageView userAvatarImage;

    @FXML
    private Button addListingButton;
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
    private final NotificationService notificationService = NotificationService.getInstance();

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

            // Initialize header button states
            initializeHeaderButtonStates();

            // Subscribe to login/logout events
            eventBus.subscribe(EventTypes.USER_LOGGED_IN, _ -> {
                Platform.runLater(() -> {
                    refreshAllViews();
                    initializeNotifications();
                });
            });

            eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
                Platform.runLater(() -> {
                    notificationService.clearNotifications();
                    // Update notification button to remove any badge
                    updateNotificationButtonBadge(0);
                });
            });

        } catch (Exception e) {
            System.err.println("Error initializing MainController: " + e.getMessage());
            e.printStackTrace();
        }

        updateNotificationButtonBadge(10);
    }

    private void initializeNotifications() {
        System.out.println("Initializing notifications in MainController...");

        // Initialize the notification service
        notificationService.initializeNotifications();

        // Set up unread count callback to update UI badge
        notificationService.setUnreadCountCallback(this::updateNotificationButtonBadge);
        
        // Set up callback for new notification alerts
        notificationService.setNewNotificationCallback(notification -> {
            Platform.runLater(() -> {
                // You could show a brief toast/alert here for new notifications
                System.out.println("New notification received: " + notification.getTitle());
                // Optionally show a brief visual indicator
                showNewNotificationAlert(notification);
            });
        });
    }

    private void updateNotificationButtonBadge(int unreadCount) {
        Platform.runLater(() -> {
            // Here you can update a badge on the notification button
            // For now, just log it - you could add a Badge overlay to the button
            System.out.println("Updating notification badge: " + unreadCount + " unread");

            // Example: Add a red dot or number badge to the notification button
            if (unreadCount > 0) {
                // Add visual indicator (you might want to add a badge overlay)
                notificationsButton.getStyleClass().add("has-notifications");
                // You could also set the button text to show count
                // notificationsButton.setText(String.valueOf(unreadCount));
            } else {
                notificationsButton.getStyleClass().remove("has-notifications");
                // notificationsButton.setText("");
            }
        });
    }

    private void showNewNotificationAlert(com.uninaswap.client.viewmodel.NotificationViewModel notification) {
        // Optional: Show a brief toast or visual alert for new notifications
        // This could be a small popup that appears briefly and disappears
        System.out.println("📢 " + notification.getTitle() + ": " + notification.getMessage());
    }

    private void initializeHeaderButtonStates() {
        // Set up click handlers with persistent state management
        setupHeaderButtonClickHandlers();
        
        // Initialize all button states
        updateAllHeaderButtonStates();
    }

    private void setupHeaderButtonClickHandlers() {
        // Add listing button - persistent selection when on create listing view
        if (addListingButton != null) {
            addListingButton.setOnAction(event -> {
                // Clear any sidebar selection when add listing is clicked
                if (sidebarIncludeController != null) {
                    sidebarIncludeController.clearAllSelections();
                }
                
                addListing(event);
                // Selection state will be managed by updateHeaderButtonStatesForContent()
            });
        }
        
        // Favorites button - persistent selection based on drawer visibility
        if (favoritesButton != null) {
            favoritesButton.setOnAction(event -> {
                toggleFavoritesDrawer(event);
                // Selection state will be managed by toggleFavoritesDrawer()
            });
        }
        
        // Notifications button - temporary selection while popup is open
        if (notificationsButton != null) {
            notificationsButton.setOnAction(event -> {
                boolean popupWasOpen = notificationPopup != null && notificationPopup.isShowing();
                
                showNotifications(event);
                
                // Update selection based on popup state
                updateNotificationButtonSelection();
            });
        }
        
        // User menu button - temporary selection while popup is open
        if (userMenuButton != null) {
            userMenuButton.setOnAction(event -> {
                boolean popupWasOpen = userMenuPopup != null && userMenuPopup.isShowing();
                
                showUserMenu(event);
                
                // Update selection based on popup state
                updateUserMenuButtonSelection();
            });
        }
    }

    private void updateNotificationButtonSelection() {
        if (notificationPopup != null && notificationPopup.isShowing()) {
            // Popup is open - select button
            setHeaderButtonSelected(notificationsButton, true);
            
            // Set up listener to clear selection when popup closes
            notificationPopup.setOnHidden(e -> {
                setHeaderButtonSelected(notificationsButton, false);
            });
        } else {
            // Popup is closed - deselect button
            setHeaderButtonSelected(notificationsButton, false);
        }
    }

    private void updateUserMenuButtonSelection() {
        if (userMenuPopup != null && userMenuPopup.isShowing()) {
            // Popup is open - select button
            setHeaderButtonSelected(userMenuButton, true);
            
            // Set up listener to clear selection when popup closes
            userMenuPopup.setOnHidden(e -> {
                setHeaderButtonSelected(userMenuButton, false);
            });
        } else {
            // Popup is closed - deselect button
            setHeaderButtonSelected(userMenuButton, false);
        }
    }

    private void setHeaderButtonSelected(Button button, boolean selected) {
        if (button == null) return;
        
        if (selected) {
            if (!button.getStyleClass().contains("selected")) {
                button.getStyleClass().add("selected");
                setWhiteHeaderIcon(button);
            }
        } else {
            button.getStyleClass().remove("selected");
            resetHeaderIconToNormal(button);
        }
    }

    private void resetHeaderIconToNormal(Button button) {
        ImageView iconView = getHeaderIconFromButton(button);
        String normalIconPath = getNormalHeaderIconPath(button);
        
        if (iconView != null && normalIconPath != null) {
            try {
                Image normalIcon = new Image(getClass().getResourceAsStream(normalIconPath));
                iconView.setImage(normalIcon);
            } catch (Exception e) {
                System.err.println("Could not load normal header icon: " + normalIconPath + " - " + e.getMessage());
            }
        }
    }

    private String getNormalHeaderIconPath(Button button) {
        if (button == addListingButton) return "/images/icons/add.png";
        if (button == favoritesButton) return "/images/icons/favorites_add.png";
        if (button == notificationsButton) return "/images/icons/notification.png";
        if (button == userMenuButton) return "/images/icons/default_profile.png"; // User avatar uses normal version
        return null;
    }

    private void updateAllHeaderButtonStates() {
        // Update add listing button based on current view
        updateAddListingButtonState();
        
        // Update favorites button based on drawer state
        updateFavoritesButtonState();
        
        // Notification and user menu buttons are handled by their respective methods
    }

    private void updateAddListingButtonState() {
        // Check if we're currently on a listing creation view
        boolean isOnCreationView = isCurrentViewListingCreation();
        setHeaderButtonSelected(addListingButton, isOnCreationView);
    }

    private void updateFavoritesButtonState() {
        // Check if favorites drawer is currently visible
        boolean drawerVisible = favoritesDrawerIncludeController.isDrawerVisible();
        setHeaderButtonSelected(favoritesButton, drawerVisible);
    }

    private boolean isCurrentViewListingCreation() {
        if (contentArea.getChildren().isEmpty()) {
            return false;
        }
        
        Node currentView = contentArea.getChildren().get(0);
        if (currentView instanceof Parent) {
            Object controller = ((Parent) currentView).getProperties().get("controller");
            if (controller != null) {
                String controllerName = controller.getClass().getSimpleName();
                return "ListingCreationController".equals(controllerName) || "SellController".equals(controllerName);
            }
        }
        return false;
    }

    private void setWhiteHeaderIcon(Button button) {
        ImageView iconView = getHeaderIconFromButton(button);
        String whiteIconPath = getWhiteHeaderIconPath(button);
        
        if (iconView != null && whiteIconPath != null) {
            try {
                Image whiteIcon = new Image(getClass().getResourceAsStream(whiteIconPath));
                iconView.setImage(whiteIcon);
            } catch (Exception e) {
                System.err.println("Could not load white header icon: " + whiteIconPath + " - " + e.getMessage());
            }
        }
    }

    private void setNormalHeaderIcon(Button button, ImageView iconView, String normalIconPath) {
        if (iconView != null && normalIconPath != null) {
            try {
                Image normalIcon = new Image(getClass().getResourceAsStream(normalIconPath));
                iconView.setImage(normalIcon);
            } catch (Exception e) {
                System.err.println("Could not load normal header icon: " + normalIconPath + " - " + e.getMessage());
            }
        }
    }

    private ImageView getHeaderIconFromButton(Button button) {
        if (button == addListingButton) return addListingIcon;
        if (button == favoritesButton) return favoritesIcon;
        if (button == notificationsButton) return notificationsIcon;
        if (button == userMenuButton) return userAvatarImage; // But we don't change this one
        return null;
    }

    private String getWhiteHeaderIconPath(Button button) {
        if (button == addListingButton) return "/images/icons/add_w.png";
        if (button == favoritesButton) return "/images/icons/favorites_add_w.png";
        if (button == notificationsButton) return "/images/icons/notification_w.png";
        // User avatar doesn't have a white version
        return null;
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
            categoryComboBox.valueProperty().addListener((_, oldValue, newValue) -> {
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
        boolean currentlyVisible = favoritesDrawerIncludeController.isDrawerVisible();
        
        favoritesDrawerIncludeController.drawerVisibleProperty()
                .set(!currentlyVisible);
        
        // Update favorites button selection based on new drawer state
        updateFavoritesButtonState();
    }

    @FXML
    public void addListing(ActionEvent event) {
        try {
            Parent addListingView = navigationService.loadListingCreationView();
            setContent(addListingView);
            // Button selection will be updated by updateHeaderButtonStatesForContent()
        } catch (java.io.IOException e) {
            e.printStackTrace();
            // Make sure button state reflects actual state even on error
            updateAddListingButtonState();
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
        
        // Update header button states based on current view
        updateHeaderButtonStatesForContent(newContent);
    }

    private void updateHeaderButtonStatesForContent(Parent content) {
        // Update add listing button based on the new content
        updateAddListingButtonState();
        
        // Favorites button state is independent of content changes
        // Notification and user menu buttons are handled by their popup state
    }

    public void updateHeaderButtonSelection(String buttonType) {
        switch (buttonType.toLowerCase()) {
            case "addlisting", "create" -> updateAddListingButtonState();
            case "favorites" -> updateFavoritesButtonState();
            case "notifications" -> updateNotificationButtonSelection();
            case "usermenu" -> updateUserMenuButtonSelection();
            case "refresh", "update" -> updateAllHeaderButtonStates();
        }
    }

    public void updateSidebarButtonSelection(String buttonType) {
        if (sidebarIncludeController != null) {
            switch (buttonType.toLowerCase()) {
                case "home" -> sidebarIncludeController.selectHomeButton();
                case "offers" -> sidebarIncludeController.selectOffersButton();
                case "inventory" -> sidebarIncludeController.selectInventoryButton();
                case "alerts", "notifications" -> sidebarIncludeController.selectNotificationsButton();
                case "addlisting", "create" -> sidebarIncludeController.selectAddListingButton();
                case "listings" -> sidebarIncludeController.selectListingsButton();
                case "profile" -> sidebarIncludeController.selectProfileButton();
            }
        }
    }

    public void sidebarClearAllSelection() {
        sidebarIncludeController.clearAllSelections();
    }

    public void onLeavingListingCreation() {
        updateAddListingButtonState();
    }

    // Add method to be called when drawer state changes
    public void onFavoritesDrawerStateChanged() {
        updateFavoritesButtonState();
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
