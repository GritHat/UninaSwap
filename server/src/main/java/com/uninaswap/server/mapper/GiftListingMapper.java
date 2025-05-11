package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.GiftListingDTO;
import com.uninaswap.server.entity.GiftListingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GiftListingMapper {
    
    private final ListingMapper listingMapper;
    private final UserMapper userMapper;
    
    @Autowired
    public GiftListingMapper(ListingMapper listingMapper, UserMapper userMapper) {
        this.listingMapper = listingMapper;
        this.userMapper = userMapper;
    }
    
    public GiftListingDTO toDto(GiftListingEntity entity) {
        if (entity == null) {
            return null;
        }
        
        GiftListingDTO dto = new GiftListingDTO();
        
        // Map common fields from base class
        listingMapper.mapCommonFields(entity, dto);
        
        // Map gift-specific fields
        dto.setPickupOnly(entity.isPickupOnly());
        dto.setAllowThankYouOffers(entity.isAllowThankYouOffers());
        dto.setRestrictions(entity.getRestrictions());
        
        if (entity.getSelectedRecipient() != null) {
            dto.setSelectedRecipient(userMapper.toDto(entity.getSelectedRecipient()));
        }
        
        return dto;
    }
}