package com.uninaswap.server.repository;

import com.uninaswap.server.entity.NotificationEntity;
import com.uninaswap.common.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    
    
    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    
    Page<NotificationEntity> findByRecipientIdAndTypeOrderByCreatedAtDesc(Long recipientId, NotificationType type, Pageable pageable);
    
    
    Page<NotificationEntity> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    
    Page<NotificationEntity> findByRecipientIdAndTypeAndReadFalseOrderByCreatedAtDesc(Long recipientId, NotificationType type, Pageable pageable);
    
    
    long countByRecipientIdAndReadFalse(Long recipientId);
    
    
    long countByRecipientIdAndTypeAndReadFalse(Long recipientId, NotificationType type);
    
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.recipient.id = :recipientId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<NotificationEntity> findRecentNotifications(@Param("recipientId") Long recipientId, @Param("since") LocalDateTime since);
    
    
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.id = :notificationId AND n.recipient.id = :recipientId")
    int markAsRead(@Param("notificationId") String notificationId, @Param("recipientId") Long recipientId, @Param("readAt") LocalDateTime readAt);
    
    
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.recipient.id = :recipientId AND n.read = false")
    int markAllAsRead(@Param("recipientId") Long recipientId, @Param("readAt") LocalDateTime readAt);
    
    
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.read = true, n.readAt = :readAt WHERE n.recipient.id = :recipientId AND n.type = :type AND n.read = false")
    int markTypeAsRead(@Param("recipientId") Long recipientId, @Param("type") NotificationType type, @Param("readAt") LocalDateTime readAt);
    
    
    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.createdAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    
    @Query("SELECT n FROM NotificationEntity n WHERE n.recipient.id IN :recipientIds ORDER BY n.createdAt DESC")
    List<NotificationEntity> findByRecipientIds(@Param("recipientIds") List<Long> recipientIds);
}