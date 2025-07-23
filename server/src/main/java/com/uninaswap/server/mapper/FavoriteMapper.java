package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.FavoriteDTO;
import com.uninaswap.server.entity.FavoriteEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ListingMapper listingMapper;

    public FavoriteDTO toDto(FavoriteEntity entity) {
        if (entity == null) {
            return null;
        }

        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setListingId(entity.getListing().getId());
        dto.setCreatedAt(entity.getCreatedAt());

        
        if (entity.getUser() != null) {
            dto.setUser(userMapper.toDto(entity.getUser()));
        }

        if (entity.getListing() != null) {
            dto.setListing(listingMapper.toDto(entity.getListing()));
        }

        return dto;
    }

    public FavoriteDTO toDtoWithoutDetails(FavoriteEntity entity) {
        if (entity == null) {
            return null;
        }

        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setListingId(entity.getListing().getId());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }
}