package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.ReviewDTO;
import com.uninaswap.server.entity.ReviewEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    @Autowired
    private UserMapper userMapper;

    public ReviewDTO toDto(ReviewEntity entity) {
        if (entity == null) {
            return null;
        }

        ReviewDTO dto = new ReviewDTO();
        dto.setId(entity.getId());
        dto.setReviewer(userMapper.toDto(entity.getReviewer()));
        dto.setReviewedUser(userMapper.toDto(entity.getReviewedUser()));
        dto.setOfferId(entity.getOffer() != null ? entity.getOffer().getId() : null);
        dto.setScore(entity.getScore());
        dto.setComment(entity.getComment());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public ReviewEntity toEntity(ReviewDTO dto) {
        if (dto == null) {
            return null;
        }

        ReviewEntity entity = new ReviewEntity();
        entity.setScore(dto.getScore());
        entity.setComment(dto.getComment());

        
        
        

        return entity;
    }
}