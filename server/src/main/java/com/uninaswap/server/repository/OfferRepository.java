package com.uninaswap.server.repository;

import com.uninaswap.common.enums.OfferStatus;
import com.uninaswap.server.entity.OfferEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<OfferEntity, String> {

    /**
     * Find all offers for a specific listing
     */
    @Query("SELECT o FROM OfferEntity o " +
            "JOIN FETCH o.listing l " +
            "LEFT JOIN FETCH l.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "WHERE l.id = :listingId " +
            "ORDER BY o.createdAt DESC")
    List<OfferEntity> findByListingIdOrderByCreatedAtDesc(@Param("listingId") String listingId);

    /**
     * Find all offers made by a user
     */
    @Query("SELECT o FROM OfferEntity o " +
            "JOIN FETCH o.listing l " +
            "LEFT JOIN FETCH l.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "WHERE o.user.id = :userId " +
            "ORDER BY o.createdAt DESC")
    List<OfferEntity> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find all offers received by a user (offers on their listings)
     */
    @Query("SELECT o FROM OfferEntity o WHERE o.listing.creator.id = :userId ORDER BY o.createdAt DESC")
    List<OfferEntity> findByListingCreatorIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find offers by status
     */
    List<OfferEntity> findByStatusOrderByCreatedAtDesc(OfferStatus status);

    /**
     * Find offers for a listing with pagination
     */
    Page<OfferEntity> findByListingIdOrderByCreatedAtDesc(String listingId, Pageable pageable);

    /**
     * Find user offers with pagination
     */
    Page<OfferEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Add to OfferRepository  
    List<OfferEntity> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
    List<OfferEntity> findByListingCreatorIdAndCreatedAtBetween(Long creatorId, LocalDateTime start, LocalDateTime end);
    long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Find received offers with pagination
     */
    @Query("SELECT o FROM OfferEntity o WHERE o.listing.creator.id = :userId ORDER BY o.createdAt DESC")
    Page<OfferEntity> findByListingCreatorIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count pending offers for a user's listings
     */
    @Query("SELECT COUNT(o) FROM OfferEntity o WHERE o.listing.creator.id = :userId AND o.status = 'PENDING'")
    long countPendingOffersForUserListings(@Param("userId") Long userId);
}