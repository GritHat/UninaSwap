package com.uninaswap.common.message;

import com.uninaswap.common.dto.NotificationDTO;
import java.util.List;

/**
 * 
 */
public class NotificationMessage extends Message {
    
    /**
     * 
     */
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
    
    /**
     * 
     */
    private NotificationMessageType type;
    /**
     * 
     */
    private List<NotificationDTO> notifications;
    /**
     * 
     */
    private NotificationDTO notification;
    /**
     * 
     */
    private String notificationId;
    /**
     * 
     */
    private List<String> notificationIds;
    /**
     * 
     */
    private int unreadCount;
    /**
     * 
     */
    private int page;
    /**
     * 
     */
    private int size;
    /**
     * 
     */
    private String notificationType; 
    
    /**
     * 
     */
    public NotificationMessage() {
        super();
        setMessageType("notification");
    }
    
    
    /**
     * @return
     */
    public NotificationMessageType getType() { return type; }
    /**
     * @param type
     */
    public void setType(NotificationMessageType type) { this.type = type; }
    
    /**
     * @return
     */
    public List<NotificationDTO> getNotifications() { return notifications; }
    /**
     * @param notifications
     */
    public void setNotifications(List<NotificationDTO> notifications) { this.notifications = notifications; }
    
    /**
     * @return
     */
    public NotificationDTO getNotification() { return notification; }
    /**
     * @param notification
     */
    public void setNotification(NotificationDTO notification) { this.notification = notification; }
    
    /**
     * @return
     */
    public String getNotificationId() { return notificationId; }
    /**
     * @param notificationId
     */
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    
    /**
     * @return
     */
    public List<String> getNotificationIds() { return notificationIds; }
    /**
     * @param notificationIds
     */
    public void setNotificationIds(List<String> notificationIds) { this.notificationIds = notificationIds; }
    
    /**
     * @return
     */
    public int getUnreadCount() { return unreadCount; }
    /**
     * @param unreadCount
     */
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    
    /**
     * @return
     */
    public int getPage() { return page; }
    /**
     * @param page
     */
    public void setPage(int page) { this.page = page; }
    
    /**
     * @return
     */
    public int getSize() { return size; }
    /**
     * @param size
     */
    public void setSize(int size) { this.size = size; }
    
    /**
     * @return
     */
    public String getNotificationType() { return notificationType; }
    /**
     * @param notificationType
     */
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
}