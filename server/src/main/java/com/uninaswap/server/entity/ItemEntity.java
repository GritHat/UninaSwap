package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.uninaswap.common.enums.ItemCondition;

/**
 * Represents a physical or virtual item that can be listed, offered, or traded
 */
@Entity
@Table(name = "items")
public class ItemEntity {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String imagePath;
    
    @Enumerated(EnumType.STRING)
    private ItemCondition condition;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Optional metadata fields
    private String category;
    private String brand;
    private String model;
    private Integer yearOfProduction;
    
    @Column(nullable = false)
    private Integer stockQuantity = 1;
    
    @Column(nullable = false)
    private Integer availableQuantity;  // How many are currently available (not in pending transactions)
    
    // Constructor
    public ItemEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.stockQuantity = 1;
        this.availableQuantity = 1;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public ItemCondition getCondition() {
        return condition;
    }
    
    public void setCondition(ItemCondition condition) {
        this.condition = condition;
    }
    
    public UserEntity getOwner() {
        return owner;
    }
    
    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public Integer getYearOfProduction() {
        return yearOfProduction;
    }
    
    public void setYearOfProduction(Integer yearOfProduction) {
        this.yearOfProduction = yearOfProduction;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
        
        // Update available quantity accordingly, but don't exceed stock
        if (this.availableQuantity > stockQuantity) {
            this.availableQuantity = stockQuantity;
        }
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
    
    public void setAvailableQuantity(Integer availableQuantity) {
        // Available can't exceed stock
        if (availableQuantity > this.stockQuantity) {
            this.availableQuantity = this.stockQuantity;
        } else {
            this.availableQuantity = availableQuantity;
        }
    }
    
    /**
     * Check if the item is in stock
     */
    @Transient
    public boolean isInStock() {
        return availableQuantity > 0;
    }
    
    /**
     * Reserve a quantity of this item for a potential transaction
     * @param quantity The quantity to reserve
     * @return true if successful, false if not enough available
     */
    public boolean reserve(int quantity) {
        if (availableQuantity >= quantity) {
            availableQuantity -= quantity;
            return true;
        }
        return false;
    }
    
    /**
     * Release a previously reserved quantity back to available
     * @param quantity The quantity to release
     */
    public void release(int quantity) {
        availableQuantity = Math.min(stockQuantity, availableQuantity + quantity);
    }
    
    /**
     * Complete a transaction by reducing stock quantity
     * @param quantity The quantity that was sold/traded
     * @return true if successful, false if not enough in stock
     */
    public boolean completeTransaction(int quantity) {
        if (stockQuantity >= quantity) {
            stockQuantity -= quantity;
            // Available quantity was already reduced during reservation
            return true;
        }
        return false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}