package com.uninaswap.server.repository;

import com.uninaswap.server.entity.FavoriteEntity;
import com.uninaswap.server.entity.ListingEntity;
import com.uninaswap.server.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, Long> {

    /**
     * Find favorite by user and listing
     */
    Optional<FavoriteEntity> findByUserAndListing(UserEntity user, ListingEntity listing);

    /**
     * Find favorite by user ID and listing ID
     */
    @Query("SELECT f FROM FavoriteEntity f WHERE f.user.id = :userId AND f.listing.id = :listingId")
    Optional<FavoriteEntity> findByUserIdAndListingId(@Param("userId") Long userId,
            @Param("listingId") String listingId);

    /**
     * Get all favorites for a user with listing details
     */
    @Query("SELECT f FROM FavoriteEntity f " +
            "JOIN FETCH f.listing l " +
            "JOIN FETCH l.creator " +
            "LEFT JOIN FETCH l.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "WHERE f.user.id = :userId " +
            "ORDER BY f.createdAt DESC")
    List<FavoriteEntity> findByUserIdWithDetails(@Param("userId") Long userId);

    /**
     * Get paginated favorites for a user
     */
    Page<FavoriteEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Get all favorite listing IDs for a user
     */
    @Query("SELECT f.listing.id FROM FavoriteEntity f WHERE f.user.id = :userId")
    List<String> findListingIdsByUserId(@Param("userId") Long userId);

    /**
     * Check if user has favorited a listing
     */
    boolean existsByUserIdAndListingId(Long userId, String listingId);

    /**
     * Count favorites for a listing
     */
    long countByListingId(String listingId);

    /**
     * Count total favorites for a user
     */
    long countByUserId(Long userId);

    /**
     * Delete favorite by user and listing IDs
     */
    void deleteByUserIdAndListingId(Long userId, String listingId);

    /**
     * Get most favorited listings
     */
    @Query("SELECT f.listing.id, COUNT(f) as favoriteCount " +
            "FROM FavoriteEntity f " +
            "GROUP BY f.listing.id " +
            "ORDER BY favoriteCount DESC")
    List<Object[]> findMostFavoritedListings(Pageable pageable);
}