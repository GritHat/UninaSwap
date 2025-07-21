package com.uninaswap.server.service;

import com.uninaswap.common.dto.FollowerDTO;
import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.server.entity.FollowerEntity;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.mapper.FollowerMapper;
import com.uninaswap.server.mapper.UserMapper;
import com.uninaswap.server.repository.FollowerRepository;
import com.uninaswap.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowerService {

    private static final Logger logger = LoggerFactory.getLogger(FollowerService.class);

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowerMapper followerMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * Follow a user
     */
    @Transactional
    public FollowerDTO followUser(Long followerId, Long followedId) {
        logger.info("User {} following user {}", followerId, followedId);

        // Validate users are different
        if (followerId.equals(followedId)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }

        // Check if already following
        if (followerRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw new IllegalStateException("Already following this user");
        }

        // Get users
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found: " + followerId));

        UserEntity followed = userRepository.findById(followedId)
                .orElseThrow(() -> new IllegalArgumentException("User to follow not found: " + followedId));

        // Create follow relationship
        FollowerEntity followerEntity = new FollowerEntity(follower, followed);
        followerEntity = followerRepository.save(followerEntity);

        logger.info("Successfully created follow relationship: {} -> {}", followerId, followedId);
        return followerMapper.toDto(followerEntity);
    }

    /**
     * Unfollow a user
     */
    @Transactional
    public void unfollowUser(Long followerId, Long followedId) {
        logger.info("User {} unfollowing user {}", followerId, followedId);

        followerRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);

        logger.info("Successfully removed follow relationship: {} -> {}", followerId, followedId);
    }

    /**
     * Check if user A is following user B
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return followerRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    /**
     * Get users that a user is following
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getFollowing(Long userId) {
        logger.info("Getting users followed by: {}", userId);

        List<UserEntity> following = followerRepository.findFollowingByUserId(userId);

        return following.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated users that a user is following
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowing(Long userId, int page, int size) {
        logger.info("Getting paginated users followed by: {} (page {}, size {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<FollowerEntity> followingPage = followerRepository.findFollowingByUserIdOrderByCreatedAtDesc(userId,
                pageable);

        return followingPage.map(follower -> userMapper.toDto(follower.getFollowed()));
    }

    /**
     * Get followers of a user
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getFollowers(Long userId) {
        logger.info("Getting followers of user: {}", userId);

        List<UserEntity> followers = followerRepository.findFollowersByUserId(userId);

        return followers.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated followers of a user
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getFollowers(Long userId, int page, int size) {
        logger.info("Getting paginated followers of user: {} (page {}, size {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<FollowerEntity> followersPage = followerRepository.findFollowersByUserIdOrderByCreatedAtDesc(userId,
                pageable);

        return followersPage.map(follower -> userMapper.toDto(follower.getFollower()));
    }

    /**
     * Get following user IDs for a user
     */
    @Transactional(readOnly = true)
    public Set<Long> getFollowingIds(Long userId) {
        logger.info("Getting following IDs for user: {}", userId);

        List<Long> followingIds = followerRepository.findFollowingIdsByUserId(userId);
        return followingIds.stream().collect(Collectors.toSet());
    }

    /**
     * Get follower IDs of a user
     */
    @Transactional(readOnly = true)
    public Set<Long> getFollowerIds(Long userId) {
        logger.info("Getting follower IDs for user: {}", userId);

        List<Long> followerIds = followerRepository.findFollowerIdsByUserId(userId);
        return followerIds.stream().collect(Collectors.toSet());
    }

    /**
     * Count how many users a user is following
     */
    @Transactional(readOnly = true)
    public long countFollowing(Long userId) {
        return followerRepository.countByFollowerId(userId);
    }

    /**
     * Count how many followers a user has
     */
    @Transactional(readOnly = true)
    public long countFollowers(Long userId) {
        return followerRepository.countByFollowedId(userId);
    }

    /**
     * Get mutual following between two users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getMutualFollowing(Long userId1, Long userId2) {
        logger.info("Getting mutual following between users: {} and {}", userId1, userId2);

        List<UserEntity> mutualFollowing = followerRepository.findMutualFollowing(userId1, userId2);

        return mutualFollowing.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get most followed users
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMostFollowedUsers(int limit) {
        logger.info("Getting top {} most followed users", limit);

        Pageable pageable = PageRequest.of(0, limit);
        return followerRepository.findMostFollowedUsers(pageable);
    }

    /**
     * Toggle follow status
     */
    @Transactional
    public boolean toggleFollow(Long followerId, Long followedId) {
        logger.info("Toggling follow: {} -> {}", followerId, followedId);

        if (followerRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            unfollowUser(followerId, followedId);
            return false;
        } else {
            followUser(followerId, followedId);
            return true;
        }
    }
}