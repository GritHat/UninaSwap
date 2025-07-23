package com.uninaswap.server.repository;

import com.uninaswap.common.enums.ListingStatus;
import com.uninaswap.server.entity.ListingEntity;
import com.uninaswap.server.entity.OfferEntity;
import com.uninaswap.server.entity.ReviewEntity;
import com.uninaswap.server.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity, String> {

    // Find all active listings
    List<ListingEntity> findByStatus(ListingStatus status);

    // Find all listings by creator
    List<ListingEntity> findByCreator(UserEntity creator);
    
    // Find featured listings
    List<ListingEntity> findByFeaturedTrue();

    // Search listings by title or description containing keyword
    @Query("SELECT l FROM ListingEntity l WHERE " +
            "LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ListingEntity> searchByKeyword(String keyword, Pageable pageable);

    // Find listings by specific class type (by discriminator value)
    @Query("SELECT l FROM ListingEntity l WHERE TYPE(l) = :type AND l.status = :status")
    List<ListingEntity> findByTypeAndStatus(Class<?> type, ListingStatus status);

    // Find all listings by creator ID
    List<ListingEntity> findByCreatorId(Long creatorId);

    // Find all active listings with pagination
    Page<ListingEntity> findByStatus(ListingStatus status, Pageable pageable);

    List<ListingEntity> findByCreatorIdAndCreatedAtBetween(Long creatorId, LocalDateTime start, LocalDateTime end);
    long countByCreatorIdAndCreatedAtBetween(Long creatorId, LocalDateTime start, LocalDateTime end);

    // New method with JOIN FETCH to avoid lazy loading issues
    @Query("SELECT DISTINCT l FROM ListingEntity l " +
            "LEFT JOIN FETCH l.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "LEFT JOIN FETCH l.creator " +
            "WHERE l.status = :status " +
            "ORDER BY l.createdAt DESC")
    Page<ListingEntity> findByStatusWithItems(ListingStatus status, Pageable pageable);

    // Also add method for finding by user with items
    @Query("SELECT DISTINCT l FROM ListingEntity l " +
            "LEFT JOIN FETCH l.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "LEFT JOIN FETCH l.creator " +
            "WHERE l.creator.id = :userId " +
            "ORDER BY l.createdAt DESC")
    List<ListingEntity> findByCreatorIdWithItems(Long userId);

    // Text search methods
    Page<ListingEntity> findByTitleContainingIgnoreCaseAndStatus(String title, ListingStatus status, Pageable pageable);

    /**
     * Find listings created between dates
     * @param startDate Start date
     * @param endDate End date
     * @return List of listings
     */
    List<ListingEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Type filtering methods
    @Query("SELECT DISTINCT l FROM ListingEntity l WHERE TYPE(l) = :listingType AND l.status = :status")
    Page<ListingEntity> findByListingTypeAndStatus(@Param("listingType") Class<?> listingType, @Param("status") ListingStatus status, Pageable pageable);

    // Category filtering methods
    @Query("SELECT DISTINCT l FROM ListingEntity l " +
           "LEFT JOIN FETCH l.listingItems li " +
           "LEFT JOIN FETCH li.item i " +
           "WHERE i.category = :category AND l.status = :status")
    Page<ListingEntity> findByItemsCategoryAndStatus(@Param("category") String category, 
                                                     @Param("status") ListingStatus status, 
                                                     Pageable pageable);
    // Combined search methods
        @Query("SELECT DISTINCT l FROM ListingEntity l " +
           "LEFT JOIN FETCH l.listingItems li " +
           "LEFT JOIN FETCH li.item i " +
           "WHERE l.title ILIKE %:title% AND i.category = :category AND l.status = :status")
    Page<ListingEntity> findByTitleContainingIgnoreCaseAndItemsCategoryAndStatus(
            @Param("title") String title, 
            @Param("category") String category, 
            @Param("status") ListingStatus status, 
            Pageable pageable);

     @Query("SELECT l FROM ListingEntity l " +
           "WHERE l.title ILIKE %:title% AND TYPE(l) = :listingType AND l.status = :status")
    Page<ListingEntity> findByTitleContainingIgnoreCaseAndListingTypeAndStatus(
            @Param("title") String title, 
            @Param("listingType") Class<?> listingType, 
            @Param("status") ListingStatus status, 
            Pageable pageable);
    
    @Query("SELECT DISTINCT l FROM ListingEntity l " +
           "LEFT JOIN FETCH l.listingItems li " +
           "LEFT JOIN FETCH li.item i " +
           "WHERE TYPE(l) = :listingType AND i.category = :category AND l.status = :status")
    Page<ListingEntity> findByListingTypeAndItemsCategoryAndStatus(
            @Param("listingType") Class<?> listingType, 
            @Param("category") String category, 
            @Param("status") ListingStatus status, 
            Pageable pageable);

    @Query("SELECT DISTINCT l FROM ListingEntity l " +
           "LEFT JOIN FETCH l.listingItems li " +
           "LEFT JOIN FETCH li.item i " +
           "WHERE l.title ILIKE %:title% AND TYPE(l) = :listingType AND i.category = :category AND l.status = :status")
    Page<ListingEntity> findByTitleContainingIgnoreCaseAndListingTypeAndItemsCategoryAndStatus(
            @Param("title") String title, 
            @Param("listingType") Class<?> listingType, 
            @Param("category") String category, 
            @Param("status") ListingStatus status, 
            Pageable pageable);
}