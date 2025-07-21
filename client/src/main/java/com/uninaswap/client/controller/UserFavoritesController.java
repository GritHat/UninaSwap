package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class UserFavoritesController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label totalFavoritesLabel;

    @FXML
    private TableView<ListingViewModel> favoritesTable;

    @FXML
    private TableColumn<ListingViewModel, String> listingTitleColumn;

    @FXML
    private TableColumn<ListingViewModel, String> listingTypeColumn;

    @FXML
    private TableColumn<ListingViewModel, String> listingPriceColumn;

    @FXML
    private TableColumn<ListingViewModel, String> listingDateColumn;

    @FXML
    private TableColumn<ListingViewModel, Void> listingActionsColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    // Data - Use the same observable list as FavoritesService
    private UserViewModel currentUser;
    private ObservableList<ListingViewModel> favoriteListings;
    private ObservableList<FavoriteViewModel> userFavorites;

    @FXML
    public void initialize() {
        setupLabels();
        setupObservableLists();
        setupTable();
        updateCounts();
    }

    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("favorites.title", "Favorite Listings"));
        refreshButton.setText(localeService.getMessage("favorites.refresh", "Refresh"));
        closeButton.setText(localeService.getMessage("favorites.close", "Close"));
    }

    private void setupObservableLists() {
        // Get the observable lists from FavoritesService (same as FavoritesDrawer)
        favoriteListings = favoritesService.getFavoriteListingViewModels();
        userFavorites = favoritesService.getUserFavoritesList();

        // Set up listener for automatic updates (like FavoritesDrawer)
        favoriteListings.addListener((ListChangeListener<ListingViewModel>) change -> {
            Platform.runLater(() -> {
                updateCounts();
                // Table is already bound to favoriteListings, so it will update automatically
            });
        });

        // Additional listener for user favorites to catch any changes
        userFavorites.addListener((ListChangeListener<FavoriteViewModel>) change -> {
            Platform.runLater(() -> {
                updateCounts();
            });
        });
    }

    private void setupTable() {
        listingTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        listingTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getListingTypeValue()));

        listingPriceColumn.setCellValueFactory(cellData -> {
            ListingViewModel listing = cellData.getValue();
            if (listing instanceof SellListingViewModel) {
                if (((SellListingViewModel)listing).getPrice() != null && (((SellListingViewModel)listing).getPrice().compareTo(BigDecimal.valueOf(0))) > 0) {
                    return new SimpleStringProperty(String.format("%.2f %s", ((SellListingViewModel)listing).getPrice(), ((SellListingViewModel)listing).getCurrency()));
                }
            }
            
            return new SimpleStringProperty("N/A");
        });

        listingDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Setup actions column
        setupActionsColumn();

        // Bind table directly to the FavoritesService observable list
        favoritesTable.setItems(favoriteListings);
    }

    private void setupActionsColumn() {
        listingActionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button removeButton = new Button("Remove");
            private final HBox actionBox = new HBox(5, viewButton, removeButton);

            {
                viewButton.getStyleClass().add("primary-button");
                removeButton.getStyleClass().add("danger-button");

                viewButton.setOnAction(e -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleViewListing(listing);
                });

                removeButton.setOnAction(e -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleRemoveFavorite(listing);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    public void setUser(UserViewModel user) {
        this.currentUser = user;

        if (user != null) {
            Platform.runLater(() -> {
                updateUserInfo();
                // No need to call loadFavorites() since we're using observable lists
                // If the list is empty, trigger a refresh
                if (favoriteListings.isEmpty()) {
                    favoritesService.refreshUserFavorites();
                }
            });
        }
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getDisplayName());
            updateCounts();
        }
    }

    private void updateCounts() {
        totalFavoritesLabel.setText(String.format(
                localeService.getMessage("favorites.total.count", "%d favorites"),
                favoriteListings.size()));
    }

    // Remove the old loadFavorites method since we're using observable lists

    private void handleViewListing(ListingViewModel listing) {
        try {
            navigationService.navigateToListingDetails(listing);
        } catch (Exception e) {
            System.err.println("Failed to navigate to listing details: " + e.getMessage());
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    "Failed to open listing details: " + e.getMessage());
        }
    }

    private void handleRemoveFavorite(ListingViewModel listing) {
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("favorites.remove.title", "Remove Favorite"),
                localeService.getMessage("favorites.remove.header", "Confirm Removal"),
                localeService.getMessage("favorites.remove.message",
                        "Are you sure you want to remove \"" + listing.getTitle() + "\" from favorites?"));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Use the FavoritesService to remove (same as other controllers)
                favoritesService.removeFavoriteFromServer(listing.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            // No need to manually remove from list - the observable list will update automatically
                            // via the FavoritesService message handler

                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("favorites.remove.success.title", "Success"),
                                    localeService.getMessage("favorites.remove.success.header", "Favorite Removed"),
                                    localeService.getMessage("favorites.remove.success.message",
                                            "Listing removed from favorites"));
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> AlertHelper.showErrorAlert(
                                    localeService.getMessage("favorites.error.title", "Error"),
                                    localeService.getMessage("favorites.error.remove", "Failed to remove favorite"),
                                    ex.getMessage()));
                            return null;
                        });
            }
        });
    }

    @FXML
    private void handleRefresh() {
        // Simply trigger refresh via FavoritesService - observable lists will update automatically
        favoritesService.refreshUserFavorites();
        updateCounts();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    // Add method to check if favorites are loaded
    public boolean hasFavorites() {
        return !favoriteListings.isEmpty();
    }

    // Add method to get favorites count
    public int getFavoritesCount() {
        return favoriteListings.size();
    }

    // Add method to refresh data (can be called externally)
    public void refreshData() {
        favoritesService.refreshUserFavorites();
    }
}