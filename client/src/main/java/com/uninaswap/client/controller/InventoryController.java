package com.uninaswap.client.controller;

import com.uninaswap.client.service.ItemService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ItemViewModel;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.common.enums.ItemCondition;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.transformation.FilteredList;
import com.uninaswap.common.enums.Category;
import java.util.Optional;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryController implements Refreshable {

    // Update table to use ItemViewModel instead of ItemDTO
    @FXML
    private TableView<ItemViewModel> itemsTable;
    @FXML
    private TableColumn<ItemViewModel, String> nameColumn;
    @FXML
    private TableColumn<ItemViewModel, String> conditionColumn;
    @FXML
    private TableColumn<ItemViewModel, Integer> stockColumn;
    @FXML
    private TableColumn<ItemViewModel, String> categoryColumn;
    @FXML
    private TableColumn<ItemViewModel, Integer> reservedColumn;
    @FXML
    private TableColumn<ItemViewModel, Integer> availableColumn;

    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    @FXML
    private ImageView itemImageView;
    @FXML
    private Label itemNameLabel;
    @FXML
    private Label itemDescriptionLabel;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilterComboBox;
    @FXML
    private ComboBox<String> conditionFilterComboBox;
    @FXML
    private ComboBox<String> availabilityFilterComboBox;
    @FXML
    private Button clearFiltersButton;

    private final ItemService itemService = ItemService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final EventBusService eventBus = EventBusService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();

    // Add field for filtered list
    private FilteredList<ItemViewModel> filteredItems;

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();

        // Get the observable list from ItemService and create filtered list
        ObservableList<ItemViewModel> allItems = itemService.getUserItemsListAsViewModel();
        FilteredList<ItemViewModel> filteredItems = new FilteredList<>(allItems);
        itemsTable.setItems(filteredItems);

        setupSearchFilter(filteredItems);

        itemsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                showItemDetails(newSelection);
            } else {
                clearItemDetails();
            }
        });

        // Enable buttons only when item is selected
        editButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());

        // Load user's items
        refreshItems();

        eventBus.subscribe(EventTypes.ITEM_UPDATED, _ -> {
            refreshItems();
        });
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            Platform.runLater(() -> {
                // Clear the table and details
                itemsTable.getItems().clear();
                clearItemDetails();
                System.out.println(localeService.getMessage("inventory.debug.cleared.on.logout", "InventoryController: Cleared view on logout"));
            });
        });
    }

    // Add new method to setup filters
    private void setupFilters() {
        // Category filter
        categoryFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("inventory.filter.all.categories", "All Categories"),
                localeService.getMessage("category.electronics", "Electronics"),
                localeService.getMessage("category.clothing", "Clothing"),
                localeService.getMessage("category.books", "Books"),
                localeService.getMessage("category.home", "Home & Garden"),
                localeService.getMessage("category.sports", "Sports"),
                localeService.getMessage("category.other", "Other")
        ));
        categoryFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.categories", "All Categories"));

        // Condition filter
        conditionFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("inventory.filter.all.conditions", "All Conditions"),
                localeService.getMessage("condition.new", "New"),
                localeService.getMessage("condition.like_new", "Like New"),
                localeService.getMessage("condition.very_good", "Very Good"),
                localeService.getMessage("condition.good", "Good"),
                localeService.getMessage("condition.acceptable", "Acceptable"),
                localeService.getMessage("condition.for_parts", "For Parts")
        ));
        conditionFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.conditions", "All Conditions"));

        // Availability filter
        availabilityFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("inventory.filter.all.availability", "All Items"),
                localeService.getMessage("inventory.filter.available", "Available"),
                localeService.getMessage("inventory.filter.reserved", "Reserved"),
                localeService.getMessage("inventory.filter.out_of_stock", "Out of Stock")
        ));
        availabilityFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.availability", "All Items"));

        // Add listeners for filter changes
        categoryFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        conditionFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        availabilityFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
    }

    // Add new method to setup search filter
    private void setupSearchFilter(FilteredList<ItemViewModel> filteredItems) {
        searchField.textProperty().addListener((_, _, _) -> updateFilters());

        // Store reference to filtered list for filter updates
        this.filteredItems = filteredItems;
    }

    // Add method to update filters
    private void updateFilters() {
        if (filteredItems == null) return;

        filteredItems.setPredicate(item -> {
            // Search filter
            String searchText = searchField.getText().toLowerCase().trim();
            if (!searchText.isEmpty()) {
                if (!item.getName().toLowerCase().contains(searchText) &&
                    !item.getDescription().toLowerCase().contains(searchText) &&
                    !item.getBrand().toLowerCase().contains(searchText) &&
                    !item.getModel().toLowerCase().contains(searchText)) {
                    return false;
                }
            }

            // Category filter
            String categoryFilter = categoryFilterComboBox.getValue();
            if (!localeService.getMessage("inventory.filter.all.categories", "All Categories").equals(categoryFilter)) {
                String itemCategory = item.getItemCategory();
                if (itemCategory == null || !getCategoryDisplayName(itemCategory).equals(categoryFilter)) {
                    return false;
                }
            }

            // Condition filter
            String conditionFilter = conditionFilterComboBox.getValue();
            if (!localeService.getMessage("inventory.filter.all.conditions", "All Conditions").equals(conditionFilter)) {
                ItemCondition itemCondition = item.getCondition();
                if (itemCondition == null || !itemCondition.getDisplayName().equals(conditionFilter)) {
                    return false;
                }
            }

            // Availability filter
            String availabilityFilter = availabilityFilterComboBox.getValue();
            if (!localeService.getMessage("inventory.filter.all.availability", "All Items").equals(availabilityFilter)) {
                if (localeService.getMessage("inventory.filter.available", "Available").equals(availabilityFilter)) {
                    if (item.getAvailableQuantity() <= 0) {
                        return false;
                    }
                } else if (localeService.getMessage("inventory.filter.reserved", "Reserved").equals(availabilityFilter)) {
                    if (item.getTotalQuantity() - item.getAvailableQuantity() <= 0) {
                        return false;
                    }
                } else if (localeService.getMessage("inventory.filter.out_of_stock", "Out of Stock").equals(availabilityFilter)) {
                    if (item.getTotalQuantity() > 0) {
                        return false;
                    }
                }
            }

            return true;
        });
    }

    // Helper method to get localized category display name
    private String getCategoryDisplayName(String categoryName) {
        try {
            Category category = Category.valueOf(categoryName.toUpperCase());
            return localeService.getMessage("category." + category.name().toLowerCase(), categoryName);
        } catch (IllegalArgumentException e) {
            return categoryName;
        }
    }

    @FXML
    private void handleAddItem() {
        // Create a new empty ItemViewModel for adding
        ItemViewModel newItem = new ItemViewModel();
        navigationService.openItemDialog(newItem);
        System.out.println(localeService.getMessage("inventory.debug.add.item.dialog", "Opening add item dialog"));
    }

    @FXML
    private void handleEditItem() {
        ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Pass the ItemViewModel directly to the navigation service
            navigationService.openItemDialog(selectedItem);
            System.out.println(localeService.getMessage("inventory.debug.edit.item.dialog", "Opening edit item dialog for: {0}").replace("{0}", selectedItem.getName()));
        } else {
            System.out.println(localeService.getMessage("inventory.debug.no.item.selected", "No item selected for editing"));
        }
    }

    @FXML
    private void handleDeleteItem() {
        ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert confirmation = AlertHelper.createConfirmationDialog(
                    localeService.getMessage("item.delete.title", "Delete Item"),
                    localeService.getMessage("item.delete.header", "Delete Item"),
                    localeService.getMessage("item.delete.content", "Are you sure you want to delete {0}?").replace("{0}", selectedItem.getName()));

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println(localeService.getMessage("inventory.debug.deleting.item", "Deleting item: {0}").replace("{0}", selectedItem.getName()));
                itemService.deleteItem(selectedItem.getId())
                        .thenAccept(_ -> {
                            Platform.runLater(() -> {
                                refreshItems();
                                System.out.println(localeService.getMessage("inventory.debug.item.deleted", "Item deleted successfully"));
                            });
                        })
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("item.delete.error.title", "Delete Error"),
                                        localeService.getMessage("item.delete.error.header", "Failed to delete item"),
                                        localeService.getMessage("inventory.error.delete.failed", "Failed to delete item: {0}").replace("{0}", ex.getMessage()));
                                System.err.println(localeService.getMessage("inventory.error.delete.exception", "Exception deleting item: {0}").replace("{0}", ex.getMessage()));
                            });
                            return null;
                        });
            } else {
                System.out.println(localeService.getMessage("inventory.debug.delete.cancelled", "Delete operation cancelled by user"));
            }
        } else {
            System.out.println(localeService.getMessage("inventory.debug.no.item.for.delete", "No item selected for deletion"));
        }
    }

    @FXML
    private void handleRefreshItems() {
        System.out.println(localeService.getMessage("inventory.debug.refresh.requested", "Manual refresh requested"));
        refreshItems();
    }

    // Add clear filters handler
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.categories", "All Categories"));
        conditionFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.conditions", "All Conditions"));
        availabilityFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.availability", "All Items"));
    }

    private void refreshItems() {
        try {
            // Convert ItemDTOs from service to ItemViewModels
            itemsTable.setItems(itemService.getUserItemsListAsViewModel());
            System.out.println(localeService.getMessage("inventory.debug.items.refreshed", "Items list refreshed successfully"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("inventory.error.refresh.failed", "Failed to refresh items: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void showItemDetails(ItemViewModel item) {
        if (item == null) {
            clearItemDetails();
            return;
        }

        itemNameLabel.setText(item.getName());
        itemDescriptionLabel.setText(item.getDescription());

        if (item.hasImage()) {
            // Load image using the ImageService
            imageService.fetchImage(item.getImagePath())
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            itemImageView.setImage(image);
                        });
                    })
                    .exceptionally(ex -> {
                        // If loading fails, set default image
                        System.err.println(localeService.getMessage("inventory.error.image.load", "Failed to load item image: {0}").replace("{0}", ex.getMessage()));
                        Platform.runLater(() -> {
                            setDefaultItemImage();
                        });
                        return null;
                    });
        } else {
            setDefaultItemImage();
        }

        System.out.println(localeService.getMessage("inventory.debug.item.details.shown", "Showing details for item: {0}").replace("{0}", item.getName()));
    }

    private void setDefaultItemImage() {
        try {
            itemImageView.setImage(new Image("/images/icons/immagine_generica.png"));
        } catch (Exception e) {
            System.err.println(localeService.getMessage("inventory.error.default.image", "Failed to load default item image: {0}").replace("{0}", e.getMessage()));
            itemImageView.setImage(null);
        }
    }

    private void clearItemDetails() {
        itemNameLabel.setText(localeService.getMessage("inventory.details.empty.name", "Select an item to view details"));
        itemDescriptionLabel.setText(localeService.getMessage("inventory.details.empty.description", ""));
        itemImageView.setImage(null);
    }

    // Update the setupTableColumns method name (if it doesn't exist, add it)
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(getCategoryDisplayName(cellData.getValue().getItemCategory())));
        conditionColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCondition() != null ?
                cellData.getValue().getCondition().getDisplayName() : ""));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("totalQuantity"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
        reservedColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getTotalQuantity() -
                cellData.getValue().getAvailableQuantity()).asObject());
    }

    @Override
    public void refreshUI() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshUI'");
    }
}