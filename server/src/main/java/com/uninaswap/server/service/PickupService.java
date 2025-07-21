package com.uninaswap.server.service;

import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.common.enums.DeliveryType;
import com.uninaswap.common.enums.ListingStatus;
import com.uninaswap.common.enums.OfferStatus;
import com.uninaswap.common.enums.PickupStatus;
import com.uninaswap.server.entity.ListingEntity;
import com.uninaswap.server.entity.OfferEntity;
import com.uninaswap.server.entity.PickupEntity;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.mapper.PickupMapper;
import com.uninaswap.server.repository.ListingRepository;
import com.uninaswap.server.repository.OfferRepository;
import com.uninaswap.server.repository.PickupRepository;
import com.uninaswap.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PickupService {

    private static final Logger logger = LoggerFactory.getLogger(PickupService.class);

    @Autowired
    private PickupRepository pickupRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private PickupMapper pickupMapper;

    /**
     * Create a pickup arrangement and update offer status to PICKUPSCHEDULING
     */
    @Transactional
    public PickupDTO createPickup(PickupDTO pickupDTO, Long createdByUserId) {
        logger.info("Creating pickup for offer {} by user {}", pickupDTO.getOfferId(), createdByUserId);

        // Validate offer exists and is in ACCEPTED status
        OfferEntity offer = offerRepository.findById(pickupDTO.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + pickupDTO.getOfferId()));

        if (offer.getStatus() != OfferStatus.ACCEPTED && 
            offer.getStatus() != OfferStatus.PICKUPSCHEDULING && 
            offer.getStatus() != OfferStatus.PICKUPRESCHEDULING) {
            throw new IllegalStateException("Can only create pickup for accepted offers or during pickup scheduling/rescheduling");
        }

        // Validate delivery type is PICKUP
        if (offer.getDeliveryType() != DeliveryType.PICKUP) {
            throw new IllegalStateException("Can only create pickup scheduling for pickup delivery offers");
        }

        // Check if pickup already exists for this offer
        Optional<PickupEntity> existingPickupOpt = pickupRepository.findByOfferId(pickupDTO.getOfferId());
        
        if (existingPickupOpt.isPresent()) {
            PickupEntity existingPickup = existingPickupOpt.get();
            
            // If there's an existing pickup, we need to handle it based on its status
            switch (existingPickup.getStatus()) {
                case DECLINED:
                    // If it was declined, delete the old one and create new
                    logger.info("Deleting declined pickup {} and creating new one for offer {}", existingPickup.getId(), pickupDTO.getOfferId());
                    pickupRepository.delete(existingPickup);
                    pickupRepository.flush();
                    offer.setStatus(OfferStatus.PICKUPSCHEDULING);
                    break;
                    
                case CANCELLED:
                    // If it was cancelled, delete the old one and create new
                    logger.info("Deleting cancelled pickup {} and creating new one for offer {}", existingPickup.getId(), pickupDTO.getOfferId());
                    pickupRepository.delete(existingPickup);
                    pickupRepository.flush();
                    offer.setStatus(OfferStatus.PICKUPRESCHEDULING);
                    break;
                    
                case PENDING:
                case ACCEPTED:
                case COMPLETED:
                    throw new IllegalStateException("Cannot create new pickup - existing pickup is already " + existingPickup.getStatus());
                    
                default:
                    throw new IllegalStateException("Cannot create pickup - existing pickup has status: " + existingPickup.getStatus());
            }
        }

        // Get creator user
        UserEntity createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + createdByUserId));

        // Validate user is involved in the offer
        if (!isUserInvolvedInOffer(offer, createdByUserId)) {
            throw new IllegalArgumentException("User is not involved in this offer");
        }

        // Validate available dates are in the future
        LocalDate today = LocalDate.now();
        boolean hasValidDates = pickupDTO.getAvailableDates().stream()
                .anyMatch(date -> !date.isBefore(today));

        if (!hasValidDates) {
            throw new IllegalArgumentException("At least one available date must be today or in the future");
        }

        // Validate time range
        if (pickupDTO.getStartTime().isAfter(pickupDTO.getEndTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }

        // Create pickup entity
        PickupEntity pickup = new PickupEntity(
                offer,
                pickupDTO.getAvailableDates(),
                pickupDTO.getStartTime(),
                pickupDTO.getEndTime(),
                pickupDTO.getOffer().getListing().getPickupLocation(),
                pickupDTO.getDetails(),
                createdBy);

        pickup = pickupRepository.save(pickup);

        // Update offer status to PICKUPSCHEDULING
        offer.setUpdatedAt(LocalDateTime.now());
        offerRepository.save(offer);

        logger.info("Successfully created pickup {} for offer {} and updated status to PICKUPSCHEDULING",
                pickup.getId(), pickupDTO.getOfferId());

        return pickupMapper.toDto(pickup);
    }

    /**
     * Update an existing pickup with new scheduling information
     */
    private PickupDTO updateExistingPickup(PickupEntity existingPickup, PickupDTO pickupDTO, Long updatingUserId, OfferEntity offer) {
        // Get updating user
        UserEntity updatedBy = userRepository.findById(updatingUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + updatingUserId));

        // Update the pickup with new information
        existingPickup.setAvailableDates(pickupDTO.getAvailableDates());
        existingPickup.setStartTime(pickupDTO.getStartTime());
        existingPickup.setEndTime(pickupDTO.getEndTime());
        existingPickup.setDetails(pickupDTO.getDetails());
        existingPickup.setUpdatedBy(updatedBy);
        existingPickup.setStatus(PickupStatus.PENDING); // Reset to pending
        existingPickup.setSelectedDate(null); // Clear any previous selection
        existingPickup.setSelectedTime(null);

        existingPickup = pickupRepository.save(existingPickup);

        // Update offer status to PICKUPSCHEDULING (or keep it in PICKUPRESCHEDULING)
        if (offer.getStatus() != OfferStatus.PICKUPRESCHEDULING) {
            offer.setStatus(OfferStatus.PICKUPSCHEDULING);
            offer.setUpdatedAt(LocalDateTime.now());
            offerRepository.save(offer);
        }

        logger.info("Successfully updated existing pickup {} for offer {}", existingPickup.getId(), pickupDTO.getOfferId());

        return pickupMapper.toDto(existingPickup);
    }

    /**
     * Accept pickup with selected date and time, updating offer status to CONFIRMED
     */
    @Transactional
    public PickupDTO acceptPickup(Long pickupId, LocalDate selectedDate, LocalTime selectedTime, Long userId) {
        logger.info("Accepting pickup {} with date {} time {} by user {}", pickupId, selectedDate, selectedTime, userId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        // Validate user is involved in the pickup
        if (!isUserInvolvedInPickup(pickup, userId)) {
            throw new IllegalArgumentException("User is not involved in this pickup");
        }

        // Validate pickup is in pending status
        if (pickup.getStatus() != PickupStatus.PENDING) {
            throw new IllegalStateException("Can only accept pending pickups");
        }

        // Validate selected date is available
        if (!pickup.isDateAvailable(selectedDate)) {
            throw new IllegalArgumentException("Selected date is not available");
        }

        // Validate selected time is within allowed range
        if (!pickup.isTimeSlotValid(selectedTime)) {
            throw new IllegalArgumentException("Selected time is not within the allowed range");
        }

        // Get updating user
        UserEntity updatedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update pickup with selected date and time
        pickup.setSelectedDate(selectedDate);
        pickup.setSelectedTime(selectedTime);
        pickup.setStatus(PickupStatus.ACCEPTED);
        pickup.setUpdatedBy(updatedBy);

        pickup = pickupRepository.save(pickup);

        // Update associated offer status to CONFIRMED
        OfferEntity offer = pickup.getOffer();
        OfferStatus oldStatus = offer.getStatus();
        offer.setStatus(OfferStatus.CONFIRMED);
        offer.setUpdatedAt(LocalDateTime.now());
        offerRepository.save(offer);

        logger.info("Successfully accepted pickup {} for {} and updated offer status from {} to CONFIRMED",
                pickupId, selectedDate.atTime(selectedTime), oldStatus);

        return pickupMapper.toDto(pickup);
    }

    /**
     * Get pickup by offer ID - ensures user is involved in the offer
     */
    @Transactional(readOnly = true)
    public PickupDTO getPickupByOfferId(String offerId, Long userId) {
        logger.info("Getting pickup for offer {} by user {}", offerId, userId);

        // First verify the user is involved in the offer
        OfferEntity offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));

        if (!isUserInvolvedInOffer(offer, userId)) {
            throw new IllegalArgumentException("User is not involved in this offer");
        }

        // Get the pickup for this offer
        Optional<PickupEntity> pickupOpt = pickupRepository.findByOfferId(offerId);
        
        if (pickupOpt.isPresent()) {
            PickupEntity pickup = pickupOpt.get();
            
            // Verify user is involved in the pickup as well
            if (!isUserInvolvedInPickup(pickup, userId)) {
                throw new IllegalArgumentException("User is not involved in this pickup");
            }
            
            return pickupMapper.toDto(pickup);
        }
        
        return null; // No pickup found for this offer
    }

    /**
     * Cancel pickup arrangement and update offer status to CANCELLED
     */
    @Transactional
    public void cancelPickupArrangement(Long pickupId, Long userId) {
        logger.info("Cancelling pickup arrangement {} by user {}", pickupId, userId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        // Validate user is involved in the pickup
        if (!isUserInvolvedInPickup(pickup, userId)) {
            throw new IllegalArgumentException("User is not involved in this pickup");
        }

        // Get the associated offer
        OfferEntity offer = pickup.getOffer();

        // Update pickup status
        pickup.setStatus(PickupStatus.CANCELLED);
        pickup.setUpdatedBy(userRepository.findById(userId).orElse(null));
        pickupRepository.save(pickup);

        // Update offer status to CANCELLED
        offer.setStatus(OfferStatus.CANCELLED);
        offer.setUpdatedAt(LocalDateTime.now());
        offerRepository.save(offer);

        logger.info("Successfully cancelled pickup arrangement {} and updated offer {} status to CANCELLED",
                pickupId, offer.getId());
    }

    /**
     * Reject pickup and update offer status based on current state
     */
    @Transactional
    public PickupDTO rejectPickup(Long pickupId, Long userId) {
        logger.info("Rejecting pickup {} by user {}", pickupId, userId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        // Validate user is involved in the pickup
        if (!isUserInvolvedInPickup(pickup, userId)) {
            throw new IllegalArgumentException("User is not involved in this pickup");
        }

        // Only allow rejection of pending pickups
        if (pickup.getStatus() != PickupStatus.PENDING) {
            throw new IllegalStateException("Can only reject pending pickups");
        }

        // Get updating user
        UserEntity updatedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update pickup status
        pickup.setStatus(PickupStatus.DECLINED);
        pickup.setUpdatedBy(updatedBy);
        pickup = pickupRepository.save(pickup);

        // Update offer status - this will trigger a choice for the user:
        // Either cancel the offer or reschedule pickup
        OfferEntity offer = pickup.getOffer();
        OfferStatus currentOfferStatus = offer.getStatus();

        // Set status to PICKUPRESCHEDULING to indicate need for rescheduling decision
        if (currentOfferStatus == OfferStatus.PICKUPSCHEDULING) {
            offer.setStatus(OfferStatus.PICKUPRESCHEDULING);
            offer.setUpdatedAt(LocalDateTime.now());
            offerRepository.save(offer);

            logger.info("Pickup rejected, offer {} status updated to PICKUPRESCHEDULING", offer.getId());
        }

        logger.info("Successfully rejected pickup {}", pickupId);
        return pickupMapper.toDto(pickup);
    }

    /**
     * Update pickup status
     */
    @Transactional
    public PickupDTO updatePickupStatus(Long pickupId, PickupStatus newStatus, Long userId) {
        logger.info("Updating pickup {} status to {} by user {}", pickupId, newStatus, userId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        // Validate user is involved in the pickup
        if (!isUserInvolvedInPickup(pickup, userId)) {
            throw new IllegalArgumentException("User is not involved in this pickup");
        }

        // Get updating user
        UserEntity updatedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        PickupStatus oldStatus = pickup.getStatus();
        pickup.setStatus(newStatus);
        pickup.setUpdatedBy(updatedBy);
        pickup = pickupRepository.save(pickup);

        // Handle status-specific logic
        if (newStatus == PickupStatus.DECLINED && oldStatus == PickupStatus.PENDING) {
            // If pickup is declined, we might want to revert the offer status
            handlePickupRejection(pickup);
        } else if (newStatus == PickupStatus.COMPLETED) {
            // Mark offer and listing as completed
            handlePickupCompletion(pickup);
        }

        logger.info("Successfully updated pickup {} status from {} to {}", pickupId, oldStatus, newStatus);
        return pickupMapper.toDto(pickup);
    }

    private void handlePickupRejection(PickupEntity pickup) {
        OfferEntity offer = pickup.getOffer();

        // Check if there are other pending pickups for this offer
        List<PickupEntity> otherPickups = pickupRepository.findByOfferIdAndStatus(
                offer.getId(), PickupStatus.PENDING);

        // If no other pending pickups, revert offer to PENDING
        if (otherPickups.isEmpty()) {
            offer.setStatus(OfferStatus.PENDING);
            offer.setUpdatedAt(LocalDateTime.now());
            offerRepository.save(offer);

            // Also revert listing status
            ListingEntity listing = offer.getListing();
            if (listing.getStatus() == ListingStatus.COMPLETED) {
                listing.setStatus(ListingStatus.ACTIVE);
                listing.setUpdatedAt(LocalDateTime.now());
                listingRepository.save(listing);
            }
        }
    }

    private void handlePickupCompletion(PickupEntity pickup) {
        OfferEntity offer = pickup.getOffer();

        // Mark offer as completed
        offer.setStatus(OfferStatus.COMPLETED);
        offer.setUpdatedAt(LocalDateTime.now());
        offerRepository.save(offer);

        // Mark listing as completed
        ListingEntity listing = offer.getListing();
        listing.setStatus(ListingStatus.COMPLETED);
        listing.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(listing);
    }

    /**
     * Update pickup details
     */
    @Transactional
    public PickupDTO updatePickup(Long pickupId, PickupDTO pickupDTO, Long userId) {
        logger.info("Updating pickup {} by user {}", pickupId, userId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        // Validate user is involved in the pickup
        if (!isUserInvolvedInPickup(pickup, userId)) {
            throw new IllegalArgumentException("User is not involved in this pickup");
        }

        // Only allow updates for pending or accepted pickups
        if (pickup.getStatus() != PickupStatus.PENDING && pickup.getStatus() != PickupStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot update pickup with status: " + pickup.getStatus());
        }

        // Get updating user
        UserEntity updatedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update fields
        pickup.setLocation(pickupDTO.getLocation());
        pickup.setDetails(pickupDTO.getDetails());
        pickup.setUpdatedBy(updatedBy);

        pickup = pickupRepository.save(pickup);

        logger.info("Successfully updated pickup {}", pickupId);
        return pickupMapper.toDto(pickup);
    }

    /**
     * Get pickup by ID
     */
    @Transactional(readOnly = true)
    public PickupDTO getPickupById(Long pickupId) {
        logger.info("Getting pickup by ID: {}", pickupId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        return pickupMapper.toDto(pickup);
    }

    /**
     * Get pickup by offer ID
     */
    @Transactional(readOnly = true)
    public Optional<PickupDTO> getPickupByOfferId(String offerId) {
        logger.info("Getting pickup by offer ID: {}", offerId);

        return pickupRepository.findByOfferId(offerId)
                .map(pickupMapper::toDto);
    }

    /**
     * Get all pickups for a user
     */
    @Transactional(readOnly = true)
    public List<PickupDTO> getUserPickups(Long userId) {
        logger.info("Getting pickups for user: {}", userId);

        List<PickupEntity> pickups = pickupRepository.findByUserIdWithDetails(userId);

        return pickups.stream()
                .map(pickupMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated pickups for a user
     */
    @Transactional(readOnly = true)
    public Page<PickupDTO> getUserPickups(Long userId, int page, int size) {
        logger.info("Getting paginated pickups for user: {} (page {}, size {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<PickupEntity> pickupPage = pickupRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return pickupPage.map(pickupMapper::toDto);
    }

    /**
     * Get upcoming pickups for a user
     */
    @Transactional(readOnly = true)
    public List<PickupDTO> getUpcomingPickups(Long userId) {
        logger.info("Getting upcoming pickups for user: {}", userId);

        // Remove the LocalDateTime.now() parameter since the query doesn't need it
        // anymore
        List<PickupEntity> pickups = pickupRepository.findUpcomingPickupsByUserId(userId, null);

        return pickups.stream()
                .map(pickupMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get past pickups for a user
     */
    @Transactional(readOnly = true)
    public List<PickupDTO> getPastPickups(Long userId) {
        logger.info("Getting past pickups for user: {}", userId);

        // Remove the LocalDateTime.now() parameter since the query doesn't need it
        // anymore
        List<PickupEntity> pickups = pickupRepository.findPastPickupsByUserId(userId, null);

        return pickups.stream()
                .map(pickupMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get pickups by status for a user
     */
    @Transactional(readOnly = true)
    public List<PickupDTO> getPickupsByStatus(Long userId, PickupStatus status) {
        logger.info("Getting pickups with status {} for user: {}", status, userId);

        List<PickupEntity> pickups = pickupRepository.findByStatusAndUserIdWithDetails(status, userId);

        return pickups.stream()
                .map(pickupMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Delete pickup
     */
    @Transactional
    public void deletePickup(Long pickupId, Long userId) {
        logger.info("Deleting pickup {} by user {}", pickupId, userId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        // Validate user is involved in the pickup
        if (!isUserInvolvedInPickup(pickup, userId)) {
            throw new IllegalArgumentException("User is not involved in this pickup");
        }

        // Only allow deletion of pending or cancelled pickups
        if (pickup.getStatus() != PickupStatus.PENDING && pickup.getStatus() != PickupStatus.CANCELLED) {
            throw new IllegalStateException("Cannot delete pickup with status: " + pickup.getStatus());
        }

        pickupRepository.delete(pickup);
        logger.info("Successfully deleted pickup {}", pickupId);
    }

    /**
     * Get pickups needing reminder notifications
     */
    @Transactional(readOnly = true)
    public List<PickupDTO> getPickupsNeedingReminder() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        List<PickupEntity> pickups = pickupRepository.findPickupsNeedingReminder(today, tomorrow);

        return pickups.stream()
                .map(pickupMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Count pickups by status for a user
     */
    @Transactional(readOnly = true)
    public long countPickupsByStatus(Long userId, PickupStatus status) {
        return pickupRepository.countByStatusAndUserId(status, userId);
    }

    // Helper methods

    private boolean isUserInvolvedInOffer(OfferEntity offer, Long userId) {
        return offer.getUser().getId().equals(userId) ||
                offer.getListing().getCreator().getId().equals(userId);
    }

    private boolean isUserInvolvedInPickup(PickupEntity pickup, Long userId) {
        return pickup.getCreatedBy().getId().equals(userId) ||
                pickup.getOffer().getUser().getId().equals(userId) ||
                pickup.getOffer().getListing().getCreator().getId().equals(userId);
    }

    private void validateStatusTransition(PickupStatus currentStatus, PickupStatus newStatus) {
        // Define valid transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != PickupStatus.ACCEPTED &&
                        newStatus != PickupStatus.DECLINED &&
                        newStatus != PickupStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case ACCEPTED:
                if (newStatus != PickupStatus.COMPLETED &&
                        newStatus != PickupStatus.CANCELLED) {
                    throw new IllegalStateException(
                            "Invalid status transition from " + currentStatus + " to " + newStatus);
                }
                break;
            case DECLINED:
            case COMPLETED:
            case CANCELLED:
                throw new IllegalStateException("Cannot change status from " + currentStatus);
            default:
                throw new IllegalStateException("Unknown status: " + currentStatus);
        }
    }
}