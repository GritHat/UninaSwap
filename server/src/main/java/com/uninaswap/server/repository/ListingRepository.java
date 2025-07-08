package com.uninaswap.server.repository;

import com.uninaswap.common.enums.ListingStatus;
import com.uninaswap.server.entity.ListingEntity;
import com.uninaswap.server.entity.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    // New method with JOIN FETCH to avoid lazy loading issues
    @Query("SELECT DISTINCT l FROM ListingEntity l " +
            "LEFT JOIN FETCH l.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "LEFT JOIN FETCH l.creator " +
            "WHERE l.status = :status")
    Page<ListingEntity> findByStatusWithItems(ListingStatus status, Pageable pageable);

    // Also add method for finding by user with items
    @Query("SELECT DISTINCT l FROM ListingEntity l " +
            "LEFT JOIN FETCH l.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "WHERE l.creator.id = :userId")
    List<ListingEntity> findByCreatorIdWithItems(Long userId);
}