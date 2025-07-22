package com.uninaswap.client.controller;

import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.CategoryService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.common.enums.ItemCondition;
import com.uninaswap.common.enums.Category;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;

public class ItemDialogController {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<ItemCondition> conditionComboBox;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private Spinner<Integer> stockSpinner;
    @FXML private Label availableValueLabel;
    @FXML private Label reservedValueLabel;
    @FXML private Button imageButton;
    @FXML private ImageView imagePreview;
    
    private final ImageService imageService = ImageService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final CategoryService categoryService = CategoryService.getInstance();
    
    private ItemDTO item;
    private boolean isNewItem;
    private int initialStock;
    private int initialAvailable;
    private int reservedQuantity;
    private File selectedImageFile;
    
    @FXML
    public void initialize() {
        conditionComboBox.setItems(FXCollections.observableArrayList(ItemCondition.values()));
        conditionComboBox.setConverter(new StringConverter<ItemCondition>() {
            @Override
            public String toString(ItemCondition object) {
                return object != null ? object.getDisplayName() : "";
            }
            @Override
            public ItemCondition fromString(String string) {
                return null;
            }
        });
        setupCategoryComboBox();
        SpinnerValueFactory.IntegerSpinnerValueFactory yearFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2030, 2023);
        yearSpinner.setValueFactory(yearFactory);
        SpinnerValueFactory.IntegerSpinnerValueFactory stockFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1);
        stockSpinner.setValueFactory(stockFactory);
    }
    
    private void setupCategoryComboBox() {
        categoryComboBox.setItems(FXCollections.observableArrayList(categoryService.getSelectableCategories()));
        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                if (category == null) {
                    return "";
                }
                return categoryService.getLocalizedCategoryName(category);
            }
            
            @Override
            public Category fromString(String string) {
                return categoryService.getCategoryByDisplayName(string);
            }
        });
        categoryComboBox.setValue(Category.OTHER);
    }
    
    public void setItem(ItemDTO item) {
        this.item = item;
        isNewItem = item.getId() == null || item.getId().isEmpty();
        initialStock = item.getStockQuantity() != null ? item.getStockQuantity() : 1;
        initialAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : initialStock;
        reservedQuantity = initialStock - initialAvailable;
        nameField.setText(item.getName() != null ? item.getName() : "");
        descriptionField.setText(item.getDescription() != null ? item.getDescription() : "");
        conditionComboBox.setValue(item.getCondition());
        if (item.getCategory() != null && !item.getCategory().isEmpty()) {
            Category category = Category.fromString(item.getCategory());
            categoryComboBox.setValue(category);
        } else {
            categoryComboBox.setValue(Category.OTHER);
        }
        brandField.setText(item.getBrand() != null ? item.getBrand() : "");
        modelField.setText(item.getModel() != null ? item.getModel() : "");
        
        if (item.getYearOfProduction() != null) {
            yearSpinner.getValueFactory().setValue(item.getYearOfProduction());
        }
        SpinnerValueFactory.IntegerSpinnerValueFactory stockFactory = 
            (SpinnerValueFactory.IntegerSpinnerValueFactory) stockSpinner.getValueFactory();
        
        if (!isNewItem) {
            stockFactory.setMin(Math.max(1, reservedQuantity));
        }
        
        stockFactory.setValue(initialStock);
        availableValueLabel.setText(String.valueOf(initialAvailable));
        reservedValueLabel.setText(String.valueOf(reservedQuantity));
        stockSpinner.valueProperty().addListener((_, oldVal, newVal) -> {
            if (isNewItem) {
                availableValueLabel.setText(newVal.toString());
            } else {
                int oldStock = oldVal.intValue();
                int newStock = newVal.intValue();
                int currentAvailable = Integer.parseInt(availableValueLabel.getText());
                if (newStock > oldStock) {
                    int increase = newStock - oldStock;
                    availableValueLabel.setText(String.valueOf(currentAvailable + increase));
                } else if (newStock < oldStock) {
                    int decrease = oldStock - newStock;
                    int newAvailable = Math.max(0, currentAvailable - decrease);
                    availableValueLabel.setText(String.valueOf(newAvailable));
                }
            }
        });
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            imageService.fetchImage(item.getImagePath())
                .thenAccept(image -> {
                    Platform.runLater(() -> {
                        imagePreview.setImage(image);
                        updateImagePreviewVisibility();
                    });
                })
                .exceptionally(ex -> { 
                    System.err.println("Failed to load item image: " + ex.getMessage());
                    return null;
                });
        } else {
            updateImagePreviewVisibility();
        }
    }
    
    private void updateImagePreviewVisibility() {
        boolean hasImage = imagePreview.getImage() != null;
        Node uploadPlaceholder = imagePreview.getParent().lookup(".upload-placeholder");
        if (uploadPlaceholder != null) {
            if (hasImage) {
                uploadPlaceholder.getStyleClass().add("has-image");
            } else {
                uploadPlaceholder.getStyleClass().remove("has-image");
            }
        }
    }
    
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(localeService.getMessage("item.image.chooser.title"));
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(imageButton.getScene().getWindow());
        if (file != null) {
            try {
                selectedImageFile = file;
                Image image = new Image(file.toURI().toString());
                imagePreview.setImage(image);
                updateImagePreviewVisibility();
                
            } catch (Exception ex) {
                AlertHelper.showErrorAlert(
                    localeService.getMessage("item.image.error.title"),
                    localeService.getMessage("item.image.error.header"),
                    ex.getMessage()
                );
            }
        }
    }
    
    public ItemDTO getUpdatedItem() {
        item.setName(nameField.getText());
        item.setDescription(descriptionField.getText());
        item.setCondition(conditionComboBox.getValue());
        
        Category selectedCategory = categoryComboBox.getValue();
        String categoryToStore = selectedCategory != null ? selectedCategory.name() : Category.OTHER.name();
        item.setCategory(categoryToStore);
        
        item.setBrand(brandField.getText());
        item.setModel(modelField.getText());
        item.setYearOfProduction(yearSpinner.getValue());
        item.setStockQuantity(stockSpinner.getValue());
        item.setAvailableQuantity(Integer.parseInt(availableValueLabel.getText()));
        
        return item;
    }
    
    public File getSelectedImageFile() {
        return selectedImageFile;
    }
}