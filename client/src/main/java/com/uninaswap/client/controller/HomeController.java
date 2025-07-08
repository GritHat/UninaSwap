package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ListingService;
import com.uninaswap.common.dto.ListingDTO;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

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
    private final FavoritesService favoritesService = FavoritesService.getInstance(); // Add this

    @FXML
    public void initialize() {
        System.out.println("Home view initialized.");

        // Load user cards
        userCard.loadUserCardsIntoTab(UserCardBox);

        // Load listings and set up data binding
        loadHomeData();

        // Listen for favorite changes to update the favorites section
        favoritesService.addListingFavoriteChangeListener(listingId -> {
            Platform.runLater(() -> {
                // Refresh the favorites container when favorites change
                updateFavoritesContainer();
            });
        });
    }

    private void loadHomeData() {
        // Refresh listings from server
        listingService.refreshAllListings();

        // Get the observable list
        ObservableList<ListingDTO> allListings = listingService.getAllListingsObservable();

        // Debug log
        if (allListings.isEmpty()) {
            System.out.println("La lista è vuota o non inizializzata");
        } else {
            System.out.println("Caricate " + allListings.size() + " inserzioni");
        }

        // Listen for changes in the listings and update UI
        allListings.addListener((ListChangeListener<ListingDTO>) change -> {
            Platform.runLater(() -> {
                System.out.println("Lista aggiornata con " + allListings.size() + " inserzioni");
                updateHomeViewWithListings(allListings);
            });
        });

        // If there are already listings, update immediately
        if (!allListings.isEmpty()) {
            updateHomeViewWithListings(allListings);
        }
    }

    private void updateHomeViewWithListings(ObservableList<ListingDTO> listings) {
        // Clear existing content
        clearAllContainers();

        // Separate listings by type and favorites
        for (ListingDTO listing : listings) {
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

                // Add to favorites container only if it's actually a favorite
                if (favoriteListingsContainer != null &&
                        favoritesService.isFavoriteListing(listing.getId())) {
                    Node favoriteCard = createListingCard(listing);
                    favoriteListingsContainer.getChildren().add(favoriteCard);
                }

            } catch (Exception e) {
                System.err.println("Error creating listing card for: " + listing.getTitle());
                e.printStackTrace();
            }
        }

        // Add placeholder messages if containers are empty
        addPlaceholdersIfEmpty();
    }

    private Node createListingCard(ListingDTO listing) throws IOException {
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

    private void updateFavoritesContainer() {
        if (favoriteListingsContainer != null) {
            favoriteListingsContainer.getChildren().clear();

            ObservableList<ListingDTO> allListings = listingService.getAllListingsObservable();
            for (ListingDTO listing : allListings) {
                if (favoritesService.isFavoriteListing(listing.getId())) {
                    try {
                        Node favoriteCard = createListingCard(listing);
                        favoriteListingsContainer.getChildren().add(favoriteCard);
                    } catch (IOException e) {
                        System.err.println("Error creating favorite card: " + e.getMessage());
                    }
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

    public void handleUserAction() {
        System.out.println("User action handled in home view.");
    }
}
