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

/**
 * 
 */
public class ListingsController implements Refreshable {

    /**
     * 
     */
    @FXML
    private Label titleLabel;

    /**
     * 
     */
    @FXML
    private Label userListingsActiveCountLabel;

    /**
     * 
     */
    @FXML
    private Label userListingsCompletedCountLabel;

    /**
     * 
     */
    @FXML
    private Label userListingsTotalCountLabel;

    /**
     * 
     */
    @FXML
    private ComboBox<String> statusFilterComboBox;

    /**
     * 
     */
    @FXML
    private ComboBox<String> typeFilterComboBox;

    /**
     * 
     */
    @FXML
    private TextField searchField;

    /**
     * 
     */
    @FXML
    private Button refreshButton;

    /**
     * 
     */
    @FXML
    private Button createNewButton;

    /**
     * 
     */
    @FXML
    private TableView<ListingViewModel> listingsTable;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> titleColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> typeColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> statusColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> priceColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, String> dateColumn;

    /**
     * 
     */
    @FXML
    private TableColumn<ListingViewModel, Void> actionsColumn;

    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final ListingService listingService = ListingService.getInstance();
    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();
    /**
     * 
     */
    private ObservableList<ListingViewModel> userListings;
    /**
     * 
     */
    private FilteredList<ListingViewModel> filteredListings;

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupLabels();
        setupFilters();
        setupObservableList();
        setupTable();
        setupSearchFilter();
        loadUserListings();
    }

    /**
     * 
     */
    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("listings.title", "My Listings"));
        refreshButton.setText(localeService.getMessage("listings.refresh", "Refresh"));
        createNewButton.setText(localeService.getMessage("listings.create.new", "Create New Listing"));
        searchField.setPromptText(localeService.getMessage("listings.search.placeholder", "Search listings..."));
    }

    /**
     * 
     */
    private void setupFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Statuses",
                "Active",
                "Completed",
                "Cancelled",
                "Expired"
        ));
        statusFilterComboBox.setValue("All Statuses");
        typeFilterComboBox.setItems(FXCollections.observableArrayList(
                "All Types",
                "Sale",
                "Auction",
                "Trade",
                "Gift"
        ));
        typeFilterComboBox.setValue("All Types");
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        typeFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
    }

    /**
     * 
     */
    private void setupObservableList() {
        userListings = listingService.getUserListingsObservable();
        filteredListings = new FilteredList<>(userListings);
        userListings.addListener((ListChangeListener<ListingViewModel>) _ -> {
            Platform.runLater(() -> {
                updateListingsCount();
            });
        });
    }

    /**
     * 
     */
    private void setupTable() {
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getListingTypeValue()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        statusColumn.setCellFactory(_ -> new TableCell<ListingViewModel, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toUpperCase()) {
                        case "ACTIVE" -> setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                        case "COMPLETED" -> setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;");
                        case "CANCELLED" -> setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                        case "EXPIRED" -> setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: #666666;");
                    }
                }
            }
        });
        priceColumn.setCellValueFactory(cellData -> {
            ListingViewModel listing = cellData.getValue();
            if (listing instanceof SellListingViewModel sellListing) {
                if (sellListing.getPrice() != null && sellListing.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                    return new SimpleStringProperty(String.format("%.2f %s", 
                            sellListing.getPrice(), sellListing.getCurrency()));
                }
            } else if (listing instanceof AuctionListingViewModel auctionListing) {
                if (auctionListing.getStartingPrice() != null) {
                    return new SimpleStringProperty(String.format("Starting: %.2f %s", 
                            auctionListing.getStartingPrice(), auctionListing.getCurrency()));
                }
            }
            return new SimpleStringProperty("N/A");
        });
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
            }
            return new SimpleStringProperty("");
        });
        setupActionsColumn();
        listingsTable.setItems(filteredListings);
        listingsTable.setRowFactory(_ -> {
            TableRow<ListingViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewListing(row.getItem());
                }
            });
            return row;
        });
    }

    /**
     * 
     */
    private void setupActionsColumn() {
            actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox actionBox = new HBox(5, viewButton, editButton, deleteButton);

            {
                viewButton.getStyleClass().add("primary-button");
                editButton.getStyleClass().add("primary-button");
                deleteButton.getStyleClass().add("danger-button");

                viewButton.setOnAction(_ -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleViewListing(listing);
                });

                editButton.setOnAction(_ -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleEditListing(listing);
                });

                deleteButton.setOnAction(_ -> {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    handleDeleteListing(listing);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ListingViewModel listing = getTableView().getItems().get(getIndex());
                    boolean canEdit = listing.getStatus() == ListingStatus.ACTIVE;
                    boolean canDelete = listing.getStatus() != ListingStatus.COMPLETED;
                    
                    editButton.setVisible(canEdit);
                    editButton.setManaged(canEdit);
                    deleteButton.setVisible(canDelete);
                    deleteButton.setManaged(canDelete);
                    
                    setGraphic(actionBox);
                }
            }
        });
    }

    /**
     * 
     */
    private void setupSearchFilter() {
        searchField.textProperty().addListener((_, _, _) -> updateFilters());
    }

    /**
     * 
     */
    private void updateFilters() {
        filteredListings.setPredicate(listing -> {
            String searchText = searchField.getText().toLowerCase().trim();
            if (!searchText.isEmpty()) {
                boolean matchesSearch = listing.getTitle().toLowerCase().contains(searchText) ||
                                     listing.getDescription().toLowerCase().contains(searchText);
                if (!matchesSearch) return false;
            }
            String statusFilter = statusFilterComboBox.getValue();
            if (!"All Statuses".equals(statusFilter)) {
                ListingStatus selectedStatus = ListingStatus.valueOf(statusFilter.toUpperCase());
                if (listing.getStatus() != selectedStatus) return false;
            }
            String typeFilter = typeFilterComboBox.getValue();
            if (!"All Types".equals(typeFilter)) {
                String listingType = listing.getListingTypeValue().toUpperCase();
                String selectedType = typeFilter.toUpperCase();
                if ("SALE".equals(selectedType)) selectedType = "SELL";
                if (!listingType.equals(selectedType)) return false;
            }

            return true;
        });
    }

    /**
     * 
     */
    private void loadUserListings() {
        if (userListings.isEmpty()) {
            listingService.refreshUserListings();
        }
        updateListingsCount();
    }

    /**
     * 
     */
    private void updateListingsCount() {
        long total = userListings.size();
        long active = userListings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
        long completed = userListings.stream().filter(l -> l.getStatus() == ListingStatus.COMPLETED).count();
        userListingsActiveCountLabel.setText(String.valueOf(active));
        userListingsCompletedCountLabel.setText(String.valueOf(completed));
        userListingsTotalCountLabel.setText(String.valueOf(total));
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
            AlertHelper.showInformationAlert(
                    localeService.getMessage("listings.edit.info.title", "Edit Listing"),
                    localeService.getMessage("listings.edit.info.header", "Feature Coming Soon"),
                    localeService.getMessage("listings.edit.info.message", 
                            "Listing editing functionality will be available soon."));
        } catch (Exception e) {
            System.err.println("Failed to edit listing: " + e.getMessage());
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("listings.edit.error", "Edit Error"),
                    "Failed to edit listing: " + e.getMessage());
        }
    }

    /**
     * @param listing
     */
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
                        "Are you sure you want to delete \"" + listing.getTitle() + "\"? This action cannot be undone."));

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
                                    localeService.getMessage("listings.delete.error", "Failed to delete listing"),
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
        listingService.refreshUserListings();
        updateListingsCount();
    }

    /**
     * 
     */
    @FXML
    private void handleCreateNew() {
        try {
            navigationService.navigateToListingCreationView();
        } catch (Exception e) {
            System.err.println("Failed to navigate to listing creation: " + e.getMessage());
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    "Failed to open listing creation: " + e.getMessage());
        }
    }

    /**
     * 
     */
    @FXML
    private void handleClearFilters() {
        statusFilterComboBox.setValue("All Statuses");
        typeFilterComboBox.setValue("All Types");
        searchField.clear();
    }

    /**
     * @return
     */
    public boolean hasListings() {
        return !userListings.isEmpty();
    }

    /**
     * @return
     */
    public int getListingsCount() {
        return userListings.size();
    }

    /**
     * @return
     */
    public int getActiveListingsCount() {
        return (int) userListings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count();
    }

    /**
     * 
     */
    public void refreshData() {
        listingService.refreshUserListings();
    }

    /**
     *
     */
    @Override
    public void refreshUI() {
        setupLabels();
        updateListingsCount();
    }
}
