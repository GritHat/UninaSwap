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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Optional;

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

    private final ItemService itemService = ItemService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final EventBusService eventBus = EventBusService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();

    @FXML
    public void initialize() {
        // Configure the table columns to work with ItemViewModel
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        conditionColumn.setCellValueFactory(cellData -> {
            ItemCondition condition = cellData.getValue().getCondition();
            return new SimpleStringProperty(condition != null ? condition.getDisplayName() : "");
        });

        stockColumn.setCellValueFactory(cellData -> cellData.getValue().totalQuantityProperty().asObject());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().itemCategoryProperty());

        // Add cell factories for the new columns
        availableColumn.setCellValueFactory(cellData -> cellData.getValue().availableQuantityProperty().asObject());

        // Calculate reserved quantity (total - available)
        reservedColumn.setCellValueFactory(cellData -> {
            return Bindings.createObjectBinding(() -> cellData.getValue().getReservedQuantity(),
                    cellData.getValue().totalQuantityProperty(),
                    cellData.getValue().availableQuantityProperty());
        });

        // Add selection listener to show details
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

        // Initial UI refresh
        refreshUI();
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

    @Override
    public void refreshUI() {
        // Update button labels
        if (addButton != null) {
            addButton.setText(localeService.getMessage("inventory.new.item", "New Item"));
        }
        if (editButton != null) {
            editButton.setText(localeService.getMessage("button.edit", "Edit"));
        }
        if (deleteButton != null) {
            deleteButton.setText(localeService.getMessage("button.delete", "Delete"));
        }
        if (refreshButton != null) {
            refreshButton.setText(localeService.getMessage("button.refresh", "Refresh"));
        }

        // Update table column headers
        if (nameColumn != null) {
            nameColumn.setText(localeService.getMessage("item.name.column", "Name"));
        }
        if (conditionColumn != null) {
            conditionColumn.setText(localeService.getMessage("item.condition.column", "Condition"));
        }
        if (stockColumn != null) {
            stockColumn.setText(localeService.getMessage("item.stock.column", "Stock"));
        }
        if (categoryColumn != null) {
            categoryColumn.setText(localeService.getMessage("item.category.column", "Category"));
        }
        if (reservedColumn != null) {
            reservedColumn.setText(localeService.getMessage("item.reserved.column", "Reserved"));
        }
        if (availableColumn != null) {
            availableColumn.setText(localeService.getMessage("item.available.column", "Available Quantity"));
        }

        // Refresh item details if an item is selected
        ItemViewModel selectedItem = itemsTable != null ? itemsTable.getSelectionModel().getSelectedItem() : null;
        if (selectedItem != null) {
            showItemDetails(selectedItem);
        } else {
            clearItemDetails();
        }

        // Refresh the table to update any localized content in cells
        if (itemsTable != null) {
            itemsTable.refresh();
        }
    }
}