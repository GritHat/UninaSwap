package com.uninaswap.client.controller;

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
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.IOException;

public class HomeController {

    // Containers from FXML
    @FXML
    private HBox allListingsContainer;
    @FXML
    private HBox favoriteListingsContainer;
    @FXML
    private HBox UserCardBox;
    @FXML
    private HBox astePreferiteBox;

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

        // Load user cards
        userCard.loadUserCardsIntoTab(UserCardBox);

        setupFavoritesListener();

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

    private void loadHomeData() {
        ObservableList<ListingViewModel> allListings = listingService.getAllListingsObservable();

        // Set up listener ONCE with debouncing
        if (!listenerRegistered) {
            allListings.addListener((ListChangeListener<ListingViewModel>) change -> {
                debouncedUpdate(allListings);
            });
            listenerRegistered = true;
        }

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

    private void updateHomeViewWithListings(ObservableList<ListingViewModel> listings) {
        // Clear existing content
        clearAllContainers();

        // Separate listings by type (but DON'T handle favorites here)
        for (ListingViewModel listing : listings) {
            try {
                Node listingCard = createListingCard(listing);

                // Add to appropriate container based on listing type
                String listingType = listing.getListingTypeValue().toUpperCase();

                switch (listingType) {
                    case "AUCTION":
                        if (astePreferiteBox != null) {
                            astePreferiteBox.getChildren().add(listingCard);
                        }
                        break;

                    default:
                        // Add all non-auction listings to the main container
                        if (allListingsContainer != null) {
                            allListingsContainer.getChildren().add(listingCard);
                        }
                        break;
                }

                // DON'T add to favorites container here - it's handled by
                // updateFavoritesContainer()

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
            // Remove all children except the fx:include placeholder
            allListingsContainer.getChildren().removeIf(node -> !node.getClass().getSimpleName().contains("Include"));
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
        if (allListingsContainer != null && allListingsContainer.getChildren().size() <= 1) {
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
