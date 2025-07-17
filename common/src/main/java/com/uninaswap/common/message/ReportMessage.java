package com.uninaswap.common.message;

import com.uninaswap.common.dto.ListingReportDTO;
import com.uninaswap.common.dto.UserReportDTO;
import java.util.List;

public class ReportMessage extends Message {

    public enum Type {
        // User Report Operations
        CREATE_USER_REPORT_REQUEST,
        CREATE_USER_REPORT_RESPONSE,
        GET_USER_REPORTS_REQUEST,
        GET_USER_REPORTS_RESPONSE,

        // Listing Report Operations
        CREATE_LISTING_REPORT_REQUEST,
        CREATE_LISTING_REPORT_RESPONSE,
        GET_LISTING_REPORTS_REQUEST,
        GET_LISTING_REPORTS_RESPONSE,

        // Admin Operations
        GET_ALL_USER_REPORTS_REQUEST,
        GET_ALL_USER_REPORTS_RESPONSE,
        GET_ALL_LISTING_REPORTS_REQUEST,
        GET_ALL_LISTING_REPORTS_RESPONSE,
        REVIEW_USER_REPORT_REQUEST,
        REVIEW_USER_REPORT_RESPONSE,
        REVIEW_LISTING_REPORT_REQUEST,
        REVIEW_LISTING_REPORT_RESPONSE
    }

    private Type type;
    private UserReportDTO userReport;
    private ListingReportDTO listingReport;
    private List<UserReportDTO> userReports;
    private List<ListingReportDTO> listingReports;
    private String reportId;
    private String userId;
    private String listingId;
    private String errorMessage;

    // Default constructor
    public ReportMessage() {
        setMessageType("report");
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public UserReportDTO getUserReport() {
        return userReport;
    }

    public void setUserReport(UserReportDTO userReport) {
        this.userReport = userReport;
    }

    public ListingReportDTO getListingReport() {
        return listingReport;
    }

    public void setListingReport(ListingReportDTO listingReport) {
        this.listingReport = listingReport;
    }

    public List<UserReportDTO> getUserReports() {
        return userReports;
    }

    public void setUserReports(List<UserReportDTO> userReports) {
        this.userReports = userReports;
    }

    public List<ListingReportDTO> getListingReports() {
        return listingReports;
    }

    public void setListingReports(List<ListingReportDTO> listingReports) {
        this.listingReports = listingReports;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}