package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.client.viewmodel.FollowerViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.dto.FollowerDTO;
import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.common.message.FollowerMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FollowerService {
    private static FollowerService instance;

    private final WebSocketClient webSocketClient = WebSocketManager.getClient();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();

    // Observable lists for UI binding
    private final ObservableList<UserViewModel> followingUsers = FXCollections.observableArrayList();
    private final ObservableList<UserViewModel> followerUsers = FXCollections.observableArrayList();
    private final ObservableList<FollowerViewModel> followers = FXCollections.observableArrayList();

    // Local tracking for quick UI updates
    private final Set<Long> followingIds = new HashSet<>();
    private long followingCount = 0;
    private long followerCount = 0;

    private CompletableFuture<?> futureToComplete;
    private Consumer<FollowerMessage> messageCallback;

    private FollowerService() {
        // Register message handler
        webSocketClient.registerMessageHandler(FollowerMessage.class, this::handleFollowerMessage);
    }

    public static synchronized FollowerService getInstance() {
        if (instance == null) {
            instance = new FollowerService();
        }
        return instance;
    }

    /**
     * Follow a user
     */
    public CompletableFuture<FollowerViewModel> followUser(Long followedId) {
        CompletableFuture<FollowerViewModel> future = new CompletableFuture<>();

        FollowerMessage message = new FollowerMessage();
        message.setType(FollowerMessage.Type.FOLLOW_USER_REQUEST);
        message.setFollowedId(followedId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Unfollow a user
     */
    public CompletableFuture<Boolean> unfollowUser(Long followedId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FollowerMessage message = new FollowerMessage();
        message.setType(FollowerMessage.Type.UNFOLLOW_USER_REQUEST);
        message.setFollowedId(followedId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get users that current user is following
     */
    public CompletableFuture<List<UserDTO>> getFollowing() {
        CompletableFuture<List<UserDTO>> future = new CompletableFuture<>();

        FollowerMessage message = new FollowerMessage();
        message.setType(FollowerMessage.Type.GET_FOLLOWING_REQUEST);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get followers of current user
     */
    public CompletableFuture<List<UserDTO>> getFollowers() {
        CompletableFuture<List<UserDTO>> future = new CompletableFuture<>();

        FollowerMessage message = new FollowerMessage();
        message.setType(FollowerMessage.Type.GET_FOLLOWERS_REQUEST);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Check if current user is following another user
     */
    public CompletableFuture<Boolean> isFollowing(Long userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FollowerMessage message = new FollowerMessage();
        message.setType(FollowerMessage.Type.IS_FOLLOWING_REQUEST);
        message.setFollowedId(userId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get follow statistics for current user
     */
    public CompletableFuture<Void> getFollowStats() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        FollowerMessage message = new FollowerMessage();
        message.setType(FollowerMessage.Type.GET_FOLLOW_STATS_REQUEST);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Toggle follow status
     */
    public CompletableFuture<Boolean> toggleFollow(Long userId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        FollowerMessage message = new FollowerMessage();
        message.setType(FollowerMessage.Type.TOGGLE_FOLLOW_REQUEST);
        message.setFollowedId(userId);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    // Handle incoming messages
    @SuppressWarnings("unchecked")
    private void handleFollowerMessage(FollowerMessage message) {
        if (message.getType() == null) {
            System.err.println("Received follower message with null type: " + message.getErrorMessage());
            if (!message.isSuccess() && futureToComplete != null) {
                futureToComplete.completeExceptionally(
                        new Exception("Server error: " + message.getErrorMessage()));
                futureToComplete = null;
            }
            return;
        }

        switch (message.getType()) {
            case FOLLOW_USER_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        FollowerViewModel followerViewModel = viewModelMapper.toViewModel(message.getFollower());
                        followers.add(followerViewModel);
                        followingIds.add(message.getFollowedId());
                        followingCount++;

                        if (futureToComplete != null) {
                            ((CompletableFuture<FollowerViewModel>) futureToComplete).complete(followerViewModel);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to follow user: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case UNFOLLOW_USER_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        followingIds.remove(message.getFollowedId());
                        followingCount = Math.max(0, followingCount - 1);

                        // Remove from following list
                        followingUsers.removeIf(user -> user.getId().equals(message.getFollowedId()));

                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(true);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to unfollow user: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_FOLLOWING_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<UserDTO> following = message.getFollowing() != null ? message.getFollowing()
                                : new ArrayList<>();
                        List<UserViewModel> followingViewModels = following.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        followingUsers.setAll(followingViewModels);

                        // Update local tracking
                        followingIds.clear();
                        followingIds.addAll(following.stream().map(UserDTO::getId).collect(Collectors.toSet()));
                        followingCount = following.size();

                        if (futureToComplete != null) {
                            ((CompletableFuture<List<UserDTO>>) futureToComplete).complete(following);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get following: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_FOLLOWERS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<UserDTO> followers = message.getFollowerUsers() != null ? message.getFollowerUsers()
                                : new ArrayList<>();
                        List<UserViewModel> followerViewModels = followers.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        followerUsers.setAll(followerViewModels);

                        followerCount = followers.size();

                        if (futureToComplete != null) {
                            ((CompletableFuture<List<UserDTO>>) futureToComplete).complete(followers);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get followers: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case IS_FOLLOWING_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(message.isFollowing());
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to check following status: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_FOLLOW_STATS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        followingCount = message.getFollowingCount();
                        followerCount = message.getFollowerCount();

                        if (futureToComplete != null) {
                            ((CompletableFuture<Void>) futureToComplete).complete(null);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get follow stats: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case TOGGLE_FOLLOW_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        boolean nowFollowing = message.isFollowing();

                        if (nowFollowing) {
                            followingIds.add(message.getFollowedId());
                            followingCount++;
                        } else {
                            followingIds.remove(message.getFollowedId());
                            followingCount = Math.max(0, followingCount - 1);
                        }

                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(nowFollowing);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to toggle follow: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            default:
                System.out.println("Unhandled follower message type: " + message.getType());
                break;
        }

        // Call any registered callback
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    // === LOCAL METHODS FOR QUICK UI UPDATES ===

    /**
     * Check if user is following someone (local check)
     */
    public boolean isFollowingLocally(Long userId) {
        return followingIds.contains(userId);
    }

    /**
     * Add user to local following set (for optimistic UI updates)
     */
    public void addToLocalFollowing(Long userId) {
        followingIds.add(userId);
    }

    /**
     * Remove user from local following set (for optimistic UI updates)
     */
    public void removeFromLocalFollowing(Long userId) {
        followingIds.remove(userId);
    }

    // === GETTERS FOR OBSERVABLE LISTS ===

    public ObservableList<UserViewModel> getFollowingUsersList() {
        return followingUsers;
    }

    public ObservableList<UserViewModel> getFollowerUsersList() {
        return followerUsers;
    }

    public ObservableList<FollowerViewModel> getFollowersList() {
        return followers;
    }

    // === GETTERS FOR COUNTS ===

    public long getFollowingCount() {
        return followingCount;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public Set<Long> getFollowingIds() {
        return new HashSet<>(followingIds);
    }

    // === UTILITY METHODS ===

    public void clearData() {
        followingUsers.clear();
        followerUsers.clear();
        followers.clear();
        followingIds.clear();
        followingCount = 0;
        followerCount = 0;
    }

    // Set a callback for incoming messages
    public void setMessageCallback(Consumer<FollowerMessage> callback) {
        this.messageCallback = callback;
    }
}