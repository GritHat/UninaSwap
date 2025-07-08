package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.dto.ListingItemDTO;
import com.uninaswap.server.entity.ListingEntity;
import com.uninaswap.server.entity.ListingItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommonListingMapper {

    private final UserMapper userMapper;

    @Autowired
    public CommonListingMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * Maps common fields from entity to DTO
     */
    public void mapCommonFields(ListingEntity entity, ListingDTO dto) {
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreator(userMapper.toDto(entity.getCreator()));
        dto.setStatus(entity.getStatus());
        dto.setFeatured(entity.isFeatured());
        dto.setItems(mapListingItems(entity.getListingItems()));
    }

    /**
     * Maps listing items from entities to DTOs
     */
    public List<ListingItemDTO> mapListingItems(List<ListingItemEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::mapListingItem)
                .collect(Collectors.toList());
    }

    /**
     * Maps a single listing item from entity to DTO
     */
    protected ListingItemDTO mapListingItem(ListingItemEntity entity) {
        if (entity == null) {
            return null;
        }

        ListingItemDTO dto = new ListingItemDTO();
        dto.setItemId(entity.getItem().getId());
        dto.setItemName(entity.getItem().getName());
        dto.setItemImagePath(entity.getItem().getImagePath());
        dto.setItemCategory(entity.getItem().getCategory()); // Add this line
        dto.setQuantity(entity.getQuantity());
        return dto;
    }
}
