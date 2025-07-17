package com.uninaswap.server.repository;

import com.uninaswap.common.enums.UserReportReason;
import com.uninaswap.server.entity.UserReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserReportRepository extends JpaRepository<UserReportEntity, String> {

    /**
     * Find all reports made by a specific user
     */
    List<UserReportEntity> findByReportingUserIdOrderByCreatedAtDesc(Long reportingUserId);

    /**
     * Find all reports against a specific user
     */
    List<UserReportEntity> findByReportedUserIdOrderByCreatedAtDesc(Long reportedUserId);

    /**
     * Find reports by reason
     */
    List<UserReportEntity> findByReasonOrderByCreatedAtDesc(UserReportReason reason);

    /**
     * Find unreviewed reports
     */
    List<UserReportEntity> findByReviewedFalseOrderByCreatedAtDesc();

    /**
     * Find reviewed reports
     */
    List<UserReportEntity> findByReviewedTrueOrderByCreatedAtDesc();

    /**
     * Check if a user has already reported another user
     */
    Optional<UserReportEntity> findByReportingUserIdAndReportedUserId(Long reportingUserId, Long reportedUserId);

    /**
     * Count reports against a specific user
     */
    @Query("SELECT COUNT(r) FROM UserReportEntity r WHERE r.reportedUser.id = :userId")
    long countReportsAgainstUser(@Param("userId") Long userId);

    /**
     * Count unreviewed reports against a specific user
     */
    @Query("SELECT COUNT(r) FROM UserReportEntity r WHERE r.reportedUser.id = :userId AND r.reviewed = false")
    long countUnreviewedReportsAgainstUser(@Param("userId") Long userId);
}