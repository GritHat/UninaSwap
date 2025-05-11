package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.uninaswap.common.enums.ListingStatus;

/**
 * Abstract base class for all types of listings in the UninaSwap platform
 */
@Entity
@Table(name = "listings")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "listing_type")
public abstract class ListingEntity {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;
    
    @Column(nullable = false)
    private String imagePath;
    
    @Column(nullable = false)
    private boolean featured = false;
    
    // Replace the direct many-to-many with a join entity
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingItemEntity> listingItems = new ArrayList<>();
    
    // Type-specific fields that subclasses will use
    @Transient
    public abstract String getListingType();
    
    @Transient
    public abstract String getPriceInfo();
    
    // Constructor
    public ListingEntity() {
        // Generate a unique ID for the listing
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ListingStatus.ACTIVE;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public UserEntity getCreator() {
        return creator;
    }
    
    public void setCreator(UserEntity creator) {
        this.creator = creator;
    }
    
    public ListingStatus getStatus() {
        return status;
    }
    
    public void setStatus(ListingStatus status) {
        this.status = status;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
    public List<ListingItemEntity> getListingItems() {
        return listingItems;
    }
    
    public void setListingItems(List<ListingItemEntity> listingItems) {
        this.listingItems = listingItems;
    }
    
    // Add helper methods
    public void addItem(ItemEntity item, int quantity) {
        ListingItemEntity listingItem = new ListingItemEntity(this, item, quantity);
        listingItems.add(listingItem);
    }

    public void removeItem(ItemEntity item) {
        listingItems.removeIf(li -> li.getItem().equals(item));
    }
    
    /**
     * Get all items in this listing
     * @return List of items (without quantity information)
     */
    @Transient
    public List<ItemEntity> getItems() {
        return listingItems.stream()
            .map(ListingItemEntity::getItem)
            .collect(Collectors.toList());
    }

    /**
     * Get the quantity of a specific item in this listing
     * @param item The item to check
     * @return The quantity, or 0 if not found
     */
    @Transient
    public int getItemQuantity(ItemEntity item) {
        return listingItems.stream()
            .filter(li -> li.getItem().equals(item))
            .mapToInt(ListingItemEntity::getQuantity)
            .findFirst()
            .orElse(0);
    }

    /**
     * Get the total number of items in this listing (sum of quantities)
     * @return Total item count
     */
    @Transient
    public int getTotalItemCount() {
        return listingItems.stream()
            .mapToInt(ListingItemEntity::getQuantity)
            .sum();
    }
    
    /**
     * Updates the modified timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}