package com.uninaswap.client.controller;

import com.uninaswap.client.service.ItemService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.common.enums.ItemCondition;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class InventoryController {
    @FXML
    private TableView<ItemDTO> itemsTable;
    @FXML
    private TableColumn<ItemDTO, String> nameColumn;
    @FXML
    private TableColumn<ItemDTO, String> conditionColumn;
    @FXML
    private TableColumn<ItemDTO, Integer> stockColumn;
    @FXML
    private TableColumn<ItemDTO, String> categoryColumn;
    @FXML
    private TableColumn<ItemDTO, Integer> reservedColumn;
    @FXML
    private TableColumn<ItemDTO, Integer> availableColumn;

    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    @FXML
    private FlowPane forSaleFlowPane;
    @FXML
    private FlowPane soldFlowPane;
    @FXML
    private FlowPane auctionsFlowPane;
    @FXML
    private FlowPane draftsFlowPane;

    private final ItemService itemService = ItemService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();

    @FXML
    public void initialize() {
        // Configure the table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        conditionColumn.setCellValueFactory(cellData -> {
            ItemCondition condition = cellData.getValue().getCondition();
            return new SimpleStringProperty(condition != null ? condition.getDisplayName() : "");
        });

        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Add cell factories for the new columns
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));

        // Calculate reserved quantity (total - available)
        reservedColumn.setCellValueFactory(cellData -> {
            int stock = cellData.getValue().getStockQuantity() != null ? cellData.getValue().getStockQuantity() : 0;
            int available = cellData.getValue().getAvailableQuantity() != null
                    ? cellData.getValue().getAvailableQuantity()
                    : 0;
            return Bindings.createObjectBinding(() -> stock - available);
        });

        // Enable buttons only when item is selected
        editButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(itemsTable.getSelectionModel().selectedItemProperty().isNull());

        // Load user's items
        loadItemCardsIntoTab(forSaleFlowPane, itemService.getUserItemsList());
        refreshItems();
    }

    @FXML
    private void handleAddItem() {
        showItemDialog(new ItemDTO());
    }

    @FXML
    private void handleEditItem() {
        ItemDTO selectedItem = itemsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            showItemDialog(selectedItem);
        }
    }

    @FXML
    private void handleDeleteItem() {
        ItemDTO selectedItem = itemsTable.getSelectionModel().getSelectedItem();
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
        itemsTable.setItems(itemService.getUserItemsList());
    }

    private void showItemDialog(ItemDTO item) {
        try {
            // Load the dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemDialogView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            DialogPane dialogPane = loader.load();
            // Create custom button types with localized text
            ButtonType confirmButtonType = new ButtonType(
                    localeService.getMessage("button.confirm"),
                    ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(
                    localeService.getMessage("button.cancel"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);
            // Get the controller
            ItemDialogController controller = loader.getController();

            // Set up the item in the controller
            controller.setItem(item);

            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(localeService.getMessage(item.getId() == null ? "item.add.title" : "item.edit.title"));
            dialog.setHeaderText(
                    localeService.getMessage(item.getId() == null ? "item.add.header" : "item.edit.header"));
            dialog.setDialogPane(dialogPane);

            // Show the dialog and handle result
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
                ItemDTO updatedItem = controller.getUpdatedItem();
                File selectedImageFile = controller.getSelectedImageFile();

                // If we have a new image, upload it first using HTTP
                if (selectedImageFile != null) {
                    imageService.uploadImageViaHttp(selectedImageFile)
                            .thenAccept(imagePath -> {
                                updatedItem.setImagePath(imagePath);
                                saveItem(updatedItem);
                            })
                            .exceptionally(ex -> {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("item.image.upload.error.title"),
                                        localeService.getMessage("item.image.upload.error.header"),
                                        ex.getMessage());
                                return null;
                            });
                } else {
                    // No new image, just save the item
                    saveItem(updatedItem);
                }
            }
        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("item.dialog.error.title"),
                    localeService.getMessage("item.dialog.error.header"),
                    e.getMessage());
        }
    }

    private void saveItem(ItemDTO item) {
        // Determine if this is a new or existing item
        if (item.getId() == null || item.getId().isEmpty()) {
            // Add new item
            itemService.addItem(item)
                    .thenAccept(_ -> {
                        refreshItems();
                    })
                    .exceptionally(ex -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("item.add.error.title"),
                                localeService.getMessage("item.add.error.header"),
                                ex.getMessage());
                        return null;
                    });
        } else {
            // Update existing item
            itemService.updateItem(item)
                    .thenAccept(_ -> {
                        refreshItems();
                    })
                    .exceptionally(ex -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("item.edit.error.title"),
                                localeService.getMessage("item.edit.error.header"),
                                ex.getMessage());
                        return null;
                    });
        }
    }

    private void loadItemCardsIntoTab(FlowPane container, List<ItemDTO> items) {
        container.getChildren().clear();
        for (ItemDTO item : items) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemCard.fxml"));
                loader.setResources(localeService.getResourceBundle());
                loader.setController(new ItemCardController(item));
                container.getChildren().add(loader.load());
            } catch (IOException e) {
                AlertHelper.showErrorAlert(
                        localeService.getMessage("item.card.load.error.title"),
                        localeService.getMessage("item.card.load.error.header"),
                        e.getMessage());
            }
        }
    }

    /*
     * Mi piace il disegno
     * ┌─────────────────────────────────┐
     * │ [Search Bar] [Add +] │
     * ├─────────────────────────────────┤
     * │ ┌─────────────────────────────┐ │
     * │ │ [img] Item Name €XX│ │ ← ItemCard
     * │ │ Description [•]│ │
     * │ └─────────────────────────────┘ │
     * │ ┌─────────────────────────────┐ │
     * │ │ [img] Item Name 2 €XX │ │ ← ItemCard
     * │ │ Description 2 [•]│ │
     * │ └─────────────────────────────┘ │
     * └─────────────────────────────────┘
     */
}