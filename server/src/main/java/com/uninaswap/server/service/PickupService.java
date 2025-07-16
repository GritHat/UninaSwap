package com.uninaswap.server.service;

import com.uninaswap.common.dto.PickupDTO;
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
     * Create a pickup arrangement for an accepted offer
     */
    @Transactional
    public PickupDTO createPickup(PickupDTO pickupDTO, Long createdByUserId) {
        logger.info("Creating pickup for offer {} by user {}", pickupDTO.getOfferId(), createdByUserId);

        // Validate offer exists and is accepted
        OfferEntity offer = offerRepository.findById(pickupDTO.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + pickupDTO.getOfferId()));

        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new IllegalStateException("Can only create pickup for accepted offers");
        }

        // Check if pickup already exists for this offer
        if (pickupRepository.existsByOfferId(pickupDTO.getOfferId())) {
            throw new IllegalStateException("Pickup already exists for this offer");
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
                pickupDTO.getLocation(),
                pickupDTO.getDetails(),
                createdBy);

        pickup = pickupRepository.save(pickup);

        logger.info("Successfully created pickup {} for offer {}", pickup.getId(), pickupDTO.getOfferId());
        return pickupMapper.toDto(pickup);
    }

    /**
     * Accept pickup with selected date and time
     */
    @Transactional
    public PickupDTO acceptPickup(Long pickupId, LocalDate selectedDate, LocalTime selectedTime, Long userId) {
        logger.info("Accepting pickup {} with date {} time {} by user {}", pickupId, selectedDate, selectedTime,
                userId);

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

        // Validate selected time is within time range
        if (!pickup.isTimeSlotValid(selectedTime)) {
            throw new IllegalArgumentException("Selected time is not within the available time range");
        }

        // Validate selected date/time is in the future
        LocalDateTime selectedDateTime = selectedDate.atTime(selectedTime);
        if (selectedDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Selected date and time cannot be in the past");
        }

        // Get updating user
        UserEntity updatedBy = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Update pickup
        pickup.setSelectedDate(selectedDate);
        pickup.setSelectedTime(selectedTime);
        pickup.setStatus(PickupStatus.ACCEPTED);
        pickup.setUpdatedBy(updatedBy);

        pickup = pickupRepository.save(pickup);

        logger.info("Successfully accepted pickup {} for {}", pickupId, selectedDateTime);
        return pickupMapper.toDto(pickup);
    }

    /**
     * Cancel pickup and revert offer status
     */
    @Transactional
    public void cancelPickup(Long pickupId, Long userId) {
        logger.info("Cancelling pickup {} by user {}", pickupId, userId);

        PickupEntity pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup not found: " + pickupId));

        // Validate user is involved in the pickup
        if (!isUserInvolvedInPickup(pickup, userId)) {
            throw new IllegalArgumentException("User is not involved in this pickup");
        }

        // Only allow cancellation of pending or accepted pickups
        if (pickup.getStatus() != PickupStatus.PENDING && pickup.getStatus() != PickupStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot cancel pickup with status: " + pickup.getStatus());
        }

        // Get the associated offer
        OfferEntity offer = pickup.getOffer();

        // Update pickup status
        pickup.setStatus(PickupStatus.CANCELLED);
        pickup.setUpdatedBy(userRepository.findById(userId).orElse(null));
        pickupRepository.save(pickup);

        // Revert offer status back to PENDING if it was ACCEPTED
        if (offer.getStatus() == OfferStatus.ACCEPTED) {
            offer.setStatus(OfferStatus.PENDING);
            offer.setUpdatedAt(LocalDateTime.now());
            offerRepository.save(offer);

            // Also revert listing status back to ACTIVE
            ListingEntity listing = offer.getListing();
            if (listing.getStatus() == ListingStatus.COMPLETED) {
                listing.setStatus(ListingStatus.ACTIVE);
                listing.setUpdatedAt(LocalDateTime.now());
                listingRepository.save(listing);
            }
        }

        logger.info("Successfully cancelled pickup {} and reverted offer status", pickupId);
    }

    /**
     * Reject pickup and update offer status
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

        // Note: Offer and listing status handling will depend on your business logic
        // You might want to keep the offer as ACCEPTED until a counter-proposal is made
        // or immediately revert it to PENDING

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