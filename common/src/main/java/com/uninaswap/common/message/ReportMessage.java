package com.uninaswap.common.message;

import com.uninaswap.common.dto.ListingReportDTO;
import com.uninaswap.common.dto.UserReportDTO;
import java.util.List;

/**
 * 
 */
public class ReportMessage extends Message {

    /**
     * 
     */
    public enum Type {
        
        CREATE_USER_REPORT_REQUEST,
        CREATE_USER_REPORT_RESPONSE,
        GET_USER_REPORTS_REQUEST,
        GET_USER_REPORTS_RESPONSE,

        
        CREATE_LISTING_REPORT_REQUEST,
        CREATE_LISTING_REPORT_RESPONSE,
        GET_LISTING_REPORTS_REQUEST,
        GET_LISTING_REPORTS_RESPONSE,

        
        GET_ALL_USER_REPORTS_REQUEST,
        GET_ALL_USER_REPORTS_RESPONSE,
        GET_ALL_LISTING_REPORTS_REQUEST,
        GET_ALL_LISTING_REPORTS_RESPONSE,
        REVIEW_USER_REPORT_REQUEST,
        REVIEW_USER_REPORT_RESPONSE,
        REVIEW_LISTING_REPORT_REQUEST,
        REVIEW_LISTING_REPORT_RESPONSE
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private UserReportDTO userReport;
    /**
     * 
     */
    private ListingReportDTO listingReport;
    /**
     * 
     */
    private List<UserReportDTO> userReports;
    /**
     * 
     */
    private List<ListingReportDTO> listingReports;
    /**
     * 
     */
    private String reportId;
    /**
     * 
     */
    private String userId;
    /**
     * 
     */
    private String listingId;
    /**
     * 
     */
    private String errorMessage;

    
    /**
     * 
     */
    public ReportMessage() {
        setMessageType("report");
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
    public UserReportDTO getUserReport() {
        return userReport;
    }

    /**
     * @param userReport
     */
    public void setUserReport(UserReportDTO userReport) {
        this.userReport = userReport;
    }

    /**
     * @return
     */
    public ListingReportDTO getListingReport() {
        return listingReport;
    }

    /**
     * @param listingReport
     */
    public void setListingReport(ListingReportDTO listingReport) {
        this.listingReport = listingReport;
    }

    /**
     * @return
     */
    public List<UserReportDTO> getUserReports() {
        return userReports;
    }

    /**
     * @param userReports
     */
    public void setUserReports(List<UserReportDTO> userReports) {
        this.userReports = userReports;
    }

    /**
     * @return
     */
    public List<ListingReportDTO> getListingReports() {
        return listingReports;
    }

    /**
     * @param listingReports
     */
    public void setListingReports(List<ListingReportDTO> listingReports) {
        this.listingReports = listingReports;
    }

    /**
     * @return
     */
    public String getReportId() {
        return reportId;
    }

    /**
     * @param reportId
     */
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    /**
     * @return
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return
     */
    public String getListingId() {
        return listingId;
    }

    /**
     * @param listingId
     */
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    /**
     *
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     *
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}