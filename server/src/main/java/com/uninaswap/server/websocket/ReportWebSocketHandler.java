package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.ListingReportDTO;
import com.uninaswap.common.dto.UserReportDTO;
import com.uninaswap.common.message.ReportMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.ReportService;
import com.uninaswap.server.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component
public class ReportWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReportWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final ReportService reportService;
    private final SessionService sessionService;

    @Autowired
    public ReportWebSocketHandler(ObjectMapper objectMapper, ReportService reportService,
            SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.reportService = reportService;
        this.sessionService = sessionService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received report message: {}", message.getPayload());

        try {
            ReportMessage reportMessage = objectMapper.readValue(message.getPayload(), ReportMessage.class);
            ReportMessage response = new ReportMessage();

            try {
                
                UserEntity currentUser = sessionService.validateSession(session);
                if (currentUser == null) {
                    throw new UnauthorizedException("Not authenticated");
                }

                switch (reportMessage.getType()) {
                    case CREATE_USER_REPORT_REQUEST:
                        handleCreateUserReport(reportMessage, response, currentUser);
                        break;

                    case CREATE_LISTING_REPORT_REQUEST:
                        handleCreateListingReport(reportMessage, response, currentUser);
                        break;

                    case GET_USER_REPORTS_REQUEST:
                        handleGetUserReports(reportMessage, response, currentUser);
                        break;

                    case GET_LISTING_REPORTS_REQUEST:
                        handleGetListingReports(reportMessage, response, currentUser);
                        break;

                    case GET_ALL_USER_REPORTS_REQUEST:
                        handleGetAllUserReports(reportMessage, response, currentUser);
                        break;

                    case GET_ALL_LISTING_REPORTS_REQUEST:
                        handleGetAllListingReports(reportMessage, response, currentUser);
                        break;

                    case REVIEW_USER_REPORT_REQUEST:
                        handleReviewUserReport(reportMessage, response, currentUser);
                        break;

                    case REVIEW_LISTING_REPORT_REQUEST:
                        handleReviewListingReport(reportMessage, response, currentUser);
                        break;

                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown report message type: " + reportMessage.getType());
                        break;
                }
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for report operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing report request: " + e.getMessage());
                logger.error("Error processing report message", e);
            }

            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("Error parsing report message", e);
            ReportMessage errorResponse = new ReportMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private void handleCreateUserReport(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            UserReportDTO reportDTO = request.getUserReport();
            UserReportDTO createdReport = reportService.createUserReport(reportDTO, currentUser.getId());

            response.setType(ReportMessage.Type.CREATE_USER_REPORT_RESPONSE);
            response.setUserReport(createdReport);
            response.setSuccess(true);

            logger.info("Created user report {} by user {}", createdReport.getId(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.CREATE_USER_REPORT_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to create user report: " + e.getMessage());
            logger.error("Failed to create user report: {}", e.getMessage());
        }
    }

    private void handleCreateListingReport(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            ListingReportDTO reportDTO = request.getListingReport();
            ListingReportDTO createdReport = reportService.createListingReport(reportDTO, currentUser.getId());

            response.setType(ReportMessage.Type.CREATE_LISTING_REPORT_RESPONSE);
            response.setListingReport(createdReport);
            response.setSuccess(true);

            logger.info("Created listing report {} by user {}", createdReport.getId(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.CREATE_LISTING_REPORT_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to create listing report: " + e.getMessage());
            logger.error("Failed to create listing report: {}", e.getMessage());
        }
    }

    private void handleGetUserReports(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            List<UserReportDTO> reports = reportService.getUserReportsByReporter(currentUser.getId());

            response.setType(ReportMessage.Type.GET_USER_REPORTS_RESPONSE);
            response.setUserReports(reports);
            response.setSuccess(true);

            logger.info("Retrieved {} user reports for user {}", reports.size(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.GET_USER_REPORTS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get user reports: " + e.getMessage());
            logger.error("Failed to get user reports: {}", e.getMessage());
        }
    }

    private void handleGetListingReports(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            List<ListingReportDTO> reports = reportService.getListingReportsByReporter(currentUser.getId());

            response.setType(ReportMessage.Type.GET_LISTING_REPORTS_RESPONSE);
            response.setListingReports(reports);
            response.setSuccess(true);

            logger.info("Retrieved {} listing reports for user {}", reports.size(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.GET_LISTING_REPORTS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get listing reports: " + e.getMessage());
            logger.error("Failed to get listing reports: {}", e.getMessage());
        }
    }

    private void handleGetAllUserReports(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            
            List<UserReportDTO> reports = reportService.getUnreviewedUserReports();

            response.setType(ReportMessage.Type.GET_ALL_USER_REPORTS_RESPONSE);
            response.setUserReports(reports);
            response.setSuccess(true);

            logger.info("Retrieved {} unreviewed user reports for admin {}", reports.size(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.GET_ALL_USER_REPORTS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get all user reports: " + e.getMessage());
            logger.error("Failed to get all user reports: {}", e.getMessage());
        }
    }

    private void handleGetAllListingReports(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            
            List<ListingReportDTO> reports = reportService.getUnreviewedListingReports();

            response.setType(ReportMessage.Type.GET_ALL_LISTING_REPORTS_RESPONSE);
            response.setListingReports(reports);
            response.setSuccess(true);

            logger.info("Retrieved {} unreviewed listing reports for admin {}", reports.size(),
                    currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.GET_ALL_LISTING_REPORTS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get all listing reports: " + e.getMessage());
            logger.error("Failed to get all listing reports: {}", e.getMessage());
        }
    }

    private void handleReviewUserReport(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            
            String reportId = request.getReportId();
            String adminNotes = request.getUserReport() != null ? request.getUserReport().getAdminNotes() : "";

            UserReportDTO reviewedReport = reportService.reviewUserReport(reportId, currentUser.getId(), adminNotes);

            response.setType(ReportMessage.Type.REVIEW_USER_REPORT_RESPONSE);
            response.setUserReport(reviewedReport);
            response.setSuccess(true);

            logger.info("Reviewed user report {} by admin {}", reportId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.REVIEW_USER_REPORT_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to review user report: " + e.getMessage());
            logger.error("Failed to review user report: {}", e.getMessage());
        }
    }

    private void handleReviewListingReport(ReportMessage request, ReportMessage response, UserEntity currentUser) {
        try {
            
            String reportId = request.getReportId();
            String adminNotes = request.getListingReport() != null ? request.getListingReport().getAdminNotes() : "";

            ListingReportDTO reviewedReport = reportService.reviewListingReport(reportId, currentUser.getId(),
                    adminNotes);

            response.setType(ReportMessage.Type.REVIEW_LISTING_REPORT_RESPONSE);
            response.setListingReport(reviewedReport);
            response.setSuccess(true);

            logger.info("Reviewed listing report {} by admin {}", reportId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(ReportMessage.Type.REVIEW_LISTING_REPORT_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to review listing report: " + e.getMessage());
            logger.error("Failed to review listing report: {}", e.getMessage());
        }
    }
}