package com.uninaswap.server.mapper;

import com.uninaswap.common.dto.FollowerDTO;
import com.uninaswap.server.entity.FollowerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FollowerMapper {

    @Autowired
    private UserMapper userMapper;

    public FollowerDTO toDto(FollowerEntity entity) {
        if (entity == null) {
            return null;
        }

        FollowerDTO dto = new FollowerDTO();
        dto.setId(entity.getId());
        dto.setFollowerId(entity.getFollower().getId());
        dto.setFollowedId(entity.getFollowed().getId());
        dto.setCreatedAt(entity.getCreatedAt());

        
        if (entity.getFollower() != null) {
            dto.setFollower(userMapper.toDto(entity.getFollower()));
        }

        if (entity.getFollowed() != null) {
            dto.setFollowed(userMapper.toDto(entity.getFollowed()));
        }

        return dto;
    }

    public FollowerDTO toDtoWithoutDetails(FollowerEntity entity) {
        if (entity == null) {
            return null;
        }

        FollowerDTO dto = new FollowerDTO();
        dto.setId(entity.getId());
        dto.setFollowerId(entity.getFollower().getId());
        dto.setFollowedId(entity.getFollowed().getId());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }
}