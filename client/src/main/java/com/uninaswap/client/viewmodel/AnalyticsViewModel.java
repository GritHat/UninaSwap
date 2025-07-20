package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;

public class AnalyticsViewModel {
    
    // User Stats Properties
    private final IntegerProperty totalListings = new SimpleIntegerProperty();
    private final IntegerProperty activeListings = new SimpleIntegerProperty();
    private final IntegerProperty completedListings = new SimpleIntegerProperty();
    private final IntegerProperty totalOffersMade = new SimpleIntegerProperty();
    private final IntegerProperty totalOffersReceived = new SimpleIntegerProperty();
    private final IntegerProperty acceptedOffers = new SimpleIntegerProperty();
    private final DoubleProperty averageRating = new SimpleDoubleProperty();
    private final IntegerProperty totalReviews = new SimpleIntegerProperty();
    private final DoubleProperty totalEarnings = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDateTime> memberSince = new SimpleObjectProperty<>();
    private final IntegerProperty totalViews = new SimpleIntegerProperty();
    private final IntegerProperty totalFavorites = new SimpleIntegerProperty();
    
    // Performance Metrics Properties
    private final DoubleProperty listingSuccessRate = new SimpleDoubleProperty();
    private final DoubleProperty offerAcceptanceRate = new SimpleDoubleProperty();
    private final DoubleProperty averageTimeToSell = new SimpleDoubleProperty();
    private final DoubleProperty customerSatisfactionRate = new SimpleDoubleProperty();
    private final DoubleProperty repeatCustomerRate = new SimpleDoubleProperty();
    private final DoubleProperty ratingTrend = new SimpleDoubleProperty();
    
    // Chart Data
    private final ObservableList<TimeSeriesData> listingStats = FXCollections.observableArrayList();
    private final ObservableList<TimeSeriesData> offerStats = FXCollections.observableArrayList();
    private final ObservableList<TimeSeriesData> reviewStats = FXCollections.observableArrayList();
    private final ObservableList<TimeSeriesData> earningsStats = FXCollections.observableArrayList();
    private final ObservableList<CategoryStats> categoryBreakdown = FXCollections.observableArrayList();
    private final ObservableList<MonthlyStats> monthlyBreakdown = FXCollections.observableArrayList();
    
    // Property getters
    public IntegerProperty totalListingsProperty() { return totalListings; }
    public IntegerProperty activeListingsProperty() { return activeListings; }
    public IntegerProperty completedListingsProperty() { return completedListings; }
    public IntegerProperty totalOffersMadeProperty() { return totalOffersMade; }
    public IntegerProperty totalOffersReceivedProperty() { return totalOffersReceived; }
    public IntegerProperty acceptedOffersProperty() { return acceptedOffers; }
    public DoubleProperty averageRatingProperty() { return averageRating; }
    public IntegerProperty totalReviewsProperty() { return totalReviews; }
    public DoubleProperty totalEarningsProperty() { return totalEarnings; }
    public ObjectProperty<LocalDateTime> memberSinceProperty() { return memberSince; }
    public IntegerProperty totalViewsProperty() { return totalViews; }
    public IntegerProperty totalFavoritesProperty() { return totalFavorites; }
    
    public DoubleProperty listingSuccessRateProperty() { return listingSuccessRate; }
    public DoubleProperty offerAcceptanceRateProperty() { return offerAcceptanceRate; }
    public DoubleProperty averageTimeToSellProperty() { return averageTimeToSell; }
    public DoubleProperty customerSatisfactionRateProperty() { return customerSatisfactionRate; }
    public DoubleProperty repeatCustomerRateProperty() { return repeatCustomerRate; }
    public DoubleProperty ratingTrendProperty() { return ratingTrend; }
    
    // Value getters and setters
    public int getTotalListings() { return totalListings.get(); }
    public void setTotalListings(int totalListings) { this.totalListings.set(totalListings); }
    
    public int getActiveListings() { return activeListings.get(); }
    public void setActiveListings(int activeListings) { this.activeListings.set(activeListings); }
    
    public int getCompletedListings() { return completedListings.get(); }
    public void setCompletedListings(int completedListings) { this.completedListings.set(completedListings); }
    
    public int getTotalOffersMade() { return totalOffersMade.get(); }
    public void setTotalOffersMade(int totalOffersMade) { this.totalOffersMade.set(totalOffersMade); }
    
    public int getTotalOffersReceived() { return totalOffersReceived.get(); }
    public void setTotalOffersReceived(int totalOffersReceived) { this.totalOffersReceived.set(totalOffersReceived); }
    
    public int getAcceptedOffers() { return acceptedOffers.get(); }
    public void setAcceptedOffers(int acceptedOffers) { this.acceptedOffers.set(acceptedOffers); }
    
    public double getAverageRating() { return averageRating.get(); }
    public void setAverageRating(double averageRating) { this.averageRating.set(averageRating); }
    
    public int getTotalReviews() { return totalReviews.get(); }
    public void setTotalReviews(int totalReviews) { this.totalReviews.set(totalReviews); }
    
    public double getTotalEarnings() { return totalEarnings.get(); }
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings.set(totalEarnings); }
    
    public LocalDateTime getMemberSince() { return memberSince.get(); }
    public void setMemberSince(LocalDateTime memberSince) { this.memberSince.set(memberSince); }
    
    public int getTotalViews() { return totalViews.get(); }
    public void setTotalViews(int totalViews) { this.totalViews.set(totalViews); }
    
    public int getTotalFavorites() { return totalFavorites.get(); }
    public void setTotalFavorites(int totalFavorites) { this.totalFavorites.set(totalFavorites); }
    
    public double getListingSuccessRate() { return listingSuccessRate.get(); }
    public void setListingSuccessRate(double listingSuccessRate) { this.listingSuccessRate.set(listingSuccessRate); }
    
    public double getOfferAcceptanceRate() { return offerAcceptanceRate.get(); }
    public void setOfferAcceptanceRate(double offerAcceptanceRate) { this.offerAcceptanceRate.set(offerAcceptanceRate); }
    
    public double getAverageTimeToSell() { return averageTimeToSell.get(); }
    public void setAverageTimeToSell(double averageTimeToSell) { this.averageTimeToSell.set(averageTimeToSell); }
    
    public double getCustomerSatisfactionRate() { return customerSatisfactionRate.get(); }
    public void setCustomerSatisfactionRate(double customerSatisfactionRate) { this.customerSatisfactionRate.set(customerSatisfactionRate); }
    
    public double getRepeatCustomerRate() { return repeatCustomerRate.get(); }
    public void setRepeatCustomerRate(double repeatCustomerRate) { this.repeatCustomerRate.set(repeatCustomerRate); }
    
    public double getRatingTrend() { return ratingTrend.get(); }
    public void setRatingTrend(double ratingTrend) { this.ratingTrend.set(ratingTrend); }
    
    // Observable lists getters
    public ObservableList<TimeSeriesData> getListingStats() { return listingStats; }
    public ObservableList<TimeSeriesData> getOfferStats() { return offerStats; }
    public ObservableList<TimeSeriesData> getReviewStats() { return reviewStats; }
    public ObservableList<TimeSeriesData> getEarningsStats() { return earningsStats; }
    public ObservableList<CategoryStats> getCategoryBreakdown() { return categoryBreakdown; }
    public ObservableList<MonthlyStats> getMonthlyBreakdown() { return monthlyBreakdown; }
    
    // Helper classes for chart data
    public static class TimeSeriesData {
        private final LocalDateTime timestamp;
        private final double value;
        private final String label;
        
        public TimeSeriesData(LocalDateTime timestamp, double value, String label) {
            this.timestamp = timestamp;
            this.value = value;
            this.label = label;
        }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public double getValue() { return value; }
        public String getLabel() { return label; }
    }
    
    public static class CategoryStats {
        private final String category;
        private final int totalListings;
        private final int activeListings;
        private final int completedListings;
        private final double totalEarnings;
        private final double averageRating;
        
        public CategoryStats(String category, int totalListings, int activeListings, 
                           int completedListings, double totalEarnings, double averageRating) {
            this.category = category;
            this.totalListings = totalListings;
            this.activeListings = activeListings;
            this.completedListings = completedListings;
            this.totalEarnings = totalEarnings;
            this.averageRating = averageRating;
        }
        
        public String getCategory() { return category; }
        public int getTotalListings() { return totalListings; }
        public int getActiveListings() { return activeListings; }
        public int getCompletedListings() { return completedListings; }
        public double getTotalEarnings() { return totalEarnings; }
        public double getAverageRating() { return averageRating; }
    }
    
    public static class MonthlyStats {
        private final int year;
        private final int month;
        private final int totalListings;
        private final int completedTransactions;
        private final double earnings;
        private final double averageRating;
        
        public MonthlyStats(int year, int month, int totalListings, int completedTransactions, 
                          double earnings, double averageRating) {
            this.year = year;
            this.month = month;
            this.totalListings = totalListings;
            this.completedTransactions = completedTransactions;
            this.earnings = earnings;
            this.averageRating = averageRating;
        }
        
        public int getYear() { return year; }
        public int getMonth() { return month; }
        public int getTotalListings() { return totalListings; }
        public int getCompletedTransactions() { return completedTransactions; }
        public double getEarnings() { return earnings; }
        public double getAverageRating() { return averageRating; }
        
        public String getFormattedPeriod() {
            return String.format("%02d/%d", month, year);
        }
    }
}