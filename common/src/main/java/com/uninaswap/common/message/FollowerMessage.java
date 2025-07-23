package com.uninaswap.common.message;

import com.uninaswap.common.dto.FollowerDTO;
import com.uninaswap.common.dto.UserDTO;

import java.util.List;

/**
 * 
 */
public class FollowerMessage extends Message {

    /**
     * 
     */
    public enum Type {
        
        FOLLOW_USER_REQUEST,
        UNFOLLOW_USER_REQUEST,
        GET_FOLLOWING_REQUEST,
        GET_FOLLOWERS_REQUEST,
        IS_FOLLOWING_REQUEST,
        GET_FOLLOW_STATS_REQUEST,
        TOGGLE_FOLLOW_REQUEST,

        
        FOLLOW_USER_RESPONSE,
        UNFOLLOW_USER_RESPONSE,
        GET_FOLLOWING_RESPONSE,
        GET_FOLLOWERS_RESPONSE,
        IS_FOLLOWING_RESPONSE,
        GET_FOLLOW_STATS_RESPONSE,
        TOGGLE_FOLLOW_RESPONSE
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private Long followerId;
    /**
     * 
     */
    private Long followedId;
    /**
     * 
     */
    private FollowerDTO follower;
    /**
     * 
     */
    private List<FollowerDTO> followers;
    /**
     * 
     */
    private List<UserDTO> following;
    /**
     * 
     */
    private List<UserDTO> followerUsers;
    /**
     * 
     */
    private boolean isFollowing;
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
    private int page = 0;
    /**
     * 
     */
    private int size = 20;
    /**
     * 
     */
    private long totalElements = 0;
    /**
     * 
     */
    private int totalPages = 0;

    
    /**
     * 
     */
    public FollowerMessage() {
        super();
        setMessageType("follower");
    }

    
    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return
     */
    public Long getFollowerId() {
        return followerId;
    }

    /**
     * @param followerId
     */
    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    /**
     * @return
     */
    public Long getFollowedId() {
        return followedId;
    }

    /**
     * @param followedId
     */
    public void setFollowedId(Long followedId) {
        this.followedId = followedId;
    }

    /**
     * @return
     */
    public FollowerDTO getFollower() {
        return follower;
    }

    /**
     * @param follower
     */
    public void setFollower(FollowerDTO follower) {
        this.follower = follower;
    }

    /**
     * @return
     */
    public List<FollowerDTO> getFollowers() {
        return followers;
    }

    /**
     * @param followers
     */
    public void setFollowers(List<FollowerDTO> followers) {
        this.followers = followers;
    }

    /**
     * @return
     */
    public List<UserDTO> getFollowing() {
        return following;
    }

    /**
     * @param following
     */
    public void setFollowing(List<UserDTO> following) {
        this.following = following;
    }

    /**
     * @return
     */
    public List<UserDTO> getFollowerUsers() {
        return followerUsers;
    }

    /**
     * @param followerUsers
     */
    public void setFollowerUsers(List<UserDTO> followerUsers) {
        this.followerUsers = followerUsers;
    }

    /**
     * @return
     */
    public boolean isFollowing() {
        return isFollowing;
    }

    /**
     * @param following
     */
    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    /**
     * @return
     */
    public long getFollowingCount() {
        return followingCount;
    }

    /**
     * @param followingCount
     */
    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    /**
     * @return
     */
    public long getFollowerCount() {
        return followerCount;
    }

    /**
     * @param followerCount
     */
    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    /**
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * @param totalElements
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * @return
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}