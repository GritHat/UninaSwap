package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.common.dto.TradeListingDTO;
import com.uninaswap.server.entity.TradeListingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TradeListingMapper {

    private final CommonListingMapper commonListingMapper;
    private final ItemMapper itemMapper;

    @Autowired
    public TradeListingMapper(CommonListingMapper commonListingMapper, ItemMapper itemMapper) {
        this.commonListingMapper = commonListingMapper;
        this.itemMapper = itemMapper;
    }

    public TradeListingDTO toDto(TradeListingEntity entity) {
        if (entity == null) {
            return null;
        }

        TradeListingDTO dto = new TradeListingDTO();

        // Map common fields using the utility
        commonListingMapper.mapCommonFields(entity, dto);

        // Map trade-specific fields
        dto.setDesiredCategories(entity.getDesiredCategories());
        dto.setAcceptMoneyOffers(entity.isAcceptMoneyOffers());
        dto.setAcceptMixedOffers(entity.isAcceptMixedOffers());
        dto.setAcceptOtherOffers(entity.isAcceptOtherOffers());

        if (entity.isAcceptMoneyOffers()) {
            dto.setReferencePrice(entity.getReferencePrice());
            dto.setCurrency(entity.getCurrency());
        }

        // Map desired items
        if (entity.getDesiredItems() != null && !entity.getDesiredItems().isEmpty()) {
            List<ItemDTO> desiredItemDtos = entity.getDesiredItems().stream()
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList());
            dto.setDesiredItems(desiredItemDtos);
        }

        return dto;
    }
}