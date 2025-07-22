package com.uninaswap.client.controller;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ListingService;
import com.uninaswap.client.viewmodel.ListingViewModel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

public class HomeController {
    @FXML
    private FlowPane allListingsContainer;
    @FXML
    private HBox favoriteListingsContainer;
    @FXML
    private HBox UserCardBox;
    @FXML
    private HBox astePreferiteBox;
    @FXML
    private VBox auctionSection;
    @FXML
    private VBox allListingsSection;
    @FXML
    private ScrollPane allListingsContainerWrapper;
    
    private static final int LISTINGS_PER_PAGE = 50;
    private int currentPage = 0;
    private boolean isLoadingMore = false;
    private boolean hasMoreListings = true;

    private final ListingService listingService = ListingService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private boolean listenerRegistered = false;
    private Timeline updateTimeline;
    private static final Duration DEBOUNCE_DELAY = Duration.millis(100);
    private ObservableList<ListingViewModel> favoriteListingViewModels;
    private boolean isDisplayingSearchResults = false;

    @FXML
    public void initialize() {
        System.out.println("Home view initialized.");
        setupFavoritesListener();
        setupAllListingsScrollListener();
        loadHomeData();
        favoritesService.addListingFavoriteChangeListener(_ -> {
            Platform.runLater(() -> {
                updateFavoritesContainer();
            });
        });
    }

    private void setupFavoritesListener() {
        favoriteListingViewModels = favoritesService.getFavoriteListingViewModels();
        favoriteListingViewModels.addListener((ListChangeListener<ListingViewModel>) _ -> {
            Platform.runLater(this::updateFavoritesContainer);
        });
    }

    private void setupAllListingsScrollListener() {
        Platform.runLater(() -> {
            allListingsContainerWrapper.vvalueProperty().addListener((_, _, newValue) -> {
                System.out.println("loading more listings");
                if (newValue.doubleValue() > 0.95 && !isLoadingMore && hasMoreListings) {
                    loadMoreListings();
                }
            });
        });
    }

    private void loadHomeData() {
        ObservableList<ListingViewModel> allListings = listingService.getAllListingsObservable();
        if (!listenerRegistered) {
            allListings.addListener((ListChangeListener<ListingViewModel>) _ -> {
                debouncedUpdate(allListings);
            });
            listenerRegistered = true;
        }
        currentPage = 0;
        hasMoreListings = true;
        if (allListings.isEmpty()) {
            System.out.println("List is empty, refreshing from server...");
            listingService.refreshAllListings();
        } else {
            System.out.println("List already has " + allListings.size() + " items, updating view...");
            updateHomeViewWithListings(allListings);
        }
        if (favoriteListingViewModels.isEmpty()) {
            System.out.println("Favorites list is empty, refreshing from server...");
            favoritesService.refreshUserFavorites();
        } else {
            updateFavoritesContainer();
        }
    }
    private void loadMoreListings() {
        if (isLoadingMore || !hasMoreListings) {
            return;
        }

        isLoadingMore = true;
        showLoadingIndicator();
        currentPage++;

        System.out.println("Loading more listings - page: " + currentPage);
        listingService.setLoadingMore(true);

        listingService.getListings(currentPage, LISTINGS_PER_PAGE)
                .thenAccept(newListings -> Platform.runLater(() -> {
                    hideLoadingIndicator();
                    if (newListings == null || newListings.isEmpty()) {
                        hasMoreListings = false;
                        System.out.println("No more listings available");
                    } else {
                        System.out.println("Loaded " + newListings.size() + " more listings");

                        if (newListings.size() < LISTINGS_PER_PAGE) {
                            hasMoreListings = false;
                            System.out.println("Reached end of listings");
                        }
                    }
                    isLoadingMore = false;
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        hideLoadingIndicator();
                        listingService.setLoadingMore(false);
                        System.err.println("Error loading more listings: " + ex.getMessage());
                        isLoadingMore = false;
                        currentPage--;
                    });
                    return null;
                });
    }

    private void showLoadingIndicator() {
        if (allListingsContainer != null && !isLoadingMore) {
            VBox loadingBox = new VBox();
            loadingBox.setAlignment(Pos.CENTER);
            loadingBox.getStyleClass().add("loading-indicator");

            Text loadingText = new Text("Caricamento...");
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
        if (updateTimeline != null) {
            updateTimeline.stop();
        }
        updateTimeline = new Timeline(new KeyFrame(DEBOUNCE_DELAY, _ -> {
            Platform.runLater(() -> {
                System.out.println("Debounced update: List now has " + listings.size() + " listings");
                updateHomeViewWithListings(listings);
            });
        }));

        updateTimeline.play();
    }

    private Node createListingCard(ListingViewModel listing) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingCardView.fxml"));
        ListingCardController controller = new ListingCardController(listing);
        loader.setController(controller);

        return loader.load();
    }

    private void updateFavoritesContainer() {
        if (favoriteListingsContainer != null) {
            favoriteListingsContainer.getChildren().clear();
            for (ListingViewModel listing : favoriteListingViewModels) {
                try {
                    Node favoriteCard = createListingCard(listing);
                    favoriteListingsContainer.getChildren().add(favoriteCard);
                } catch (IOException e) {
                    System.err.println("Error creating favorite card: " + e.getMessage());
                }
            }
            if (favoriteListingsContainer.getChildren().isEmpty()) {
                javafx.scene.text.Text placeholder = new javafx.scene.text.Text("Nessun preferito trovato");
                placeholder.getStyleClass().add("placeholder-text");
                favoriteListingsContainer.getChildren().add(placeholder);
            }
        }
    }

    /**
     * Display search results instead of normal listings
     * 
     * @param searchResults The results to display
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
    
    /**
     * Populate the search results into the allListingsContainer
     * 
     * @param searchResults The search results to display
     */
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
                        System.err.println("Error creating search result card for: " + listing.getTitle());
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
        
        Text noResultsText = new Text("Nessun risultato trovato");
        noResultsText.getStyleClass().add("no-results-text");
        noResultsText.setStyle("-fx-font-size: 18px; -fx-fill: #666666; -fx-font-weight: bold;");
        
        Text suggestionText = new Text("Prova a modificare i filtri o la ricerca");
        suggestionText.getStyleClass().add("suggestion-text");
        suggestionText.setStyle("-fx-font-size: 14px; -fx-fill: #888888;");
        
        noResultsContainer.getChildren().addAll(noResultsText, suggestionText);
        allListingsContainer.getChildren().add(noResultsContainer);
    }

    /**
     * Display listings for a specific user (called from profile view)
     * This reuses the existing search results infrastructure
     * 
     * @param userId The ID of the user whose listings to display
     */
    public void displayUserListings(Long userId) {
        listingService.getUserListings(userId)
            .thenAccept(listings -> Platform.runLater(() -> {
                List<ListingViewModel> listingViewModels = listings.stream()
                        .map(ViewModelMapper.getInstance()::toViewModel)
                        .collect(Collectors.toList());
                ObservableList<ListingViewModel> userListingsResults = FXCollections.observableArrayList(listingViewModels);
                displaySearchResults(userListingsResults);
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    System.err.println("Error loading user listings for home display: " + ex.getMessage());
                    isDisplayingSearchResults = true;
                    clearAllContainers();
                    if (auctionSection != null) {
                        auctionSection.setVisible(false);
                        auctionSection.setManaged(false);
                    }
                    addUserListingsErrorPlaceholder();
                });
                return null;
            });
    }

    private void addUserListingsErrorPlaceholder() {
        if (allListingsContainer != null) {
            allListingsContainer.getChildren().clear();
            
            VBox errorContainer = new VBox(10);
            errorContainer.setAlignment(Pos.CENTER);
            errorContainer.getStyleClass().add("no-results-container");
            
            Text errorText = new Text("Errore nel caricamento");
            errorText.getStyleClass().add("no-results-text");
            errorText.setStyle("-fx-font-size: 18px; -fx-fill: #d32f2f; -fx-font-weight: bold;");
            
            Text errorDetailText = new Text("Non è stato possibile caricare le inserzioni dell'utente");
            errorDetailText.getStyleClass().add("suggestion-text");
            errorDetailText.setStyle("-fx-font-size: 14px; -fx-fill: #888888;");
            
            errorContainer.getChildren().addAll(errorText, errorDetailText);
            allListingsContainer.getChildren().add(errorContainer);
        }
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
    
    private void updateHomeViewWithListings(ObservableList<ListingViewModel> listings) {
        if (isDisplayingSearchResults) {
            return;
        }
        clearAllContainers();
        boolean hasAuctions = false;
        for (ListingViewModel listing : listings) {
            try {
                Node listingCard = createListingCard(listing);
                String listingType = listing.getListingTypeValue().toUpperCase();
                if ("AUCTION".equals(listingType)) {
                    if (astePreferiteBox != null) {
                        astePreferiteBox.getChildren().add(listingCard);
                        hasAuctions = true;
                    }
                } else {
                    if (allListingsContainer != null) {
                        allListingsContainer.getChildren().add(listingCard);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error creating listing card for: " + listing.getTitle());
                e.printStackTrace();
            }
        }
        updateAuctionSectionVisibility(hasAuctions);
        addPlaceholdersIfEmpty();
    }

    private void updateAuctionSectionVisibility(boolean hasAuctions) {
        if (auctionSection != null && allListingsSection != null) {
            auctionSection.setVisible(hasAuctions);
            auctionSection.setManaged(hasAuctions);
            if (hasAuctions) {
                VBox.setVgrow(allListingsSection, Priority.SOMETIMES);
                allListingsSection.setMaxHeight(Region.USE_COMPUTED_SIZE);
            } else {
                VBox.setVgrow(allListingsSection, Priority.ALWAYS);
                allListingsSection.setMaxHeight(Double.MAX_VALUE);
            }
            System.out.println("Auction section visibility: " + hasAuctions);
        }
    }

    private void clearAllContainers() {
        if (allListingsContainer != null) {
            allListingsContainer.getChildren().clear();
        }
        if (favoriteListingsContainer != null) {
            favoriteListingsContainer.getChildren()
                    .removeIf(node -> !node.getClass().getSimpleName().contains("Include"));
        }
        if (astePreferiteBox != null) {
            astePreferiteBox.getChildren().clear();
        }
    }

    private void addPlaceholdersIfEmpty() {
        if (allListingsContainer != null && allListingsContainer.getChildren().isEmpty() && !isLoadingMore) {
            javafx.scene.text.Text placeholder = new javafx.scene.text.Text("Nessuna inserzione disponibile");
            placeholder.getStyleClass().add("placeholder-text");
            allListingsContainer.getChildren().add(placeholder);
        }
        if (favoriteListingsContainer != null && favoriteListingsContainer.getChildren().size() <= 1) {
            javafx.scene.text.Text placeholder = new javafx.scene.text.Text("Nessun preferito trovato");
            placeholder.getStyleClass().add("placeholder-text");
            favoriteListingsContainer.getChildren().add(placeholder);
        }
    }

    public void handleUserAction() {
        System.out.println("User action handled in home view.");
    }
}
