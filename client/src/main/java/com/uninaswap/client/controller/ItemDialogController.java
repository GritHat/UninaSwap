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
    @FXML private ComboBox<Category> categoryComboBox; // Changed from TextField to ComboBox<Category>
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
    private final CategoryService categoryService = CategoryService.getInstance(); // Add this
    
    private ItemDTO item;
    private boolean isNewItem;
    private int initialStock;
    private int initialAvailable;
    private int reservedQuantity;
    private File selectedImageFile;
    
    @FXML
    public void initialize() {
        // Set up the condition combo box
        conditionComboBox.setItems(FXCollections.observableArrayList(ItemCondition.values()));
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
        
        // Set up the category combo box
        setupCategoryComboBox();
        
        // Set up the year spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory yearFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2030, 2023);
        yearSpinner.setValueFactory(yearFactory);
        
        // Set up the stock spinner
        SpinnerValueFactory.IntegerSpinnerValueFactory stockFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1);
        stockSpinner.setValueFactory(stockFactory);
    }
    
    private void setupCategoryComboBox() {
        // Get categories excluding ALL (which is only for search/filtering)
        categoryComboBox.setItems(FXCollections.observableArrayList(categoryService.getSelectableCategories()));
        
        // Set up string converter to show localized names but work with Category objects
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
                // This won't be used for ComboBox selection, but needed for interface
                return categoryService.getCategoryByDisplayName(string);
            }
        });
        
        // Set default to OTHER
        categoryComboBox.setValue(Category.OTHER);
    }
    
    public void setItem(ItemDTO item) {
        this.item = item;
        
        // Determine if this is a new item
        isNewItem = item.getId() == null || item.getId().isEmpty();
        
        // Calculate initial values
        initialStock = item.getStockQuantity() != null ? item.getStockQuantity() : 1;
        initialAvailable = item.getAvailableQuantity() != null ? item.getAvailableQuantity() : initialStock;
        reservedQuantity = initialStock - initialAvailable;
        
        // Populate fields with item data
        nameField.setText(item.getName() != null ? item.getName() : "");
        descriptionField.setText(item.getDescription() != null ? item.getDescription() : "");
        conditionComboBox.setValue(item.getCondition());
        
        // Set category from existing item data
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
        
        // Set stock spinner minimum to the reserved quantity for existing items
        SpinnerValueFactory.IntegerSpinnerValueFactory stockFactory = 
            (SpinnerValueFactory.IntegerSpinnerValueFactory) stockSpinner.getValueFactory();
        
        if (!isNewItem) {
            stockFactory.setMin(Math.max(1, reservedQuantity));
        }
        
        stockFactory.setValue(initialStock);
        
        // Set initial labels
        availableValueLabel.setText(String.valueOf(initialAvailable));
        reservedValueLabel.setText(String.valueOf(reservedQuantity));
        
        // Set up listener for stock changes
        stockSpinner.valueProperty().addListener((_, oldVal, newVal) -> {
            if (isNewItem) {
                // For new items, available = stock
                availableValueLabel.setText(newVal.toString());
            } else {
                int oldStock = oldVal.intValue();
                int newStock = newVal.intValue();
                int currentAvailable = Integer.parseInt(availableValueLabel.getText());
                
                if (newStock > oldStock) {
                    // If stock increases, available increases by the same amount
                    int increase = newStock - oldStock;
                    availableValueLabel.setText(String.valueOf(currentAvailable + increase));
                } else if (newStock < oldStock) {
                    // If stock decreases, available decreases (but not below 0)
                    int decrease = oldStock - newStock;
                    int newAvailable = Math.max(0, currentAvailable - decrease);
                    availableValueLabel.setText(String.valueOf(newAvailable));
                }
            }
        });
        
        // Load existing image if available
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
                // Store the file for later upload
                selectedImageFile = file;
                
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
    }
    
    public ItemDTO getUpdatedItem() {
        // Update the item with values from form fields
        item.setName(nameField.getText());
        item.setDescription(descriptionField.getText());
        item.setCondition(conditionComboBox.getValue());
        
        // Store the unlocalized category string for server consistency
        Category selectedCategory = categoryComboBox.getValue();
        String categoryToStore = selectedCategory != null ? selectedCategory.name() : Category.OTHER.name();
        item.setCategory(categoryToStore); // This sends the enum name (e.g., "ELECTRONICS") to server
        
        item.setBrand(brandField.getText());
        item.setModel(modelField.getText());
        item.setYearOfProduction(yearSpinner.getValue());
        item.setStockQuantity(stockSpinner.getValue());
        
        // Set available quantity based on displayed value
        item.setAvailableQuantity(Integer.parseInt(availableValueLabel.getText()));
        
        return item;
    }
    
    public File getSelectedImageFile() {
        return selectedImageFile;
    }
}