package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.dto.ListingItemDTO;
import com.uninaswap.server.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ListingMapper {

    private final UserMapper userMapper;
    private final SellListingMapper sellListingMapper;
    private final TradeListingMapper tradeListingMapper;
    private final GiftListingMapper giftListingMapper;
    private final AuctionListingMapper auctionListingMapper;

    @Autowired
    public ListingMapper(UserMapper userMapper,
            SellListingMapper sellListingMapper,
            TradeListingMapper tradeListingMapper,
            GiftListingMapper giftListingMapper,
            AuctionListingMapper auctionListingMapper) {
        this.userMapper = userMapper;
        this.sellListingMapper = sellListingMapper;
        this.tradeListingMapper = tradeListingMapper;
        this.giftListingMapper = giftListingMapper;
        this.auctionListingMapper = auctionListingMapper;
    }

    public ListingDTO toDto(ListingEntity entity) {
        if (entity == null) {
            return null;
        }

        // Route to the appropriate specific mapper based on the entity type
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

    // Helper method to map listing items
    protected List<ListingItemDTO> mapListingItems(List<ListingItemEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(this::mapListingItem)
                .collect(Collectors.toList());
    }

    protected ListingItemDTO mapListingItem(ListingItemEntity entity) {
        if (entity == null) {
            return null;
        }

        ListingItemDTO dto = new ListingItemDTO();
        dto.setItemId(entity.getItem().getId());
        dto.setItemName(entity.getItem().getName());
        dto.setItemImagePath(entity.getItem().getImagePath());
        dto.setQuantity(entity.getQuantity());
        return dto;
    }

    // Common method to set shared attributes from entity to DTO
    protected void mapCommonFields(ListingEntity entity, ListingDTO dto) {
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
}