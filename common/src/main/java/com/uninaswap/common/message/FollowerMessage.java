package com.uninaswap.common.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.uninaswap.common.dto.FollowerDTO;
import com.uninaswap.common.dto.UserDTO;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "messageType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FollowerMessage.class, name = "follower")
})
public class FollowerMessage extends Message {

    public enum Type {
        // Requests
        FOLLOW_USER_REQUEST,
        UNFOLLOW_USER_REQUEST,
        GET_FOLLOWING_REQUEST,
        GET_FOLLOWERS_REQUEST,
        IS_FOLLOWING_REQUEST,
        GET_FOLLOW_STATS_REQUEST,
        TOGGLE_FOLLOW_REQUEST,

        // Responses
        FOLLOW_USER_RESPONSE,
        UNFOLLOW_USER_RESPONSE,
        GET_FOLLOWING_RESPONSE,
        GET_FOLLOWERS_RESPONSE,
        IS_FOLLOWING_RESPONSE,
        GET_FOLLOW_STATS_RESPONSE,
        TOGGLE_FOLLOW_RESPONSE
    }

    private Type type;
    private Long followerId;
    private Long followedId;
    private FollowerDTO follower;
    private List<FollowerDTO> followers;
    private List<UserDTO> following;
    private List<UserDTO> followerUsers;
    private boolean isFollowing;
    private long followingCount = 0;
    private long followerCount = 0;
    private int page = 0;
    private int size = 20;
    private long totalElements = 0;
    private int totalPages = 0;

    // Default constructor
    public FollowerMessage() {
        super();
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public Long getFollowedId() {
        return followedId;
    }

    public void setFollowedId(Long followedId) {
        this.followedId = followedId;
    }

    public FollowerDTO getFollower() {
        return follower;
    }

    public void setFollower(FollowerDTO follower) {
        this.follower = follower;
    }

    public List<FollowerDTO> getFollowers() {
        return followers;
    }

    public void setFollowers(List<FollowerDTO> followers) {
        this.followers = followers;
    }

    public List<UserDTO> getFollowing() {
        return following;
    }

    public void setFollowing(List<UserDTO> following) {
        this.following = following;
    }

    public List<UserDTO> getFollowerUsers() {
        return followerUsers;
    }

    public void setFollowerUsers(List<UserDTO> followerUsers) {
        this.followerUsers = followerUsers;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}