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

/**
 * 
 */
public class NotificationService {
    /**
     * 
     */
    private static NotificationService instance;
    /**
     * 
     */
    private final WebSocketClient webSocketClient;
    /**
     * 
     */
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    /**
     * 
     */
    private final ObservableList<NotificationViewModel> allNotifications = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<NotificationViewModel> recentNotifications = FXCollections.observableArrayList();
    /**
     * 
     */
    private int unreadCount = 0;
    /**
     * 
     */
    private Consumer<Integer> unreadCountCallback;
    /**
     * 
     */
    private Consumer<NotificationViewModel> newNotificationCallback;
    
    /**
     * 
     */
    private NotificationService() {
        webSocketClient = WebSocketClient.getInstance();
        webSocketClient.registerMessageHandler(NotificationMessage.class, this::handleNotificationMessage);
    }
    
    /**
     * @return
     */
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Get all notifications with pagination
     * 
     * @param page The page number to retrieve (0-based)
     * @param size The number of notifications per page
     * @return A CompletableFuture that will complete with the list of notifications
     *         or an empty list if no notifications are available.
     *         The future will complete exceptionally if an error occurs.
     */
    /**
     * @param page
     * @param size
     * @return
     */
    public CompletableFuture<List<NotificationDTO>> getNotifications(int page, int size) {
        CompletableFuture<List<NotificationDTO>> future = new CompletableFuture<>();
        
        NotificationMessage message = new NotificationMessage();
        message.setType(NotificationMessage.NotificationMessageType.GET_NOTIFICATIONS_REQUEST);
        message.setPage(page);
        message.setSize(size);
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Get notifications by type
     * 
     * @param type The type of notifications to retrieve
     * @param page The page number to retrieve (0-based)
     * @param size The number of notifications per page
     * @return A CompletableFuture that will complete with the list of notifications
     *         or an empty list if no notifications are available.
     *         The future will complete exceptionally if an error occurs.
     */
    /**
     * @param type
     * @param page
     * @param size
     * @return
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
     * 
     * @param notificationId The ID of the notification to mark as read
     * @return A CompletableFuture that will complete with true if successful,
     *         or false if the notification was not found or could not be marked as read.
     *         The future will complete exceptionally if an error occurs.
     */
    /**
     * @param notificationId
     * @return
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
     * 
     * @return A CompletableFuture that will complete with true if successful,
     *         or false if there were no notifications to mark as read.
     *         The future will complete exceptionally if an error occurs.
     */
    /**
     * @return
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
     * 
     * @return A CompletableFuture that will complete with the count of unread notifications.
     *         The future will complete exceptionally if an error occurs.
     */
    /**
     * @return
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
    /**
     * 
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
    /**
     * 
     */
    public void initializeNotifications() {
        System.out.println("Initializing notifications for logged-in user...");
        refreshRecentNotifications();
        getUnreadCountFromServer()
            .thenAccept(count -> Platform.runLater(() -> {
                updateUnreadCount(count);
                System.out.println("Initial unread count: " + count);
            }))
            .exceptionally(ex -> {
                System.err.println("Failed to get initial unread count: " + ex.getMessage());
                return null;
            });
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
    /**
     * 
     */
    public void clearNotifications() {
        Platform.runLater(() -> {
            allNotifications.clear();
            recentNotifications.clear();
            updateUnreadCount(0);
            System.out.println("Cleared all notifications on logout");
        });
    }
    
    /**
     * 
     */
    private final java.util.Map<String, CompletableFuture<?>> pendingFutures = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * @param message
     */
    @SuppressWarnings("unchecked")
    private void handleNotificationMessage(NotificationMessage message) {
        switch (message.getType()) {
            case GET_NOTIFICATIONS_RESPONSE -> {
                CompletableFuture<List<NotificationDTO>> future = 
                    (CompletableFuture<List<NotificationDTO>>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    if (message.isSuccess()) {
                        Platform.runLater(() -> {
                            
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
                        updateNotificationReadState(message.getNotificationId(), true);
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
                
                Platform.runLater(() -> updateUnreadCount(message.getUnreadCount()));
            }
            
            case NOTIFICATION_RECEIVED -> {
                Platform.runLater(() -> {
                    NotificationViewModel newNotification = viewModelMapper.toViewModel(message.getNotification());
                    recentNotifications.add(0, newNotification);
                    if (recentNotifications.size() > 10) {
                        recentNotifications.remove(10);
                    }
                    boolean alreadyExists = allNotifications.stream()
                        .anyMatch(n -> n.getId().equals(newNotification.getId()));
                    if (!alreadyExists) {
                        allNotifications.add(0, newNotification);
                    }
                    if (!newNotification.isRead()) {
                        updateUnreadCount(unreadCount + 1);
                    }
                    if (newNotificationCallback != null) {
                        newNotificationCallback.accept(newNotification);
                    }
                });
            }
        }
    }
    
    /**
     * @param notificationId
     * @param read
     */
    private void updateNotificationReadState(String notificationId, boolean read) {
        allNotifications.stream()
            .filter(n -> n.getId().equals(notificationId))
            .findFirst()
            .ifPresent(n -> n.setRead(read));
        recentNotifications.stream()
            .filter(n -> n.getId().equals(notificationId))
            .findFirst()
            .ifPresent(n -> n.setRead(read));
    }
    
    /**
     * @param newCount
     */
    private void updateUnreadCount(int newCount) {
        this.unreadCount = newCount;
        if (unreadCountCallback != null) {
            unreadCountCallback.accept(newCount);
        }
    }
    
    /**
     * @return
     */
    public ObservableList<NotificationViewModel> getAllNotifications() {
        return allNotifications;
    }
    
    /**
     * @return
     */
    public ObservableList<NotificationViewModel> getRecentNotifications() {
        return recentNotifications;
    }
    
    /**
     * @return
     */
    public int getUnreadCount() {
        return unreadCount;
    }
    
    /**
     * @param callback
     */
    public void setUnreadCountCallback(Consumer<Integer> callback) {
        this.unreadCountCallback = callback;
    }
    
    /**
     * @param callback
     */
    public void setNewNotificationCallback(Consumer<NotificationViewModel> callback) {
        this.newNotificationCallback = callback;
    }
}