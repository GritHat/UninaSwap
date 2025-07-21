package com.uninaswap.client.controller;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ListingService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NotificationService;
import com.uninaswap.client.viewmodel.ListingViewModel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController implements Refreshable {

    // Containers from FXML
    @FXML
    private FlowPane allListingsContainer;
    @FXML
    private HBox favoriteListingsContainer;
    @FXML
    private HBox UserCardBox;
    @FXML
    private HBox astePreferiteBox;
    
    // Add these new FXML fields
    @FXML
    private VBox auctionSection; // The entire auction section
    @FXML
    private VBox allListingsSection; // The entire all listings section
    @FXML
    private ScrollPane allListingsContainerWrapper;
    
    // Add pagination fields
    private static final int LISTINGS_PER_PAGE = 50;
    private int currentPage = 0;
    private boolean isLoadingMore = false;
    private boolean hasMoreListings = true;

    // Services
    private final UserCardController userCard = new UserCardController();
    private final ListingService listingService = ListingService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final NotificationService notificationService = NotificationService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();

    // Add flag to track listener registration
    private boolean listenerRegistered = false;

    // Add debouncing fields
    private Timeline updateTimeline;
    private static final Duration DEBOUNCE_DELAY = Duration.millis(100);

    // Add reference to favorite listings observable list
    private ObservableList<ListingViewModel> favoriteListingViewModels;

    // Add this field to track search state
    private boolean isDisplayingSearchResults = false;

    @FXML
    public void initialize() {
        System.out.println(localeService.getMessage("home.debug.initialized", "Home view initialized."));
        setupFavoritesListener();
        setupAllListingsScrollListener();

        // Load listings and set up data binding
        loadHomeData();

        // Listen for favorite changes to update the favorites section
        favoritesService.addListingFavoriteChangeListener(_ -> {
            Platform.runLater(() -> {
                updateFavoritesContainer();
            });
        });
        
        // Initial UI refresh
        refreshUI();
    }

    private void setupFavoritesListener() {
        // Get the observable list from FavoritesService
        favoriteListingViewModels = favoritesService.getFavoriteListingViewModels();

        // Set up listener for automatic updates
        favoriteListingViewModels.addListener((ListChangeListener<ListingViewModel>) change -> {
            Platform.runLater(this::updateFavoritesContainer);
        });
    }

    // NEW METHOD: Setup scroll listener for pagination
    private void setupAllListingsScrollListener() {
        Platform.runLater(() -> {
            // Add scroll listener for pagination
            allListingsContainerWrapper.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                // Check if we're near the bottom (95% scrolled)
                System.out.println(localeService.getMessage("home.debug.loading.more", "Loading more listings"));
                if (newValue.doubleValue() > 0.95 && !isLoadingMore && hasMoreListings) {
                    loadMoreListings();
                }
            });
        });
    }

    private void loadHomeData() {
        ObservableList<ListingViewModel> allListings = listingService.getAllListingsObservable();

        // Set up listener ONCE with debouncing
        if (!listenerRegistered) {
            allListings.addListener((ListChangeListener<ListingViewModel>) change -> {
                debouncedUpdate(allListings);
            });
            listenerRegistered = true;
        }

        // Reset pagination when loading initial data
        currentPage = 0;
        hasMoreListings = true;

        // Only refresh if the list is actually empty
        if (allListings.isEmpty()) {
            System.out.println(localeService.getMessage("home.debug.list.empty", "List is empty, refreshing from server..."));
            listingService.refreshAllListings();
        } else {
            System.out.println(localeService.getMessage("home.debug.list.has.items", "List already has {0} items, updating view...").replace("{0}", String.valueOf(allListings.size())));
            updateHomeViewWithListings(allListings);
        }

        // ALSO refresh favorites if empty
        if (favoriteListingViewModels.isEmpty()) {
            System.out.println(localeService.getMessage("home.debug.favorites.empty", "Favorites list is empty, refreshing from server..."));
            favoritesService.refreshUserFavorites();
        } else {
            updateFavoritesContainer();
        }
    }

    // NEW METHOD: Load more listings for pagination
    private void loadMoreListings() {
        if (isLoadingMore || !hasMoreListings) {
            return;
        }

        isLoadingMore = true;
        showLoadingIndicator();
        currentPage++;

        System.out.println(localeService.getMessage("home.debug.pagination.loading", "Loading more listings - page: {0}").replace("{0}", String.valueOf(currentPage)));

        // Set the flag in ListingService to indicate this is pagination
        listingService.setLoadingMore(true);

        listingService.getListings(currentPage, LISTINGS_PER_PAGE)
                .thenAccept(newListings -> Platform.runLater(() -> {
                    hideLoadingIndicator();
                    if (newListings == null || newListings.isEmpty()) {
                        hasMoreListings = false;
                        System.out.println(localeService.getMessage("home.debug.no.more.listings", "No more listings available"));
                    } else {
                        // The service will automatically append the listings due to the flag
                        // No need to manually add them here

                        System.out.println(localeService.getMessage("home.debug.loaded.more", "Loaded {0} more listings").replace("{0}", String.valueOf(newListings.size())));

                        // Check if we got less than requested (indicates last page)
                        if (newListings.size() < LISTINGS_PER_PAGE) {
                            hasMoreListings = false;
                            System.out.println(localeService.getMessage("home.debug.reached.end", "Reached end of listings"));
                        }
                    }
                    isLoadingMore = false;
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        hideLoadingIndicator();
                        listingService.setLoadingMore(false); // Reset flag on error
                        System.err.println(localeService.getMessage("home.error.loading.more", "Error loading more listings: {0}").replace("{0}", ex.getMessage()));
                        isLoadingMore = false;
                        currentPage--; // Reset page counter on error
                    });
                    return null;
                });
    }

    private void showLoadingIndicator() {
        if (allListingsContainer != null && !isLoadingMore) {
            VBox loadingBox = new VBox();
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.getStyleClass().add("loading-indicator");

            Text loadingText = new Text(localeService.getMessage("home.loading.text", "Loading..."));
            loadingText.getStyleClass().add("loading-text");

            loadingBox.getChildren().add(loadingText);
            allListingsContainer.getChildren().add(loadingBox);
        }
    }

    private void hideLoadingIndicator() {
        if (allListingsContainer != null) {
            allListingsContainer.getChildren().removeIf(node -> node.getStyleClass().contains("loading-indicator"));
        }
    }

    private void debouncedUpdate(ObservableList<ListingViewModel> listings) {
        // Cancel any existing timer
        if (updateTimeline != null) {
            updateTimeline.stop();
        }

        // Create new timer that will fire after a short delay
        updateTimeline = new Timeline(new KeyFrame(DEBOUNCE_DELAY, e -> {
            Platform.runLater(() -> {
                System.out.println(localeService.getMessage("home.debug.debounced.update", "Debounced update: List now has {0} listings").replace("{0}", String.valueOf(listings.size())));
                updateHomeViewWithListings(listings);
            });
        }));

        updateTimeline.play();
    }

    private Node createListingCard(ListingViewModel listing) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingCardView.fxml"));

        // Create controller with the listing data
        ListingCardController controller = new ListingCardController(listing);
        loader.setController(controller);

        // Load and return the card
        return loader.load();
    }

    // SIMPLIFIED: Use observable list directly instead of checking each listing
    private void updateFavoritesContainer() {
        if (favoriteListingsContainer != null) {
            favoriteListingsContainer.getChildren().clear();

            // Use the observable list directly - no need to check individual listings
            for (ListingViewModel listing : favoriteListingViewModels) {
                try {
                    Node favoriteCard = createListingCard(listing);
                    favoriteListingsContainer.getChildren().add(favoriteCard);
                } catch (IOException e) {
                    System.err.println(localeService.getMessage("home.error.creating.favorite.card", "Error creating favorite card: {0}").replace("{0}", e.getMessage()));
                }
            }

            // Add placeholder if no favorites
            if (favoriteListingsContainer.getChildren().isEmpty()) {
                javafx.scene.text.Text placeholder = new javafx.scene.text.Text(localeService.getMessage("home.favorites.empty", "No favorites found"));
                placeholder.getStyleClass().add("placeholder-text");
                favoriteListingsContainer.getChildren().add(placeholder);
            }
        }
    }

    /**
     * Display search results instead of normal listings
     */
    public void displaySearchResults(ObservableList<ListingViewModel> searchResults) {
        isDisplayingSearchResults = true;
        
        // Clear all containers
        clearAllContainers();
        
        // Hide auction section when showing search results
        if (auctionSection != null) {
            auctionSection.setVisible(false);
            auctionSection.setManaged(false);
        }
        
        // Ensure all listings section takes full space
        if (allListingsSection != null) {
            VBox.setVgrow(allListingsSection, Priority.ALWAYS);
            allListingsSection.setMaxHeight(Double.MAX_VALUE);
        }
        
        // Populate search results
        populateSearchResults(searchResults);
    }
    
    private void populateSearchResults(ObservableList<ListingViewModel> searchResults) {
        if (allListingsContainer != null) {
            allListingsContainer.getChildren().clear();
            
            if (searchResults.isEmpty()) {
                // Show "no results" message
                addNoResultsPlaceholder();
            } else {
                // Add search result cards
                for (ListingViewModel listing : searchResults) {
                    try {
                        Node listingCard = createListingCard(listing);
                        allListingsContainer.getChildren().add(listingCard);
                    } catch (Exception e) {
                        System.err.println(localeService.getMessage("home.error.creating.search.card", "Error creating search result card for: {0}").replace("{0}", listing.getTitle()));
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private void addNoResultsPlaceholder() {
        VBox noResultsContainer = new VBox(10);
        noResultsContainer.setAlignment(Pos.CENTER);
        noResultsContainer.getStyleClass().add("no-results-container");
        
        Text noResultsText = new Text(localeService.getMessage("home.search.no.results", "No results found"));
        noResultsText.getStyleClass().add("no-results-text");
        noResultsText.setStyle("-fx-font-size: 18px; -fx-fill: #666666; -fx-font-weight: bold;");
        
        Text suggestionText = new Text(localeService.getMessage("home.search.suggestion", "Try modifying your search or filters"));
        suggestionText.getStyleClass().add("suggestion-text");
        suggestionText.setStyle("-fx-font-size: 14px; -fx-fill: #888888;");
        
        noResultsContainer.getChildren().addAll(noResultsText, suggestionText);
        allListingsContainer.getChildren().add(noResultsContainer);
    }
    
    /**
     * Return to normal view (called when search is cleared)
     */
    public void returnToNormalView() {
        isDisplayingSearchResults = false;
        
        // Reload normal data
        loadHomeData();
    }
    
    /**
     * Check if currently displaying search results
     */
    public boolean isDisplayingSearchResults() {
        return isDisplayingSearchResults;
    }
    
    // UPDATED: Change to support FlowPane layout and auction visibility management
    private void updateHomeViewWithListings(ObservableList<ListingViewModel> listings) {
        // Don't update if we're showing search results
        if (isDisplayingSearchResults) {
            return;
        }
        
        // Clear existing content
        clearAllContainers();

        // Track if we have any auctions
        boolean hasAuctions = false;
        
        // Add all listings to the appropriate containers
        for (ListingViewModel listing : listings) {
            try {
                Node listingCard = createListingCard(listing);

                String listingType = listing.getListingTypeValue().toUpperCase();
                
                if ("AUCTION".equals(listingType)) {
                    // Add auctions to their separate horizontal container
                    if (astePreferiteBox != null) {
                        astePreferiteBox.getChildren().add(listingCard);
                        hasAuctions = true;
                    }
                } else {
                    // Add all other listings to the main grid container
                    if (allListingsContainer != null) {
                        allListingsContainer.getChildren().add(listingCard);
                    }
                }

            } catch (Exception e) {
                System.err.println(localeService.getMessage("home.error.creating.listing.card", "Error creating listing card for: {0}").replace("{0}", listing.getTitle()));
                e.printStackTrace();
            }
        }

        // Update auction section visibility and layout
        updateAuctionSectionVisibility(hasAuctions);

        // Add placeholder messages if containers are empty
        addPlaceholdersIfEmpty();
    }

    private void updateAuctionSectionVisibility(boolean hasAuctions) {
        if (auctionSection != null && allListingsSection != null) {
            // Show/hide auction section based on whether we have auctions
            auctionSection.setVisible(hasAuctions);
            auctionSection.setManaged(hasAuctions); // This removes it from layout when hidden
            
            if (hasAuctions) {
                // Reset all listings section to normal size
                VBox.setVgrow(allListingsSection, Priority.SOMETIMES);
                allListingsSection.setMaxHeight(Region.USE_COMPUTED_SIZE);
            } else {
                // Expand all listings section to fill the space
                VBox.setVgrow(allListingsSection, Priority.ALWAYS);
                allListingsSection.setMaxHeight(Double.MAX_VALUE);
            }
            
            System.out.println(localeService.getMessage("home.debug.auction.visibility", "Auction section visibility: {0}").replace("{0}", String.valueOf(hasAuctions)));
        }
    }

    private void clearAllContainers() {
        if (allListingsContainer != null) {
            // For FlowPane, just clear all children
            allListingsContainer.getChildren().clear();
        }

        if (favoriteListingsContainer != null) {
            favoriteListingsContainer.getChildren()
                    .removeIf(node -> !node.getClass().getSimpleName().contains("Include"));
        }

        if (astePreferiteBox != null) {
            // Clear all auction cards (both static and dynamic)
            astePreferiteBox.getChildren().clear();
        }
    }

    private void addPlaceholdersIfEmpty() {
        // Add placeholder text if no listings found
        if (allListingsContainer != null && allListingsContainer.getChildren().isEmpty() && !isLoadingMore) {
            javafx.scene.text.Text placeholder = new javafx.scene.text.Text(localeService.getMessage("home.listings.empty", "No listings available"));
            placeholder.getStyleClass().add("placeholder-text");
            allListingsContainer.getChildren().add(placeholder);
        }

        if (favoriteListingsContainer != null && favoriteListingsContainer.getChildren().size() <= 1) {
            javafx.scene.text.Text placeholder = new javafx.scene.text.Text(localeService.getMessage("home.favorites.empty", "No favorites found"));
            placeholder.getStyleClass().add("placeholder-text");
            favoriteListingsContainer.getChildren().add(placeholder);
        }

        // Don't add placeholder to auction section when it's hidden
        // The section will be completely hidden when there are no auctions
    }

    public void handleUserAction() {
        System.out.println(localeService.getMessage("home.debug.user.action", "User action handled in home view."));
    }

    @Override
    public void refreshUI() {
        // No direct UI text elements to update in HomeController since it's mostly dynamic content
        // However, we need to refresh any existing placeholder texts
        if (!isDisplayingSearchResults) {
            // Refresh favorites container to update any placeholder text
            updateFavoritesContainer();
            
            // If we have current listings, refresh the main view to update placeholders
            ObservableList<ListingViewModel> currentListings = listingService.getAllListingsObservable();
            if (!currentListings.isEmpty()) {
                addPlaceholdersIfEmpty(); // This will update placeholder texts with current locale
            }
        } else {
            // If showing search results, refresh the no results placeholder if it exists
            if (allListingsContainer != null && allListingsContainer.getChildren().stream()
                    .anyMatch(node -> node.getStyleClass().contains("no-results-container"))) {
                // Clear and re-add the no results placeholder with updated text
                allListingsContainer.getChildren().removeIf(node -> node.getStyleClass().contains("no-results-container"));
                addNoResultsPlaceholder();
            }
        }
    }
}
