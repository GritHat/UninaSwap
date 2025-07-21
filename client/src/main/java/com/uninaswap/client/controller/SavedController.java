package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.client.mapper.ViewModelMapper;

import java.util.List;

public class SavedController implements Refreshable {

    // Main container elements
    @FXML
    private TabPane favoritesTabPane;
    @FXML
    private Tab itemsTab;
    @FXML
    private Tab usersTab;
    @FXML
    private Tab auctionsTab;

    // Items tab elements
    @FXML
    private ScrollPane itemsScrollPane;
    @FXML
    private FlowPane itemsFlowPane;

    // Users tab elements
    @FXML
    private ScrollPane usersScrollPane;
    @FXML
    private FlowPane usersFlowPane;

    // Auctions tab elements
    @FXML
    private ScrollPane auctionsScrollPane;
    @FXML
    private VBox auctionsVBox;

    // Sample UI elements for localization
    @FXML
    private Text auctionTitleText;
    @FXML
    private Text auctionExpiryText;
    @FXML
    private Text currentBidText;
    @FXML
    private Text priceText;
    @FXML
    private Text yourBidText;
    @FXML
    private Button bidAgainButton;
    @FXML
    private Button viewAuctionButton;
    @FXML
    private Button visitUserButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();

    // Data
    private ObservableList<ListingViewModel> favoriteItems = FXCollections.observableArrayList();
    private ObservableList<UserViewModel> favoriteUsers = FXCollections.observableArrayList();
    private ObservableList<AuctionViewModel> favoriteAuctions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Verify user is logged in
        if (!sessionService.isLoggedIn()) {
            try {
                navigationService.navigateToLogin(favoritesTabPane);
                return;
            } catch (Exception e) {
                System.err.println(localeService.getMessage("saved.debug.navigation.error", "Error navigating to login: {0}")
                    .replace("{0}", e.getMessage()));
                return;
            }
        }

        setupTabs();
        setupEventHandlers();
        loadFavorites();
        
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });

        // Subscribe to favorites updates
        EventBusService.getInstance().subscribe(EventTypes.FAVORITES_UPDATED, _ -> {
            Platform.runLater(this::loadFavorites);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("saved.debug.initialized", "SavedController initialized"));
    }

    @Override
    public void refreshUI() {
        // Update tab labels
        if (itemsTab != null) {
            itemsTab.setText(localeService.getMessage("favorites.tab.items", "Items"));
        }
        if (usersTab != null) {
            usersTab.setText(localeService.getMessage("favorites.tab.users", "Users"));
        }
        if (auctionsTab != null) {
            auctionsTab.setText(localeService.getMessage("favorites.tab.auctions", "Auctions"));
        }

        // Update auction sample elements (these are template elements)
        if (auctionTitleText != null) {
            auctionTitleText.setText(localeService.getMessage("favorites.auction.title", "Laptop Auction"));
        }
        if (auctionExpiryText != null) {
            auctionExpiryText.setText(localeService.getMessage("favorites.auction.expiry", "Expires in 2h 15m"));
        }
        if (currentBidText != null) {
            currentBidText.setText(localeService.getMessage("favorites.auction.current.bid", "Current bid"));
        }
        if (priceText != null) {
            priceText.setText(localeService.getMessage("favorites.auction.price", "€450.00"));
        }
        if (yourBidText != null) {
            yourBidText.setText(localeService.getMessage("favorites.auction.your.bid", "Your bid"));
        }

        // Update button text
        if (bidAgainButton != null) {
            bidAgainButton.setText(localeService.getMessage("button.bid.again", "Bid Again"));
        }
        if (viewAuctionButton != null) {
            viewAuctionButton.setText(localeService.getMessage("button.view", "View"));
        }
        if (visitUserButton != null) {
            visitUserButton.setText(localeService.getMessage("button.visit", "Visit"));
        }

        // Refresh existing content with current locale
        refreshTabContent();
    }

    private void setupTabs() {
        // Set up tab selection handlers
        favoritesTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == itemsTab) {
                loadFavoriteItems();
                System.out.println(localeService.getMessage("saved.debug.tab.items", "Items tab selected"));
            } else if (newTab == usersTab) {
                loadFavoriteUsers();
                System.out.println(localeService.getMessage("saved.debug.tab.users", "Users tab selected"));
            } else if (newTab == auctionsTab) {
                loadFavoriteAuctions();
                System.out.println(localeService.getMessage("saved.debug.tab.auctions", "Auctions tab selected"));
            }
        });
    }

    private void setupEventHandlers() {
        // Subscribe to item favorited/unfavorited events
        EventBusService.getInstance().subscribe(EventTypes.ITEM_FAVORITED, data -> {
            Platform.runLater(this::loadFavoriteItems);
            System.out.println(localeService.getMessage("saved.debug.item.favorited", "Item favorited, refreshing items"));
        });

        EventBusService.getInstance().subscribe(EventTypes.ITEM_UNFAVORITED, data -> {
            Platform.runLater(this::loadFavoriteItems);
            System.out.println(localeService.getMessage("saved.debug.item.unfavorited", "Item unfavorited, refreshing items"));
        });

        // Subscribe to user favorited/unfavorited events
        EventBusService.getInstance().subscribe(EventTypes.USER_FAVORITED, data -> {
            Platform.runLater(this::loadFavoriteUsers);
            System.out.println(localeService.getMessage("saved.debug.user.favorited", "User favorited, refreshing users"));
        });

        EventBusService.getInstance().subscribe(EventTypes.USER_UNFAVORITED, data -> {
            Platform.runLater(this::loadFavoriteUsers);
            System.out.println(localeService.getMessage("saved.debug.user.unfavorited", "User unfavorited, refreshing users"));
        });
    }

    private void loadFavorites() {
        loadFavoriteItems();
        loadFavoriteUsers();
        loadFavoriteAuctions();
        System.out.println(localeService.getMessage("saved.debug.favorites.loaded", "All favorites loaded"));
    }

    private void loadFavoriteItems() {
        try {
            favoritesService.getFavoriteItems()
                .thenAccept(items -> Platform.runLater(() -> {
                    favoriteItems.clear();
                    favoriteItems.addAll(items.stream()
                        .map(ViewModelMapper.getInstance()::toViewModel)
                        .toList());
                    updateItemsDisplay();
                    System.out.println(localeService.getMessage("saved.debug.items.loaded", "Loaded {0} favorite items")
                        .replace("{0}", String.valueOf(items.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showEmptyItemsMessage();
                        System.err.println(localeService.getMessage("saved.debug.items.error", "Error loading favorite items: {0}")
                            .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
        } catch (Exception e) {
            showEmptyItemsMessage();
            System.err.println(localeService.getMessage("saved.debug.items.exception", "Exception loading favorite items: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void loadFavoriteUsers() {
        try {
            favoritesService.getFavoriteUsers()
                .thenAccept(users -> Platform.runLater(() -> {
                    favoriteUsers.clear();
                    favoriteUsers.addAll(users.stream()
                        .map(ViewModelMapper.getInstance()::toViewModel)
                        .toList());
                    updateUsersDisplay();
                    System.out.println(localeService.getMessage("saved.debug.users.loaded", "Loaded {0} favorite users")
                        .replace("{0}", String.valueOf(users.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showEmptyUsersMessage();
                        System.err.println(localeService.getMessage("saved.debug.users.error", "Error loading favorite users: {0}")
                            .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
        } catch (Exception e) {
            showEmptyUsersMessage();
            System.err.println(localeService.getMessage("saved.debug.users.exception", "Exception loading favorite users: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void loadFavoriteAuctions() {
        try {
            favoritesService.getFavoriteAuctions()
                .thenAccept(auctions -> Platform.runLater(() -> {
                    favoriteAuctions.clear();
                    favoriteAuctions.addAll(auctions.stream()
                        .map(ViewModelMapper.getInstance()::toAuctionViewModel)
                        .toList());
                    updateAuctionsDisplay();
                    System.out.println(localeService.getMessage("saved.debug.auctions.loaded", "Loaded {0} favorite auctions")
                        .replace("{0}", String.valueOf(auctions.size())));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showEmptyAuctionsMessage();
                        System.err.println(localeService.getMessage("saved.debug.auctions.error", "Error loading favorite auctions: {0}")
                            .replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
        } catch (Exception e) {
            showEmptyAuctionsMessage();
            System.err.println(localeService.getMessage("saved.debug.auctions.exception", "Exception loading favorite auctions: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void updateItemsDisplay() {
        if (itemsFlowPane == null) return;

        itemsFlowPane.getChildren().clear();

        if (favoriteItems.isEmpty()) {
            showEmptyItemsMessage();
            return;
        }

        // Create item cards for each favorite item
        for (ListingViewModel item : favoriteItems) {
            try {
                // Load item card and set the item data
                // This would normally load the ItemCard.fxml and set up the controller
                VBox itemCard = createItemCard(item);
                itemsFlowPane.getChildren().add(itemCard);
            } catch (Exception e) {
                System.err.println(localeService.getMessage("saved.debug.item.card.error", "Error creating item card: {0}")
                    .replace("{0}", e.getMessage()));
            }
        }
    }

    private void updateUsersDisplay() {
        if (usersFlowPane == null) return;

        usersFlowPane.getChildren().clear();

        if (favoriteUsers.isEmpty()) {
            showEmptyUsersMessage();
            return;
        }

        // Create user cards for each favorite user
        for (UserViewModel user : favoriteUsers) {
            try {
                VBox userCard = createUserCard(user);
                usersFlowPane.getChildren().add(userCard);
            } catch (Exception e) {
                System.err.println(localeService.getMessage("saved.debug.user.card.error", "Error creating user card: {0}")
                    .replace("{0}", e.getMessage()));
            }
        }
    }

    private void updateAuctionsDisplay() {
        if (auctionsVBox == null) return;

        auctionsVBox.getChildren().clear();

        if (favoriteAuctions.isEmpty()) {
            showEmptyAuctionsMessage();
            return;
        }

        // Create auction cards for each favorite auction
        for (AuctionViewModel auction : favoriteAuctions) {
            try {
                VBox auctionCard = createAuctionCard(auction);
                auctionsVBox.getChildren().add(auctionCard);
            } catch (Exception e) {
                System.err.println(localeService.getMessage("saved.debug.auction.card.error", "Error creating auction card: {0}")
                    .replace("{0}", e.getMessage()));
            }
        }
    }

    private VBox createItemCard(ListingViewModel item) {
        // This is a simplified version - in practice, you'd load the actual ItemCard.fxml
        VBox card = new VBox();
        card.getStyleClass().add("item-card");
        
        Label titleLabel = new Label(item.getTitle());
        titleLabel.getStyleClass().add("item-title");
        
        Label priceLabel = new Label(item.getPriceDisplayText());
        priceLabel.getStyleClass().add("item-price");
        
        Button viewButton = new Button(localeService.getMessage("button.view", "View"));
        viewButton.setOnAction(e -> handleViewItem(item));
        
        card.getChildren().addAll(titleLabel, priceLabel, viewButton);
        return card;
    }

    private VBox createUserCard(UserViewModel user) {
        VBox card = new VBox();
        card.getStyleClass().add("user-card");
        
        Label nameLabel = new Label(user.getDisplayName());
        nameLabel.getStyleClass().add("user-name");
        
        Button visitButton = new Button(localeService.getMessage("button.visit", "Visit"));
        visitButton.setOnAction(e -> handleVisitUser(user));
        
        card.getChildren().addAll(nameLabel, visitButton);
        return card;
    }

    private VBox createAuctionCard(AuctionViewModel auction) {
        VBox card = new VBox();
        card.getStyleClass().add("auction-card");
        
        Label titleLabel = new Label(auction.getTitle());
        titleLabel.getStyleClass().add("auction-title");
        
        Label bidLabel = new Label(localeService.getMessage("favorites.auction.current.bid", "Current bid") + 
            ": " + auction.getCurrentBidDisplay());
        bidLabel.getStyleClass().add("auction-bid");
        
        Button bidButton = new Button(localeService.getMessage("button.bid.again", "Bid Again"));
        bidButton.setOnAction(e -> handleBidOnAuction(auction));
        
        Button viewButton = new Button(localeService.getMessage("button.view", "View"));
        viewButton.setOnAction(e -> handleViewAuction(auction));
        
        card.getChildren().addAll(titleLabel, bidLabel, bidButton, viewButton);
        return card;
    }

    private void showEmptyItemsMessage() {
        if (itemsFlowPane == null) return;
        
        itemsFlowPane.getChildren().clear();
        Label emptyLabel = new Label(localeService.getMessage("favorites.empty.items", "No favorite items yet"));
        emptyLabel.getStyleClass().addAll("empty-message", "subtitle-text");
        itemsFlowPane.getChildren().add(emptyLabel);
    }

    private void showEmptyUsersMessage() {
        if (usersFlowPane == null) return;
        
        usersFlowPane.getChildren().clear();
        Label emptyLabel = new Label(localeService.getMessage("favorites.empty.users", "No favorite users yet"));
        emptyLabel.getStyleClass().addAll("empty-message", "subtitle-text");
        usersFlowPane.getChildren().add(emptyLabel);
    }

    private void showEmptyAuctionsMessage() {
        if (auctionsVBox == null) return;
        
        auctionsVBox.getChildren().clear();
        Label emptyLabel = new Label(localeService.getMessage("favorites.empty.auctions", "No favorite auctions yet"));
        emptyLabel.getStyleClass().addAll("empty-message", "subtitle-text");
        auctionsVBox.getChildren().add(emptyLabel);
    }

    private void refreshTabContent() {
        // Refresh the currently active tab
        Tab selectedTab = favoritesTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == itemsTab) {
            updateItemsDisplay();
        } else if (selectedTab == usersTab) {
            updateUsersDisplay();
        } else if (selectedTab == auctionsTab) {
            updateAuctionsDisplay();
        }
    }

    // Event handlers
    private void handleViewItem(ListingViewModel item) {
        try {
            navigationService.navigateToListingDetails(item);
            System.out.println(localeService.getMessage("saved.debug.view.item", "Navigating to item details: {0}")
                .replace("{0}", item.getTitle()));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("saved.debug.navigation.item.error", "Error navigating to item: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void handleVisitUser(UserViewModel user) {
        try {
            navigationService.navigateToUserProfile(user);
            System.out.println(localeService.getMessage("saved.debug.visit.user", "Navigating to user profile: {0}")
                .replace("{0}", user.getDisplayName()));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("saved.debug.navigation.user.error", "Error navigating to user: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void handleViewAuction(AuctionViewModel auction) {
        try {
            navigationService.navigateToAuctionDetails(auction);
            System.out.println(localeService.getMessage("saved.debug.view.auction", "Navigating to auction details: {0}")
                .replace("{0}", auction.getTitle()));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("saved.debug.navigation.auction.error", "Error navigating to auction: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    private void handleBidOnAuction(AuctionViewModel auction) {
        try {
            navigationService.navigateToAuctionBidding(auction);
            System.out.println(localeService.getMessage("saved.debug.bid.auction", "Navigating to auction bidding: {0}")
                .replace("{0}", auction.getTitle()));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("saved.debug.navigation.bid.error", "Error navigating to auction bidding: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    // Public methods for external access
    public void refreshFavorites() {
        loadFavorites();
        System.out.println(localeService.getMessage("saved.debug.refresh.requested", "Favorites refresh requested"));
    }

    public int getFavoriteItemsCount() {
        return favoriteItems.size();
    }

    public int getFavoriteUsersCount() {
        return favoriteUsers.size();
    }

    public int getFavoriteAuctionsCount() {
        return favoriteAuctions.size();
    }

    public void selectItemsTab() {
        if (favoritesTabPane != null) {
            favoritesTabPane.getSelectionModel().select(itemsTab);
        }
    }

    public void selectUsersTab() {
        if (favoritesTabPane != null) {
            favoritesTabPane.getSelectionModel().select(usersTab);
        }
    }

    public void selectAuctionsTab() {
        if (favoritesTabPane != null) {
            favoritesTabPane.getSelectionModel().select(auctionsTab);
        }
    }
}
