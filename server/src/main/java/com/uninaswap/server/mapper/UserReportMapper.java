package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.UserReportDTO;
import com.uninaswap.server.entity.UserReportEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserReportMapper {

    @Autowired
    private UserMapper userMapper;

    public UserReportDTO toDto(UserReportEntity entity) {
        if (entity == null) {
            return null;
        }

        UserReportDTO dto = new UserReportDTO();
        dto.setId(entity.getId());
        dto.setReportingUser(userMapper.toDto(entity.getReportingUser()));
        dto.setReportedUser(userMapper.toDto(entity.getReportedUser()));
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

    public UserReportEntity toEntity(UserReportDTO dto) {
        if (dto == null) {
            return null;
        }

        UserReportEntity entity = new UserReportEntity();
        entity.setReason(dto.getReason());
        entity.setDescription(dto.getDescription());
        entity.setReviewed(dto.isReviewed());
        entity.setReviewedAt(dto.getReviewedAt());
        entity.setAdminNotes(dto.getAdminNotes());

        
        
        

        return entity;
    }
}