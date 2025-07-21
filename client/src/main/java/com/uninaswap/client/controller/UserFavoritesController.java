package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
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

public class UserFavoritesController implements Refreshable {

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
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });

        setupLabels();
        setupObservableLists();
        setupTable();
        updateCounts();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("user.favorites.debug.initialized", "UserFavorites controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update all text labels
        setupLabels();
        
        // Update table column headers
        updateTableHeaders();
        
        // Update action buttons in table
        refreshActionButtons();
        
        // Update user info if user is set
        if (currentUser != null) {
            updateUserInfo();
        }
        
        // Update counts display
        updateCounts();
        
        System.out.println(localeService.getMessage("user.favorites.debug.ui.refreshed", "UserFavorites UI refreshed"));
    }

    private void setupLabels() {
        if (titleLabel != null) {
            titleLabel.setText(localeService.getMessage("user.favorites.title", "Favorite Listings"));
        }
        if (refreshButton != null) {
            refreshButton.setText(localeService.getMessage("user.favorites.button.refresh", "Refresh"));
        }
        if (closeButton != null) {
            closeButton.setText(localeService.getMessage("user.favorites.button.close", "Close"));
        }
    }

    private void updateTableHeaders() {
        if (listingTitleColumn != null) {
            listingTitleColumn.setText(localeService.getMessage("user.favorites.column.title", "Title"));
        }
        if (listingTypeColumn != null) {
            listingTypeColumn.setText(localeService.getMessage("user.favorites.column.type", "Type"));
        }
        if (listingPriceColumn != null) {
            listingPriceColumn.setText(localeService.getMessage("user.favorites.column.price", "Price"));
        }
        if (listingDateColumn != null) {
            listingDateColumn.setText(localeService.getMessage("user.favorites.column.date", "Date Added"));
        }
        if (listingActionsColumn != null) {
            listingActionsColumn.setText(localeService.getMessage("user.favorites.column.actions", "Actions"));
        }
    }

    private void setupObservableLists() {
        // Get the observable lists from FavoritesService (same as FavoritesDrawer)
        favoriteListings = favoritesService.getFavoriteListingViewModels();
        userFavorites = favoritesService.getUserFavoritesList();

        // Set up listener for automatic updates (like FavoritesDrawer)
        favoriteListings.addListener((ListChangeListener<ListingViewModel>) change -> {
            Platform.runLater(() -> {
                updateCounts();
                System.out.println(localeService.getMessage("user.favorites.debug.list.changed", "Favorite listings list changed"));
                // Table is already bound to favoriteListings, so it will update automatically
            });
        });

        // Additional listener for user favorites to catch any changes
        userFavorites.addListener((ListChangeListener<FavoriteViewModel>) change -> {
            Platform.runLater(() -> {
                updateCounts();
                System.out.println(localeService.getMessage("user.favorites.debug.favorites.changed", "User favorites list changed"));
            });
        });
    }

    private void setupTable() {
        listingTitleColumn.setCellValueFactory(cellData -> {
            String title = cellData.getValue().getTitle();
            return new SimpleStringProperty(title != null ? title : 
                localeService.getMessage("user.favorites.no.title", "No Title"));
        });

        listingTypeColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getListingTypeValue();
            return new SimpleStringProperty(type != null ? type : 
                localeService.getMessage("user.favorites.no.type", "Unknown"));
        });

        listingPriceColumn.setCellValueFactory(cellData -> {
            ListingViewModel listing = cellData.getValue();
            if (listing instanceof SellListingViewModel) {
                BigDecimal price = ((SellListingViewModel)listing).getPrice();
                String currency = ((SellListingViewModel)listing).getCurrency();
                
                if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
                    return new SimpleStringProperty(String.format("%.2f %s", price, 
                        currency != null ? currency : "€"));
                }
            }
            
            return new SimpleStringProperty(localeService.getMessage("user.favorites.price.na", "N/A"));
        });

        listingDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty(localeService.getMessage("user.favorites.date.unknown", "Unknown"));
        });

        // Setup actions column
        setupActionsColumn();

        // Bind table directly to the FavoritesService observable list
        favoritesTable.setItems(favoriteListings);
        
        // Update table placeholder text
        updateTablePlaceholder();
    }

    private void updateTablePlaceholder() {
        if (favoritesTable != null) {
            Label placeholderLabel = new Label(localeService.getMessage("user.favorites.empty.message", "No favorite listings yet"));
            placeholderLabel.getStyleClass().add("placeholder-text");
            favoritesTable.setPlaceholder(placeholderLabel);
        }
    }

    private void setupActionsColumn() {
        listingActionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button();
            private final Button removeButton = new Button();
            private final HBox actionBox = new HBox(5, viewButton, removeButton);

            {
                // Set initial button text
                updateButtonTexts();
                
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

            private void updateButtonTexts() {
                viewButton.setText(localeService.getMessage("user.favorites.button.view", "View"));
                removeButton.setText(localeService.getMessage("user.favorites.button.remove", "Remove"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    updateButtonTexts(); // Update button texts when cell is updated
                }
                setGraphic(empty ? null : actionBox);
            }
        });
    }

    private void refreshActionButtons() {
        // Force refresh of action column to update button texts
        if (listingActionsColumn != null) {
            listingActionsColumn.setVisible(false);
            listingActionsColumn.setVisible(true);
        }
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
                System.out.println(localeService.getMessage("user.favorites.debug.user.set", "User set for favorites view: {0}")
                    .replace("{0}", user.getDisplayName() != null ? user.getDisplayName() : "unknown"));
            });
        }
    }

    private void updateUserInfo() {
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getDisplayName() != null ? 
                currentUser.getDisplayName() : 
                localeService.getMessage("user.favorites.unknown.user", "Unknown User"));
            updateCounts();
        }
    }

    private void updateCounts() {
        if (totalFavoritesLabel != null) {
            int count = favoriteListings.size();
            totalFavoritesLabel.setText(localeService.getMessage("user.favorites.total.count", "{0} favorites")
                .replace("{0}", String.valueOf(count)));
            
            System.out.println(localeService.getMessage("user.favorites.debug.count.updated", "Favorites count updated: {0}")
                .replace("{0}", String.valueOf(count)));
        }
    }

    private void handleViewListing(ListingViewModel listing) {
        try {
            navigationService.navigateToListingDetails(listing);
            System.out.println(localeService.getMessage("user.favorites.debug.view.listing", "Viewing listing: {0}")
                .replace("{0}", listing.getTitle() != null ? listing.getTitle() : "unknown"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.favorites.debug.navigation.error", "Failed to navigate to listing details: {0}")
                .replace("{0}", e.getMessage()));
            
            AlertHelper.showErrorAlert(
                    localeService.getMessage("user.favorites.error.title", "Error"),
                    localeService.getMessage("user.favorites.error.navigation.header", "Navigation Error"),
                    localeService.getMessage("user.favorites.error.navigation.content", "Failed to open listing details: {0}")
                        .replace("{0}", e.getMessage()));
        }
    }

    private void handleRemoveFavorite(ListingViewModel listing) {
        String listingTitle = listing.getTitle() != null ? listing.getTitle() : 
            localeService.getMessage("user.favorites.unnamed.listing", "Unnamed Listing");
        
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("user.favorites.remove.title", "Remove Favorite"),
                localeService.getMessage("user.favorites.remove.header", "Confirm Removal"),
                localeService.getMessage("user.favorites.remove.message",
                        "Are you sure you want to remove \"{0}\" from favorites?")
                        .replace("{0}", listingTitle));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println(localeService.getMessage("user.favorites.debug.removing", "Removing favorite: {0}")
                    .replace("{0}", listingTitle));
                
                // Use the FavoritesService to remove (same as other controllers)
                favoritesService.removeFavoriteFromServer(listing.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            // No need to manually remove from list - the observable list will update automatically
                            // via the FavoritesService message handler

                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("user.favorites.remove.success.title", "Success"),
                                    localeService.getMessage("user.favorites.remove.success.header", "Favorite Removed"),
                                    localeService.getMessage("user.favorites.remove.success.message",
                                            "Listing removed from favorites"));
                            
                            System.out.println(localeService.getMessage("user.favorites.debug.removed.success", "Successfully removed favorite: {0}")
                                .replace("{0}", listingTitle));
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                System.err.println(localeService.getMessage("user.favorites.debug.remove.error", "Error removing favorite: {0}")
                                    .replace("{0}", ex.getMessage()));
                                
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("user.favorites.error.title", "Error"),
                                        localeService.getMessage("user.favorites.error.remove.header", "Failed to remove favorite"),
                                        localeService.getMessage("user.favorites.error.remove.content", "Could not remove the listing from favorites: {0}")
                                            .replace("{0}", ex.getMessage()));
                            });
                            return null;
                        });
            }
        });
    }

    @FXML
    private void handleRefresh() {
        System.out.println(localeService.getMessage("user.favorites.debug.refresh.requested", "Refresh requested"));
        
        // Simply trigger refresh via FavoritesService - observable lists will update automatically
        favoritesService.refreshUserFavorites();
        updateCounts();
        
        // Show brief feedback to user
        if (refreshButton != null) {
            String originalText = refreshButton.getText();
            refreshButton.setText(localeService.getMessage("user.favorites.button.refreshing", "Refreshing..."));
            refreshButton.setDisable(true);
            
            // Reset button after a short delay
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            refreshButton.setText(originalText);
                            refreshButton.setDisable(false);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
    }

    @FXML
    private void handleClose() {
        try {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
                System.out.println(localeService.getMessage("user.favorites.debug.closed", "UserFavorites dialog closed"));
            } else {
                System.err.println(localeService.getMessage("user.favorites.debug.close.error", "Cannot close dialog: no stage found"));
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("user.favorites.debug.close.exception", "Error closing dialog: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    // Public methods for external access
    
    /**
     * Check if favorites are loaded
     */
    public boolean hasFavorites() {
        return favoriteListings != null && !favoriteListings.isEmpty();
    }

    /**
     * Get favorites count
     */
    public int getFavoritesCount() {
        return favoriteListings != null ? favoriteListings.size() : 0;
    }

    /**
     * Refresh data (can be called externally)
     */
    public void refreshData() {
        favoritesService.refreshUserFavorites();
        System.out.println(localeService.getMessage("user.favorites.debug.external.refresh", "External refresh triggered"));
    }

    /**
     * Get the current user
     */
    public UserViewModel getCurrentUser() {
        return currentUser;
    }

    /**
     * Clear all data
     */
    public void clearData() {
        currentUser = null;
        if (userNameLabel != null) {
            userNameLabel.setText("");
        }
        updateCounts();
        System.out.println(localeService.getMessage("user.favorites.debug.data.cleared", "UserFavorites data cleared"));
    }

    /**
     * Check if user is set
     */
    public boolean hasUser() {
        return currentUser != null;
    }

    /**
     * Get user display name
     */
    public String getUserDisplayName() {
        return currentUser != null ? currentUser.getDisplayName() : 
            localeService.getMessage("user.favorites.no.user", "No User");
    }

    /**
     * Force refresh of the table display
     */
    public void refreshTable() {
        if (favoritesTable != null) {
            favoritesTable.refresh();
            updateTablePlaceholder();
        }
        System.out.println(localeService.getMessage("user.favorites.debug.table.refreshed", "Table display refreshed"));
    }
}