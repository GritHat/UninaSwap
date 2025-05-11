package com.uninaswap.server.repository;

import com.uninaswap.server.entity.ItemEntity;
import com.uninaswap.server.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, String> {
    
    // Find items by owner
    List<ItemEntity> findByOwner(UserEntity owner);
    
    // Find items by category
    List<ItemEntity> findByCategory(String category);
    
    // Search items by name or description
    @Query("SELECT i FROM ItemEntity i WHERE " +
          "LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
          "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ItemEntity> searchByKeyword(String keyword, Pageable pageable);
    
    // Find items by brand and model
    List<ItemEntity> findByBrandAndModel(String brand, String model);
}