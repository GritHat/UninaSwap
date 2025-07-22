package com.uninaswap.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * DTO representing a user's follower relationship
 */
public class FollowerDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long followerId;
    private Long followedId;
    private UserDTO follower;
    private UserDTO followed;
    private LocalDateTime createdAt;

    // Default constructor
    public FollowerDTO() {
    }

    // Constructor
    public FollowerDTO(Long followerId, Long followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserDTO getFollower() {
        return follower;
    }

    public void setFollower(UserDTO follower) {
        this.follower = follower;
    }

    public UserDTO getFollowed() {
        return followed;
    }

    public void setFollowed(UserDTO followed) {
        this.followed = followed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}