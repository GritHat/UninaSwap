package com.uninaswap.client.controller;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ListingService;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController {

    // Containers from FXML
    @FXML
    private FlowPane allListingsContainer; // Changed from HBox to FlowPane
    @FXML
    private HBox favoriteListingsContainer;
    @FXML
    private HBox UserCardBox;
    @FXML
    private HBox astePreferiteBox;
    @FXML
    private ScrollPane allListingsScrollPane;
    // Add pagination fields
    private static final int LISTINGS_PER_PAGE = 50;
    private int currentPage = 0;
    private boolean isLoadingMore = false;
    private boolean hasMoreListings = true;

    private final UserCardController userCard = new UserCardController();
    private final ListingService listingService = ListingService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();

    // Add flag to track listener registration
    private boolean listenerRegistered = false;

    // Add debouncing fields
    private Timeline updateTimeline;
    private static final Duration DEBOUNCE_DELAY = Duration.millis(100);

    // Add reference to favorite listings observable list
    private ObservableList<ListingViewModel> favoriteListingViewModels;

    @FXML
    public void initialize() {
        System.out.println("Home view initialized.");
        setupFavoritesListener();
        setupAllListingsScrollListener(); // NEW METHOD

        // Load listings and set up data binding
        loadHomeData();

        // Listen for favorite changes to update the favorites section
        favoritesService.addListingFavoriteChangeListener(_ -> {
            Platform.runLater(() -> {
                updateFavoritesContainer();
            });
        });
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
            // Find the ScrollPane parent of allListingsContainer
            Parent parent = allListingsContainer.getParent();
            while (parent != null && !(parent instanceof ScrollPane)) {
                parent = parent.getParent();
            }

            if (parent instanceof ScrollPane) {
                allListingsScrollPane = (ScrollPane) parent;

                // Add scroll listener for pagination
                allListingsScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                    // Check if we're near the bottom (95% scrolled)
                    if (newValue.doubleValue() > 0.95 && !isLoadingMore && hasMoreListings) {
                        loadMoreListings();
                    }
                });
            }
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
            System.out.println("List is empty, refreshing from server...");
            listingService.refreshAllListings();
        } else {
            System.out.println("List already has " + allListings.size() + " items, updating view...");
            updateHomeViewWithListings(allListings);
        }

        // ALSO refresh favorites if empty
        if (favoriteListingViewModels.isEmpty()) {
            System.out.println("Favorites list is empty, refreshing from server...");
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

        System.out.println("Loading more listings - page: " + currentPage);

        listingService.getListings(currentPage, LISTINGS_PER_PAGE)
                .thenAccept(newListings -> Platform.runLater(() -> {
                    hideLoadingIndicator();
                    if (newListings == null || newListings.isEmpty()) {
                        hasMoreListings = false;
                        System.out.println("No more listings available");
                    } else {
                        // Convert DTOs to ViewModels and add to the observable list
                        List<ListingViewModel> newViewModels = newListings.stream()
                                .map(ViewModelMapper.getInstance()::toViewModel)
                                .collect(Collectors.toList());

                        // Add to the service's observable list (this will trigger UI update)
                        listingService.getAllListingsObservable().addAll(newViewModels);

                        System.out.println("Loaded " + newListings.size() + " more listings");

                        // Check if we got less than requested (indicates last page)
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
                        System.err.println("Error loading more listings: " + ex.getMessage());
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
        // Cancel any existing timer
        if (updateTimeline != null) {
            updateTimeline.stop();
        }

        // Create new timer that will fire after a short delay
        updateTimeline = new Timeline(new KeyFrame(DEBOUNCE_DELAY, e -> {
            Platform.runLater(() -> {
                System.out.println("Debounced update: List now has " + listings.size() + " listings");
                updateHomeViewWithListings(listings);
            });
        }));

        updateTimeline.play();
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
                    System.err.println("Error creating favorite card: " + e.getMessage());
                }
            }

            // Add placeholder if no favorites
            if (favoriteListingsContainer.getChildren().isEmpty()) {
                javafx.scene.text.Text placeholder = new javafx.scene.text.Text("Nessun preferito trovato");
                placeholder.getStyleClass().add("placeholder-text");
                favoriteListingsContainer.getChildren().add(placeholder);
            }
        }
    }

    // UPDATED: Change to support FlowPane layout instead of separating by type
    private void updateHomeViewWithListings(ObservableList<ListingViewModel> listings) {
        // Clear existing content
        clearAllContainers();

        // Add all listings to the grid layout (no separation by type for main
        // container)
        for (ListingViewModel listing : listings) {
            try {
                Node listingCard = createListingCard(listing);

                // Add all non-auction listings to the main container
                String listingType = listing.getListingTypeValue().toUpperCase();

                if ("AUCTION".equals(listingType)) {
                    // Keep auctions in their separate horizontal container
                    if (astePreferiteBox != null) {
                        astePreferiteBox.getChildren().add(listingCard);
                    }
                } else {
                    // Add all other listings to the main grid container
                    if (allListingsContainer != null) {
                        allListingsContainer.getChildren().add(listingCard);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error creating listing card for: " + listing.getTitle());
                e.printStackTrace();
            }
        }

        // Add placeholder messages if containers are empty
        addPlaceholdersIfEmpty();
    }

    private Node createListingCard(ListingViewModel listing) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingCardView.fxml"));

        // Create controller with the listing data
        ListingCardController controller = new ListingCardController(listing);
        loader.setController(controller);

        // Load and return the card
        return loader.load();
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
            // Keep the static auction card, remove dynamic ones
            astePreferiteBox.getChildren().removeIf(node -> node.getStyleClass().contains("dynamic-listing-card"));
        }
    }

    private void addPlaceholdersIfEmpty() {
        // Add placeholder text if no listings found
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
