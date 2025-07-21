package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.ListingReportDTO;
import com.uninaswap.server.entity.ListingReportEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListingReportMapper {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ListingMapper listingMapper;

    public ListingReportDTO toDto(ListingReportEntity entity) {
        if (entity == null) {
            return null;
        }

        ListingReportDTO dto = new ListingReportDTO();
        dto.setId(entity.getId());
        dto.setReportingUser(userMapper.toDto(entity.getReportingUser()));
        dto.setReportedListing(listingMapper.toDto(entity.getReportedListing()));
        dto.setReason(entity.getReason());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setReviewed(entity.isReviewed());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setReviewedByAdmin(
                entity.getReviewedByAdmin() != null ? userMapper.toDto(entity.getReviewedByAdmin()) : null);
        dto.setAdminNotes(entity.getAdminNotes());

        return dto;
    }

    public ListingReportEntity toEntity(ListingReportDTO dto) {
        if (dto == null) {
            return null;
        }

        ListingReportEntity entity = new ListingReportEntity();
        entity.setReason(dto.getReason());
        entity.setDescription(dto.getDescription());
        entity.setReviewed(dto.isReviewed());
        entity.setReviewedAt(dto.getReviewedAt());
        entity.setAdminNotes(dto.getAdminNotes());

        // Note: reporting user, reported listing, and admin relationships should be set
        // by the service
        // using proper entity lookups, not through the mapper

        return entity;
    }
}