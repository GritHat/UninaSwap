package com.uninaswap.client.viewmodel;

import com.uninaswap.common.enums.ItemCondition;
import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * 
 */
public class ItemViewModel {
    /**
     * 
     */
    private final StringProperty id = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty name = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty description = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty itemCategory = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<ItemCondition> condition = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<Integer> year = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final StringProperty model = new SimpleStringProperty();
    /**
     * 
     */
    private final StringProperty brand = new SimpleStringProperty();
    /**
     * 
     */
    private final IntegerProperty totalQuantity = new SimpleIntegerProperty();
    /**
     * 
     */
    private final IntegerProperty availableQuantity = new SimpleIntegerProperty();
    /**
     * 
     */
    private final StringProperty imagePath = new SimpleStringProperty();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final ObjectProperty<UserViewModel> owner = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final BooleanProperty isAvailable = new SimpleBooleanProperty(true);
    /**
     * 
     */
    private final BooleanProperty isVisible = new SimpleBooleanProperty(true);

    
    /**
     * 
     */
    private final BooleanProperty isSelected = new SimpleBooleanProperty(false);
    /**
     * 
     */
    private final IntegerProperty selectedQuantity = new SimpleIntegerProperty(0);

    
    /**
     * 
     */
    public ItemViewModel() {
    }

    /**
     * @param id
     * @param name
     * @param condition
     * @param totalQuantity
     * @param availableQuantity
     */
    public ItemViewModel(String id, String name, ItemCondition condition,
            int totalQuantity, int availableQuantity) {
        setId(id);
        setName(name);
        setCondition(condition);
        setTotalQuantity(totalQuantity);
        setAvailableQuantity(availableQuantity);
    }

    
    /**
     * @return
     */
    public StringProperty idProperty() {
        return id;
    }

    /**
     * @return
     */
    public StringProperty nameProperty() {
        return name;
    }

    /**
     * @return
     */
    public StringProperty descriptionProperty() {
        return description;
    }

    /**
     * @return
     */
    public StringProperty itemCategoryProperty() {
        return itemCategory;
    }

    /**
     * @return
     */
    public ObjectProperty<ItemCondition> conditionProperty() {
        return condition;
    }

    /**
     * @return
     */
    public ObjectProperty<Integer> yearProperty() {
        return year;
    }

    /**
     * @return
     */
    public IntegerProperty totalQuantityProperty() {
        return totalQuantity;
    }

    /**
     * @return
     */
    public IntegerProperty availableQuantityProperty() {
        return availableQuantity;
    }

    /**
     * @return
     */
    public StringProperty imagePathProperty() {
        return imagePath;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> updatedAtProperty() {
        return updatedAt;
    }

    /**
     * @return
     */
    public ObjectProperty<UserViewModel> ownerProperty() {
        return owner;
    }

    /**
     * @return
     */
    public BooleanProperty isAvailableProperty() {
        return isAvailable;
    }

    /**
     * @return
     */
    public BooleanProperty isVisibleProperty() {
        return isVisible;
    }

    /**
     * @return
     */
    public BooleanProperty isSelectedProperty() {
        return isSelected;
    }

    /**
     * @return
     */
    public IntegerProperty selectedQuantityProperty() {
        return selectedQuantity;
    }

    /**
     * @return
     */
    public StringProperty modelProperty() {
        return model;
    }

    /**
     * @return
     */
    public StringProperty brandProperty() {
        return brand;
    }

    
    /**
     * @return
     */
    public String getId() {
        return id.get();
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id.set(id);
    }

    /**
     * @return
     */
    public String getName() {
        return name.get();
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * @return
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description.set(description);
    }

    /**
     * @return
     */
    public String getItemCategory() {
        return itemCategory.get();
    }

    /**
     * @param itemCategory
     */
    public void setItemCategory(String itemCategory) {
        this.itemCategory.set(itemCategory);
    }

    /**
     * @return
     */
    public ItemCondition getCondition() {
        return condition.get();
    }

    /**
     * @param condition
     */
    public void setCondition(ItemCondition condition) {
        this.condition.set(condition);
    }

    /**
     * @return
     */
    public Integer getYear() {
        return year.get();
    }

    /**
     * @param year
     */
    public void setYear(Integer year) {
        this.year.set(year);
    }

    /**
     * @return
     */
    public int getTotalQuantity() {
        return totalQuantity.get();
    }

    /**
     * @param totalQuantity
     */
    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity.set(totalQuantity);
    }

    /**
     * @return
     */
    public int getAvailableQuantity() {
        return availableQuantity.get();
    }

    /**
     * @param availableQuantity
     */
    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity.set(availableQuantity);
    }

    /**
     * @return
     */
    public String getImagePath() {
        return imagePath.get();
    }

    /**
     * @param imagePath
     */
    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    /**
     * @return
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt.get();
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    /**
     * @return
     */
    public UserViewModel getOwner() {
        return owner.get();
    }

    /**
     * @param owner
     */
    public void setOwner(UserViewModel owner) {
        this.owner.set(owner);
    }

    /**
     * @return
     */
    public boolean isAvailable() {
        return isAvailable.get();
    }

    /**
     * @param available
     */
    public void setAvailable(boolean available) {
        this.isAvailable.set(available);
    }

    /**
     * @return
     */
    public boolean isVisible() {
        return isVisible.get();
    }

    /**
     * @param visible
     */
    public void setVisible(boolean visible) {
        this.isVisible.set(visible);
    }

    /**
     * @return
     */
    public boolean isSelected() {
        return isSelected.get();
    }

    /**
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.isSelected.set(selected);
    }

    /**
     * @return
     */
    public int getSelectedQuantity() {
        return selectedQuantity.get();
    }

    /**
     * @param selectedQuantity
     */
    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity.set(selectedQuantity);
    }

    /**
     * @param model
     */
    public void setModel(String model) {
        this.model.set(model);
    }

    /**
     * @return
     */
    public String getModel() {
        return model.get();
    }

    /**
     * @param brand
     */
    public void setBrand(String brand) {
        this.brand.set(brand);
    }

    /**
     * @return
     */
    public String getBrand() {
        return brand.get();
    }

    
    /**
     * @return
     */
    public boolean hasImage() {
        String path = getImagePath();
        return path != null && !path.trim().isEmpty() && !path.equals("default");
    }

    /**
     * @return
     */
    public String getConditionDisplayName() {
        ItemCondition cond = getCondition();
        return cond != null ? cond.getDisplayName() : "Non specificata";
    }

    /**
     * @return
     */
    public boolean isInStock() {
        return getAvailableQuantity() > 0;
    }

    /**
     * @return
     */
    public boolean isFullyReserved() {
        return getAvailableQuantity() == 0 && getTotalQuantity() > 0;
    }

    /**
     * @return
     */
    public int getReservedQuantity() {
        return Math.max(0, getTotalQuantity() - getAvailableQuantity());
    }

    /**
     * @return
     */
    public String getQuantityString() {
        if (getTotalQuantity() == getAvailableQuantity()) {
            return String.valueOf(getTotalQuantity());
        } else {
            return getAvailableQuantity() + "/" + getTotalQuantity();
        }
    }

    /**
     * @return
     */
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

    /**
     * @param quantity
     * @return
     */
    public boolean canSelectQuantity(int quantity) {
        return quantity > 0 && quantity <= getAvailableQuantity();
    }

    /**
     * 
     */
    public void selectMaxAvailable() {
        setSelectedQuantity(getAvailableQuantity());
        setSelected(getAvailableQuantity() > 0);
    }

    /**
     * 
     */
    public void clearSelection() {
        setSelected(false);
        setSelectedQuantity(0);
    }

    
    /**
     * @return
     */
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

    /**
     *
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ItemViewModel that = (ItemViewModel) obj;
        return getId() != null && getId().equals(that.getId());
    }

    /**
     *
     */
    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    /**
     *
     */
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
