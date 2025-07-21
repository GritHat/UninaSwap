package com.uninaswap.server.repository;

import com.uninaswap.common.enums.ListingStatus;
import com.uninaswap.server.entity.TradeListingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TradeListingRepository extends JpaRepository<TradeListingEntity, String> {
    // Find trades that request items in specific categories
    List<TradeListingEntity> findByDesiredCategoriesContaining(String category);

    @Query("SELECT DISTINCT t FROM TradeListingEntity t " +
            "LEFT JOIN FETCH t.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "LEFT JOIN FETCH t.creator " +
            "LEFT JOIN FETCH t.desiredItems " +
            "LEFT JOIN FETCH t.desiredCategories " + // Add this if possible
            "WHERE t.status = :status " +
            "ORDER BY t.createdAt DESC")
    List<TradeListingEntity> findByStatusWithAllData(@Param("status") ListingStatus status);

    @Query("SELECT DISTINCT t FROM TradeListingEntity t " +
            "LEFT JOIN FETCH t.listingItems li " +
            "LEFT JOIN FETCH li.item " +
            "LEFT JOIN FETCH t.creator " +
            "LEFT JOIN FETCH t.desiredItems " +
            "WHERE t.creator.id = :userId " +
            "ORDER BY t.createdAt DESC")
    List<TradeListingEntity> findByCreatorIdWithAllData(@Param("userId") Long userId);
}