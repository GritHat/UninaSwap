package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.FollowerViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
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

/**
 * 
 */
public class FollowerService {
    /**
     * 
     */
    private static FollowerService instance;

    /**
     * 
     */
    private final WebSocketClient webSocketClient = WebSocketClient.getInstance();
    /**
     * 
     */
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    /**
     * 
     */
    private final ObservableList<UserViewModel> followingUsers = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<UserViewModel> followerUsers = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<FollowerViewModel> followers = FXCollections.observableArrayList();

    /**
     * 
     */
    private final Set<Long> followingIds = new HashSet<>();
    /**
     * 
     */
    private long followingCount = 0;
    /**
     * 
     */
    private long followerCount = 0;

    /**
     * 
     */
    private CompletableFuture<?> futureToComplete;
    /**
     * 
     */
    private Consumer<FollowerMessage> messageCallback;

    /**
     * 
     */
    private FollowerService() {
        webSocketClient.registerMessageHandler(FollowerMessage.class, this::handleFollowerMessage);
    }

    /**
     * @return
     */
    public static synchronized FollowerService getInstance() {
        if (instance == null) {
            instance = new FollowerService();
        }
        return instance;
    }

    /**
     * Follow a user
     * 
     * @param followedId The ID of the user to follow
     * @return A CompletableFuture that completes with the FollowerViewModel if the
     *         follow is successful, or fails with an exception if the connection
     *         fails or the follow request is rejected.
     */
    /**
     * @param followedId
     * @return
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
     * 
     * @param followedId The ID of the user to unfollow
     * @return A CompletableFuture that completes with true if the unfollow is
     *         successful, or fails with an exception if the connection fails or the
     *         unfollow request is rejected.
     *         If the user is not following the other user, it will complete with
     *         false.
     */
    /**
     * @param followedId
     * @return
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
     * 
     * @return A CompletableFuture that completes with a list of UserDTOs representing
     *         the users that the current user is following, or fails with an exception
     *         if the connection fails or the request is rejected.
     *         The list will be empty if the user is not following anyone.
     */
    /**
     * @return
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
     * 
     * @return A CompletableFuture that completes with a list of UserDTOs representing
     *         the followers of the current user, or fails with an exception if the
     *         connection fails or the request is rejected.
     *         The list will be empty if the user has no followers.
     *         If the user is not logged in, it will complete with an empty list.
     *         If the user is logged in but has no followers, it will complete with an
     *         empty list.
     *         If the user is logged in and has followers, it will complete with a list
     *         of UserDTOs representing the followers.
     */
    /**
     * @return
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
     * 
     * @param userId The ID of the user to check
     * @return A CompletableFuture that completes with true if the current user is
     *         following the user, false if not, or fails with an exception if the
     *         connection fails or the request is rejected.
     */
    /**
     * @param userId
     * @return
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
     * 
     * @return A CompletableFuture that completes with the follow statistics, or fails
     *         with an exception if the connection fails or the request is rejected.
     *         The statistics include the number of followers and the number of users
     *         the current user is following.
     */
    /**
     * @return
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
     * 
     * @param userId The ID of the user to toggle follow status for
     * @return A CompletableFuture that completes with true if the user is now
     *         following the other user, false if not, or fails with an exception if
     *         the connection fails or the request is rejected.
     */
    /**
     * @param userId
     * @return
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

    /**
     * @param message
     */
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
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    /**
     * Check if user is following someone (local check)
     * 
     * @param userId The ID of the user to check
     * @return true if the user is following the other user, false if not.
     *         This is a local check and does not require a server request.
     *         It checks the local followingIds set.
     */
    /**
     * @param userId
     * @return
     */
    public boolean isFollowingLocally(Long userId) {
        return followingIds.contains(userId);
    }

    /**
     * Add user to local following set (for optimistic UI updates)
     * 
     * @param userId The ID of the user to add
     */
    /**
     * @param userId
     */
    public void addToLocalFollowing(Long userId) {
        followingIds.add(userId);
    }

    /**
     * Remove user from local following set (for optimistic UI updates)
     * 
     * @param userId The ID of the user to remove
     */
    /**
     * @param userId
     */
    public void removeFromLocalFollowing(Long userId) {
        followingIds.remove(userId);
    }

    /**
     * @return
     */
    public ObservableList<UserViewModel> getFollowingUsersList() {
        return followingUsers;
    }

    /**
     * @return
     */
    public ObservableList<UserViewModel> getFollowerUsersList() {
        return followerUsers;
    }

    /**
     * @return
     */
    public ObservableList<FollowerViewModel> getFollowersList() {
        return followers;
    }

    /**
     * @return
     */
    public long getFollowingCount() {
        return followingCount;
    }

    /**
     * @return
     */
    public long getFollowerCount() {
        return followerCount;
    }

    /**
     * @return
     */
    public Set<Long> getFollowingIds() {
        return new HashSet<>(followingIds);
    }

    /**
     * 
     */
    public void clearData() {
        followingUsers.clear();
        followerUsers.clear();
        followers.clear();
        followingIds.clear();
        followingCount = 0;
        followerCount = 0;
    }

    /**
     * @param callback
     */
    public void setMessageCallback(Consumer<FollowerMessage> callback) {
        this.messageCallback = callback;
    }
}