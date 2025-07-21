package com.uninaswap.client.controller;

import com.uninaswap.client.service.ListingService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.SellListingViewModel;
import com.uninaswap.client.viewmodel.AuctionListingViewModel;
import com.uninaswap.common.enums.ListingStatus;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ListingsController implements Refreshable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label userListingsActiveCountLabel;

    @FXML
    private Label userListingsCompletedCountLabel;

    @FXML
    private Label userListingsTotalCountLabel;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private ComboBox<String> typeFilterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private Button refreshButton;

    @FXML
    private Button createNewButton;

    @FXML
    private TableView<ListingViewModel> listingsTable;

    @FXML
    private TableColumn<ListingViewModel, String> titleColumn;

    @FXML
    private TableColumn<ListingViewModel, String> typeColumn;

    @FXML
    private TableColumn<ListingViewModel, String> statusColumn;

    @FXML
    private TableColumn<ListingViewModel, String> priceColumn;

    @FXML
    private TableColumn<ListingViewModel, String> dateColumn;

    @FXML
    private TableColumn<ListingViewModel, Void> actionsColumn;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final ListingService listingService = ListingService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    // Data
    private ObservableList<ListingViewModel> userListings;
    private FilteredList<ListingViewModel> filteredListings;

    @FXML
    public void initialize() {
        setupFilters();
        setupObservableList();
        setupTable();
        setupSearchFilter();
        loadUserListings();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("listings.debug.initialized", "Listings controller initialized"));
    }

    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("listings.title", "My Listings"));
        refreshButton.setText(localeService.getMessage("listings.button.refresh", "Refresh"));
        createNewButton.setText(localeService.getMessage("listings.button.create.new", "Create New Listing"));
        searchField.setPromptText(localeService.getMessage("listings.search.placeholder", "Search listings..."));
    }

    private void setupFilters() {
        // Status filter - using localized values
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("listings.filter.status.all", "All Statuses"),
                localeService.getMessage("listings.filter.status.active", "Active"),
                localeService.getMessage("listings.filter.status.completed", "Completed"),
                localeService.getMessage("listings.filter.status.cancelled", "Cancelled"),
                localeService.getMessage("listings.filter.status.expired", "Expired")
        ));
        statusFilterComboBox.setValue(localeService.getMessage("listings.filter.status.all", "All Statuses"));

        // Type filter - using localized values
        typeFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("listings.filter.type.all", "All Types"),
                localeService.getMessage("listings.filter.type.sale", "Sale"),
                localeService.getMessage("listings.filter.type.auction", "Auction"),
                localeService.getMessage("listings.filter.type.trade", "Trade"),
                localeService.getMessage("listings.filter.type.gift", "Gift")
        ));
        typeFilterComboBox.setValue(localeService.getMessage("listings.filter.type.all", "All Types"));

        // Add listeners for filter changes
        statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        typeFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());
    }

    private void setupObservableList() {
        // Get the observable list from ListingService
        userListings = listingService.getUserListingsObservable();

        // Create filtered list
        filteredListings = new FilteredList<>(userListings);

        // Set up listener for automatic updates
        userListings.addListener((ListChangeListener<ListingViewModel>) change -> {
            Platform.runLater(() -> {
                updateListingsCount();
            });
        });
    }

    private void setupTable() {
        // Title column
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        // Type column
        typeColumn.setCellValueFactory(cellData -> {
            String type = cellData.getValue().getListingTypeValue().toLowerCase();
            String localizedType = switch (type) {
                case "sell" -> localeService.getMessage("listings.type.sell", "Sell");
                case "trade" -> localeService.getMessage("listings.type.trade", "Trade");
                case "gift" -> localeService.getMessage("listings.type.gift", "Gift");
                case "auction" -> localeService.getMessage("listings.type.auction", "Auction");
                default -> type;
            };
            return new SimpleStringProperty(localizedType);
        });

        // Status column
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus().toString().toLowerCase();
            String localizedStatus = switch (status) {
                case "active" -> localeService.getMessage("listings.status.active", "Active");
                case "completed" -> localeService.getMessage("listings.status.completed", "Completed");
                case "cancelled" -> localeService.getMessage("listings.status.cancelled", "Cancelled");
                case "expired" -> localeService.getMessage("listings.status.expired", "Expired");
                default -> status;
            };
            return new SimpleStringProperty(localizedStatus);
        });
        
        statusColumn.setCellFactory(column -> new TableCell<ListingViewModel, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    // Color code the status based on the original status value
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    String originalStatus = listing.getStatus().toString().toUpperCase();
                    switch (originalStatus) {
                        case "ACTIVE" -> setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                        case "COMPLETED" -> setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
                        case "CANCELLED" -> setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                        case "EXPIRED" -> setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #666666;");
                    }
                }
            }
        });

        // Price column
        priceColumn.setCellValueFactory(cellData -> {
            ListingViewModel listing = cellData.getValue();
            if (listing instanceof SellListingViewModel sellListing) {
                if (sellListing.getPrice() != null && sellListing.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    return new SimpleStringProperty(String.format("%.2f %s", 
                            sellListing.getPrice(), sellListing.getCurrency()));
                }
            } else if (listing instanceof AuctionListingViewModel auctionListing) {
                if (auctionListing.getStartingPrice() != null) {
                    return new SimpleStringProperty(localeService.getMessage("listings.price.starting", "Starting: {0} {1}")
                            .replace("{0}", String.format("%.2f", auctionListing.getStartingPrice()))
                            .replace("{1}", auctionListing.getCurrency().toString()));
                }
            }
            return new SimpleStringProperty(localeService.getMessage("listings.price.na", "N/A"));
        });

        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        // Actions column
        setupActionsColumn();

        // Bind table to filtered list
        listingsTable.setItems(filteredListings);

        // Add row double-click handler for viewing details
        listingsTable.setRowFactory(tv -> {
            TableRow<ListingViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewListing(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox actionBox = new HBox(5, viewButton, editButton, deleteButton);

            {
                viewButton.getStyleClass().add("primary-button");
                editButton.getStyleClass().add("primary-button");
                deleteButton.getStyleClass().add("danger-button");

                viewButton.setOnAction(e -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleViewListing(listing);
                });

                editButton.setOnAction(e -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleEditListing(listing);
                });

                deleteButton.setOnAction(e -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleDeleteListing(listing);
                });
                
                // Set initial text
                updateButtonText();
            }

            private void updateButtonText() {
                viewButton.setText(localeService.getMessage("listings.button.view", "View"));
                editButton.setText(localeService.getMessage("listings.button.edit", "Edit"));
                deleteButton.setText(localeService.getMessage("listings.button.delete", "Delete"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    
                    // Show/hide buttons based on listing status
                    boolean canEdit = listing.getStatus() == ListingStatus.ACTIVE;
                    boolean canDelete = listing.getStatus() != ListingStatus.COMPLETED;
                    
                    editButton.setVisible(canEdit);
                    editButton.setManaged(canEdit);
                    deleteButton.setVisible(canDelete);
                    deleteButton.setManaged(canDelete);
                    
                    // Update button text in case locale changed
                    updateButtonText();
                    
                    setGraphic(actionBox);
                }
            }
        });
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
    }

    private void updateFilters() {
        filteredListings.setPredicate(listing -> {
            // Search filter
            String searchText = searchField.getText().toLowerCase().trim();
            if (!searchText.isEmpty()) {
                boolean matchesSearch = listing.getTitle().toLowerCase().contains(searchText) ||
                                     listing.getDescription().toLowerCase().contains(searchText);
                if (!matchesSearch) return false;
            }

            // Status filter
            String statusFilter = statusFilterComboBox.getValue();
            String allStatusesText = localeService.getMessage("listings.filter.status.all", "All Statuses");
            if (!allStatusesText.equals(statusFilter)) {
                // Map localized status back to enum
                ListingStatus selectedStatus = mapLocalizedStatusToEnum(statusFilter);
                if (selectedStatus != null && listing.getStatus() != selectedStatus) return false;
            }

            // Type filter
            String typeFilter = typeFilterComboBox.getValue();
            String allTypesText = localeService.getMessage("listings.filter.type.all", "All Types");
            if (!allTypesText.equals(typeFilter)) {
                String listingType = listing.getListingTypeValue().toUpperCase();
                String selectedType = mapLocalizedTypeToEnum(typeFilter);
                if (selectedType != null && !listingType.equals(selectedType)) return false;
            }

            return true;
        });
    }

    private ListingStatus mapLocalizedStatusToEnum(String localizedStatus) {
        if (localizedStatus.equals(localeService.getMessage("listings.filter.status.active", "Active"))) {
            return ListingStatus.ACTIVE;
        } else if (localizedStatus.equals(localeService.getMessage("listings.filter.status.completed", "Completed"))) {
            return ListingStatus.COMPLETED;
        } else if (localizedStatus.equals(localeService.getMessage("listings.filter.status.cancelled", "Cancelled"))) {
            return ListingStatus.CANCELLED;
        } else if (localizedStatus.equals(localeService.getMessage("listings.filter.status.expired", "Expired"))) {
            return ListingStatus.EXPIRED;
        }
        return null;
    }

    private String mapLocalizedTypeToEnum(String localizedType) {
        if (localizedType.equals(localeService.getMessage("listings.filter.type.sale", "Sale"))) {
            return "SELL";
        } else if (localizedType.equals(localeService.getMessage("listings.filter.type.auction", "Auction"))) {
            return "AUCTION";
        } else if (localizedType.equals(localeService.getMessage("listings.filter.type.trade", "Trade"))) {
            return "TRADE";
        } else if (localizedType.equals(localeService.getMessage("listings.filter.type.gift", "Gift"))) {
            return "GIFT";
        }
        return null;
    }

    private void loadUserListings() {
        // If the observable list is empty, refresh from server
        if (userListings.isEmpty()) {
            listingService.refreshUserListings();
        }
        updateListingsCount();
    }

    private void updateListingsCount() {
        long total = userListings.size();
        long active = userListings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
        long completed = userListings.stream().filter(l -> l.getStatus() == ListingStatus.COMPLETED).count();
        userListingsActiveCountLabel.setText(String.valueOf(active));
        userListingsCompletedCountLabel.setText(String.valueOf(completed));
        userListingsTotalCountLabel.setText(String.valueOf(total));
    }

    private void handleViewListing(ListingViewModel listing) {
        try {
            navigationService.navigateToListingDetails(listing);
        } catch (Exception e) {
            System.err.println(localeService.getMessage("listings.error.view.failed", "Failed to view listing: {0}").replace("{0}", e.getMessage()));
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    localeService.getMessage("listings.error.view.message", "Failed to open listing details: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void handleEditListing(ListingViewModel listing) {
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listings.edit.warning.title", "Cannot Edit"),
                    localeService.getMessage("listings.edit.warning.header", "Listing Not Editable"),
                    localeService.getMessage("listings.edit.warning.message", 
                            "Only active listings can be edited."));
            return;
        }

        try {
            // TODO: Implement listing editing
            // For now, show a placeholder message
            AlertHelper.showInformationAlert(
                    localeService.getMessage("listings.edit.info.title", "Edit Listing"),
                    localeService.getMessage("listings.edit.info.header", "Feature Coming Soon"),
                    localeService.getMessage("listings.edit.info.message", 
                            "Listing editing functionality will be available soon."));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("listings.error.edit.failed", "Failed to edit listing: {0}").replace("{0}", e.getMessage()));
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("listings.edit.error.title", "Edit Error"),
                    localeService.getMessage("listings.error.edit.message", "Failed to edit listing: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void handleDeleteListing(ListingViewModel listing) {
        if (listing.getStatus() == ListingStatus.COMPLETED) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("listings.delete.warning.title", "Cannot Delete"),
                    localeService.getMessage("listings.delete.warning.header", "Listing Cannot Be Deleted"),
                    localeService.getMessage("listings.delete.warning.message", 
                            "Completed listings cannot be deleted."));
            return;
        }

        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("listings.delete.title", "Delete Listing"),
                localeService.getMessage("listings.delete.header", "Confirm Deletion"),
                localeService.getMessage("listings.delete.message",
                        "Are you sure you want to delete \"{0}\"? This action cannot be undone.").replace("{0}", listing.getTitle()));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                listingService.deleteListing(listing.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            if (success) {
                                AlertHelper.showInformationAlert(
                                        localeService.getMessage("listings.delete.success.title", "Success"),
                                        localeService.getMessage("listings.delete.success.header", "Listing Deleted"),
                                        localeService.getMessage("listings.delete.success.message",
                                                "Listing has been successfully deleted."));
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> AlertHelper.showErrorAlert(
                                    localeService.getMessage("error.title", "Error"),
                                    localeService.getMessage("listings.delete.error.title", "Delete Error"),
                                    localeService.getMessage("listings.error.delete.message", "Failed to delete listing: {0}").replace("{0}", ex.getMessage())));
                            return null;
                        });
            }
        });
    }

    @FXML
    private void handleRefresh() {
        listingService.refreshUserListings();
        updateListingsCount();
        System.out.println(localeService.getMessage("listings.debug.refreshed", "Listings refreshed"));
    }

    @FXML
    private void handleCreateNew() {
        try {
            navigationService.navigateToListingCreationView();
        } catch (Exception e) {
            System.err.println(localeService.getMessage("listings.error.create.failed", "Failed to navigate to listing creation: {0}").replace("{0}", e.getMessage()));
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    localeService.getMessage("listings.error.create.message", "Failed to open listing creation: {0}").replace("{0}", e.getMessage()));
        }
    }

    @FXML
    private void handleClearFilters() {
        statusFilterComboBox.setValue(localeService.getMessage("listings.filter.status.all", "All Statuses"));
        typeFilterComboBox.setValue(localeService.getMessage("listings.filter.type.all", "All Types"));
        searchField.clear();
        System.out.println(localeService.getMessage("listings.debug.filters.cleared", "Filters cleared"));
    }

    public boolean hasListings() {
        return !userListings.isEmpty();
    }

    public int getListingsCount() {
        return userListings.size();
    }

    public int getActiveListingsCount() {
        return (int) userListings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
    }

    public void refreshData() {
        listingService.refreshUserListings();
    }

    @Override
    public void refreshUI() {
        // Update labels and button text
        setupLabels();
        
        // Update filter combo boxes with localized values
        String currentStatusFilter = statusFilterComboBox.getValue();
        String currentTypeFilter = typeFilterComboBox.getValue();
        
        setupFilters();
        
        // Try to maintain the same selection if possible
        if (currentStatusFilter != null) {
            statusFilterComboBox.setValue(currentStatusFilter);
        }
        if (currentTypeFilter != null) {
            typeFilterComboBox.setValue(currentTypeFilter);
        }
        
        // Update table column headers
        if (titleColumn != null) {
            titleColumn.setText(localeService.getMessage("listings.column.title", "Title"));
        }
        if (typeColumn != null) {
            typeColumn.setText(localeService.getMessage("listings.column.type", "Type"));
        }
        if (statusColumn != null) {
            statusColumn.setText(localeService.getMessage("listings.column.status", "Status"));
        }
        if (priceColumn != null) {
            priceColumn.setText(localeService.getMessage("listings.column.price", "Price"));
        }
        if (dateColumn != null) {
            dateColumn.setText(localeService.getMessage("listings.column.date", "Created"));
        }
        if (actionsColumn != null) {
            actionsColumn.setText(localeService.getMessage("listings.column.actions", "Actions"));
        }
        
        // Refresh table to update cell content
        if (listingsTable != null) {
            listingsTable.refresh();
        }
        
        updateListingsCount();
    }
}
