package com.uninaswap.server.repository;

import com.uninaswap.common.enums.ListingReportReason;
import com.uninaswap.server.entity.ListingReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingReportRepository extends JpaRepository<ListingReportEntity, String> {

    /**
     * Find all reports made by a specific user
     */
    List<ListingReportEntity> findByReportingUserIdOrderByCreatedAtDesc(Long reportingUserId);

    /**
     * Find all reports against a specific listing
     */
    List<ListingReportEntity> findByReportedListingIdOrderByCreatedAtDesc(String listingId);

    /**
     * Find reports by reason
     */
    List<ListingReportEntity> findByReasonOrderByCreatedAtDesc(ListingReportReason reason);

    /**
     * Find unreviewed reports
     */
    List<ListingReportEntity> findByReviewedFalseOrderByCreatedAtDesc();

    /**
     * Find reviewed reports
     */
    List<ListingReportEntity> findByReviewedTrueOrderByCreatedAtDesc();

    /**
     * Check if a user has already reported a specific listing
     */
    Optional<ListingReportEntity> findByReportingUserIdAndReportedListingId(Long reportingUserId, String listingId);

    /**
     * Count reports against a specific listing
     */
    @Query("SELECT COUNT(r) FROM ListingReportEntity r WHERE r.reportedListing.id = :listingId")
    long countReportsAgainstListing(@Param("listingId") String listingId);

    /**
     * Count unreviewed reports against a specific listing
     */
    @Query("SELECT COUNT(r) FROM ListingReportEntity r WHERE r.reportedListing.id = :listingId AND r.reviewed = false")
    long countUnreviewedReportsAgainstListing(@Param("listingId") String listingId);
}