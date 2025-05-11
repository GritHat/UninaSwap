package com.uninaswap.server.repository;

import com.uninaswap.server.entity.ListingItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListingItemRepository extends JpaRepository<ListingItemEntity, Long> {
    List<ListingItemEntity> findByItemId(String itemId);
    List<ListingItemEntity> findByListingId(String listingId);
}