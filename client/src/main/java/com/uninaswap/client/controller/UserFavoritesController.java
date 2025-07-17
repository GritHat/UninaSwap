package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.FavoriteViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.common.dto.ListingDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class UserFavoritesController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label totalFavoritesLabel;

    @FXML
    private TableView<ListingDTO> favoritesTable;

    @FXML
    private TableColumn<ListingDTO, String> listingTitleColumn;

    @FXML
    private TableColumn<ListingDTO, String> listingTypeColumn;

    @FXML
    private TableColumn<ListingDTO, String> listingPriceColumn;

    @FXML
    private TableColumn<ListingDTO, String> listingDateColumn;

    @FXML
    private TableColumn<ListingDTO, Void> listingActionsColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button closeButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    // Data
    private UserViewModel currentUser;
    private final ObservableList<ListingDTO> favoriteListings = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupLabels();
        setupTable();
        updateCounts();
    }

    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("favorites.title", "Favorite Listings"));
        refreshButton.setText(localeService.getMessage("favorites.refresh", "Refresh"));
        closeButton.setText(localeService.getMessage("favorites.close", "Close"));
    }

    private void setupTable() {
        listingTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        listingTypeColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getListingTypeValue()));

        listingPriceColumn.setCellValueFactory(cellData -> {
            ListingDTO listing = cellData.getValue();
            // You'll need to implement price display logic based on listing type
            return new SimpleStringProperty("N/A"); // Placeholder
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

        // Bind table to observable list
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
                    ListingDTO listing = getTableView().getItems().get(getIndex());
                    handleViewListing(listing);
                });

                removeButton.setOnAction(e -> {
                    ListingDTO listing = getTableView().getItems().get(getIndex());
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
                loadFavorites();
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

    private void loadFavorites() {
        if (currentUser == null)
            return;

        favoritesService.getUserFavorites()
                .thenAccept(favorites -> Platform.runLater(() -> {
                    favoriteListings.clear();
                    favorites.forEach(favoriteDTO -> {
                        if (favoriteDTO.getListing() != null) {
                            favoriteListings.add(favoriteDTO.getListing());
                        }
                    });
                    updateCounts();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> AlertHelper.showErrorAlert(
                            localeService.getMessage("favorites.error.title", "Error"),
                            localeService.getMessage("favorites.error.load", "Failed to load favorites"),
                            ex.getMessage()));
                    return null;
                });
    }

    private void handleViewListing(ListingDTO listing) {
        // Navigate to listing details
        System.out.println("View listing: " + listing.getTitle());
        // You can implement navigation to listing details here
    }

    private void handleRemoveFavorite(ListingDTO listing) {
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("favorites.remove.title", "Remove Favorite"),
                localeService.getMessage("favorites.remove.header", "Confirm Removal"),
                localeService.getMessage("favorites.remove.message",
                        "Are you sure you want to remove \"" + listing.getTitle() + "\" from favorites?"));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                favoritesService.removeFavoriteFromServer(listing.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            favoriteListings.removeIf(l -> l.getId().equals(listing.getId()));
                            updateCounts();
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
        updateCounts();
        loadFavorites();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}