package com.uninaswap.server.service;

import com.uninaswap.server.entity.NotificationEntity;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.repository.NotificationRepository;
import com.uninaswap.server.repository.UserRepository;
import com.uninaswap.server.mapper.NotificationMapper;
import com.uninaswap.common.dto.NotificationDTO;
import com.uninaswap.common.enums.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Create and send a notification to a user
     */
    public NotificationDTO createNotification(Long recipientId, NotificationType type, String title, String message) {
        return createNotification(recipientId, type, title, message, null);
    }
    
    /**
     * Create and send a notification with additional data
     */
    public NotificationDTO createNotification(Long recipientId, NotificationType type, String title, String message, String data) {
        logger.info("creating new notification for " + recipientId);
        UserEntity recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + recipientId));
        
        NotificationEntity notification = new NotificationEntity(recipient, type, title, message, data);
        notification = notificationRepository.save(notification);
        
        NotificationDTO notificationDTO = notificationMapper.toDto(notification);
        
        
        eventPublisher.publishEvent(new NotificationCreatedEvent(recipientId, notificationDTO));
        
        return notificationDTO;
    }
    
    /**
     * Get notifications for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEntity> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(notificationMapper::toDto);
    }
    
    /**
     * Get notifications by type for a user
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByType(Long userId, NotificationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEntity> notifications = notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        
        return notifications.map(notificationMapper::toDto);
    }
    
    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUnreadNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationEntity> notifications = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId, pageable);
        
        return notifications.map(notificationMapper::toDto);
    }
    
    /**
     * Get recent notifications (last 24 hours) for dropdown
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> getRecentNotifications(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<NotificationEntity> notifications = notificationRepository.findRecentNotifications(userId, since);
        
        return notifications.stream()
            .limit(10) 
            .map(notificationMapper::toDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }
    
    /**
     * Mark a notification as read
     */
    public boolean markAsRead(String notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
        
        if (updated > 0) {
            
            long unreadCount = getUnreadCount(userId);
            eventPublisher.publishEvent(new UnreadCountChangedEvent(userId, unreadCount));
            return true;
        }
        return false;
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public int markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        
        if (updated > 0) {
            
            eventPublisher.publishEvent(new UnreadCountChangedEvent(userId, 0L));
        }
        
        return updated;
    }
    
    /**
     * Mark notifications by type as read
     */
    public int markTypeAsRead(Long userId, NotificationType type) {
        int updated = notificationRepository.markTypeAsRead(userId, type, LocalDateTime.now());
        
        if (updated > 0) {
            
            long unreadCount = getUnreadCount(userId);
            eventPublisher.publishEvent(new UnreadCountChangedEvent(userId, unreadCount));
        }
        
        return updated;
    }
    
    /**
     * Delete notification
     */
    public boolean deleteNotification(String notificationId, Long userId) {
        Optional<NotificationEntity> notification = notificationRepository.findById(notificationId);
        
        if (notification.isPresent() && notification.get().getRecipient().getId().equals(userId)) {
            notificationRepository.delete(notification.get());
            
            
            long unreadCount = getUnreadCount(userId);
            eventPublisher.publishEvent(new UnreadCountChangedEvent(userId, unreadCount));
            
            return true;
        }
        return false;
    }
    
    /**
     * Cleanup old notifications (runs daily)
     */
    @Scheduled(cron = "0 0 2 * * ?") 
    public void cleanupOldNotifications() {
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOldNotifications(cutoffDate);
        
        if (deleted > 0) {
            System.out.println("Cleaned up " + deleted + " old notifications");
        }
    }
    
    
    
    public NotificationDTO createOfferReceivedNotification(Long recipientId, String listingTitle, String offerAmount) {
        String title = "New Offer Received";
        String message = String.format("You received a new offer of %s for '%s'", offerAmount, listingTitle);
        return createNotification(recipientId, NotificationType.OFFER_RECEIVED, title, message);
    }
    
    public NotificationDTO createOfferAcceptedNotification(Long recipientId, String listingTitle) {
        String title = "Offer Accepted";
        String message = String.format("Your offer for '%s' has been accepted!", listingTitle);
        return createNotification(recipientId, NotificationType.OFFER_ACCEPTED, title, message);
    }
    
    public NotificationDTO createAuctionEndingNotification(Long recipientId, String listingTitle, String timeRemaining) {
        String title = "Auction Ending Soon";
        String message = String.format("The auction for '%s' ends in %s", listingTitle, timeRemaining);
        return createNotification(recipientId, NotificationType.AUCTION_ENDING_SOON, title, message);
    }
    
    public NotificationDTO createSystemNotification(Long recipientId, String title, String message) {
        return createNotification(recipientId, NotificationType.SYSTEM_ANNOUNCEMENT, title, message);
    }
    
    
    public static class NotificationCreatedEvent {
        private final Long recipientId;
        private final NotificationDTO notification;
        
        public NotificationCreatedEvent(Long recipientId, NotificationDTO notification) {
            this.recipientId = recipientId;
            this.notification = notification;
        }
        
        public Long getRecipientId() { return recipientId; }
        public NotificationDTO getNotification() { return notification; }
    }
    
    public static class UnreadCountChangedEvent {
        private final Long userId;
        private final long unreadCount;
        
        public UnreadCountChangedEvent(Long userId, long unreadCount) {
            this.userId = userId;
            this.unreadCount = unreadCount;
        }
        
        public Long getUserId() { return userId; }
        public long getUnreadCount() { return unreadCount; }
    }
}