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

public class InventoryController {

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
                System.out.println("InventoryController: Cleared view on logout");
            });
        });
    }

    @FXML
    private void handleAddItem() {
        // Create a new empty ItemViewModel for adding
        ItemViewModel newItem = new ItemViewModel();
        navigationService.openItemDialog(newItem);
    }

    @FXML
    private void handleEditItem() {
        ItemViewModel selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Pass the ItemViewModel directly to the navigation service
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

    private void refreshItems() {
        // Convert ItemDTOs from service to ItemViewModels
        itemsTable.setItems(itemService.getUserItemsListAsViewModel());
    }

    private void showItemDetails(ItemViewModel item) {
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
}