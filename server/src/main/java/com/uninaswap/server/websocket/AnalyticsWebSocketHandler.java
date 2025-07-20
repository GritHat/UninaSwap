package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.AnalyticsDTO;
import com.uninaswap.common.message.AnalyticsMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.AnalyticsService;
import com.uninaswap.server.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class AnalyticsWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsWebSocketHandler.class);
    
    private final ObjectMapper objectMapper;
    private final AnalyticsService analyticsService;
    private final SessionService sessionService;
    
    @Autowired
    public AnalyticsWebSocketHandler(ObjectMapper objectMapper, AnalyticsService analyticsService,
                                   SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.analyticsService = analyticsService;
        this.sessionService = sessionService;
    }
    
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received analytics message: {}", message.getPayload());
        
        try {
            AnalyticsMessage analyticsMessage = objectMapper.readValue(message.getPayload(), AnalyticsMessage.class);
            AnalyticsMessage response = new AnalyticsMessage();
            response.setMessageId(analyticsMessage.getMessageId());
            
            try {
                // All analytics operations require authentication
                UserEntity currentUser = sessionService.validateSession(session);
                if (currentUser == null) {
                    throw new UnauthorizedException("Not authenticated");
                }
                
                logger.info("Processing analytics request type: {} for user: {}", 
                           analyticsMessage.getType(), currentUser.getUsername());
                
                switch (analyticsMessage.getType()) {
                    case GET_ANALYTICS_REQUEST:
                        handleGetAnalytics(analyticsMessage, response, currentUser);
                        break;
                        
                    case GET_CATEGORY_ANALYTICS_REQUEST:
                        handleGetCategoryAnalytics(analyticsMessage, response, currentUser);
                        break;
                        
                    case GET_PERFORMANCE_COMPARISON_REQUEST:
                        handleGetPerformanceComparison(analyticsMessage, response, currentUser);
                        break;
                        
                    case EXPORT_ANALYTICS_REQUEST:
                        handleExportAnalytics(analyticsMessage, response, currentUser);
                        break;
                        
                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown analytics message type: " + analyticsMessage.getType());
                        logger.warn("Unknown analytics message type: {}", analyticsMessage.getType());
                        break;
                }
                
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for analytics operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing analytics request: " + e.getMessage());
                logger.error("Error processing analytics message: {}", analyticsMessage.getType(), e);
            }
            
            // Send the response back to the client
            logger.debug("Sending analytics response: {}", response.getType());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            
        } catch (Exception e) {
            logger.error("Error parsing analytics message", e);
            AnalyticsMessage errorResponse = new AnalyticsMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
            } catch (Exception sendException) {
                logger.error("Failed to send error response", sendException);
            }
        }
    }
    
    private void handleGetAnalytics(AnalyticsMessage request, AnalyticsMessage response, UserEntity currentUser) {
        try {
            logger.info("Getting analytics for user: {}", currentUser.getUsername());
            
            AnalyticsDTO analytics;
            
            if (request.getStartDate() != null && request.getEndDate() != null) {
                // Custom date range
                analytics = analyticsService.getUserAnalytics(currentUser.getId(), 
                                                            request.getStartDate(), 
                                                            request.getEndDate());
            } else {
                // Period-based analytics
                String period = request.getPeriod() != null ? request.getPeriod() : "month";
                analytics = analyticsService.getUserAnalyticsByPeriod(currentUser.getId(), period);
            }
            
            response.setType(AnalyticsMessage.AnalyticsMessageType.GET_ANALYTICS_RESPONSE);
            response.setAnalytics(analytics);
            response.setSuccess(true);
            
            logger.info("Retrieved analytics for user {}", currentUser.getUsername());
            
        } catch (Exception e) {
            logger.error("Error getting analytics for user {}: {}", currentUser.getUsername(), e.getMessage(), e);
            response.setType(AnalyticsMessage.AnalyticsMessageType.GET_ANALYTICS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to retrieve analytics: " + e.getMessage());
        }
    }
    
    private void handleGetCategoryAnalytics(AnalyticsMessage request, AnalyticsMessage response, UserEntity currentUser) {
        try {
            logger.info("Getting category analytics for user: {} category: {}", 
                       currentUser.getUsername(), request.getCategory());
            
            String category = request.getCategory();
            String period = request.getPeriod() != null ? request.getPeriod() : "month";
            
            AnalyticsDTO analytics = analyticsService.getUserCategoryAnalytics(
                currentUser.getId(), category, period);
            
            response.setType(AnalyticsMessage.AnalyticsMessageType.GET_CATEGORY_ANALYTICS_RESPONSE);
            response.setAnalytics(analytics);
            response.setSuccess(true);
            
            logger.info("Retrieved category analytics for user {}", currentUser.getUsername());
            
        } catch (Exception e) {
            logger.error("Error getting category analytics for user {}: {}", 
                        currentUser.getUsername(), e.getMessage(), e);
            response.setType(AnalyticsMessage.AnalyticsMessageType.GET_CATEGORY_ANALYTICS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to retrieve category analytics: " + e.getMessage());
        }
    }
    
    private void handleGetPerformanceComparison(AnalyticsMessage request, AnalyticsMessage response, UserEntity currentUser) {
        try {
            logger.info("Getting performance comparison for user: {}", currentUser.getUsername());
            
            String period = request.getPeriod() != null ? request.getPeriod() : "month";
            
            AnalyticsDTO analytics = analyticsService.getUserPerformanceComparison(
                currentUser.getId(), period);
            
            response.setType(AnalyticsMessage.AnalyticsMessageType.GET_PERFORMANCE_COMPARISON_RESPONSE);
            response.setAnalytics(analytics);
            response.setSuccess(true);
            
            logger.info("Retrieved performance comparison for user {}", currentUser.getUsername());
            
        } catch (Exception e) {
            logger.error("Error getting performance comparison for user {}: {}", 
                        currentUser.getUsername(), e.getMessage(), e);
            response.setType(AnalyticsMessage.AnalyticsMessageType.GET_PERFORMANCE_COMPARISON_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to retrieve performance comparison: " + e.getMessage());
        }
    }
    
    private void handleExportAnalytics(AnalyticsMessage request, AnalyticsMessage response, UserEntity currentUser) {
        try {
            logger.info("Exporting analytics for user: {} format: {}", 
                       currentUser.getUsername(), request.getExportFormat());
            
            String format = request.getExportFormat();
            String period = request.getPeriod() != null ? request.getPeriod() : "month";
            
            String exportData = analyticsService.exportUserAnalytics(
                currentUser.getId(), format, period);
            
            response.setType(AnalyticsMessage.AnalyticsMessageType.EXPORT_ANALYTICS_RESPONSE);
            response.setExportData(exportData);
            response.setSuccess(true);
            
            logger.info("Exported analytics for user {}", currentUser.getUsername());
            
        } catch (Exception e) {
            logger.error("Error exporting analytics for user {}: {}", 
                        currentUser.getUsername(), e.getMessage(), e);
            response.setType(AnalyticsMessage.AnalyticsMessageType.EXPORT_ANALYTICS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to export analytics: " + e.getMessage());
        }
    }
}