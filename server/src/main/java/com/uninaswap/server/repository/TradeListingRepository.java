package com.uninaswap.server.repository;

import com.uninaswap.server.entity.TradeListingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeListingRepository extends JpaRepository<TradeListingEntity, String> {
    // Find trades that request items in specific categories
    List<TradeListingEntity> findByDesiredCategoriesContaining(String category);
}