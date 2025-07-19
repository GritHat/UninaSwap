package com.uninaswap.server.mapper;

import com.uninaswap.server.entity.NotificationEntity;
import com.uninaswap.common.dto.NotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    
    @Autowired
    private UserMapper userMapper;
    
    public NotificationDTO toDto(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setRecipient(userMapper.toDto(entity.getRecipient()));
        dto.setType(entity.getType().getValue());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setData(entity.getData());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setReadAt(entity.getReadAt());
        
        return dto;
    }
    
    public NotificationEntity toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }
        
        NotificationEntity entity = new NotificationEntity();
        entity.setId(dto.getId());
        // Note: recipient should be set by the service using proper entity lookup
        entity.setType(com.uninaswap.common.enums.NotificationType.valueOf(dto.getType()));
        entity.setTitle(dto.getTitle());
        entity.setMessage(dto.getMessage());
        entity.setData(dto.getData());
        entity.setRead(dto.isRead());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setReadAt(dto.getReadAt());
        
        return entity;
    }
}