package com.uninaswap.server.mapper;

import org.springframework.stereotype.Component;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.server.entity.UserEntity;

@Component
public class UserMapper {
    public UserDTO toDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setBio(entity.getBio());
        dto.setProfileImagePath(entity.getProfileImagePath());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastLoginAt(entity.getLastLoginAt());
        dto.setActive(entity.isActive());
        dto.setPhoneNumber(entity.getPhoneNumber());
        dto.setAddress(entity.getAddress());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());

        return dto;
    }
}