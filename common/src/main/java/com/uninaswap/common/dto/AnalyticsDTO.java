package com.uninaswap.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnalyticsDTO {
    
    // Overall Stats
    private UserStatsDTO userStats;
    private List<TimeSeriesDataDTO> listingStats;
    private List<TimeSeriesDataDTO> offerStats;
    private List<TimeSeriesDataDTO> reviewStats;
    private List<TimeSeriesDataDTO> earningsStats;
    private List<CategoryStatsDTO> categoryBreakdown;
    private List<MonthlyStatsDTO> monthlyBreakdown;
    
    // Performance Metrics
    private PerformanceMetricsDTO performanceMetrics;
    
    // Constructors
    public AnalyticsDTO() {}
    
    // Getters and setters
    public UserStatsDTO getUserStats() { return userStats; }
    public void setUserStats(UserStatsDTO userStats) { this.userStats = userStats; }
    
    public List<TimeSeriesDataDTO> getListingStats() { return listingStats; }
    public void setListingStats(List<TimeSeriesDataDTO> listingStats) { this.listingStats = listingStats; }
    
    public List<TimeSeriesDataDTO> getOfferStats() { return offerStats; }
    public void setOfferStats(List<TimeSeriesDataDTO> offerStats) { this.offerStats = offerStats; }
    
    public List<TimeSeriesDataDTO> getReviewStats() { return reviewStats; }
    public void setReviewStats(List<TimeSeriesDataDTO> reviewStats) { this.reviewStats = reviewStats; }
    
    public List<TimeSeriesDataDTO> getEarningsStats() { return earningsStats; }
    public void setEarningsStats(List<TimeSeriesDataDTO> earningsStats) { this.earningsStats = earningsStats; }
    
    public List<CategoryStatsDTO> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategoryStatsDTO> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
    
    public List<MonthlyStatsDTO> getMonthlyBreakdown() { return monthlyBreakdown; }
    public void setMonthlyBreakdown(List<MonthlyStatsDTO> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }
    
    public PerformanceMetricsDTO getPerformanceMetrics() { return performanceMetrics; }
    public void setPerformanceMetrics(PerformanceMetricsDTO performanceMetrics) { this.performanceMetrics = performanceMetrics; }
    
    // Inner classes for specific analytics data
    public static class UserStatsDTO {
        private int totalListings;
        private int activeListings;
        private int completedListings;
        private int totalOffersMade;
        private int totalOffersReceived;
        private int acceptedOffers;
        private double averageRating;
        private int totalReviews;
        private double totalEarnings;
        private LocalDateTime memberSince;
        private int totalViews;
        private int totalFavorites;
        
        // Constructors, getters and setters
        public UserStatsDTO() {}
        
        public int getTotalListings() { return totalListings; }
        public void setTotalListings(int totalListings) { this.totalListings = totalListings; }
        
        public int getActiveListings() { return activeListings; }
        public void setActiveListings(int activeListings) { this.activeListings = activeListings; }
        
        public int getCompletedListings() { return completedListings; }
        public void setCompletedListings(int completedListings) { this.completedListings = completedListings; }
        
        public int getTotalOffersMade() { return totalOffersMade; }
        public void setTotalOffersMade(int totalOffersMade) { this.totalOffersMade = totalOffersMade; }
        
        public int getTotalOffersReceived() { return totalOffersReceived; }
        public void setTotalOffersReceived(int totalOffersReceived) { this.totalOffersReceived = totalOffersReceived; }
        
        public int getAcceptedOffers() { return acceptedOffers; }
        public void setAcceptedOffers(int acceptedOffers) { this.acceptedOffers = acceptedOffers; }
        
        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
        
        public int getTotalReviews() { return totalReviews; }
        public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }
        
        public double getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
        
        public LocalDateTime getMemberSince() { return memberSince; }
        public void setMemberSince(LocalDateTime memberSince) { this.memberSince = memberSince; }
        
        public int getTotalViews() { return totalViews; }
        public void setTotalViews(int totalViews) { this.totalViews = totalViews; }
        
        public int getTotalFavorites() { return totalFavorites; }
        public void setTotalFavorites(int totalFavorites) { this.totalFavorites = totalFavorites; }
    }
    
    public static class TimeSeriesDataDTO {
        private LocalDateTime timestamp;
        private double value;
        private String label;
        
        public TimeSeriesDataDTO() {}
        
        public TimeSeriesDataDTO(LocalDateTime timestamp, double value, String label) {
            this.timestamp = timestamp;
            this.value = value;
            this.label = label;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }
    
    public static class CategoryStatsDTO {
        private String category;
        private int totalListings;
        private int activeListing;
        private int completedListings;
        private double totalEarnings;
        private double averageRating;
        
        public CategoryStatsDTO() {}
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public int getTotalListings() { return totalListings; }
        public void setTotalListings(int totalListings) { this.totalListings = totalListings; }
        
        public int getActiveListings() { return activeListing; }
        public void setActiveListings(int activeListings) { this.activeListing = activeListings; }
        
        public int getCompletedListings() { return completedListings; }
        public void setCompletedListings(int completedListings) { this.completedListings = completedListings; }
        
        public double getTotalEarnings() { return totalEarnings; }
        public void setTotalEarnings(double totalEarnings) { this.totalEarnings = totalEarnings; }
        
        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    }
    
    public static class MonthlyStatsDTO {
        private int year;
        private int month;
        private int totalListings;
        private int completedTransactions;
        private double earnings;
        private double averageRating;
        
        public MonthlyStatsDTO() {}
        
        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        
        public int getTotalListings() { return totalListings; }
        public void setTotalListings(int totalListings) { this.totalListings = totalListings; }
        
        public int getCompletedTransactions() { return completedTransactions; }
        public void setCompletedTransactions(int completedTransactions) { this.completedTransactions = completedTransactions; }
        
        public double getEarnings() { return earnings; }
        public void setEarnings(double earnings) { this.earnings = earnings; }
        
        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    }
    
    public static class PerformanceMetricsDTO {
        private double listingSuccessRate;
        private double offerAcceptanceRate;
        private double averageTimeToSell; // in days
        private double customerSatisfactionRate;
        private double repeatCustomerRate;
        private double ratingTrend; // positive/negative trend
        
        public PerformanceMetricsDTO() {}
        
        public double getListingSuccessRate() { return listingSuccessRate; }
        public void setListingSuccessRate(double listingSuccessRate) { this.listingSuccessRate = listingSuccessRate; }
        
        public double getOfferAcceptanceRate() { return offerAcceptanceRate; }
        public void setOfferAcceptanceRate(double offerAcceptanceRate) { this.offerAcceptanceRate = offerAcceptanceRate; }
        
        public double getAverageTimeToSell() { return averageTimeToSell; }
        public void setAverageTimeToSell(double averageTimeToSell) { this.averageTimeToSell = averageTimeToSell; }
        
        public double getCustomerSatisfactionRate() { return customerSatisfactionRate; }
        public void setCustomerSatisfactionRate(double customerSatisfactionRate) { this.customerSatisfactionRate = customerSatisfactionRate; }
        
        public double getRepeatCustomerRate() { return repeatCustomerRate; }
        public void setRepeatCustomerRate(double repeatCustomerRate) { this.repeatCustomerRate = repeatCustomerRate; }
        
        public double getRatingTrend() { return ratingTrend; }
        public void setRatingTrend(double ratingTrend) { this.ratingTrend = ratingTrend; }
    }
}