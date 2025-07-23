package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.AuctionListingDTO;
import com.uninaswap.server.entity.AuctionListingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuctionListingMapper {

    private final CommonListingMapper commonListingMapper;
    private final UserMapper userMapper;

    @Autowired
    public AuctionListingMapper(CommonListingMapper commonListingMapper, UserMapper userMapper) {
        this.commonListingMapper = commonListingMapper;
        this.userMapper = userMapper;
    }

    public AuctionListingDTO toDto(AuctionListingEntity entity) {
        if (entity == null) {
            return null;
        }

        AuctionListingDTO dto = new AuctionListingDTO();

        
        commonListingMapper.mapCommonFields(entity, dto);

        
        dto.setStartingPrice(entity.getStartingPrice());
        dto.setReservePrice(entity.getReservePrice());
        dto.setCurrency(entity.getCurrency());
        dto.setEndTime(entity.getEndTime());
        dto.setMinimumBidIncrement(entity.getMinimumBidIncrement());
        dto.setCurrentHighestBid(entity.getCurrentHighestBid());
        dto.setStartTime(entity.getStartTime());
        dto.setAnyBids(entity.getHasBids());
        dto.setPickupLocation(entity.getPickupLocation());

        if (entity.getHighestBidder() != null) {
            dto.setHighestBidder(userMapper.toDto(entity.getHighestBidder()));
        }

        
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            long days = java.time.Duration.between(entity.getStartTime(), entity.getEndTime()).toDays();
            dto.setDurationInDays((int) days);
        }

        return dto;
    }
}