package com.uninaswap.server.repository;

import com.uninaswap.server.entity.GiftListingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftListingRepository extends JpaRepository<GiftListingEntity, String> {
    
    List<GiftListingEntity> findByPickupOnlyTrue();
    
    
    List<GiftListingEntity> findByAllowThankYouOffersTrue();
}