package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.uninaswap.common.enums.ItemCondition;

/**
 * 
 */
public class ItemDTO implements Serializable {
    /**
     * 
     */
    private String id;
    /**
     * 
     */
    private UserDTO owner;
    /**
     * 
     */
    private String name;
    /**
     * 
     */
    private String description;
    /**
     * 
     */
    private String imagePath;
    /**
     * 
     */
    private ItemCondition condition;
    /**
     * 
     */
    private String category;
    /**
     * 
     */
    private String brand;
    /**
     * 
     */
    private String model;
    /**
     * 
     */
    private Integer yearOfProduction;
    /**
     * 
     */
    private Integer stockQuantity;
    /**
     * 
     */
    private Integer availableQuantity;
    /**
     * 
     */
    private Long ownerId;
    /**
     * 
     */
    private LocalDateTime createdAt;
    /**
     * 
     */
    private LocalDateTime updatedAt;
    /**
     * 
     */
    private Boolean isAvailable;
    /**
     * 
     */
    private Boolean isVisible;

    
    /**
     * 
     */
    public ItemDTO() {
    }

    
    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return
     */
    public UserDTO getOwner() {
        return owner;
    }

    /**
     * @param owner
     */
    public void setOwner(UserDTO owner) {
        this.owner = owner;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * @param imagePath
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * @return
     */
    public ItemCondition getCondition() {
        return condition;
    }

    /**
     * @param condition
     */
    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }

    /**
     * @return
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return
     */
    public String getBrand() {
        return brand;
    }

    /**
     * @param brand
     */
    public void setBrand(String brand) {
        this.brand = brand;
    }

    /**
     * @return
     */
    public String getModel() {
        return model;
    }

    /**
     * @param model
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return
     */
    public Integer getYearOfProduction() {
        return yearOfProduction;
    }

    /**
     * @param yearOfProduction
     */
    public void setYearOfProduction(Integer yearOfProduction) {
        this.yearOfProduction = yearOfProduction;
    }

    /**
     * @return
     */
    public Integer getStockQuantity() {
        return stockQuantity;
    }

    /**
     * @param stockQuantity
     */
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    /**
     * @return
     */
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    /**
     * @param availableQuantity
     */
    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    /**
     * @return
     */
    public Long getOwnerId() {
        return ownerId;
    }

    /**
     * @param ownerId
     */
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * @return
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return
     */
    public Boolean isAvailable() {
        return isAvailable;
    }

    /**
     * @param available
     */
    public void setAvailable(Boolean available) {
        isAvailable = available;
    }

    /**
     * @return
     */
    public Boolean isVisible() {
        return isVisible;
    }

    /**
     * @param visible
     */
    public void setVisible(Boolean visible) {
        isVisible = visible;
    }
}