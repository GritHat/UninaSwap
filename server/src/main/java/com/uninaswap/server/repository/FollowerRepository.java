package com.uninaswap.server.repository;

import com.uninaswap.server.entity.FollowerEntity;
import com.uninaswap.server.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowerRepository extends JpaRepository<FollowerEntity, Long> {

    /**
     * Find follower relationship
     */
    Optional<FollowerEntity> findByFollowerAndFollowed(UserEntity follower, UserEntity followed);

    /**
     * Find follower relationship by IDs
     */
    @Query("SELECT f FROM FollowerEntity f WHERE f.follower.id = :followerId AND f.followed.id = :followedId")
    Optional<FollowerEntity> findByFollowerIdAndFollowedId(@Param("followerId") Long followerId,
            @Param("followedId") Long followedId);

    /**
     * Get all users that a user is following
     */
    @Query("SELECT f.followed FROM FollowerEntity f WHERE f.follower.id = :userId ORDER BY f.createdAt DESC")
    List<UserEntity> findFollowingByUserId(@Param("userId") Long userId);

    /**
     * Get paginated users that a user is following
     */
    @Query("SELECT f FROM FollowerEntity f JOIN FETCH f.followed WHERE f.follower.id = :userId ORDER BY f.createdAt DESC")
    Page<FollowerEntity> findFollowingByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Get all followers of a user
     */
    @Query("SELECT f.follower FROM FollowerEntity f WHERE f.followed.id = :userId ORDER BY f.createdAt DESC")
    List<UserEntity> findFollowersByUserId(@Param("userId") Long userId);

    /**
     * Get paginated followers of a user
     */
    @Query("SELECT f FROM FollowerEntity f JOIN FETCH f.follower WHERE f.followed.id = :userId ORDER BY f.createdAt DESC")
    Page<FollowerEntity> findFollowersByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Check if user A is following user B
     */
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    /**
     * Count how many users a user is following
     */
    long countByFollowerId(Long userId);

    /**
     * Count how many followers a user has
     */
    long countByFollowedId(Long userId);

    /**
     * Delete follower relationship
     */
    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

    /**
     * Get user IDs that a user is following
     */
    @Query("SELECT f.followed.id FROM FollowerEntity f WHERE f.follower.id = :userId")
    List<Long> findFollowingIdsByUserId(@Param("userId") Long userId);

    /**
     * Get follower IDs of a user
     */
    @Query("SELECT f.follower.id FROM FollowerEntity f WHERE f.followed.id = :userId")
    List<Long> findFollowerIdsByUserId(@Param("userId") Long userId);

    /**
     * Get mutual followers between two users
     */
    @Query("SELECT f1.followed FROM FollowerEntity f1 " +
            "WHERE f1.follower.id = :userId1 " +
            "AND EXISTS (SELECT f2 FROM FollowerEntity f2 WHERE f2.follower.id = :userId2 AND f2.followed = f1.followed)")
    List<UserEntity> findMutualFollowing(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Get most followed users
     */
    @Query("SELECT f.followed, COUNT(f) as followerCount " +
            "FROM FollowerEntity f " +
            "GROUP BY f.followed " +
            "ORDER BY followerCount DESC")
    List<Object[]> findMostFollowedUsers(Pageable pageable);
}