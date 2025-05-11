package com.uninaswap.server.repository;

import com.uninaswap.server.entity.SellListingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellListingRepository extends JpaRepository<SellListingEntity, String> {
    // Add any sell listing specific query methods here if needed
}