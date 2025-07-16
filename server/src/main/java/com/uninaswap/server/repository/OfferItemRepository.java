package com.uninaswap.server.repository;

import com.uninaswap.server.entity.OfferItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferItemRepository extends JpaRepository<OfferItemEntity, Long> {

    /**
     * Find all offer items for a specific offer
     */
    List<OfferItemEntity> findByOfferId(String offerId);

    /**
     * Find all offer items for a specific item
     */
    List<OfferItemEntity> findByItemId(String itemId);

    /**
     * Get total reserved quantity for an item across all pending offers
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OfferItemEntity oi WHERE oi.item.id = :itemId AND oi.offer.status = 'PENDING'")
    int getTotalReservedQuantityForItem(@Param("itemId") String itemId);
}