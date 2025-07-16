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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        Integer stockQuantity = itemDTO.getStockQuantity() != null ? itemDTO.getStockQuantity() : 1;
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
     * Reserve items for an offer
     * 
     * @param itemIds    List of item IDs to reserve
     * @param quantities List of quantities to reserve for each item
     */
    @Transactional
    public void reserveItems(List<String> itemIds, List<Integer> quantities) {
        if (itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Item IDs and quantities lists must have the same size");
        }

        logger.info("Reserving {} items", itemIds.size());

        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            int quantity = quantities.get(i);

            reserveItem(itemId, quantity);
        }

        logger.info("Successfully reserved items: {}", itemIds);
    }

    /**
     * Reserve a single item
     * 
     * @param itemId   The item ID to reserve
     * @param quantity The quantity to reserve
     */
    @Transactional
    public void reserveItem(String itemId, int quantity) {
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new IllegalArgumentException("Item with ID " + itemId + " not found");
        }

        ItemEntity item = itemOpt.get();

        if (!item.reserve(quantity)) {
            throw new IllegalArgumentException(
                    String.format("Insufficient quantity for item %s. Available: %d, Requested: %d",
                            item.getName(), item.getAvailableQuantity(), quantity));
        }

        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);

        logger.debug("Reserved {} units of item {} ({})", quantity, item.getName(), itemId);
    }

    /**
     * Release reservations for items
     * 
     * @param itemIds    List of item IDs to release reservations for
     * @param quantities List of quantities to release for each item
     */
    @Transactional
    public void releaseReservations(List<String> itemIds, List<Integer> quantities) {
        if (itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Item IDs and quantities lists must have the same size");
        }

        logger.info("Releasing reservations for {} items", itemIds.size());

        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            int quantity = quantities.get(i);

            releaseReservation(itemId, quantity);
        }

        logger.info("Successfully released reservations for items: {}", itemIds);
    }

    /**
     * Release reservation for a single item
     * 
     * @param itemId   The item ID to release reservation for
     * @param quantity The quantity to release
     */
    @Transactional
    public void releaseReservation(String itemId, int quantity) {
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            logger.warn("Attempting to release reservation for non-existent item: {}", itemId);
            return; // Don't throw exception for missing items during cleanup
        }

        ItemEntity item = itemOpt.get();

        // Ensure we don't exceed the total stock quantity
        int newAvailableQuantity = Math.min(
                item.getAvailableQuantity() + quantity,
                item.getStockQuantity());

        item.setAvailableQuantity(newAvailableQuantity);
        item.setUpdatedAt(LocalDateTime.now());

        itemRepository.save(item);

        logger.debug("Released {} units of item {} ({}). New available: {}",
                quantity, item.getName(), itemId, newAvailableQuantity);
    }

    /**
     * Transfer ownership of items from one user to another
     * 
     * @param itemIds    List of item IDs to transfer
     * @param quantities List of quantities to transfer for each item
     * @param fromUserId The current owner's ID
     * @param toUserId   The new owner's ID
     */
    @Transactional
    public void transferItems(List<String> itemIds, List<Integer> quantities,
            Long fromUserId, Long toUserId) {
        if (itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Item IDs and quantities lists must have the same size");
        }

        logger.info("Transferring {} items from user {} to user {}",
                itemIds.size(), fromUserId, toUserId);

        // Validate users exist
        Optional<UserEntity> fromUserOpt = userRepository.findById(fromUserId);
        Optional<UserEntity> toUserOpt = userRepository.findById(toUserId);

        if (!fromUserOpt.isPresent()) {
            throw new IllegalArgumentException("Source user not found: " + fromUserId);
        }
        if (!toUserOpt.isPresent()) {
            throw new IllegalArgumentException("Target user not found: " + toUserId);
        }

        UserEntity toUser = toUserOpt.get();

        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            int quantity = quantities.get(i);

            transferItem(itemId, quantity, fromUserId, toUser);
        }

        logger.info("Successfully transferred items from user {} to user {}", fromUserId, toUserId);
    }

    /**
     * Transfer a single item (or part of it) to another user
     * 
     * @param itemId     The item ID to transfer
     * @param quantity   The quantity to transfer
     * @param fromUserId The current owner's ID
     * @param toUser     The new owner entity
     */
    @Transactional
    private void transferItem(String itemId, int quantity, Long fromUserId, UserEntity toUser) {
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new IllegalArgumentException("Item not found: " + itemId);
        }

        ItemEntity originalItem = itemOpt.get();

        // Verify ownership
        if (!originalItem.getOwner().getId().equals(fromUserId)) {
            throw new IllegalArgumentException(
                    String.format("Item %s is not owned by user %d", itemId, fromUserId));
        }

        // Check if we're transferring the entire stock
        if (quantity >= originalItem.getStockQuantity()) {
            // Transfer entire item
            originalItem.setOwner(toUser);
            originalItem.setUpdatedAt(LocalDateTime.now());
            itemRepository.save(originalItem);

            logger.debug("Transferred entire item {} to user {}", originalItem.getName(), toUser.getId());
        } else {
            // Partial transfer - need to split the item
            splitAndTransferItem(originalItem, quantity, toUser);
        }
    }

    /**
     * Split an item and transfer part to another user
     * 
     * @param originalItem       The original item to split
     * @param quantityToTransfer How much to transfer
     * @param toUser             The recipient user
     */
    @Transactional
    private void splitAndTransferItem(ItemEntity originalItem, int quantityToTransfer, UserEntity toUser) {
        // Reduce the original item's quantity
        originalItem.setStockQuantity(originalItem.getStockQuantity() - quantityToTransfer);
        originalItem.setAvailableQuantity(
                Math.max(0, originalItem.getAvailableQuantity() - quantityToTransfer));
        originalItem.setUpdatedAt(LocalDateTime.now());

        // Create new item for the recipient
        ItemEntity newItem = new ItemEntity();
        newItem.setName(originalItem.getName());
        newItem.setDescription(originalItem.getDescription());
        newItem.setCategory(originalItem.getCategory());
        newItem.setCondition(originalItem.getCondition());
        newItem.setYearOfProduction(originalItem.getYearOfProduction());
        newItem.setStockQuantity(quantityToTransfer);
        newItem.setAvailableQuantity(quantityToTransfer); // All transferred items are available
        newItem.setImagePath(originalItem.getImagePath());
        newItem.setOwner(toUser);
        newItem.setAvailable(true);
        newItem.setVisible(true);

        // Save both items
        itemRepository.save(originalItem);
        itemRepository.save(newItem);

        logger.debug("Split item {}: {} units remain with original owner, {} units transferred to user {}",
                originalItem.getName(), originalItem.getStockQuantity(), quantityToTransfer, toUser.getId());
    }

    /**
     * Get total reserved quantity for an item across all pending offers
     * 
     * @param itemId The item ID to check
     * @return Total reserved quantity
     */
    public int getTotalReservedQuantity(String itemId) {
        // This would typically query the OfferItemRepository to get all pending offer
        // items
        // For now, we'll calculate it based on the difference between stock and
        // available
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            return 0;
        }

        ItemEntity item = itemOpt.get();
        return Math.max(0, item.getStockQuantity() - item.getAvailableQuantity());
    }

    /**
     * Check if an item has sufficient available quantity
     * 
     * @param itemId            The item ID to check
     * @param requestedQuantity The quantity requested
     * @return true if sufficient quantity is available
     */
    public boolean hasAvailableQuantity(String itemId, int requestedQuantity) {
        Optional<ItemEntity> itemOpt = itemRepository.findById(itemId);
        if (!itemOpt.isPresent()) {
            return false;
        }

        return itemOpt.get().getAvailableQuantity() >= requestedQuantity;
    }

    /**
     * Batch check availability for multiple items
     * 
     * @param itemIds    List of item IDs to check
     * @param quantities List of quantities requested for each item
     * @return Map of item ID to availability status
     */
    public Map<String, Boolean> checkAvailability(List<String> itemIds, List<Integer> quantities) {
        if (itemIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Item IDs and quantities lists must have the same size");
        }

        Map<String, Boolean> availability = new HashMap<>();

        for (int i = 0; i < itemIds.size(); i++) {
            String itemId = itemIds.get(i);
            int quantity = quantities.get(i);
            availability.put(itemId, hasAvailableQuantity(itemId, quantity));
        }

        return availability;
    }
}