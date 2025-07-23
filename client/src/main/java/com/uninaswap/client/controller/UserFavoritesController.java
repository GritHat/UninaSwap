package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * 
 */
public class UserFavoritesController {

    /**
     * 
     */
    @FXML
    private Label titleLabel;

    /**
     * 
     */
    @FXML
    private Label userNameLabel;

    /**
     * 
     */
    @FXML
    private Label totalFavoritesLabel;

    /**
     * 
     */
    @FXML
    private TableView<ListingViewModel> favoritesTable;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> listingTitleColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> listingTypeColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> listingPriceColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> listingDateColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, Void> listingActionsColumn;

    /**
     * 
     */
    @FXML
    private Button refreshButton;

    /**
     * 
     */
    @FXML
    private Button closeButton;

    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();
    /**
     * 
     */
    private UserViewModel currentUser;
    /**
     * 
     */
    private ObservableList<ListingViewModel> favoriteListings;
    /**
     * 
     */
    private ObservableList<FavoriteViewModel> userFavorites;

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupLabels();
        setupObservableLists();
        setupTable();
        updateCounts();
    }

    /**
     * 
     */
    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("favorites.title", "Favorite Listings"));
        refreshButton.setText(localeService.getMessage("favorites.refresh", "Refresh"));
        closeButton.setText(localeService.getMessage("favorites.close", "Close"));
    }

    /**
     * 
     */
    private void setupObservableLists() {
        favoriteListings = favoritesService.getFavoriteListingViewModels();
        userFavorites = favoritesService.getUserFavoritesList();
        favoriteListings.addListener((ListChangeListener<ListingViewModel>) _ -> {
            Platform.runLater(() -> {
                updateCounts();
            });
        });

        userFavorites.addListener((ListChangeListener<FavoriteViewModel>) _ -> {
            Platform.runLater(() -> {
                updateCounts();
            });
        });
    }

    /**
     * 
     */
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
        setupActionsColumn();
        favoritesTable.setItems(favoriteListings);
    }

    /**
     * 
     */
    private void setupActionsColumn() {
        listingActionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button removeButton = new Button("Remove");
            private final HBox actionBox = new HBox(5, viewButton, removeButton);

            {
                viewButton.getStyleClass().add("primary-button");
                removeButton.getStyleClass().add("danger-button");

                viewButton.setOnAction(_ -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleViewListing(listing);
                });

                removeButton.setOnAction(_ -> {
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

    /**
     * @param user
     */
    public void setUser(UserViewModel user) {
        this.currentUser = user;

        if (user != null) {
            Platform.runLater(() -> {
                updateUserInfo();
                if (favoriteListings.isEmpty()) {
                    favoritesService.refreshUserFavorites();
                }
            });
        }
    }

    /**
     * 
     */
    private void updateUserInfo() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getDisplayName());
            updateCounts();
        }
    }

    /**
     * 
     */
    private void updateCounts() {
        totalFavoritesLabel.setText(String.format(
                localeService.getMessage("favorites.total.count"),
                favoriteListings.size()));
    }

    /**
     * @param listing
     */
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

    /**
     * @param listing
     */
    private void handleRemoveFavorite(ListingViewModel listing) {
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("favorites.remove.title", "Remove Favorite"),
                localeService.getMessage("favorites.remove.header", "Confirm Removal"),
                localeService.getMessage("favorites.remove.message",
                        "Are you sure you want to remove \"" + listing.getTitle() + "\" from favorites?"));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                favoritesService.removeFavoriteFromServer(listing.getId())
                        .thenAccept(_ -> Platform.runLater(() -> {
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

    /**
     * 
     */
    @FXML
    private void handleRefresh() {
        favoritesService.refreshUserFavorites();
        updateCounts();
    }

    /**
     * 
     */
    @FXML
    private void handleClose() {
        try {
            navigationService.navigateToHomeView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to home view: " + e.getMessage());
        }
    }

    /**
     * @return
     */
    public boolean hasFavorites() {
        return !favoriteListings.isEmpty();
    }
    
    /**
     * @return
     */
    public int getFavoritesCount() {
        return favoriteListings.size();
    }

    /**
     * 
     */
    public void refreshData() {
        favoritesService.refreshUserFavorites();
    }
}