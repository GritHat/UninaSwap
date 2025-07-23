package com.uninaswap.client.controller;

import com.uninaswap.client.service.ItemService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.CategoryService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ItemViewModel;
import com.uninaswap.common.enums.ItemCondition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.transformation.FilteredList;
import com.uninaswap.common.enums.Category;
import javafx.util.StringConverter;
import java.util.Optional;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 */
public class InventoryController {
    /**
     * 
     */
    @FXML
    private TableView<ItemViewModel> itemsTable;
    /**
     * 
     */
    @FXML
    private TableColumn<ItemViewModel, String> nameColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<ItemViewModel, String> conditionColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<ItemViewModel, Integer> stockColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<ItemViewModel, String> categoryColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<ItemViewModel, Integer> reservedColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<ItemViewModel, Integer> availableColumn;
    /**
     * 
     */
    @FXML
    private TableColumn<ItemViewModel, Void> actionsColumn;
    /**
     * 
     */
    @FXML
    private Button addButton;
    /**
     * 
     */
    @FXML
    private Button editButton;
    /**
     * 
     */
    @FXML
    private Button deleteButton;
    /**
     * 
     */
    @FXML
    private Button refreshButton;
    /**
     * 
     */
    @FXML
    private ImageView itemImageView;
    /**
     * 
     */
    @FXML
    private Label itemNameLabel;
    /**
     * 
     */
    @FXML
    private Label itemDescriptionLabel;
    /**
     * 
     */
    @FXML
    private TextField searchField;
    /**
     * 
     */
    @FXML
    private ComboBox<Category> categoryFilterComboBox;
    /**
     * 
     */
    @FXML
    private ComboBox<ItemCondition> conditionFilterComboBox;
    /**
     * 
     */
    @FXML
    private ComboBox<String> availabilityFilterComboBox;
    /**
     * 
     */
    @FXML
    private Button clearFiltersButton;

    /**
     * 
     */
    private final ItemService itemService = ItemService.getInstance();
    /**
     * 
     */
    private final ImageService imageService = ImageService.getInstance();
    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final EventBusService eventBus = EventBusService.getInstance();
    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();
    /**
     * 
     */
    private final CategoryService categoryService = CategoryService.getInstance();
    /**
     * 
     */
    private FilteredList<ItemViewModel> filteredItems;

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
        
        // Get all items and set up filtering
        ObservableList<ItemViewModel> allItems = itemService.getUserItemsListAsViewModel();
        filteredItems = new FilteredList<>(allItems);
        itemsTable.setItems(filteredItems);
        
        setupSearchAndFilters();
        setupTableEventHandlers();
        setupActionButtons();
        refreshItems();
        setupEventSubscriptions();
    }
    
    /**
     * 
     */
    private void setupFilters() {
        // Setup category filter with actual Category enum
        ObservableList<Category> categories = FXCollections.observableArrayList();
        categories.add(null); // Add null for "All Categories"
        categories.addAll(categoryService.getSelectableCategories());
        categoryFilterComboBox.setItems(categories);
        
        categoryFilterComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                if (category == null) {
                    return localeService.getMessage("inventory.filter.all.categories", "All Categories");
                }
                return categoryService.getLocalizedCategoryName(category);
            }
            
            @Override
            public Category fromString(String string) {
                if (string.equals(localeService.getMessage("inventory.filter.all.categories", "All Categories"))) {
                    return null;
                }
                return categoryService.getCategoryByDisplayName(string);
            }
        });
        categoryFilterComboBox.setValue(null); // Set to "All Categories"
        
        // Setup condition filter with actual ItemCondition enum
        ObservableList<ItemCondition> conditions = FXCollections.observableArrayList();
        conditions.add(null); // Add null for "All Conditions"
        conditions.addAll(ItemCondition.values());
        conditionFilterComboBox.setItems(conditions);
        
        conditionFilterComboBox.setConverter(new StringConverter<ItemCondition>() {
            @Override
            public String toString(ItemCondition condition) {
                if (condition == null) {
                    return localeService.getMessage("inventory.filter.all.conditions", "All Conditions");
                }
                return condition.getDisplayName();
            }
            
            @Override
            public ItemCondition fromString(String string) {
                if (string.equals(localeService.getMessage("inventory.filter.all.conditions", "All Conditions"))) {
                    return null;
                }
                for (ItemCondition condition : ItemCondition.values()) {
                    if (condition.getDisplayName().equals(string)) {
                        return condition;
                    }
                }
                return null;
            }
        });
        conditionFilterComboBox.setValue(null); // Set to "All Conditions"
        
        // Setup availability filter with localized strings
        availabilityFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("inventory.filter.all.availability", "All Items"),
                localeService.getMessage("inventory.filter.available", "Available"),
                localeService.getMessage("inventory.filter.reserved", "Reserved"),
                localeService.getMessage("inventory.filter.out_of_stock", "Out of Stock")
        ));
        availabilityFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.availability", "All Items"));
    }

    /**
     * 
     */
    private void setupSearchAndFilters() {
        // Add listeners to all filter controls
        searchField.textProperty().addListener((_, _, _) -> updateFilters());
        categoryFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        conditionFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        availabilityFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
    }

    /**
     * 
     */
    private void setupTableEventHandlers() {
        itemsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null) {
                showItemDetails(newSelection);
            } else {
                clearItemDetails();
            }
        });
    }
    
    /**
     * 
     */
    private void setupActionButtons() {
        editButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
        
        // Disable delete button when no item is selected OR when selected item has reserved quantities
        deleteButton.disableProperty().bind(
            itemsTable.getSelectionModel().selectedItemProperty().isNull()
            .or(
                javafx.beans.binding.Bindings.createBooleanBinding(() -> {
                    ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
                    if (selectedItem == null) {
                        return false; // Already handled by isNull() check above
                    }
                    int reservedQuantity = selectedItem.getTotalQuantity() - selectedItem.getAvailableQuantity();
                    return reservedQuantity > 0;
                }, itemsTable.getSelectionModel().selectedItemProperty())
            )
        );
        
        // Add tooltip to delete button that updates based on selection
        itemsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection == null) {
                deleteButton.setTooltip(new Tooltip(localeService.getMessage("item.delete.tooltip.no.selection", "Select an item to delete")));
            } else {
                int reservedQuantity = newSelection.getTotalQuantity() - newSelection.getAvailableQuantity();
                if (reservedQuantity > 0) {
                    deleteButton.setTooltip(new Tooltip(localeService.getMessage("item.delete.tooltip.reserved", 
                            "Cannot delete: " + reservedQuantity + " quantities are reserved")));
                } else {
                    deleteButton.setTooltip(new Tooltip(localeService.getMessage("item.delete.tooltip.available", "Delete selected item")));
                }
            }
        });
    }
    
    /**
     * 
     */
    private void setupEventSubscriptions() {
        eventBus.subscribe(EventTypes.ITEM_UPDATED, _ -> {
            Platform.runLater(() -> {
                System.out.println("InventoryController: ITEM_UPDATED event received");
                refreshItems();
            });
        });
        
        eventBus.subscribe(EventTypes.LISTING_CREATED, _ -> {
            Platform.runLater(() -> {
                System.out.println("InventoryController: LISTING_CREATED event received - refreshing items for quantity sync");
                refreshItems();
            });
        });
        
        eventBus.subscribe(EventTypes.LISTING_DELETED, _ -> {
            Platform.runLater(() -> {
                System.out.println("InventoryController: LISTING_DELETED event received - refreshing items for quantity sync");
                refreshItems();
            });
        });
        
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            Platform.runLater(() -> {
                System.out.println("InventoryController: USER_LOGGED_OUT event received");
                if (itemsTable != null) {
                    itemsTable.getItems().clear();
                }
                clearItemDetails();
                System.out.println("InventoryController: Cleared view on logout");
            });
        });
    }

    /**
     * 
     */
    private void updateFilters() {
        if (filteredItems == null) return;

        filteredItems.setPredicate(item -> {
            // Search text filter
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerSearchText = searchText.toLowerCase().trim();
                boolean matchesSearch = 
                    (item.getName() != null && item.getName().toLowerCase().contains(lowerSearchText)) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerSearchText)) ||
                    (item.getBrand() != null && item.getBrand().toLowerCase().contains(lowerSearchText)) ||
                    (item.getModel() != null && item.getModel().toLowerCase().contains(lowerSearchText));
                
                if (!matchesSearch) {
                    return false;
                }
            }
            
            // Category filter
            Category selectedCategory = categoryFilterComboBox.getValue();
            if (selectedCategory != null) {
                String itemCategory = item.getItemCategory();
                if (itemCategory == null || !selectedCategory.name().equalsIgnoreCase(itemCategory)) {
                    return false;
                }
            }
            
            // Condition filter
            ItemCondition selectedCondition = conditionFilterComboBox.getValue();
            if (selectedCondition != null) {
                ItemCondition itemCondition = item.getCondition();
                if (itemCondition == null || itemCondition != selectedCondition) {
                    return false;
                }
            }
            
            // Availability filter
            String availabilityFilter = availabilityFilterComboBox.getValue();
            if (availabilityFilter != null && !localeService.getMessage("inventory.filter.all.availability", "All Items").equals(availabilityFilter)) {
                if (localeService.getMessage("inventory.filter.available", "Available").equals(availabilityFilter)) {
                    if (item.getAvailableQuantity() <= 0) {
                        return false;
                    }
                } else if (localeService.getMessage("inventory.filter.reserved", "Reserved").equals(availabilityFilter)) {
                    int reservedQuantity = item.getTotalQuantity() - item.getAvailableQuantity();
                    if (reservedQuantity <= 0) {
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

    /**
     * @param categoryName
     * @return
     */
    private String getCategoryDisplayName(String categoryName) {
        try {
            Category category = Category.valueOf(categoryName.toUpperCase());
            return localeService.getMessage(category.getMessageKey());
        } catch (IllegalArgumentException e) {
            return categoryName;
        }
    }

    /**
     * 
     */
    @FXML
    private void handleAddItem() {
        ItemViewModel newItem = new ItemViewModel();
        navigationService.openItemDialog(newItem);
    }

    /**
     * 
     */
    @FXML
    private void handleEditItem() {
        ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            navigationService.openItemDialog(selectedItem);
        }
    }

    /**
     * 
     */
    @FXML
    private void handleDeleteItem() {
        ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Check if item has reserved quantities
            int reservedQuantity = selectedItem.getTotalQuantity() - selectedItem.getAvailableQuantity();
            if (reservedQuantity > 0) {
                AlertHelper.showWarningAlert(
                        localeService.getMessage("item.delete.warning.title", "Cannot Delete Item"),
                        localeService.getMessage("item.delete.warning.header", "Item Cannot Be Deleted"),
                        localeService.getMessage("item.delete.warning.reserved.message", 
                                "This item cannot be deleted because it has " + reservedQuantity + 
                                " reserved quantities. Reserved items are currently part of active offers or listings."));
                return;
            }
            
            Alert confirmation = AlertHelper.createConfirmationDialog(
                    localeService.getMessage("item.delete.title"),
                    localeService.getMessage("item.delete.header"),
                    localeService.getMessage("item.delete.content", selectedItem.getName()));

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                itemService.deleteItem(selectedItem.getId())
                        .thenAccept(_ -> {
                            refreshItems();
                        })
                        .exceptionally(ex -> {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("item.delete.error.title"),
                                    localeService.getMessage("item.delete.error.header"),
                                    ex.getMessage());
                            return null;
                        });
            }
        }
    }

    /**
     * 
     */
    @FXML
    private void handleRefreshItems() {
        refreshItems();
    }

    /**
     * 
     */
    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilterComboBox.setValue(null);
        conditionFilterComboBox.setValue(null);
        availabilityFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.availability", "All Items"));
    }

    /**
     * 
     */
    private void refreshItems() {
        System.out.println("InventoryController: refreshItems() called");
        
        try {
            // Get fresh data from the service
            ObservableList<ItemViewModel> newItems = itemService.getUserItemsListAsViewModel();
            System.out.println("InventoryController: Got " + newItems.size() + " items from service");
            
            // Always recreate the FilteredList to avoid synchronization issues
            filteredItems = new FilteredList<>(newItems);
            
            // Apply current filter settings before setting to table
            updateFilters();
            
            // Set to table
            itemsTable.setItems(filteredItems);
            
            // Re-setup listeners
            setupSearchAndFilters();
            
            // Force refresh
            Platform.runLater(() -> {
                itemsTable.refresh();
                System.out.println("InventoryController: Table refresh completed. Filtered items count: " + filteredItems.size());
            });
            
        } catch (Exception e) {
            System.err.println("Error refreshing items: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param item
     */
    private void showItemDetails(ItemViewModel item) {
        itemNameLabel.setText(item.getName());
        itemDescriptionLabel.setText(item.getDescription());

        if (item.hasImage()) {
            imageService.fetchImage(item.getImagePath())
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            itemImageView.setImage(image);
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println("Failed to load item image: " + ex.getMessage());
                        Platform.runLater(() -> {
                            itemImageView.setImage(new Image("/images/no_image.png"));
                        });
                        return null;
                    });
        } else {
            itemImageView.setImage(new Image("/images/no_image.png"));
        }
    }

    /**
     * 
     */
    private void clearItemDetails() {
        itemNameLabel.setText("");
        itemDescriptionLabel.setText("");
        itemImageView.setImage(null);
    }

    /**
     * 
     */
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
        
        // Setup actions column
        setupActionsColumn();
    }
    
    /**
     * 
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button viewButton = new Button(localeService.getMessage("button.view", "View"));
            private final Button editButton = new Button(localeService.getMessage("button.edit", "Edit"));
            private final Button deleteButton = new Button(localeService.getMessage("button.delete", "Delete"));
            private final javafx.scene.layout.HBox actionBox = new javafx.scene.layout.HBox(5, viewButton, editButton, deleteButton);
            
            {
                viewButton.getStyleClass().addAll("secondary-button", "table-action-button");
                editButton.getStyleClass().addAll("primary-button", "table-action-button");
                deleteButton.getStyleClass().addAll("danger-button", "table-action-button");
                actionBox.setAlignment(javafx.geometry.Pos.CENTER);
                
                viewButton.setOnAction(_ -> {
                    ItemViewModel item = getTableView().getItems().get(getIndex());
                    showItemDetails(item);
                });
                
                editButton.setOnAction(_ -> {
                    ItemViewModel item = getTableView().getItems().get(getIndex());
                    navigationService.openItemDialog(item);
                });
                
                deleteButton.setOnAction(_ -> {
                    ItemViewModel item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionBox);
                    
                    // Update delete button state based on item's reserved quantity
                    ItemViewModel currentItem = getTableView().getItems().get(getIndex());
                    if (currentItem != null) {
                        int reservedQuantity = currentItem.getTotalQuantity() - currentItem.getAvailableQuantity();
                        boolean hasReservedQuantities = reservedQuantity > 0;
                        
                        deleteButton.setDisable(hasReservedQuantities);
                        
                        if (hasReservedQuantities) {
                            deleteButton.setTooltip(new Tooltip(localeService.getMessage("item.delete.tooltip.reserved", 
                                    "Cannot delete: " + reservedQuantity + " quantities are reserved")));
                            deleteButton.getStyleClass().add("disabled-button");
                        } else {
                            deleteButton.setTooltip(new Tooltip(localeService.getMessage("item.delete.tooltip.available", "Delete this item")));
                            deleteButton.getStyleClass().remove("disabled-button");
                        }
                    }
                }
            }
        });
    }
    
    /**
     * @param item
     */
    private void handleDeleteItem(ItemViewModel item) {
        // Check if item has reserved quantities
        int reservedQuantity = item.getTotalQuantity() - item.getAvailableQuantity();
        if (reservedQuantity > 0) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("item.delete.warning.title", "Cannot Delete Item"),
                    localeService.getMessage("item.delete.warning.header", "Item Cannot Be Deleted"),
                    localeService.getMessage("item.delete.warning.reserved.message", 
                            "This item cannot be deleted because it has " + reservedQuantity + 
                            " reserved quantities. Reserved items are currently part of active offers or listings."));
            return;
        }
        
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("item.delete.title"),
                localeService.getMessage("item.delete.header"),
                localeService.getMessage("item.delete.content", item.getName()));

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            itemService.deleteItem(item.getId())
                    .thenAccept(_ -> {
                        refreshItems();
                    })
                    .exceptionally(ex -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("item.delete.error.title"),
                                localeService.getMessage("item.delete.error.header"),
                                ex.getMessage());
                        return null;
                    });
        }
    }
}