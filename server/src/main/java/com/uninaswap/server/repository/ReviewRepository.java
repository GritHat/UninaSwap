package com.uninaswap.server.repository;

import com.uninaswap.server.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, String> {

    /**
     * Find all reviews received by a specific user (reviews about them)
     */
    @Query("SELECT r FROM ReviewEntity r " +
            "WHERE r.reviewedUser.id = :userId " +
            "ORDER BY r.createdAt DESC")
    List<ReviewEntity> findByReviewedUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find all reviews given by a specific user (reviews they wrote)
     */
    @Query("SELECT r FROM ReviewEntity r " +
            "WHERE r.reviewer.id = :userId " +
            "ORDER BY r.createdAt DESC")
    List<ReviewEntity> findByReviewerIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Find reviews received by a user with pagination
     */
    @Query("SELECT r FROM ReviewEntity r " +
            "WHERE r.reviewedUser.id = :userId " +
            "ORDER BY r.createdAt DESC")
    Page<ReviewEntity> findByReviewedUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find reviews given by a user with pagination
     */
    @Query("SELECT r FROM ReviewEntity r " +
            "WHERE r.reviewer.id = :userId " +
            "ORDER BY r.createdAt DESC")
    Page<ReviewEntity> findByReviewerIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find review for a specific offer
     */
    Optional<ReviewEntity> findByOfferId(String offerId);

    /**
     * Check if a review exists for a specific offer and reviewer
     */
    @Query("SELECT r FROM ReviewEntity r " +
            "WHERE r.offer.id = :offerId AND r.reviewer.id = :reviewerId")
    Optional<ReviewEntity> findByOfferIdAndReviewerId(@Param("offerId") String offerId,
            @Param("reviewerId") Long reviewerId);

    /**
     * Get average rating for a user
     */
    @Query("SELECT AVG(r.score) FROM ReviewEntity r WHERE r.reviewedUser.id = :userId")
    Optional<Double> getAverageRatingForUser(@Param("userId") Long userId);

    /**
     * Get total number of reviews for a user
     */
    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.reviewedUser.id = :userId")
    Long getTotalReviewsForUser(@Param("userId") Long userId);

    /**
     * Get reviews between two users
     */
    @Query("SELECT r FROM ReviewEntity r " +
            "WHERE (r.reviewer.id = :user1Id AND r.reviewedUser.id = :user2Id) " +
            "OR (r.reviewer.id = :user2Id AND r.reviewedUser.id = :user1Id) " +
            "ORDER BY r.createdAt DESC")
    List<ReviewEntity> findReviewsBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * Check if user can review another user (they had a completed transaction)
     */
    @Query("SELECT COUNT(o) > 0 FROM OfferEntity o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND ((o.user.id = :reviewerId AND o.listing.creator.id = :reviewedUserId) " +
            "OR (o.listing.creator.id = :reviewerId AND o.user.id = :reviewedUserId))")
    boolean canUserReviewUser(@Param("reviewerId") Long reviewerId, @Param("reviewedUserId") Long reviewedUserId);

    /**
     * Get completed offers between two users that haven't been reviewed yet
     */
    @Query("SELECT o FROM OfferEntity o " +
            "WHERE o.status = 'COMPLETED' " +
            "AND ((o.user.id = :user1Id AND o.listing.creator.id = :user2Id) " +
            "OR (o.listing.creator.id = :user1Id AND o.user.id = :user2Id)) " +
            "AND NOT EXISTS (SELECT r FROM ReviewEntity r WHERE r.offer.id = o.id AND r.reviewer.id = :reviewerId) " +
            "ORDER BY o.updatedAt DESC")
    List<Object> findReviewableOffersBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id,
            @Param("reviewerId") Long reviewerId);
}