package com.uninaswap.server.service;

import com.uninaswap.common.dto.OfferDTO;
import com.uninaswap.common.dto.OfferItemDTO;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;
import com.uninaswap.common.enums.NotificationType;
import com.uninaswap.common.enums.OfferStatus;
import com.uninaswap.common.enums.PickupStatus;
import com.uninaswap.server.entity.*;
import com.uninaswap.server.mapper.OfferMapper;
import com.uninaswap.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OfferService {

    private static final Logger logger = LoggerFactory.getLogger(OfferService.class);

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OfferItemRepository offerItemRepository;

    @Autowired
    private PickupRepository pickupRepository;

    @Autowired
    private OfferMapper offerMapper;

    @Autowired
    private ItemService itemService;

    @Autowired 
    private NotificationService notificationService;

    /**
     * Create a new offer
     */
    @Transactional
    public OfferDTO createOffer(OfferDTO offerDTO, Long userId) {
        logger.info("Creating new offer for listing {} by user {}", offerDTO.getListingId(), userId);

        // Validate the user
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }
        UserEntity user = userOpt.get();

        // Validate the listing
        Optional<ListingEntity> listingOpt = listingRepository.findById(offerDTO.getListingId());
        if (!listingOpt.isPresent()) {
            throw new IllegalArgumentException("Listing with ID " + offerDTO.getListingId() + " not found");
        }
        ListingEntity listing = listingOpt.get();

        // Check if user is trying to make an offer on their own listing
        if (listing.getCreator().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot make an offer on your own listing");
        }

        // Create the offer entity
        OfferEntity offer = new OfferEntity();
        offer.setListing(listing);
        offer.setUser(user);
        offer.setAmount(offerDTO.getAmount());
        offer.setCurrency(offerDTO.getCurrency());
        offer.setMessage(offerDTO.getMessage());
        offer.setDeliveryType(offerDTO.getDeliveryType());
        offer.setStatus(OfferStatus.PENDING);

        // Save the offer first
        OfferEntity savedOffer = offerRepository.save(offer);

        // Handle offer items if present
        if (offerDTO.getOfferItems() != null && !offerDTO.getOfferItems().isEmpty()) {
            List<String> itemIds = new ArrayList<>();
            List<Integer> quantities = new ArrayList<>();

            for (OfferItemDTO offerItemDTO : offerDTO.getOfferItems()) {
                // Validate item exists and belongs to the user
                Optional<ItemEntity> itemOpt = itemRepository.findById(offerItemDTO.getItemId());
                if (!itemOpt.isPresent()) {
                    throw new IllegalArgumentException("Item with ID " + offerItemDTO.getItemId() + " not found");
                }

                ItemEntity item = itemOpt.get();

                // Verify ownership
                if (!item.getOwner().getId().equals(userId)) {
                    throw new IllegalArgumentException("Item " + item.getName() + " is not owned by the user");
                }

                // Check availability
                if (item.getAvailableQuantity() < offerItemDTO.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient quantity for item " + item.getName());
                }

                // Create offer item
                OfferItemEntity offerItem = new OfferItemEntity();
                offerItem.setOffer(savedOffer);
                offerItem.setItem(item);
                offerItem.setQuantity(offerItemDTO.getQuantity());

                offerItemRepository.save(offerItem);

                // Add to lists for reservation
                itemIds.add(item.getId());
                quantities.add(offerItemDTO.getQuantity());
            }

            // Reserve the items
            itemService.reserveItems(itemIds, quantities);
        }
        notificationService.createNotification(
                listing.getCreator().getId(),
                NotificationType.OFFER_RECEIVED,
                "New Offer Received",
                "You have received a new offer on your listing: " + listing.getTitle(),
                savedOffer.getId()
        );
        logger.info("Successfully created offer with ID: {}", savedOffer.getId());
        return offerMapper.toDto(savedOffer);
    }

    /**
     * Get all offers made by a user
     */
    @Transactional(readOnly = true)
    public List<OfferDTO> getUserOffers(Long userId) {
        logger.info("Retrieving offers for user: {}", userId);

        List<OfferEntity> offers = offerRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return offers.stream()
                .map(offerMapper::toDto)
                .toList();
    }

    /**
     * Get all offers received by a user (offers on their listings)
     */
    @Transactional(readOnly = true)
    public List<OfferDTO> getReceivedOffers(Long userId) {
        logger.info("Retrieving received offers for user: {}", userId);

        List<OfferEntity> offers = offerRepository.findByListingCreatorIdOrderByCreatedAtDesc(userId);
        return offers.stream()
                .map(offerMapper::toDto)
                .toList();
    }

    /**
     * Get all offers for a specific listing
     */
    @Transactional(readOnly = true)
    public List<OfferDTO> getOffersForListing(String listingId) {
        logger.info("Retrieving offers for listing: {}", listingId);

        List<OfferEntity> offers = offerRepository.findByListingIdOrderByCreatedAtDesc(listingId);
        return offers.stream()
                .map(offerMapper::toDto)
                .toList();
    }

    /**
     * Update offer status
     */
    @Transactional
    public OfferDTO updateOfferStatus(String offerId, OfferStatus newStatus, Long userId) {
        logger.info("Updating offer {} status to {} by user {}", offerId, newStatus, userId);

        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        if (!offerOpt.isPresent()) {
            throw new IllegalArgumentException("Offer with ID " + offerId + " not found");
        }

        OfferEntity offer = offerOpt.get();

        // Verify user has permission to update this offer
        boolean isOfferOwner = offer.getUser().getId().equals(userId);
        boolean isListingOwner = offer.getListing().getCreator().getId().equals(userId);

        if (!isOfferOwner && !isListingOwner) {
            throw new IllegalArgumentException("User does not have permission to update this offer");
        }

        // Validate status transition
        if (!isValidStatusTransition(offer.getStatus(), newStatus, isListingOwner)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + offer.getStatus() + " to " + newStatus);
        }

        OfferStatus oldStatus = offer.getStatus();
        offer.setStatus(newStatus);
        offer.setUpdatedAt(LocalDateTime.now());

        // Handle item reservations based on status change
        handleItemReservationsOnStatusChange(offer, oldStatus, newStatus);

        OfferEntity savedOffer = offerRepository.save(offer);
        logger.info("Successfully updated offer status to: {}", newStatus);

        return offerMapper.toDto(savedOffer);
    }

    /**
     * Get offer by ID
     */
    public OfferDTO getOfferById(String offerId) {
        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        if (!offerOpt.isPresent()) {
            throw new IllegalArgumentException("Offer with ID " + offerId + " not found");
        }

        return offerMapper.toDto(offerOpt.get());
    }

    /**
     * Check if user owns the offer
     */
    public boolean isOfferOwnedByUser(String offerId, Long userId) {
        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        return offerOpt.isPresent() && offerOpt.get().getUser().getId().equals(userId);
    }

    /**
     * Check if user owns the listing that received the offer
     */
    public boolean isOfferForUserListing(String offerId, Long userId) {
        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        return offerOpt.isPresent() && offerOpt.get().getListing().getCreator().getId().equals(userId);
    }

    /**
     * Confirm transaction (buyer or seller verification)
     */
    @Transactional
    public OfferDTO confirmTransaction(String offerId, Long userId) {
        logger.info("Confirming transaction for offer {} by user {}", offerId, userId);

        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        if (!offerOpt.isPresent()) {
            throw new IllegalArgumentException("Offer with ID " + offerId + " not found");
        }

        OfferEntity offer = offerOpt.get();

        // Verify user is involved in the offer
        boolean isOfferOwner = offer.getUser().getId().equals(userId);
        boolean isListingOwner = offer.getListing().getCreator().getId().equals(userId);

        if (!isOfferOwner && !isListingOwner) {
            throw new IllegalArgumentException("User is not involved in this offer");
        }

        OfferStatus currentStatus = offer.getStatus();
        OfferStatus newStatus;

        // Determine new status based on current status and user role
        if (currentStatus == OfferStatus.CONFIRMED) {
            if (offer.getDeliveryType() == DeliveryType.PICKUP) {
                // For pickup: both parties need to verify
                if (isListingOwner) {
                    newStatus = OfferStatus.SELLERVERIFIED;
                } else {
                    newStatus = OfferStatus.BUYERVERIFIED;
                }
            } else {
                // For shipping: only buyer verifies and it goes to COMPLETED
                if (isOfferOwner) {
                    newStatus = OfferStatus.COMPLETED;
                } else {
                    throw new IllegalStateException("Only the buyer can confirm shipping transactions");
                }
            }
        } else if (currentStatus == OfferStatus.SELLERVERIFIED) {
            // Seller already verified, buyer confirming should complete
            if (isOfferOwner) {
                newStatus = OfferStatus.COMPLETED;
            } else {
                throw new IllegalStateException("Seller already verified, waiting for buyer");
            }
        } else if (currentStatus == OfferStatus.BUYERVERIFIED) {
            // Buyer already verified, seller confirming should complete
            if (isListingOwner) {
                newStatus = OfferStatus.COMPLETED;
            } else {
                throw new IllegalStateException("Buyer already verified, waiting for seller");
            }
        } else {
            throw new IllegalStateException("Cannot confirm transaction for offer in status: " + currentStatus);
        }

        // Update offer status
        offer.setStatus(newStatus);
        offer.setUpdatedAt(LocalDateTime.now());
        OfferEntity savedOffer = offerRepository.save(offer);

        // If completed, update listing status
        if (newStatus == OfferStatus.COMPLETED) {
            ListingEntity listing = offer.getListing();
            listing.setStatus(ListingStatus.COMPLETED);
            listing.setUpdatedAt(LocalDateTime.now());
            listingRepository.save(listing);
            
            logger.info("Transaction completed - offer {} and listing {} marked as completed", offerId, listing.getId());
        }

        logger.info("Successfully confirmed transaction for offer {} - status updated to {}", offerId, newStatus);
        return offerMapper.toDto(savedOffer);
    }

    /**
     * Cancel transaction
     */
    @Transactional
    public OfferDTO cancelTransaction(String offerId, Long userId) {
        logger.info("Cancelling transaction for offer {} by user {}", offerId, userId);

        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        if (!offerOpt.isPresent()) {
            throw new IllegalArgumentException("Offer with ID " + offerId + " not found");
        }

        OfferEntity offer = offerOpt.get();

        // Verify user is involved in the offer
        boolean isOfferOwner = offer.getUser().getId().equals(userId);
        boolean isListingOwner = offer.getListing().getCreator().getId().equals(userId);

        if (!isOfferOwner && !isListingOwner) {
            throw new IllegalArgumentException("User is not involved in this offer");
        }

        OfferStatus currentStatus = offer.getStatus();

        // Can only cancel from verification states
        if (currentStatus != OfferStatus.CONFIRMED && 
            currentStatus != OfferStatus.SELLERVERIFIED && 
            currentStatus != OfferStatus.BUYERVERIFIED) {
            throw new IllegalStateException("Cannot cancel transaction for offer in status: " + currentStatus);
        }

        // Update offer status to CANCELLED
        offer.setStatus(OfferStatus.CANCELLED);
        offer.setUpdatedAt(LocalDateTime.now());
        OfferEntity savedOffer = offerRepository.save(offer);

        offer.getListing().setStatus(ListingStatus.PENDING);

        // If there's an associated pickup, cancel it too
        Optional<PickupEntity> pickup = pickupRepository.findByOfferId(offerId);
        if (pickup.isPresent()) {
            PickupEntity pickupEntity = pickup.get();
            pickupEntity.setStatus(PickupStatus.DECLINED);
            pickupEntity.setUpdatedAt(LocalDateTime.now());
            pickupRepository.save(pickupEntity);
            logger.info("Associated pickup {} also cancelled", pickupEntity.getId());
        }

        logger.info("Successfully cancelled transaction for offer {} - status updated to CANCELLED", offerId);
        return offerMapper.toDto(savedOffer);
    }

    /**
     * Validate status transitions with new pickup scheduling logic
     */
    private boolean isValidStatusTransition(OfferStatus currentStatus, OfferStatus newStatus, boolean isListingOwner) {
        switch (currentStatus) {
            case PENDING:
                if (isListingOwner) {
                    // Listing owner can accept or reject
                    return newStatus == OfferStatus.ACCEPTED || 
                           newStatus == OfferStatus.REJECTED ||
                           newStatus == OfferStatus.CONFIRMED; // Direct to CONFIRMED for non-pickup
                } else {
                    // Offer creator can only withdraw
                    return newStatus == OfferStatus.WITHDRAWN;
                }
                
            case ACCEPTED:
                // From ACCEPTED, can go to PICKUPSCHEDULING, CONFIRMED, or CANCELLED
                return newStatus == OfferStatus.PICKUPSCHEDULING ||
                       newStatus == OfferStatus.CONFIRMED ||
                       newStatus == OfferStatus.CANCELLED;
                       
            case PICKUPSCHEDULING:
                // From PICKUPSCHEDULING, can go to CONFIRMED, CANCELLED, or PICKUPRESCHEDULING
                return newStatus == OfferStatus.CONFIRMED ||
                       newStatus == OfferStatus.CANCELLED ||
                       newStatus == OfferStatus.PICKUPRESCHEDULING;
                       
            case PICKUPRESCHEDULING:
                // From PICKUPRESCHEDULING, can go to CONFIRMED or CANCELLED
                return newStatus == OfferStatus.CONFIRMED ||
                       newStatus == OfferStatus.CANCELLED;
                       
            case CONFIRMED:
                // From CONFIRMED, can go to SELLERVERIFIED, BUYERVERIFIED, COMPLETED, or CANCELLED
                return newStatus == OfferStatus.SELLERVERIFIED ||
                       newStatus == OfferStatus.BUYERVERIFIED ||
                       newStatus == OfferStatus.COMPLETED ||
                       newStatus == OfferStatus.CANCELLED;
                       
            case SELLERVERIFIED:
                // From SELLERVERIFIED, can go to COMPLETED or CANCELLED
                return newStatus == OfferStatus.COMPLETED ||
                       newStatus == OfferStatus.CANCELLED;
                       
            case BUYERVERIFIED:
                // From BUYERVERIFIED, can go to COMPLETED or CANCELLED
                return newStatus == OfferStatus.COMPLETED ||
                       newStatus == OfferStatus.CANCELLED;
                       
            case COMPLETED:
                // From COMPLETED, can only go to REVIEWED (when review is submitted)
                return newStatus == OfferStatus.REVIEWED;
                       
            case REVIEWED:
            case REJECTED:
            case WITHDRAWN:
            case EXPIRED:
            case CANCELLED:
                return false; // Terminal states
                
            default:
                return false;
        }
    }

    /**
     * Accept an offer with new status logic based on delivery type
     */
    @Transactional
    public OfferDTO acceptOffer(String offerId, Long userId) {
        logger.info("Accepting offer {} by user {}", offerId, userId);

        Optional<OfferEntity> offerOpt = offerRepository.findById(offerId);
        if (!offerOpt.isPresent()) {
            throw new IllegalArgumentException("Offer with ID " + offerId + " not found");
        }

        OfferEntity offer = offerOpt.get();

        // Verify user is the listing owner
        if (!offer.getListing().getCreator().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the listing owner can accept offers");
        }

        // Validate current status
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalStateException("Can only accept pending offers");
        }

        // Determine next status based on delivery type
        OfferStatus nextStatus;
        if (offer.getDeliveryType() == DeliveryType.PICKUP) {
            // For pickup delivery, go to ACCEPTED (waiting for pickup scheduling)
            nextStatus = OfferStatus.ACCEPTED;
            logger.info("Offer {} accepted with PICKUP delivery, status set to ACCEPTED", offerId);
        } else {
            // For shipping delivery, go directly to CONFIRMED
            nextStatus = OfferStatus.CONFIRMED;
            logger.info("Offer {} accepted with SHIPPING delivery, status set to CONFIRMED", offerId);
        }

        OfferStatus oldStatus = offer.getStatus();
        offer.setStatus(nextStatus);
        offer.setUpdatedAt(LocalDateTime.now());

        // Handle item reservations
        handleItemReservationsOnStatusChange(offer, oldStatus, nextStatus);

        OfferEntity savedOffer = offerRepository.save(offer);
        logger.info("Successfully accepted offer {} with status {}", offerId, nextStatus);

        return offerMapper.toDto(savedOffer);
    }

    /**
     * Handle item reservations when offer status changes
     */
    private void handleItemReservationsOnStatusChange(OfferEntity offer, OfferStatus oldStatus, OfferStatus newStatus) {
        if (offer.getOfferItems().isEmpty()) {
            return;
        }

        List<String> itemIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (OfferItemEntity offerItem : offer.getOfferItems()) {
            itemIds.add(offerItem.getItem().getId());
            quantities.add(offerItem.getQuantity());
        }

        // Release reservations when offer is rejected, withdrawn, or expired
        if (newStatus == OfferStatus.REJECTED || newStatus == OfferStatus.WITHDRAWN
                || newStatus == OfferStatus.EXPIRED) {
            itemService.releaseReservations(itemIds, quantities);
        }

        // Transfer items when offer is completed
        if (newStatus == OfferStatus.COMPLETED) {
            // TODO: Implement item transfer logic
            // This would involve transferring ownership of items from offer creator to
            // listing creator
            logger.info("Item transfer needed for completed offer: {}", offer.getId());
        }
    }
}