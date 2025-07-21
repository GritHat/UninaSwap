package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.GiftListingDTO;
import com.uninaswap.server.entity.GiftListingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GiftListingMapper {

    private final CommonListingMapper commonListingMapper;
    private final UserMapper userMapper;

    @Autowired
    public GiftListingMapper(CommonListingMapper commonListingMapper, UserMapper userMapper) {
        this.commonListingMapper = commonListingMapper;
        this.userMapper = userMapper;
    }

    public GiftListingDTO toDto(GiftListingEntity entity) {
        if (entity == null) {
            return null;
        }

        GiftListingDTO dto = new GiftListingDTO();

        // Map common fields using the utility
        commonListingMapper.mapCommonFields(entity, dto);

        // Map gift-specific fields
        dto.setPickupOnly(entity.isPickupOnly());
        dto.setAllowThankYouOffers(entity.isAllowThankYouOffers());
        dto.setRestrictions(entity.getRestrictions());
        dto.setPickupLocation(entity.getPickupLocation());
        return dto;
    }
}