package com.uninaswap.server.repository;

import com.uninaswap.server.entity.AuctionListingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionListingRepository extends JpaRepository<AuctionListingEntity, String> {
    
    List<AuctionListingEntity> findByEndTimeBetween(LocalDateTime start, LocalDateTime end);
    
    
    @Query("SELECT a FROM AuctionListingEntity a WHERE a.highestBidder IS NULL")
    List<AuctionListingEntity> findWithNoBids();
}