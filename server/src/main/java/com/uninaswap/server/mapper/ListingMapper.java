package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.*;
import com.uninaswap.server.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    private final SellListingMapper sellListingMapper;
    private final TradeListingMapper tradeListingMapper;
    private final GiftListingMapper giftListingMapper;
    private final AuctionListingMapper auctionListingMapper;

    @Autowired
    public ListingMapper(SellListingMapper sellListingMapper,
            TradeListingMapper tradeListingMapper,
            GiftListingMapper giftListingMapper,
            AuctionListingMapper auctionListingMapper) {
        this.sellListingMapper = sellListingMapper;
        this.tradeListingMapper = tradeListingMapper;
        this.giftListingMapper = giftListingMapper;
        this.auctionListingMapper = auctionListingMapper;
    }

    public ListingDTO toDto(ListingEntity entity) {
        if (entity == null) {
            return null;
        }

        
        if (entity instanceof SellListingEntity) {
            return sellListingMapper.toDto((SellListingEntity) entity);
        } else if (entity instanceof TradeListingEntity) {
            return tradeListingMapper.toDto((TradeListingEntity) entity);
        } else if (entity instanceof GiftListingEntity) {
            return giftListingMapper.toDto((GiftListingEntity) entity);
        } else if (entity instanceof AuctionListingEntity) {
            return auctionListingMapper.toDto((AuctionListingEntity) entity);
        } else {
            throw new UnsupportedOperationException(
                    "Unknown listing entity type: " + entity.getClass().getSimpleName());
        }
    }
}