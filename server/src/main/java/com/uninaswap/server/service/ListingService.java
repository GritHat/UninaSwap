package com.uninaswap.server.service;

import com.uninaswap.common.dto.*;
import com.uninaswap.common.enums.Category;
import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.ListingStatus;
import com.uninaswap.server.entity.*;
import com.uninaswap.server.mapper.*;
import com.uninaswap.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Service
public class ListingService {
    private static final Logger logger = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final SellListingRepository sellListingRepository;

    private final TradeListingRepository tradeListingRepository;
    private final GiftListingRepository giftListingRepository;
    private final AuctionListingRepository auctionListingRepository;
    private final ItemRepository itemRepository;
    private final ListingItemRepository listingItemRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;

    // Mappers
    private final ListingMapper listingMapper;
    private final SellListingMapper sellListingMapper;
    private final TradeListingMapper tradeListingMapper;
    private final GiftListingMapper giftListingMapper;
    private final AuctionListingMapper auctionListingMapper;

    @Autowired
    public ListingService(
            ListingRepository listingRepository,
            SellListingRepository sellListingRepository,
            TradeListingRepository tradeListingRepository,
            GiftListingRepository giftListingRepository,
            AuctionListingRepository auctionListingRepository,
            ItemRepository itemRepository,
            ListingItemRepository listingItemRepository,
            UserRepository userRepository,
            ItemService itemService,
            ListingMapper listingMapper,
            SellListingMapper sellListingMapper,
            TradeListingMapper tradeListingMapper,
            GiftListingMapper giftListingMapper,
            AuctionListingMapper auctionListingMapper) {
        this.listingRepository = listingRepository;
        this.sellListingRepository = sellListingRepository;
        this.tradeListingRepository = tradeListingRepository;
        this.giftListingRepository = giftListingRepository;
        this.auctionListingRepository = auctionListingRepository;
        this.itemRepository = itemRepository;
        this.listingItemRepository = listingItemRepository;
        this.userRepository = userRepository;
        this.itemService = itemService;
        this.listingMapper = listingMapper;
        this.sellListingMapper = sellListingMapper;
        this.tradeListingMapper = tradeListingMapper;
        this.giftListingMapper = giftListingMapper;
        this.auctionListingMapper = auctionListingMapper;
    }

    /**
     * Get all active listings with pagination
     */
    @Transactional(readOnly = true)
    public Page<ListingDTO> getActiveListings(Pageable pageable) {
        logger.info("Getting active listings with pagination: {}", pageable);

        // Use JOIN FETCH to eagerly load the listingItems collection
        Page<ListingEntity> listingEntities = listingRepository.findByStatusWithItems(ListingStatus.ACTIVE, pageable);

        return listingEntities.map(listingMapper::toDto);
    }

    /**
     * Get all listings by user
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getUserListings(Long userId) {
        logger.info("Getting listings for user ID: {}", userId);

        List<ListingEntity> listings = listingRepository.findByCreatorIdWithItems(userId);
        return listings.stream()
                .map(listingMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get listing by ID
     */
    public ListingDTO getListingById(String listingId) {
        logger.info("Getting listing with ID: {}", listingId);

        Optional<ListingEntity> listingOpt = listingRepository.findById(listingId);
        if (!listingOpt.isPresent()) {
            throw new IllegalArgumentException("Listing with ID " + listingId + " not found");
        }

        return listingMapper.toDto(listingOpt.get());
    }

    /**
     * Create a new listing
     */
    @Transactional
    public ListingDTO createListing(ListingDTO listingDTO, String listingType) {
        logger.info("Creating new {} listing for user ID: {}",
                listingType, listingDTO.getCreator().getId());

        // Validate the creator
        Optional<UserEntity> creatorOpt = userRepository.findById(listingDTO.getCreator().getId());
        if (!creatorOpt.isPresent()) {
            throw new IllegalArgumentException("Creator with ID " + listingDTO.getCreator().getId() + " not found");
        }
        UserEntity creator = creatorOpt.get();

        // Prepare items to add to listing
        List<String> itemIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        // Create the appropriate listing type
        ListingEntity listing;

        switch (listingType.toUpperCase()) {
            case "SELL":
                listing = createSellListing((SellListingDTO) listingDTO, creator);
                break;

            case "TRADE":
                listing = createTradeListing((TradeListingDTO) listingDTO, creator);
                break;

            case "GIFT":
                listing = createGiftListing((GiftListingDTO) listingDTO, creator);
                break;

            case "AUCTION":
                listing = createAuctionListing((AuctionListingDTO) listingDTO, creator);
                break;

            default:
                throw new IllegalArgumentException("Unsupported listing type: " + listingType);
        }

        // Add common fields
        listing.setTitle(listingDTO.getTitle());
        listing.setDescription(listingDTO.getDescription());
        listing.setCreator(creator);
        listing.setStatus(ListingStatus.ACTIVE);
        listing.setCreatedAt(LocalDateTime.now());
        listing.setUpdatedAt(LocalDateTime.now());

        // Set image path from the first item's image if available
        if (listingDTO.getItems() != null && !listingDTO.getItems().isEmpty() &&
                listingDTO.getItems().get(0).getItemImagePath() != null) {
            listing.setImagePath(listingDTO.getItems().get(0).getItemImagePath());
        } else {
            // Use a default image path if no item image is available
            listing.setImagePath("default-listing-image.jpg");
        }

        // Save the listing first to get the ID
        ListingEntity savedListing = listingRepository.save(listing);

        // Now add items to the listing
        for (ListingItemDTO itemDTO : listingDTO.getItems()) {
            Optional<ItemEntity> itemOpt = itemRepository.findById(itemDTO.getItemId());
            if (!itemOpt.isPresent()) {
                throw new IllegalArgumentException("Item with ID " + itemDTO.getItemId() + " not found");
            }

            ItemEntity item = itemOpt.get();

            // Verify ownership
            if (!item.getOwner().getId().equals(creator.getId())) {
                throw new IllegalArgumentException("Item " + item.getName() + " is not owned by the creator");
            }

            // Add to lists for reservation
            itemIds.add(item.getId());
            quantities.add(itemDTO.getQuantity());

            // Create listing item
            ListingItemEntity listingItem = new ListingItemEntity();
            listingItem.setListing(savedListing);
            listingItem.setItem(item);
            listingItem.setQuantity(itemDTO.getQuantity());

            listingItemRepository.save(listingItem);
        }

        // Reserve the items
        itemService.reserveItems(itemIds, quantities);

        // Return the DTO using the appropriate mapper based on the listing type
        switch (listingType.toUpperCase()) {
            case "SELL":
                return sellListingMapper.toDto((SellListingEntity) savedListing);
            case "TRADE":
                return tradeListingMapper.toDto((TradeListingEntity) savedListing);
            case "GIFT":
                return giftListingMapper.toDto((GiftListingEntity) savedListing);
            case "AUCTION":
                return auctionListingMapper.toDto((AuctionListingEntity) savedListing);
            default:
                throw new IllegalArgumentException("Unsupported listing type: " + listingType);
        }
    }

    /**
     * Create a sell listing
     */
    private SellListingEntity createSellListing(SellListingDTO dto, UserEntity creator) {
        SellListingEntity sellListing = new SellListingEntity();
        sellListing.setPrice(dto.getPrice() != null ? dto.getPrice() : BigDecimal.ZERO);
        sellListing.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : Currency.EUR);
        return sellListing;
    }

    /**
     * Create a trade listing
     */
    private TradeListingEntity createTradeListing(TradeListingDTO dto, UserEntity creator) {
        TradeListingEntity tradeListing = new TradeListingEntity();

        // Set basic trade parameters
        tradeListing.setAcceptOtherOffers(dto.isAcceptOtherOffers());
        tradeListing.setAcceptMixedOffers(dto.isAcceptMixedOffers());
        tradeListing.setAcceptMoneyOffers(dto.isAcceptMoneyOffers());

        // Set categories
        if (dto.getDesiredCategories() != null) {
            tradeListing.setDesiredCategories(dto.getDesiredCategories());
        }

        // Set reference price if accepting money offers
        if (dto.isAcceptMoneyOffers()) {
            tradeListing.setReferencePrice(dto.getReferencePrice());
            tradeListing.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : Currency.EUR);
        }

        // Add desired items if any
        if (dto.getDesiredItems() != null && !dto.getDesiredItems().isEmpty()) {
            for (ItemDTO desiredItemDTO : dto.getDesiredItems()) {
                Optional<ItemEntity> itemOpt = itemRepository.findById(desiredItemDTO.getId());
                if (itemOpt.isPresent()) {
                    tradeListing.addDesiredItem(itemOpt.get());
                }
            }
        }

        return tradeListing;
    }

    /**
     * Create a gift listing
     */
    private GiftListingEntity createGiftListing(GiftListingDTO dto, UserEntity creator) {
        GiftListingEntity giftListing = new GiftListingEntity();
        giftListing.setPickupOnly(dto.isPickupOnly());
        giftListing.setAllowThankYouOffers(dto.isAllowThankYouOffers());
        giftListing.setRestrictions(dto.getRestrictions());
        return giftListing;
    }

    /**
     * Create an auction listing
     */
    private AuctionListingEntity createAuctionListing(AuctionListingDTO dto, UserEntity creator) {
        AuctionListingEntity auctionListing = new AuctionListingEntity();
        auctionListing.setStartingPrice(dto.getStartingPrice());
        auctionListing.setReservePrice(dto.getReservePrice());
        auctionListing.setMinimumBidIncrement(dto.getMinimumBidIncrement());
        auctionListing.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : Currency.EUR);

        // Set the start time to now
        LocalDateTime now = LocalDateTime.now();
        auctionListing.setStartTime(now);

        // Set duration based on the DTO's durationInDays field
        int days = dto.getDurationInDays();
        if (days <= 0) {
            // Default to 7 days if not specified
            days = 7;
        }
        auctionListing.setEndTime(now.plusDays(days));

        return auctionListing;
    }

    /**
     * Update an existing listing
     */
    @Transactional
    public ListingDTO updateListing(ListingDTO listingDTO, String listingType) {
        logger.info("Updating {} listing with ID: {}", listingType, listingDTO.getId());

        // Find the existing listing
        Optional<ListingEntity> listingOpt = listingRepository.findById(listingDTO.getId());
        if (!listingOpt.isPresent()) {
            throw new IllegalArgumentException("Listing with ID " + listingDTO.getId() + " not found");
        }

        ListingEntity listing = listingOpt.get();

        // Update common fields
        listing.setTitle(listingDTO.getTitle());
        listing.setDescription(listingDTO.getDescription());
        listing.setUpdatedAt(LocalDateTime.now());

        // Update type-specific fields
        switch (listingType.toUpperCase()) {
            case "SELL":
                updateSellListing((SellListingEntity) listing, (SellListingDTO) listingDTO);
                break;
            case "TRADE":
                updateTradeListing((TradeListingEntity) listing, (TradeListingDTO) listingDTO);
                break;
            case "GIFT":
                updateGiftListing((GiftListingEntity) listing, (GiftListingDTO) listingDTO);
                break;
            case "AUCTION":
                updateAuctionListing((AuctionListingEntity) listing, (AuctionListingDTO) listingDTO);
                break;
            default:
                throw new IllegalArgumentException("Unsupported listing type: " + listingType);
        }

        // Update the listing first
        ListingEntity savedListing = listingRepository.save(listing);

        // Handle items - this is more complex for updates
        // For simplicity, we'll assume no item changes in updates right now

        // Return the DTO using the appropriate mapper
        switch (listingType.toUpperCase()) {
            case "SELL":
                return sellListingMapper.toDto((SellListingEntity) savedListing);
            case "TRADE":
                return tradeListingMapper.toDto((TradeListingEntity) savedListing);
            case "GIFT":
                return giftListingMapper.toDto((GiftListingEntity) savedListing);
            case "AUCTION":
                return auctionListingMapper.toDto((AuctionListingEntity) savedListing);
            default:
                throw new IllegalArgumentException("Unsupported listing type: " + listingType);
        }
    }

    /**
     * Update a sell listing
     */
    private void updateSellListing(SellListingEntity listing, SellListingDTO dto) {
        if (dto.getPrice() != null) {
            listing.setPrice(dto.getPrice());
        }
        if (dto.getCurrency() != null) {
            listing.setCurrency(dto.getCurrency());
        }
    }

    /**
     * Update a trade listing
     */
    private void updateTradeListing(TradeListingEntity listing, TradeListingDTO dto) {
        listing.setAcceptOtherOffers(dto.isAcceptOtherOffers());
        listing.setAcceptMixedOffers(dto.isAcceptMixedOffers());
        listing.setAcceptMoneyOffers(dto.isAcceptMoneyOffers());

        if (dto.getDesiredCategories() != null) {
            listing.setDesiredCategories(dto.getDesiredCategories());
        }

        if (dto.isAcceptMoneyOffers()) {
            listing.setReferencePrice(dto.getReferencePrice());
            if (dto.getCurrency() != null) {
                listing.setCurrency(dto.getCurrency());
            }
        }

        // Handling desired items is more complex - omitted for brevity
    }

    /**
     * Update a gift listing
     */
    private void updateGiftListing(GiftListingEntity listing, GiftListingDTO dto) {
        listing.setPickupOnly(dto.isPickupOnly());
        listing.setAllowThankYouOffers(dto.isAllowThankYouOffers());
        if (dto.getRestrictions() != null) {
            listing.setRestrictions(dto.getRestrictions());
        }
    }

    /**
     * Update an auction listing
     */
    private void updateAuctionListing(AuctionListingEntity listing, AuctionListingDTO dto) {
        // Only allow certain fields to be updated if the auction hasn't started
        if (listing.getHasBids()) {
            throw new IllegalStateException("Cannot modify an auction that has bids");
        }

        if (dto.getStartingPrice() != null) {
            listing.setStartingPrice(dto.getStartingPrice());
        }
        if (dto.getReservePrice() != null) {
            listing.setReservePrice(dto.getReservePrice());
        }
        if (dto.getMinimumBidIncrement() != null) {
            listing.setMinimumBidIncrement(dto.getMinimumBidIncrement());
        }
        if (dto.getCurrency() != null) {
            listing.setCurrency(dto.getCurrency());
        }

        // Adjust end time if duration changed
        if (dto.getDurationInDays() > 0) {
            listing.setEndTime(listing.getStartTime().plusDays(dto.getDurationInDays()));
        }
    }

    /**
     * Delete a listing
     */
    @Transactional
    public void deleteListing(String listingId) {
        logger.info("Deleting listing with ID: {}", listingId);

        // Find the listing
        Optional<ListingEntity> listingOpt = listingRepository.findById(listingId);
        if (!listingOpt.isPresent()) {
            throw new IllegalArgumentException("Listing with ID " + listingId + " not found");
        }

        ListingEntity listing = listingOpt.get();

        // Release reserved items
        List<ListingItemEntity> listingItems = listingItemRepository.findByListingId(listingId);
        for (ListingItemEntity listingItem : listingItems) {
            ItemEntity item = listingItem.getItem();
            item.release(listingItem.getQuantity());
            itemRepository.save(item);
        }

        // Delete the listing
        listingRepository.delete(listing);
    }

    /**
     * Check if a listing is owned by a user
     */
    public boolean isListingOwnedByUser(String listingId, Long userId) {
        Optional<ListingEntity> listingOpt = listingRepository.findById(listingId);
        if (!listingOpt.isPresent()) {
            return false;
        }

        return listingOpt.get().getCreator().getId().equals(userId);
    }

    /**
     * Search listings by text query
     */
    @Transactional(readOnly = true)  // Add this annotation
    public Page<ListingDTO> searchListingsByText(String query, Pageable pageable) {
        try {
            Page<ListingEntity> entities = listingRepository.findByTitleContainingIgnoreCaseAndStatus(
                    query, ListingStatus.ACTIVE, pageable);
            return entities.map(listingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching listings by text: {}", e.getMessage());
            throw new RuntimeException("Failed to search listings by text", e);
        }
    }

    /**
     * Get listings by type
     */
    @Transactional(readOnly = true)  // Add this annotation
    public Page<ListingDTO> getListingsByType(String listingType, Pageable pageable) {
        try {
            Page<ListingEntity> entities;
            
            Class<?> entityClass = mapListingTypeToClass(listingType);
            entities = listingRepository.findByListingTypeAndStatus(entityClass, ListingStatus.ACTIVE, pageable);
            
            return entities.map(listingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error getting listings by type: {}", e.getMessage());
            throw new RuntimeException("Failed to get listings by type", e);
        }
    }

    /**
     * Search listings by text and type
     */
    @Transactional(readOnly = true)  // Add this annotation
    public Page<ListingDTO> searchListingsByTextAndType(String query, String listingType, Pageable pageable) {
        try {
            Page<ListingEntity> entities;
            
            Class<?> entityClass = mapListingTypeToClass(listingType);
            if (entityClass != null) {
                entities = listingRepository.findByTitleContainingIgnoreCaseAndListingTypeAndStatus(
                        query, entityClass, ListingStatus.ACTIVE, pageable);
            } else {
                entities = listingRepository.findByTitleContainingIgnoreCaseAndStatus(
                        query, ListingStatus.ACTIVE, pageable);
            }
            
            return entities.map(listingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching listings by text and type: {}", e.getMessage());
            throw new RuntimeException("Failed to search listings by text and type", e);
        }
    }

    /**
     * Get listings by category
     */
    @Transactional(readOnly = true)  // Add this annotation
    public Page<ListingDTO> getListingsByCategory(Category category, Pageable pageable) {
        try {
            Page<ListingEntity> entities = listingRepository.findByItemsCategoryAndStatus(
                    category.name(), ListingStatus.ACTIVE, pageable);
            return entities.map(listingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error getting listings by category: {}", e.getMessage());
            throw new RuntimeException("Failed to get listings by category", e);
        }
    }

    /**
     * Get listings by type and category
     */
    @Transactional(readOnly = true)  // Add this annotation
    public Page<ListingDTO> getListingsByTypeAndCategory(String listingType, Category category, Pageable pageable) {
        try {
            Class<?> entityClass = mapListingTypeToClass(listingType);
            Page<ListingEntity> entities;
            
            if (entityClass != null) {
                entities = listingRepository.findByListingTypeAndItemsCategoryAndStatus(
                        entityClass, category.name(), ListingStatus.ACTIVE, pageable);
            } else {
                entities = listingRepository.findByItemsCategoryAndStatus(
                        category.name(), ListingStatus.ACTIVE, pageable);
            }
            
            return entities.map(listingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error getting listings by type and category: {}", e.getMessage());
            throw new RuntimeException("Failed to get listings by type and category", e);
        }
    }

    @Transactional(readOnly = true)  // Add this annotation
    public Page<ListingDTO> searchListingsByTextAndCategory(String query, Category category, Pageable pageable) {
        try {
            Page<ListingEntity> entities = listingRepository.findByTitleContainingIgnoreCaseAndItemsCategoryAndStatus(
                    query, category.name(), ListingStatus.ACTIVE, pageable);
            return entities.map(listingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching listings by text and category: {}", e.getMessage());
            throw new RuntimeException("Failed to search listings by text and category", e);
        }
    }

    /**
     * Search listings by text, type, and category
     */
    @Transactional(readOnly = true)  // Add this annotation
    public Page<ListingDTO> searchListingsByTextTypeAndCategory(String query, String listingType, Category category, Pageable pageable) {
        try {
            Class<?> entityClass = mapListingTypeToClass(listingType);
            Page<ListingEntity> entities;
            
            if (entityClass != null) {
                entities = listingRepository.findByTitleContainingIgnoreCaseAndListingTypeAndItemsCategoryAndStatus(
                        query, entityClass, category.name(), ListingStatus.ACTIVE, pageable);
            } else {
                entities = listingRepository.findByTitleContainingIgnoreCaseAndItemsCategoryAndStatus(
                        query, category.name(), ListingStatus.ACTIVE, pageable);
            }
            
            return entities.map(listingMapper::toDto);
        } catch (Exception e) {
            logger.error("Error searching listings by text, type and category: {}", e.getMessage());
            throw new RuntimeException("Failed to search listings by text, type and category", e);
        }
    }

    /**
     * Helper method to map listing type strings to entity classes
     */
    private Class<?> mapListingTypeToClass(String listingType) {
        if (listingType == null) return null;
        
        switch (listingType.toLowerCase()) {
            case "auctions":
            case "auction":
                return com.uninaswap.server.entity.AuctionListingEntity.class;
            case "sales":
            case "sell":
                return com.uninaswap.server.entity.SellListingEntity.class;
            case "trades":
            case "trade":
                return com.uninaswap.server.entity.TradeListingEntity.class;
            case "gifts":
            case "gift":
                return com.uninaswap.server.entity.GiftListingEntity.class;
            default:
                return null; // Will use fallback methods
        }
    }

    /**
     * Helper method to map listing type strings to proper enum values
     */
    private String mapListingType(String listingType) {
        if (listingType == null) return null;
        
        switch (listingType.toLowerCase()) {
            case "auctions":
            case "auction":
                return "AUCTION";
            case "sales":
            case "sell":
                return "SELL";
            case "trades":
            case "trade":
                return "TRADE";
            case "gifts":
            case "gift":
                return "GIFT";
            default:
                return listingType.toUpperCase();
        }
    }
}