package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.NotificationDTO;
import com.uninaswap.common.message.NotificationMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.service.NotificationService;
import com.uninaswap.server.service.SessionService;
import com.uninaswap.common.enums.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(ListingWebSocketHandler.class);
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();
        NotificationMessage notificationMessage = objectMapper.readValue(payload, NotificationMessage.class);
        
        UserEntity authenticatedUser = sessionService.validateSession(session);
        if (authenticatedUser == null) {
            sendError(session, "Authentication required", notificationMessage.getMessageId());
            return;
        }
        
        try {
            switch (notificationMessage.getType()) {
                case GET_NOTIFICATIONS_REQUEST -> handleGetNotifications(session, notificationMessage, authenticatedUser.getId());
                case MARK_AS_READ_REQUEST -> handleMarkAsRead(session, notificationMessage, authenticatedUser.getId());
                case MARK_ALL_AS_READ_REQUEST -> handleMarkAllAsRead(session, notificationMessage, authenticatedUser.getId());
                case GET_UNREAD_COUNT_REQUEST -> handleGetUnreadCount(session, notificationMessage, authenticatedUser.getId());
                default -> sendError(session, "Unknown notification message type", notificationMessage.getMessageId());
            }
        } catch (Exception e) {
            sendError(session, "Error processing notification request: " + e.getMessage(), notificationMessage.getMessageId());
        }
    }
    
    private void handleGetNotifications(WebSocketSession session, NotificationMessage request, Long userId) throws Exception {
        int page = request.getPage() != 0 ? request.getPage() : 0;
        int size = request.getSize() != 0 ? request.getSize() : 20;
        
        Page<NotificationDTO> notifications;
        
        if (request.getNotificationType() != null && !request.getNotificationType().isEmpty()) {
            NotificationType type = NotificationType.valueOf(request.getNotificationType());
            notifications = notificationService.getNotificationsByType(userId, type, page, size);
        } else {
            notifications = notificationService.getNotifications(userId, page, size);
        }
        
        NotificationMessage response = new NotificationMessage();
        response.setType(NotificationMessage.NotificationMessageType.GET_NOTIFICATIONS_RESPONSE);
        response.setNotifications(notifications.getContent());
        response.setSuccess(true);
        response.setMessageId(request.getMessageId());
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    private void handleMarkAsRead(WebSocketSession session, NotificationMessage request, Long userId) throws Exception {
        String notificationId = request.getNotificationId();
        boolean success = notificationService.markAsRead(notificationId, userId);
        
        NotificationMessage response = new NotificationMessage();
        response.setType(NotificationMessage.NotificationMessageType.MARK_AS_READ_RESPONSE);
        response.setSuccess(success);
        response.setMessageId(request.getMessageId());
        response.setNotificationId(notificationId); 
    
        
        if (success) {
            
            long unreadCount = notificationService.getUnreadCount(userId);
            response.setUnreadCount((int) unreadCount);
        } else {
            response.setErrorMessage("Failed to mark notification as read");
        }
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    private void handleMarkAllAsRead(WebSocketSession session, NotificationMessage request, Long userId) throws Exception {
        notificationService.markAllAsRead(userId);
        
        NotificationMessage response = new NotificationMessage();
        response.setType(NotificationMessage.NotificationMessageType.MARK_ALL_AS_READ_RESPONSE);
        response.setSuccess(true);
        response.setUnreadCount(0);
        response.setMessageId(request.getMessageId());
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    private void handleGetUnreadCount(WebSocketSession session, NotificationMessage request, Long userId) throws Exception {
        long unreadCount = notificationService.getUnreadCount(userId);
        
        NotificationMessage response = new NotificationMessage();
        response.setType(NotificationMessage.NotificationMessageType.GET_UNREAD_COUNT_RESPONSE);
        response.setUnreadCount((int) unreadCount);
        response.setSuccess(true);
        response.setMessageId(request.getMessageId());
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
    
    /**
     * Event listener for new notifications
     */
    @EventListener
    public void handleNotificationCreated(NotificationService.NotificationCreatedEvent event) {
        sendNotificationToUser(event.getRecipientId(), event.getNotification());
    }
    
    /**
     * Event listener for unread count changes
     */
    @EventListener
    public void handleUnreadCountChanged(NotificationService.UnreadCountChangedEvent event) {
        sendUnreadCountUpdate(event.getUserId(), event.getUnreadCount());
    }
    
    /**
     * Send real-time notification to a specific user
     */
    private void sendNotificationToUser(long userId, NotificationDTO notification) {
        WebSocketSession userSession = sessionService.getSessionByUserId(userId);
        logger.info("Sending notification to user {}", userId);
        if (userSession != null && userSession.isOpen()) {
            logger.info("User session is open for user {}", userId);
            try {
                NotificationMessage message = new NotificationMessage();
                message.setType(NotificationMessage.NotificationMessageType.NOTIFICATION_RECEIVED);
                message.setNotification(notification);
                message.setSuccess(true);
                
                String jsonMessage = objectMapper.writeValueAsString(message);
                userSession.sendMessage(new TextMessage(jsonMessage));
                logger.info("Notification sent to user {}", userId);
            } catch (Exception e) {
                System.err.println("Failed to send real-time notification to user " + userId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Send unread count update to a specific user
     */
    private void sendUnreadCountUpdate(long userId, long unreadCount) {
        WebSocketSession userSession = sessionService.getSessionByUserId(userId);
        if (userSession != null && userSession.isOpen()) {
            try {
                NotificationMessage message = new NotificationMessage();
                message.setType(NotificationMessage.NotificationMessageType.GET_UNREAD_COUNT_RESPONSE);
                message.setUnreadCount((int) unreadCount);
                message.setSuccess(true);
                
                String jsonMessage = objectMapper.writeValueAsString(message);
                userSession.sendMessage(new TextMessage(jsonMessage));
            } catch (Exception e) {
                System.err.println("Failed to send unread count update to user " + userId + ": " + e.getMessage());
            }
        }
    }
    
    private void sendError(WebSocketSession session, String errorMessage, String messageId) throws Exception {
        NotificationMessage response = new NotificationMessage();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        response.setMessageId(messageId);
        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }
}