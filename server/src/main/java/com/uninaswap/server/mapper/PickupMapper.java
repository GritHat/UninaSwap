package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.server.entity.PickupEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PickupMapper {

    @Autowired
    private OfferMapper offerMapper;

    @Autowired
    private UserMapper userMapper;

    public PickupDTO toDto(PickupEntity entity) {
        if (entity == null) {
            return null;
        }

        PickupDTO dto = new PickupDTO();
        dto.setId(entity.getId());
        dto.setOfferId(entity.getOffer().getId());
        dto.setAvailableDates(entity.getAvailableDates());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSelectedDate(entity.getSelectedDate());
        dto.setSelectedTime(entity.getSelectedTime());
        dto.setLocation(entity.getLocation());
        dto.setDetails(entity.getDetails());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // Map relationships if needed
        if (entity.getOffer() != null) {
            dto.setOffer(offerMapper.toDto(entity.getOffer()));
        }

        if (entity.getCreatedBy() != null) {
            dto.setCreatedBy(userMapper.toDto(entity.getCreatedBy()));
        }

        if (entity.getUpdatedBy() != null) {
            dto.setUpdatedBy(userMapper.toDto(entity.getUpdatedBy()));
        }

        return dto;
    }

    public PickupDTO toDtoWithoutDetails(PickupEntity entity) {
        if (entity == null) {
            return null;
        }

        PickupDTO dto = new PickupDTO();
        dto.setId(entity.getId());
        dto.setOfferId(entity.getOffer().getId());
        dto.setAvailableDates(entity.getAvailableDates());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setSelectedDate(entity.getSelectedDate());
        dto.setSelectedTime(entity.getSelectedTime());
        dto.setLocation(entity.getLocation());
        dto.setDetails(entity.getDetails());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}