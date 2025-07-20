package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.ItemCondition;
import javafx.beans.property.*;

import java.time.LocalDateTime;

public class ItemViewModel {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty itemCategory = new SimpleStringProperty();
    private final ObjectProperty<ItemCondition> condition = new SimpleObjectProperty<>();
    private final IntegerProperty year = new SimpleIntegerProperty();
    private final StringProperty model = new SimpleStringProperty();
    private final StringProperty brand = new SimpleStringProperty();
    private final IntegerProperty totalQuantity = new SimpleIntegerProperty();
    private final IntegerProperty availableQuantity = new SimpleIntegerProperty();
    private final StringProperty imagePath = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private final ObjectProperty<UserViewModel> owner = new SimpleObjectProperty<>();
    private final BooleanProperty isAvailable = new SimpleBooleanProperty(true);
    private final BooleanProperty isVisible = new SimpleBooleanProperty(true);

    // Additional UI-specific properties
    private final BooleanProperty isSelected = new SimpleBooleanProperty(false);
    private final IntegerProperty selectedQuantity = new SimpleIntegerProperty(0);

    // Constructors
    public ItemViewModel() {
    }

    public ItemViewModel(String id, String name, ItemCondition condition,
            int totalQuantity, int availableQuantity) {
        setId(id);
        setName(name);
        setCondition(condition);
        setTotalQuantity(totalQuantity);
        setAvailableQuantity(availableQuantity);
    }

    // Property getters
    public StringProperty idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty itemCategoryProperty() {
        return itemCategory;
    }

    public ObjectProperty<ItemCondition> conditionProperty() {
        return condition;
    }

    public IntegerProperty yearProperty() {
        return year;
    }

    public IntegerProperty totalQuantityProperty() {
        return totalQuantity;
    }

    public IntegerProperty availableQuantityProperty() {
        return availableQuantity;
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    public ObjectProperty<UserViewModel> ownerProperty() {
        return owner;
    }

    public BooleanProperty isAvailableProperty() {
        return isAvailable;
    }

    public BooleanProperty isVisibleProperty() {
        return isVisible;
    }

    public BooleanProperty isSelectedProperty() {
        return isSelected;
    }

    public IntegerProperty selectedQuantityProperty() {
        return selectedQuantity;
    }

    public StringProperty modelProperty() {
        return model;
    }

    public StringProperty brandProperty() {
        return brand;
    }

    // Getters and setters
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getItemCategory() {
        return itemCategory.get();
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory.set(itemCategory);
    }

    public ItemCondition getCondition() {
        return condition.get();
    }

    public void setCondition(ItemCondition condition) {
        this.condition.set(condition);
    }

    public int getYear() {
        return year.get();
    }

    public void setYear(int year) {
        this.year.set(year);
    }

    public int getTotalQuantity() {
        return totalQuantity.get();
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity.set(totalQuantity);
    }

    public int getAvailableQuantity() {
        return availableQuantity.get();
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity.set(availableQuantity);
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    public UserViewModel getOwner() {
        return owner.get();
    }

    public void setOwner(UserViewModel owner) {
        this.owner.set(owner);
    }

    public boolean isAvailable() {
        return isAvailable.get();
    }

    public void setAvailable(boolean available) {
        this.isAvailable.set(available);
    }

    public boolean isVisible() {
        return isVisible.get();
    }

    public void setVisible(boolean visible) {
        this.isVisible.set(visible);
    }

    public boolean isSelected() {
        return isSelected.get();
    }

    public void setSelected(boolean selected) {
        this.isSelected.set(selected);
    }

    public int getSelectedQuantity() {
        return selectedQuantity.get();
    }

    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity.set(selectedQuantity);
    }

    public void setModel(String model) {
        this.model.set(model);
    }

    public String getModel() {
        return model.get();
    }

    public void setBrand(String brand) {
        this.brand.set(brand);
    }

    public String getBrand() {
        return brand.get();
    }

    // Utility methods
    public boolean hasImage() {
        String path = getImagePath();
        return path != null && !path.trim().isEmpty() && !path.equals("default");
    }

    public String getConditionDisplayName() {
        ItemCondition cond = getCondition();
        return cond != null ? cond.getDisplayName() : "Non specificata";
    }

    public boolean isInStock() {
        return getAvailableQuantity() > 0;
    }

    public boolean isFullyReserved() {
        return getAvailableQuantity() == 0 && getTotalQuantity() > 0;
    }

    public int getReservedQuantity() {
        return Math.max(0, getTotalQuantity() - getAvailableQuantity());
    }

    public String getQuantityString() {
        if (getTotalQuantity() == getAvailableQuantity()) {
            return String.valueOf(getTotalQuantity());
        } else {
            return getAvailableQuantity() + "/" + getTotalQuantity();
        }
    }

    public String getAvailabilityStatus() {
        if (!isAvailable()) {
            return "Non disponibile";
        } else if (getAvailableQuantity() == 0) {
            return "Esaurito";
        } else if (getAvailableQuantity() < getTotalQuantity()) {
            return "Parzialmente disponibile";
        } else {
            return "Disponibile";
        }
    }

    public boolean canSelectQuantity(int quantity) {
        return quantity > 0 && quantity <= getAvailableQuantity();
    }

    public void selectMaxAvailable() {
        setSelectedQuantity(getAvailableQuantity());
        setSelected(getAvailableQuantity() > 0);
    }

    public void clearSelection() {
        setSelected(false);
        setSelectedQuantity(0);
    }

    // Create a display string for lists/tables
    public String getDisplayText() {
        StringBuilder display = new StringBuilder(getName());

        if (getCondition() != null) {
            display.append(" (").append(getConditionDisplayName()).append(")");
        }

        if (getYear() > 0) {
            display.append(" - ").append(getYear());
        }

        return display.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ItemViewModel that = (ItemViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ItemViewModel{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", condition=" + getCondition() +
                ", availableQuantity=" + getAvailableQuantity() +
                '}';
    }
}
