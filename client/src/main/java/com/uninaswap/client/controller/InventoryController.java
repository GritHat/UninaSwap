package com.uninaswap.client.controller;

import com.uninaswap.client.service.ItemService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.common.enums.ItemCondition;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
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
    private final UserSessionService userSessionService = UserSessionService.getInstance();
    
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
                localeService.getMessage("item.delete.content", selectedItem.getName())
            );
            
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
                            ex.getMessage()
                        );
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
    
    private void showItemDetails(ItemDTO item) {
        itemNameLabel.setText(item.getName());
        itemDescriptionLabel.setText(item.getDescription());
        
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
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
    
    private void showItemDialog(ItemDTO item) {
        // Create dialog
        Dialog<ItemDTO> dialog = new Dialog<>();
        dialog.setTitle(localeService.getMessage(item.getId() == null ? "item.add.title" : "item.edit.title"));
        dialog.setHeaderText(localeService.getMessage(item.getId() == null ? "item.add.header" : "item.edit.header"));
        
        // Set buttons
        ButtonType saveButtonType = new ButtonType(localeService.getMessage("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText(localeService.getMessage("item.name.prompt"));
        nameField.setText(item.getName() != null ? item.getName() : "");
        
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText(localeService.getMessage("item.description.prompt"));
        descriptionField.setText(item.getDescription() != null ? item.getDescription() : "");
        descriptionField.setPrefRowCount(3);
        
        ComboBox<ItemCondition> conditionComboBox = new ComboBox<>(
            FXCollections.observableArrayList(ItemCondition.values())
        );
        conditionComboBox.setConverter(new StringConverter<ItemCondition>() {
            @Override
            public String toString(ItemCondition object) {
                return object != null ? object.getDisplayName() : "";
            }
            
            @Override
            public ItemCondition fromString(String string) {
                return null; // Not used for ComboBox
            }
        });
        conditionComboBox.setValue(item.getCondition());
        
        TextField categoryField = new TextField();
        categoryField.setPromptText(localeService.getMessage("item.category.prompt"));
        categoryField.setText(item.getCategory() != null ? item.getCategory() : "");
        
        TextField brandField = new TextField();
        brandField.setPromptText(localeService.getMessage("item.brand.prompt"));
        brandField.setText(item.getBrand() != null ? item.getBrand() : "");
        
        TextField modelField = new TextField();
        modelField.setPromptText(localeService.getMessage("item.model.prompt"));
        modelField.setText(item.getModel() != null ? item.getModel() : "");
        
        Spinner<Integer> yearSpinner = new Spinner<>(1900, 2030, 2023);
        yearSpinner.setEditable(true);
        if (item.getYearOfProduction() != null) {
            yearSpinner.getValueFactory().setValue(item.getYearOfProduction());
        }
        
        Spinner<Integer> stockSpinner = new Spinner<>(1, 9999, 1);
        stockSpinner.setEditable(true);
        if (item.getStockQuantity() != null) {
            stockSpinner.getValueFactory().setValue(item.getStockQuantity());
        }
        
        ImageView imagePreview = new ImageView();
        imagePreview.setFitHeight(100);
        imagePreview.setFitWidth(100);
        imagePreview.setPreserveRatio(true);
        
        Button imageButton = new Button(localeService.getMessage("item.image.select"));
        
        // Store the selected image file
        final File[] selectedImageFile = {null};
        
        // Show existing image if available
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            imageService.fetchImage(item.getImagePath())
                .thenAccept(image -> {
                    Platform.runLater(() -> {
                        imagePreview.setImage(image);
                    });
                })
                .exceptionally(ex -> { 
                    System.err.println("Failed to load item image: " + ex.getMessage());
                    return null;
                });
        }
        
        imageButton.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(localeService.getMessage("item.image.chooser.title"));
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            
            File file = fileChooser.showOpenDialog(dialog.getOwner());
            if (file != null) {
                try {
                    // Store the file for later upload
                    selectedImageFile[0] = file;
                    
                    // Show preview
                    Image image = new Image(file.toURI().toString());
                    imagePreview.setImage(image);
                } catch (Exception ex) {
                    AlertHelper.showErrorAlert(
                        localeService.getMessage("item.image.error.title"),
                        localeService.getMessage("item.image.error.header"),
                        ex.getMessage()
                    );
                }
            }
        });
        
        // Add all components to grid
        grid.add(new Label(localeService.getMessage("item.name.label")), 0, 0);
        grid.add(nameField, 1, 0);
        
        grid.add(new Label(localeService.getMessage("item.description.label")), 0, 1);
        grid.add(descriptionField, 1, 1);
        
        grid.add(new Label(localeService.getMessage("item.condition.label")), 0, 2);
        grid.add(conditionComboBox, 1, 2);
        
        grid.add(new Label(localeService.getMessage("item.category.label")), 0, 3);
        grid.add(categoryField, 1, 3);
        
        grid.add(new Label(localeService.getMessage("item.brand.label")), 0, 4);
        grid.add(brandField, 1, 4);
        
        grid.add(new Label(localeService.getMessage("item.model.label")), 0, 5);
        grid.add(modelField, 1, 5);
        
        grid.add(new Label(localeService.getMessage("item.year.label")), 0, 6);
        grid.add(yearSpinner, 1, 6);
        
        grid.add(new Label(localeService.getMessage("item.stock.label")), 0, 7);
        grid.add(stockSpinner, 1, 7);
        
        grid.add(new Label(localeService.getMessage("item.image.label")), 0, 8);
        
        HBox imageBox = new HBox(10);
        imageBox.getChildren().addAll(imageButton, imagePreview);
        grid.add(imageBox, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);
        
        // Convert the result when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Update item with form values
                item.setName(nameField.getText());
                item.setDescription(descriptionField.getText());
                item.setCondition(conditionComboBox.getValue());
                item.setCategory(categoryField.getText());
                item.setBrand(brandField.getText());
                item.setModel(modelField.getText());
                item.setYearOfProduction(yearSpinner.getValue());
                item.setStockQuantity(stockSpinner.getValue());
                item.setAvailableQuantity(stockSpinner.getValue()); // Initially available = stock
                
                return item;
            }
            return null;
        });
        
        Optional<ItemDTO> result = dialog.showAndWait();
        
        result.ifPresent(updatedItem -> {
            // If we have a new image, upload it first using HTTP
            if (selectedImageFile[0] != null) {
                String username = userSessionService.getUser().getUsername();
                imageService.uploadImageViaHttp(username, selectedImageFile[0])
                    .thenAccept(imagePath -> {
                        updatedItem.setImagePath(imagePath);
                        saveItem(updatedItem);
                    })
                    .exceptionally(ex -> {
                        AlertHelper.showErrorAlert(
                            localeService.getMessage("item.image.upload.error.title"),
                            localeService.getMessage("item.image.upload.error.header"),
                            ex.getMessage()
                        );
                        return null;
                    });
            } else {
                // No new image, just save the item
                saveItem(updatedItem);
            }
        });
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
                        ex.getMessage()
                    );
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
                        ex.getMessage()
                    );
                    return null;
                });
        }
    }
}