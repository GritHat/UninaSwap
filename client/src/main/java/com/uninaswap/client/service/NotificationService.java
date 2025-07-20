package com.uninaswap.client.service;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.message.NotificationMessage;
import com.uninaswap.common.dto.NotificationDTO;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.NotificationViewModel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {
    private static NotificationService instance;
    
    private final WebSocketClient webSocketClient;
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    
    // Observable lists for UI binding
    private final ObservableList<NotificationViewModel> allNotifications = FXCollections.observableArrayList();
    private final ObservableList<NotificationViewModel> recentNotifications = FXCollections.observableArrayList();
    
    // Current state
    private int unreadCount = 0;
    private Consumer<Integer> unreadCountCallback;
    private Consumer<NotificationViewModel> newNotificationCallback;
    
    private NotificationService() {
        webSocketClient = WebSocketClient.getInstance();
        // Register message handlers
        webSocketClient.registerMessageHandler(NotificationMessage.class, this::handleNotificationMessage);
    }
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Get all notifications with pagination
     */
    public CompletableFuture<List<NotificationDTO>> getNotifications(int page, int size) {
        CompletableFuture<List<NotificationDTO>> future = new CompletableFuture<>();
        
        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationMessage.NotificationMessageType.GET_NOTIFICATIONS_REQUEST);
        message.setPage(page);
        message.setSize(size);
        
        // Store future to complete when response arrives
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Get notifications by type
     */
    public CompletableFuture<List<NotificationDTO>> getNotificationsByType(String type, int page, int size) {
        CompletableFuture<List<NotificationDTO>> future = new CompletableFuture<>();
        
        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationMessage.NotificationMessageType.GET_NOTIFICATIONS_REQUEST);
        message.setNotificationType(type);
        message.setPage(page);
        message.setSize(size);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Mark notification as read
     */
    public CompletableFuture<Boolean> markAsRead(String notificationId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationMessage.NotificationMessageType.MARK_AS_READ_REQUEST);
        message.setNotificationId(notificationId);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Mark all notifications as read
     */
    public CompletableFuture<Boolean> markAllAsRead() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationMessage.NotificationMessageType.MARK_ALL_AS_READ_REQUEST);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Get unread notification count
     */
    public CompletableFuture<Integer> getUnreadCountFromServer() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        
        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationMessage.NotificationMessageType.GET_UNREAD_COUNT_REQUEST);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Refresh recent notifications for dropdown
     */
    public void refreshRecentNotifications() {
        getNotifications(0, 10).thenAccept(notifications -> {
            Platform.runLater(() -> {
                recentNotifications.clear();
                recentNotifications.addAll(notifications.stream()
                    .map(viewModelMapper::toViewModel)
                    .collect(Collectors.toList()));
            });
        });
    }
    
    /**
     * Initialize notifications when user logs in
     */
    public void initializeNotifications() {
        System.out.println("Initializing notifications for logged-in user...");
        
        // Load initial notifications
        refreshRecentNotifications();
        
        // Get initial unread count
        getUnreadCountFromServer()
            .thenAccept(count -> Platform.runLater(() -> {
                updateUnreadCount(count);
                System.out.println("Initial unread count: " + count);
            }))
            .exceptionally(ex -> {
                System.err.println("Failed to get initial unread count: " + ex.getMessage());
                return null;
            });
        
        // Load all notifications for the notification center
        getNotifications(0, 100)
            .thenAccept(notifications -> {
                System.out.println("Loaded " + notifications.size() + " total notifications");
            })
            .exceptionally(ex -> {
                System.err.println("Failed to load initial notifications: " + ex.getMessage());
                return null;
            });
    }
    
    /**
     * Clear notifications when user logs out
     */
    public void clearNotifications() {
        Platform.runLater(() -> {
            allNotifications.clear();
            recentNotifications.clear();
            updateUnreadCount(0);
            System.out.println("Cleared all notifications on logout");
        });
    }
    
    // Handle incoming WebSocket messages
    private final java.util.Map<String, CompletableFuture<?>> pendingFutures = new java.util.concurrent.ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    private void handleNotificationMessage(NotificationMessage message) {
        switch (message.getType()) {
            case GET_NOTIFICATIONS_RESPONSE -> {
                CompletableFuture<List<NotificationDTO>> future = 
                    (CompletableFuture<List<NotificationDTO>>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    if (message.isSuccess()) {
                        Platform.runLater(() -> {
                            // Update observable list
                            allNotifications.clear();
                            allNotifications.addAll(message.getNotifications().stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList()));
                        });
                        future.complete(message.getNotifications());
                    } else {
                        future.completeExceptionally(new Exception(message.getErrorMessage()));
                    }
                }
            }
            
            case MARK_AS_READ_RESPONSE -> {
                CompletableFuture<Boolean> future = 
                    (CompletableFuture<Boolean>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    future.complete(message.isSuccess());
                }
                
                if (message.isSuccess()) {
                    Platform.runLater(() -> {
                        // Update local notification state
                        updateNotificationReadState(message.getNotificationId(), true);
                        
                        // Update unread count if provided in response
                        if (message.getUnreadCount() >= 0) {
                            updateUnreadCount(message.getUnreadCount());
                            System.out.println("Updated unread count after marking as read: " + message.getUnreadCount());
                        }
                    });
                }
            }
            
            case MARK_ALL_AS_READ_RESPONSE -> {
                CompletableFuture<Boolean> future = 
                    (CompletableFuture<Boolean>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    future.complete(message.isSuccess());
                }
                
                if (message.isSuccess()) {
                    Platform.runLater(() -> {
                        // Mark all local notifications as read
                        allNotifications.forEach(n -> n.setRead(true));
                        recentNotifications.forEach(n -> n.setRead(true));
                        updateUnreadCount(0);
                    });
                }
            }
            
            case GET_UNREAD_COUNT_RESPONSE -> {
                CompletableFuture<Integer> future = 
                    (CompletableFuture<Integer>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    future.complete(message.getUnreadCount());
                }
                
                // Always update the unread count when we receive this response
                Platform.runLater(() -> updateUnreadCount(message.getUnreadCount()));
            }
            
            case NOTIFICATION_RECEIVED -> {
                // Real-time notification received
                Platform.runLater(() -> {
                    NotificationViewModel newNotification = viewModelMapper.toViewModel(message.getNotification());
                    
                    // Add to recent notifications
                    recentNotifications.add(0, newNotification);
                    if (recentNotifications.size() > 10) {
                        recentNotifications.remove(10);
                    }
                    
                    // Add to all notifications if not already present
                    boolean alreadyExists = allNotifications.stream()
                        .anyMatch(n -> n.getId().equals(newNotification.getId()));
                    if (!alreadyExists) {
                        allNotifications.add(0, newNotification);
                    }
                    
                    // Update unread count
                    if (!newNotification.isRead()) {
                        updateUnreadCount(unreadCount + 1);
                    }
                    
                    // Notify callback for real-time UI updates
                    if (newNotificationCallback != null) {
                        newNotificationCallback.accept(newNotification);
                    }
                });
            }
        }
    }
    
    private void updateNotificationReadState(String notificationId, boolean read) {
        // Update in all notifications list
        allNotifications.stream()
            .filter(n -> n.getId().equals(notificationId))
            .findFirst()
            .ifPresent(n -> n.setRead(read));
        
        // Update in recent notifications list
        recentNotifications.stream()
            .filter(n -> n.getId().equals(notificationId))
            .findFirst()
            .ifPresent(n -> n.setRead(read));
    }
    
    private void updateUnreadCount(int newCount) {
        this.unreadCount = newCount;
        if (unreadCountCallback != null) {
            unreadCountCallback.accept(newCount);
        }
    }
    
    // Getters for observable lists
    public ObservableList<NotificationViewModel> getAllNotifications() {
        return allNotifications;
    }
    
    public ObservableList<NotificationViewModel> getRecentNotifications() {
        return recentNotifications;
    }
    
    public int getUnreadCount() {
        return unreadCount;
    }
    
    // Callback setters
    public void setUnreadCountCallback(Consumer<Integer> callback) {
        this.unreadCountCallback = callback;
    }
    
    public void setNewNotificationCallback(Consumer<NotificationViewModel> callback) {
        this.newNotificationCallback = callback;
    }
}