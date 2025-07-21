package com.uninaswap.server.repository;

import com.uninaswap.common.enums.PickupStatus;
import com.uninaswap.server.entity.PickupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PickupRepository extends JpaRepository<PickupEntity, Long> {

        /**
         * Find pickup by offer ID
         */
        @Query("SELECT p FROM PickupEntity p WHERE p.offer.id = :offerId")
        Optional<PickupEntity> findByOfferId(@Param("offerId") String offerId);

        /**
         * Get all pickups for a user (both created by and involved in)
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "JOIN FETCH p.offer o " +
                        "JOIN FETCH o.user " +
                        "JOIN FETCH o.listing l " +
                        "JOIN FETCH l.creator " +
                        "WHERE p.createdBy.id = :userId " +
                        "   OR o.user.id = :userId " +
                        "   OR l.creator.id = :userId " +
                        "ORDER BY p.createdAt DESC")
        List<PickupEntity> findByUserIdWithDetails(@Param("userId") Long userId);

        /**
         * Get paginated pickups for a user
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.createdBy.id = :userId " +
                        "   OR p.offer.user.id = :userId " +
                        "   OR p.offer.listing.creator.id = :userId " +
                        "ORDER BY p.createdAt DESC")
        Page<PickupEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

        /**
         * Get pickups created by a user
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "JOIN FETCH p.offer o " +
                        "JOIN FETCH o.user " +
                        "JOIN FETCH o.listing l " +
                        "JOIN FETCH l.creator " +
                        "WHERE p.createdBy.id = :userId " +
                        "ORDER BY p.createdAt DESC")
        List<PickupEntity> findByCreatedByIdWithDetails(@Param("userId") Long userId);

        /**
         * Get pickups by status
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "JOIN FETCH p.offer o " +
                        "JOIN FETCH o.user " +
                        "JOIN FETCH o.listing l " +
                        "JOIN FETCH l.creator " +
                        "WHERE p.status = :status " +
                        "ORDER BY p.createdAt ASC")
        List<PickupEntity> findByStatusWithDetails(@Param("status") PickupStatus status);

        /**
         * Get pickups by status for a specific user
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.status = :status " +
                        "  AND (p.createdBy.id = :userId " +
                        "       OR p.offer.user.id = :userId " +
                        "       OR p.offer.listing.creator.id = :userId) " +
                        "ORDER BY p.createdAt ASC")
        List<PickupEntity> findByStatusAndUserIdWithDetails(@Param("status") PickupStatus status,
                        @Param("userId") Long userId);

        /**
         * Get upcoming pickups - those with selected date/time in the future
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.selectedDate IS NOT NULL " +
                        "  AND p.selectedTime IS NOT NULL " +
                        "  AND p.selectedDate >= CURRENT_DATE " +
                        "  AND p.status IN ('PENDING', 'ACCEPTED') " +
                        "  AND (p.createdBy.id = :userId " +
                        "       OR p.offer.user.id = :userId " +
                        "       OR p.offer.listing.creator.id = :userId) " +
                        "ORDER BY p.selectedDate ASC, p.selectedTime ASC")
        List<PickupEntity> findUpcomingPickupsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

        /**
         * Get past pickups - those with selected date/time in the past
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.selectedDate IS NOT NULL " +
                        "  AND p.selectedTime IS NOT NULL " +
                        "  AND p.selectedDate < CURRENT_DATE " +
                        "  AND (p.createdBy.id = :userId " +
                        "       OR p.offer.user.id = :userId " +
                        "       OR p.offer.listing.creator.id = :userId) " +
                        "ORDER BY p.selectedDate DESC, p.selectedTime DESC")
        List<PickupEntity> findPastPickupsByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

        /**
         * Count pickups by status for a user
         */
        @Query("SELECT COUNT(p) FROM PickupEntity p " +
                        "WHERE p.status = :status " +
                        "  AND (p.createdBy.id = :userId " +
                        "       OR p.offer.user.id = :userId " +
                        "       OR p.offer.listing.creator.id = :userId)")
        long countByStatusAndUserId(@Param("status") PickupStatus status, @Param("userId") Long userId);

        /**
         * Find pickups scheduled within a date range (for those with selected dates)
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.selectedDate IS NOT NULL " +
                        "  AND p.selectedDate BETWEEN :startDate AND :endDate " +
                        "  AND (p.createdBy.id = :userId " +
                        "       OR p.offer.user.id = :userId " +
                        "       OR p.offer.listing.creator.id = :userId) " +
                        "ORDER BY p.selectedDate ASC, p.selectedTime ASC")
        List<PickupEntity> findByDateRangeAndUserId(@Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate,
                        @Param("userId") Long userId);

        /**
         * Check if pickup exists for offer
         */
        boolean existsByOfferIdAndStatus(String offerId, PickupStatus status);

        /**
         * Delete pickup by offer ID
         */
        void deleteByOfferId(String offerId);

        /**
         * Find confirmed pickups that need reminder notifications (24 hours before)
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.selectedDate IS NOT NULL " +
                        "  AND p.selectedTime IS NOT NULL " +
                        "  AND (p.selectedDate = :todayDate OR p.selectedDate = :tomorrowDate)" +
                        "  AND p.status = 'ACCEPTED'")
        List<PickupEntity> findPickupsNeedingReminder(@Param("todayDate") LocalDate todayDate,
                        @Param("tomorrowDate") LocalDate tomorrowDate);

        /**
         * Get upcoming confirmed pickups (with selected date/time)
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.selectedDate IS NOT NULL " +
                        "  AND p.selectedTime IS NOT NULL " +
                        "  AND p.selectedDate >= CURRENT_DATE " +
                        "  AND p.status = 'ACCEPTED' " +
                        "  AND (p.createdBy.id = :userId " +
                        "       OR p.offer.user.id = :userId " +
                        "       OR p.offer.listing.creator.id = :userId) " +
                        "ORDER BY p.selectedDate ASC, p.selectedTime ASC")
        List<PickupEntity> findUpcomingConfirmedPickups(@Param("userId") Long userId, @Param("now") LocalDateTime now);

        /**
         * Get pickups pending date/time selection
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.status = 'PENDING' " +
                        "  AND (p.createdBy.id = :userId " +
                        "       OR p.offer.user.id = :userId " +
                        "       OR p.offer.listing.creator.id = :userId) " +
                        "ORDER BY p.createdAt ASC")
        List<PickupEntity> findPendingPickupSelection(@Param("userId") Long userId);

        /**
         * Find pickups with selected times needing reminders
         */
        @Query("SELECT p FROM PickupEntity p " +
                        "WHERE p.selectedDate IS NOT NULL " +
                        "  AND p.selectedTime IS NOT NULL " +
                        "  AND p.selectedDate BETWEEN :startDate AND :endDate " +
                        "  AND p.status = 'ACCEPTED'")
        List<PickupEntity> findConfirmedPickupsNeedingReminder(@Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate);

        /**
         * Find pickups by offer ID and status
         *
         * @param id      Offer ID
         * @param pending Status to filter by
         * @return List of pickups matching the criteria
         */
        @Query("SELECT p FROM PickupEntity p WHERE p.offer.id = :id AND p.status = :status")
        List<PickupEntity> findByOfferIdAndStatus(@Param("id") String id, @Param("status") PickupStatus status);
}