package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.server.entity.ItemEntity;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {
    public ItemDTO toDto(ItemEntity entity) {
        if (entity == null) {
            return null;
        }
        
        ItemDTO dto = new ItemDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setImagePath(entity.getImagePath());
        dto.setCondition(entity.getCondition());
        dto.setCategory(entity.getCategory());
        dto.setBrand(entity.getBrand());
        dto.setModel(entity.getModel());
        dto.setYearOfProduction(entity.getYearOfProduction());
        dto.setStockQuantity(entity.getStockQuantity());
        dto.setAvailableQuantity(entity.getAvailableQuantity());
        
        if (entity.getOwner() != null) {
            dto.setOwnerId(entity.getOwner().getId());
        }
        
        return dto;
    }
}