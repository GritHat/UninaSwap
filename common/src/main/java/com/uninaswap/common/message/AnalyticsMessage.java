package com.uninaswap.common.message;

import com.uninaswap.common.dto.AnalyticsDTO;
import java.time.LocalDateTime;

public class AnalyticsMessage extends Message {
    
    public enum AnalyticsMessageType {
        GET_ANALYTICS_REQUEST,
        GET_ANALYTICS_RESPONSE,
        GET_CATEGORY_ANALYTICS_REQUEST,
        GET_CATEGORY_ANALYTICS_RESPONSE,
        GET_PERFORMANCE_COMPARISON_REQUEST,
        GET_PERFORMANCE_COMPARISON_RESPONSE,
        EXPORT_ANALYTICS_REQUEST,
        EXPORT_ANALYTICS_RESPONSE
    }
    
    private AnalyticsMessageType type;
    private AnalyticsDTO analytics;
    private String period; // "week", "month", "quarter", "year", "all"
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String exportFormat; // "pdf", "csv", "excel"
    private String exportData;
    
    public AnalyticsMessage() {
        super();
        setMessageType("analytics");
    }
    
    // Getters and setters
    public AnalyticsMessageType getType() { return type; }
    public void setType(AnalyticsMessageType type) { this.type = type; }
    
    public AnalyticsDTO getAnalytics() { return analytics; }
    public void setAnalytics(AnalyticsDTO analytics) { this.analytics = analytics; }
    
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    
    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }
    
    public String getExportData() { return exportData; }
    public void setExportData(String exportData) { this.exportData = exportData; }
}