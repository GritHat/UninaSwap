package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.SellListingDTO;
import com.uninaswap.server.entity.SellListingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SellListingMapper {

    private final CommonListingMapper commonListingMapper;

    @Autowired
    public SellListingMapper(CommonListingMapper commonListingMapper) {
        this.commonListingMapper = commonListingMapper;
    }

    public SellListingDTO toDto(SellListingEntity entity) {
        if (entity == null) {
            return null;
        }

        SellListingDTO dto = new SellListingDTO();

        // Map common fields using the utility
        commonListingMapper.mapCommonFields(entity, dto);

        // Map sell-specific fields
        dto.setPrice(entity.getPrice());
        dto.setCurrency(entity.getCurrency());
        dto.setPickupLocation(entity.getPickupLocation());

        return dto;
    }
}