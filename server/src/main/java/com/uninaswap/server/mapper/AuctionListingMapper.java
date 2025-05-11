package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.AuctionListingDTO;
import com.uninaswap.server.entity.AuctionListingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuctionListingMapper {
    
    private final ListingMapper listingMapper;
    private final UserMapper userMapper;
    
    @Autowired
    public AuctionListingMapper(ListingMapper listingMapper, UserMapper userMapper) {
        this.listingMapper = listingMapper;
        this.userMapper = userMapper;
    }
    
    public AuctionListingDTO toDto(AuctionListingEntity entity) {
        if (entity == null) {
            return null;
        }
        
        AuctionListingDTO dto = new AuctionListingDTO();
        
        // Map common fields from base class
        listingMapper.mapCommonFields(entity, dto);
        
        // Map auction-specific fields
        dto.setStartingPrice(entity.getStartingPrice());
        dto.setReservePrice(entity.getReservePrice());
        dto.setCurrency(entity.getCurrency());
        dto.setEndTime(entity.getEndTime());
        dto.setMinimumBidIncrement(entity.getMinimumBidIncrement());
        dto.setCurrentHighestBid(entity.getCurrentHighestBid());
        
        if (entity.getHighestBidder() != null) {
            dto.setHighestBidder(userMapper.toDto(entity.getHighestBidder()));
        }
        
        // Calculate duration in days for the DTO
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            long days = java.time.Duration.between(entity.getStartTime(), entity.getEndTime()).toDays();
            dto.setDurationInDays((int) days);
        }
        
        return dto;
    }
}