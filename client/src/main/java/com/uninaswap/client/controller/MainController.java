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
import com.uninaswap.client.service.Refreshable;
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

    @FXML
    private ComboBox<Category> categoryComboBox;

    // Services
    private final NavigationService navigationService;
    private final LocaleService localeService;
    private final UserSessionService sessionService;
    private final EventBusService eventBus = EventBusService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final CategoryService categoryService = CategoryService.getInstance();
    private final SearchService searchService = SearchService.getInstance();
    private final NotificationService notificationService = NotificationService.getInstance();

    // State
    private String currentFilter = "all";
    private boolean isInSearchMode = false;

    private Popup notificationPopup;
    private Popup userMenuPopup;

    // Add a field to track the badge
    private Label notificationBadge;

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

            initializeNotifications();
            
            // Initial UI refresh
            refreshUI();
            
            System.out.println(localeService.getMessage("main.debug.initialized", "MainController initialized"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("main.error.initialization", "Error initializing MainController: {0}").replace("{0}", e.getMessage()));
            e.printStackTrace();
        }
    }

    private void initializeNotifications() {
        System.out.println(localeService.getMessage("main.debug.notifications.initializing", "Initializing notifications in MainController..."));

        // Initialize the notification service
        notificationService.initializeNotifications();

        // Set up unread count callback to update UI badge
        notificationService.setUnreadCountCallback(this::updateNotificationButtonBadge);
        
        // Set up callback for new notification alerts
        notificationService.setNewNotificationCallback(notification -> {
            Platform.runLater(() -> {
                // You could show a brief toast/alert here for new notifications
                System.out.println(localeService.getMessage("main.debug.notification.received", "New notification received: {0}").replace("{0}", notification.getTitle()));
                // Optionally show a brief visual indicator
                showNewNotificationAlert(notification);
            });
        });
    }

    private void updateNotificationButtonBadge(int unreadCount) {
        System.out.println(localeService.getMessage("main.debug.badge.updating", "updating badge {0}").replace("{0}", String.valueOf(unreadCount)));
        Platform.runLater(() -> {
            // Remove existing badge if present
            if (notificationBadge != null && notificationsButton.getParent() instanceof StackPane) {
                StackPane parent = (StackPane) notificationsButton.getParent();
                parent.getChildren().remove(notificationBadge);
                notificationBadge = null;
            }
            
            // Add badge if there are unread notifications
            if (unreadCount > 0) {
                createNotificationBadge(unreadCount);
            }
            
            System.out.println(localeService.getMessage("main.debug.badge.updated", "Notification badge updated: {0} unread").replace("{0}", String.valueOf(unreadCount)));
        });
    }
    
    private void createNotificationBadge(int unreadCount) {
        // Make sure the button is wrapped in a StackPane for proper positioning
        if (!(notificationsButton.getParent() instanceof StackPane)) {
            // We need to wrap the button in a StackPane
            wrapNotificationButtonInStackPane();
        }
        
        StackPane buttonContainer = (StackPane) notificationsButton.getParent();
        
        // Create the badge
        notificationBadge = new Label();
        
        if (unreadCount > 99) {
            notificationBadge.setText(localeService.getMessage("main.badge.overflow", "99+"));
        } else if (unreadCount > 0) {
            notificationBadge.setText(String.valueOf(unreadCount));
        }
        
        // Apply CSS styling
        notificationBadge.getStyleClass().add("notification-badge");
        
        // Position the badge in the top-right corner
        StackPane.setAlignment(notificationBadge, javafx.geometry.Pos.TOP_RIGHT);
        notificationBadge.setTranslateY(5);   // Move slightly down from top edge
        
        // Add to the container
        buttonContainer.getChildren().add(notificationBadge);
        
        // Make sure badge is on top
        notificationBadge.toFront();
        notificationBadge.setMouseTransparent(true);
    }
    
    private void wrapNotificationButtonInStackPane() {
        // Get the current parent and index
        javafx.scene.Parent currentParent = notificationsButton.getParent();
        if (currentParent instanceof javafx.scene.layout.HBox) {
            HBox hbox = (HBox) currentParent;
            int buttonIndex = hbox.getChildren().indexOf(notificationsButton);
            
            // Remove button from current parent
            hbox.getChildren().remove(notificationsButton);
            
            // Create StackPane wrapper
            StackPane wrapper = new StackPane();
            wrapper.getChildren().add(notificationsButton);
            
            // Add wrapper back to original position
            hbox.getChildren().add(buttonIndex, wrapper);
        }
    }
    
    private void showNewNotificationAlert(com.uninaswap.client.viewmodel.NotificationViewModel notification) {
        // Optional: Show a brief toast or visual alert for new notifications
        // This could be a small popup that appears briefly and disappears
        System.out.println(localeService.getMessage("main.notification.alert", "📢 {0}: {1}")
            .replace("{0}", notification.getTitle())
            .replace("{1}", notification.getMessage()));
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
                showNotifications(event);
                
                // Update selection based on popup state
                updateNotificationButtonSelection();
            });
        }
        
        // User menu button - temporary selection while popup is open
        if (userMenuButton != null) {
            userMenuButton.setOnAction(event -> {
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
                System.err.println(localeService.getMessage("main.error.icon.load", "Could not load normal header icon: {0} - {1}")
                    .replace("{0}", normalIconPath)
                    .replace("{1}", e.getMessage()));
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
                System.err.println(localeService.getMessage("main.error.white.icon.load", "Could not load white header icon: {0} - {1}")
                    .replace("{0}", whiteIconPath)
                    .replace("{1}", e.getMessage()));
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
                        return localeService.getMessage("header.category.all", "All Categories");
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
                    System.out.println(localeService.getMessage("main.debug.category.changed", "Category changed to: {0}").replace("{0}", newValue.toString()));
                    if (isInSearchMode) {
                        performCurrentSearch();
                    } else {
                        triggerSearch();
                    }
                }
            });
        } else {
            System.err.println(localeService.getMessage("main.error.category.combo.null", "categoryComboBox is null - check FXML fx:id"));
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
            System.err.println(localeService.getMessage("main.error.notifications.dropdown", "Failed to show notifications dropdown: {0}").replace("{0}", e.getMessage()));
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
            System.err.println(localeService.getMessage("main.error.usermenu.dropdown", "Failed to show user menu dropdown: {0}").replace("{0}", e.getMessage()));
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
        System.out.println(localeService.getMessage("main.debug.search.performing", "Performing search: query='{0}', type='{1}', category={2}")
            .replace("{0}", query)
            .replace("{1}", listingType)
            .replace("{2}", category != null ? category.toString() : "null"));
        
        searchService.search(query, listingType, category)
            .thenAccept(searchResult -> Platform.runLater(() -> {
                isInSearchMode = true;
                updateContentWithSearchResults(searchResult);
                showSearchResultsMessage(searchResult);
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println(localeService.getMessage("main.error.search.failed", "Search failed: {0}").replace("{0}", ex.getMessage()));
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
            System.err.println(localeService.getMessage("main.error.home.view.return", "Error returning to home view: {0}").replace("{0}", e.getMessage()));
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
            System.err.println(localeService.getMessage("main.error.search.results.update", "Error updating search results: {0}").replace("{0}", e.getMessage()));
        }
    }
    
    private void showSearchResultsMessage(SearchService.SearchResult searchResult) {
        String query = searchService.getLastQuery();
        String message;
        
        if (query.isEmpty()) {
            message = localeService.getMessage("main.search.results.filters", "Filters applied: {0} results found")
                .replace("{0}", String.valueOf(searchResult.getTotalResults()));
        } else {
            message = localeService.getMessage("main.search.results.query", "Results for '{0}': {1} listings found")
                .replace("{0}", query)
                .replace("{1}", String.valueOf(searchResult.getTotalResults()));
        }
        
        // You can update a status label or show a temporary message
        System.out.println(message);
    }
    
    private void showSearchError(String error) {
        // Handle search error - could show an alert or update UI
        System.err.println(localeService.getMessage("main.error.search.general", "Search error: {0}").replace("{0}", error));
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
                    System.err.println(localeService.getMessage("main.error.avatar.load", "Failed to load user avatar: {0}").replace("{0}", ex.getMessage()));
                    return null;
                });
    }

    private void checkAuthentication() {
        if (!sessionService.isLoggedIn()) {
            Platform.runLater(() -> {
                try {
                    navigationService.navigateToLogin(usernameLabel);
                } catch (Exception e) {
                    System.err.println(localeService.getMessage("main.error.authentication.redirect", "User is not logged in, redirecting to login screen"));
                }
            });
        }
    }

    private void refreshAllViews() {
        refreshUI();
        refreshCurrentContentView();
    }

    @Override
    public void refreshUI() {
        // Update existing labels
        if (statusLabel != null) {
            statusLabel.setText(localeService.getMessage("label.ready", "Ready"));
        }
        if (contentAreaSubtitleLabel != null) {
            contentAreaSubtitleLabel.setText(localeService.getMessage("main.content.title", "Welcome to UninaSwap"));
        }
        if (contentAreaTitleLabel != null) {
            contentAreaTitleLabel.setText(localeService.getMessage("main.content.subtitle", "Select an option from the sidebar to get started"));
        }

        // Update header elements
        if
