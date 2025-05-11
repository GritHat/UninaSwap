package com.uninaswap.server.service;

import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.server.entity.ItemEntity;
import com.uninaswap.server.entity.ListingEntity;
import com.uninaswap.server.entity.ListingItemEntity;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.repository.ItemRepository;
import com.uninaswap.server.repository.ListingItemRepository;
import com.uninaswap.server.repository.UserRepository;
import com.uninaswap.server.mapper.ItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ListingItemRepository listingItemRepository;
    private final ItemMapper itemMapper;
    
    @Autowired
    public ItemService(ItemRepository itemRepository, 
                       UserRepository userRepository,
                       ListingItemRepository listingItemRepository,
                       ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.listingItemRepository = listingItemRepository;
        this.itemMapper = itemMapper;
    }
    
    /**
     * Get all items for a user
     */
    public List<ItemDTO> getUserItems(Long userId) {
        logger.info("Getting items for user ID: {}", userId);
        List<ItemEntity> items = itemRepository.findByOwnerId(userId);
        return items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Add a new item
     */
    @Transactional
    public ItemDTO addItem(ItemDTO itemDTO) {
        logger.info("Adding new item for user ID: {}", itemDTO.getOwnerId());
        
        // Find the owner
        Optional<UserEntity> ownerOpt = userRepository.findById(itemDTO.getOwnerId());
        if (!ownerOpt.isPresent()) {
            throw new IllegalArgumentException("Owner with ID " + itemDTO.getOwnerId() + " not found");
        }
        
        // Create and save the item
        ItemEntity item = new ItemEntity();
        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        item.setImagePath(itemDTO.getImagePath());
        item.setCondition(itemDTO.getCondition());
        item.setCategory(itemDTO.getCategory());
        item.setBrand(itemDTO.getBrand());
        item.setModel(itemDTO.getModel());
        item.setYearOfProduction(itemDTO.getYearOfProduction());
        item.setOwner(ownerOpt.get());
        
        // Set stock and available quantities
        Integer stockQuantity = itemDTO.getStockQuantity() != null ? 
                               itemDTO.getStockQuantity() : 1;
        item.setStockQuantity(stockQuantity);
        item.setAvailableQuantity(stockQuantity); // Initially available = stock
        
        ItemEntity savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }
    
    /**
     * Update an existing item
     */
    @Transactional
    public ItemDTO updateItem(ItemDTO itemDTO) {
        logger.info("Updating item ID: {}", itemDTO.getId());
        
        // Find the item
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemDTO.getId());
        if (!itemOpt.isPresent()) {
            throw new IllegalArgumentException("Item with ID " + itemDTO.getId() + " not found");
        }
        
        ItemEntity item = itemOpt.get();
        
        // Update fields
        item.setName(itemDTO.getName());
        item.setDescription(itemDTO.getDescription());
        if (itemDTO.getImagePath() != null && !itemDTO.getImagePath().isEmpty()) {
            item.setImagePath(itemDTO.getImagePath());
        }
        item.setCondition(itemDTO.getCondition());
        item.setCategory(itemDTO.getCategory());
        item.setBrand(itemDTO.getBrand());
        item.setModel(itemDTO.getModel());
        item.setYearOfProduction(itemDTO.getYearOfProduction());
        
        // Update stock quantity (will automatically update available if needed)
        if (itemDTO.getStockQuantity() != null) {
            item.setStockQuantity(itemDTO.getStockQuantity());
        }
        
        // Update available quantity within constraints
        if (itemDTO.getAvailableQuantity() != null) {
            item.setAvailableQuantity(itemDTO.getAvailableQuantity());
        }
        
        item.setUpdatedAt(LocalDateTime.now());
        
        ItemEntity updatedItem = itemRepository.save(item);
        return itemMapper.toDto(updatedItem);
    }
    
    /**
     * Delete an item
     */
    @Transactional
    public void deleteItem(String itemId) {
        logger.info("Deleting item ID: {}", itemId);
        
        // First check if there are any listing items referencing this item
        if (isItemUsedInActiveListing(itemId)) {
            throw new IllegalStateException("Cannot delete an item that is part of an active listing");
        }
        
        // Delete the item
        itemRepository.deleteById(itemId);
    }
    
    /**
     * Check if an item is owned by a user
     */
    public boolean isItemOwnedByUser(String itemId, Long userId) {
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            return false;
        }
        
        return itemOpt.get().getOwner().getId().equals(userId);
    }
    
    /**
     * Check if an item is used in any active listing
     */
    public boolean isItemUsedInActiveListing(String itemId) {
        List<ListingItemEntity> listingItems = listingItemRepository.findByItemId(itemId);
        
        if (listingItems.isEmpty()) {
            return false;
        }
        
        // Check if any of the listings are active
        for (ListingItemEntity listingItem : listingItems) {
            ListingEntity listing = listingItem.getListing();
            if (listing != null && listing.getStatus() != null && 
                (listing.getStatus().equals("ACTIVE") || listing.getStatus().equals("PENDING"))) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get an item by ID
     */
    public ItemDTO getItemById(String itemId) {
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new IllegalArgumentException("Item with ID " + itemId + " not found");
        }
        
        return itemMapper.toDto(itemOpt.get());
    }
    
    /**
     * Reserve items for a listing
     */
    @Transactional
    public void reserveItems(List<String> itemIds, List<Integer> quantities) {
        if (itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Item IDs and quantities lists must have same size");
        }
        
        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            int quantity = quantities.get(i);
            
            Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
            if (!itemOpt.isPresent()) {
                throw new IllegalArgumentException("Item with ID " + itemId + " not found");
            }
            
            ItemEntity item = itemOpt.get();
            if (!item.reserve(quantity)) {
                throw new IllegalStateException("Not enough available quantity for item: " + item.getName());
            }
            
            itemRepository.save(item);
        }
    }
}