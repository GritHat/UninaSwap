package com.uninaswap.client.controller;

import com.uninaswap.client.service.ItemService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
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
import java.util.Optional;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventoryController {
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
    private FilteredList<ItemViewModel> filteredItems;

    @FXML
    public void initialize() {
        setupFilters();
        setupTableColumns();
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
        editButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
        refreshItems();
        eventBus.subscribe(EventTypes.ITEM_UPDATED, _ -> {
            refreshItems();
        });
        eventBus.subscribe(EventTypes.USER_LOGGED_OUT, _ -> {
            Platform.runLater(() -> {
                itemsTable.getItems().clear();
                clearItemDetails();
                System.out.println("InventoryController: Cleared view on logout");
            });
        });
    }
    private void setupFilters() {
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
        availabilityFilterComboBox.setItems(FXCollections.observableArrayList(
                localeService.getMessage("inventory.filter.all.availability", "All Items"),
                localeService.getMessage("inventory.filter.available", "Available"),
                localeService.getMessage("inventory.filter.reserved", "Reserved"),
                localeService.getMessage("inventory.filter.out_of_stock", "Out of Stock")
        ));
        availabilityFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.availability", "All Items"));
        categoryFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        conditionFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
        availabilityFilterComboBox.valueProperty().addListener((_, _, _) -> updateFilters());
    }

    private void setupSearchFilter(FilteredList<ItemViewModel> filteredItems) {
        searchField.textProperty().addListener((_, _, _) -> updateFilters());
        this.filteredItems = filteredItems;
    }

    private void updateFilters() {
        if (filteredItems == null) return;

        filteredItems.setPredicate(item -> {
            String searchText = searchField.getText().toLowerCase().trim();
            if (!searchText.isEmpty()) {
                if (!item.getName().toLowerCase().contains(searchText) &&
                    !item.getDescription().toLowerCase().contains(searchText) &&
                    !item.getBrand().toLowerCase().contains(searchText) &&
                    !item.getModel().toLowerCase().contains(searchText)) {
                    return false;
                }
            }
            String categoryFilter = categoryFilterComboBox.getValue();
            if (!localeService.getMessage("inventory.filter.all.categories", "All Categories").equals(categoryFilter)) {
                String itemCategory = item.getItemCategory();
                if (itemCategory == null || !getCategoryDisplayName(itemCategory).equals(categoryFilter)) {
                    return false;
                }
            }
            String conditionFilter = conditionFilterComboBox.getValue();
            if (!localeService.getMessage("inventory.filter.all.conditions", "All Conditions").equals(conditionFilter)) {
                ItemCondition itemCondition = item.getCondition();
                if (itemCondition == null || !itemCondition.getDisplayName().equals(conditionFilter)) {
                    return false;
                }
            }
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
        ItemViewModel newItem = new ItemViewModel();
        navigationService.openItemDialog(newItem);
    }

    @FXML
    private void handleEditItem() {
        ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            navigationService.openItemDialog(selectedItem);
        }
    }

    @FXML
    private void handleDeleteItem() {
        ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
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

    @FXML
    private void handleRefreshItems() {
        refreshItems();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        categoryFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.categories", "All Categories"));
        conditionFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.conditions", "All Conditions"));
        availabilityFilterComboBox.setValue(localeService.getMessage("inventory.filter.all.availability", "All Items"));
    }

    private void refreshItems() {
        itemsTable.setItems(itemService.getUserItemsListAsViewModel());
    }

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

    private void clearItemDetails() {
        itemNameLabel.setText("");
        itemDescriptionLabel.setText("");
        itemImageView.setImage(null);
    }

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
}