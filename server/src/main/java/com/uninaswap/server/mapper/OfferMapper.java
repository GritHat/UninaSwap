package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.OfferDTO;
import com.uninaswap.common.dto.OfferItemDTO;
import com.uninaswap.server.entity.OfferEntity;
import com.uninaswap.server.entity.OfferItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OfferMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ListingMapper listingMapper;

    public OfferDTO toDto(OfferEntity entity) {
        if (entity == null) {
            return null;
        }

        OfferDTO dto = new OfferDTO();
        dto.setId(entity.getId());
        dto.setListingId(entity.getListing().getId());
        dto.setOfferingUser(userMapper.toDto(entity.getUser()));
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setStatus(entity.getStatus());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setMessage(entity.getMessage());
        dto.setListing(listingMapper.toDto(entity.getListing()));

        // Map offer items
        if (entity.getOfferItems() != null && !entity.getOfferItems().isEmpty()) {
            List<OfferItemDTO> offerItemDTOs = entity.getOfferItems().stream()
                    .map(this::toOfferItemDto)
                    .collect(Collectors.toList());
            dto.setOfferItems(offerItemDTOs);
        }

        return dto;
    }

    private OfferItemDTO toOfferItemDto(OfferItemEntity entity) {
        if (entity == null || entity.getItem() == null) {
            return null;
        }

        return new OfferItemDTO(
                entity.getItem().getId(),
                entity.getItem().getName(),
                entity.getItem().getImagePath(),
                entity.getItem().getCondition(),
                entity.getQuantity());
    }
}