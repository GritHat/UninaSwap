package com.uninaswap.server.service;

import com.uninaswap.common.dto.ListingReportDTO;
import com.uninaswap.common.dto.UserReportDTO;
import com.uninaswap.common.enums.ListingReportReason;
import com.uninaswap.common.enums.UserReportReason;
import com.uninaswap.server.entity.*;
import com.uninaswap.server.mapper.ListingReportMapper;
import com.uninaswap.server.mapper.UserReportMapper;
import com.uninaswap.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private ListingReportRepository listingReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserReportMapper userReportMapper;

    @Autowired
    private ListingReportMapper listingReportMapper;

    /**
     * Create a user report
     */
    @Transactional
    public UserReportDTO createUserReport(UserReportDTO reportDTO, Long reportingUserId) {
        logger.info("Creating user report by user {} against user {}",
                reportingUserId, reportDTO.getReportedUser().getId());

        
        UserEntity reportingUser = userRepository.findById(reportingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reporting user not found: " + reportingUserId));

        
        UserEntity reportedUser = userRepository.findById(reportDTO.getReportedUser().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reported user not found: " + reportDTO.getReportedUser().getId()));

        
        if (reportingUserId.equals(reportedUser.getId())) {
            throw new IllegalArgumentException("Cannot report yourself");
        }

        
        Optional<UserReportEntity> existingReport = userReportRepository
                .findByReportingUserIdAndReportedUserId(reportingUserId, reportedUser.getId());

        if (existingReport.isPresent()) {
            throw new IllegalArgumentException("You have already reported this user");
        }

        
        UserReportEntity report = new UserReportEntity(reportingUser, reportedUser,
                reportDTO.getReason(), reportDTO.getDescription());

        UserReportEntity savedReport = userReportRepository.save(report);

        logger.info("Successfully created user report with ID: {}", savedReport.getId());
        return userReportMapper.toDto(savedReport);
    }

    /**
     * Create a listing report
     */
    @Transactional
    public ListingReportDTO createListingReport(ListingReportDTO reportDTO, Long reportingUserId) {
        logger.info("Creating listing report by user {} against listing {}",
                reportingUserId, reportDTO.getReportedListing().getId());

        
        UserEntity reportingUser = userRepository.findById(reportingUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reporting user not found: " + reportingUserId));

        
        ListingEntity reportedListing = listingRepository.findById(reportDTO.getReportedListing().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reported listing not found: " + reportDTO.getReportedListing().getId()));

        
        if (reportingUserId.equals(reportedListing.getCreator().getId())) {
            throw new IllegalArgumentException("Cannot report your own listing");
        }

        
        Optional<ListingReportEntity> existingReport = listingReportRepository
                .findByReportingUserIdAndReportedListingId(reportingUserId, reportedListing.getId());

        if (existingReport.isPresent()) {
            throw new IllegalArgumentException("You have already reported this listing");
        }

        
        ListingReportEntity report = new ListingReportEntity(reportingUser, reportedListing,
                reportDTO.getReason(), reportDTO.getDescription());

        ListingReportEntity savedReport = listingReportRepository.save(report);

        logger.info("Successfully created listing report with ID: {}", savedReport.getId());
        return listingReportMapper.toDto(savedReport);
    }

    /**
     * Get user reports made by a specific user
     */
    @Transactional(readOnly = true)
    public List<UserReportDTO> getUserReportsByReporter(Long reportingUserId) {
        List<UserReportEntity> reports = userReportRepository
                .findByReportingUserIdOrderByCreatedAtDesc(reportingUserId);
        return reports.stream()
                .map(userReportMapper::toDto)
                .toList();
    }

    /**
     * Get user reports against a specific user
     */
    @Transactional(readOnly = true)
    public List<UserReportDTO> getUserReportsAgainstUser(Long reportedUserId) {
        List<UserReportEntity> reports = userReportRepository.findByReportedUserIdOrderByCreatedAtDesc(reportedUserId);
        return reports.stream()
                .map(userReportMapper::toDto)
                .toList();
    }

    /**
     * Get listing reports made by a specific user
     */
    @Transactional(readOnly = true)
    public List<ListingReportDTO> getListingReportsByReporter(Long reportingUserId) {
        List<ListingReportEntity> reports = listingReportRepository
                .findByReportingUserIdOrderByCreatedAtDesc(reportingUserId);
        return reports.stream()
                .map(listingReportMapper::toDto)
                .toList();
    }

    /**
     * Get listing reports against a specific listing
     */
    @Transactional(readOnly = true)
    public List<ListingReportDTO> getListingReportsAgainstListing(String listingId) {
        List<ListingReportEntity> reports = listingReportRepository
                .findByReportedListingIdOrderByCreatedAtDesc(listingId);
        return reports.stream()
                .map(listingReportMapper::toDto)
                .toList();
    }

    /**
     * Get all unreviewed user reports (for admin)
     */
    @Transactional(readOnly = true)
    public List<UserReportDTO> getUnreviewedUserReports() {
        List<UserReportEntity> reports = userReportRepository.findByReviewedFalseOrderByCreatedAtDesc();
        return reports.stream()
                .map(userReportMapper::toDto)
                .toList();
    }

    /**
     * Get all unreviewed listing reports (for admin)
     */
    @Transactional(readOnly = true)
    public List<ListingReportDTO> getUnreviewedListingReports() {
        List<ListingReportEntity> reports = listingReportRepository.findByReviewedFalseOrderByCreatedAtDesc();
        return reports.stream()
                .map(listingReportMapper::toDto)
                .toList();
    }

    /**
     * Review a user report (admin operation)
     */
    @Transactional
    public UserReportDTO reviewUserReport(String reportId, Long adminUserId, String adminNotes) {
        logger.info("Reviewing user report {} by admin {}", reportId, adminUserId);

        UserReportEntity report = userReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("User report not found: " + reportId));

        UserEntity admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + adminUserId));

        report.setReviewed(true);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedByAdmin(admin);
        report.setAdminNotes(adminNotes);

        UserReportEntity savedReport = userReportRepository.save(report);

        logger.info("Successfully reviewed user report: {}", reportId);
        return userReportMapper.toDto(savedReport);
    }

    /**
     * Review a listing report (admin operation)
     */
    @Transactional
    public ListingReportDTO reviewListingReport(String reportId, Long adminUserId, String adminNotes) {
        logger.info("Reviewing listing report {} by admin {}", reportId, adminUserId);

        ListingReportEntity report = listingReportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Listing report not found: " + reportId));

        UserEntity admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + adminUserId));

        report.setReviewed(true);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedByAdmin(admin);
        report.setAdminNotes(adminNotes);

        ListingReportEntity savedReport = listingReportRepository.save(report);

        logger.info("Successfully reviewed listing report: {}", reportId);
        return listingReportMapper.toDto(savedReport);
    }

    /**
     * Count reports against a user
     */
    public long countReportsAgainstUser(Long userId) {
        return userReportRepository.countReportsAgainstUser(userId);
    }

    /**
     * Count reports against a listing
     */
    public long countReportsAgainstListing(String listingId) {
        return listingReportRepository.countReportsAgainstListing(listingId);
    }
}