package com.uninaswap.common.message;

import com.uninaswap.common.dto.NotificationDTO;
import java.util.List;

public class NotificationMessage extends Message {
    
    public enum NotificationMessageType {
        GET_NOTIFICATIONS_REQUEST,
        GET_NOTIFICATIONS_RESPONSE,
        MARK_AS_READ_REQUEST,
        MARK_AS_READ_RESPONSE,
        MARK_ALL_AS_READ_REQUEST,
        MARK_ALL_AS_READ_RESPONSE,
        CREATE_NOTIFICATION_REQUEST,
        CREATE_NOTIFICATION_RESPONSE,
        NOTIFICATION_RECEIVED,
        GET_UNREAD_COUNT_REQUEST,
        GET_UNREAD_COUNT_RESPONSE
    }
    
    private NotificationMessageType type;
    private List<NotificationDTO> notifications;
    private NotificationDTO notification;
    private String notificationId;
    private List<String> notificationIds;
    private int unreadCount;
    private int page;
    private int size;
    private String notificationType; // Filter by type
    
    public NotificationMessage() {
        super();
        setMessageType("notification");
    }
    
    // Getters and setters
    public NotificationMessageType getType() { return type; }
    public void setType(NotificationMessageType type) { this.type = type; }
    
    public List<NotificationDTO> getNotifications() { return notifications; }
    public void setNotifications(List<NotificationDTO> notifications) { this.notifications = notifications; }
    
    public NotificationDTO getNotification() { return notification; }
    public void setNotification(NotificationDTO notification) { this.notification = notification; }
    
    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    
    public List<String> getNotificationIds() { return notificationIds; }
    public void setNotificationIds(List<String> notificationIds) { this.notificationIds = notificationIds; }
    
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
}