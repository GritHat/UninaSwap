package com.uninaswap.client.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;

/**
 * 
 */
public class AnalyticsViewModel {
    /**
     * 
     */
    private final IntegerProperty totalListings = new SimpleIntegerProperty();
    /**
     * 
     */
    private final IntegerProperty activeListings = new SimpleIntegerProperty();
    /**
     * 
     */
    private final IntegerProperty completedListings = new SimpleIntegerProperty();
    /**
     * 
     */
    private final IntegerProperty totalOffersMade = new SimpleIntegerProperty();
    /**
     * 
     */
    private final IntegerProperty totalOffersReceived = new SimpleIntegerProperty();
    /**
     * 
     */
    private final IntegerProperty acceptedOffers = new SimpleIntegerProperty();
    /**
     * 
     */
    private final DoubleProperty averageRating = new SimpleDoubleProperty();
    /**
     * 
     */
    private final IntegerProperty totalReviews = new SimpleIntegerProperty();
    /**
     * 
     */
    private final DoubleProperty totalEarnings = new SimpleDoubleProperty();
    /**
     * 
     */
    private final ObjectProperty<LocalDateTime> memberSince = new SimpleObjectProperty<>();
    /**
     * 
     */
    private final IntegerProperty totalViews = new SimpleIntegerProperty();
    /**
     * 
     */
    private final IntegerProperty totalFavorites = new SimpleIntegerProperty();
    /**
     * 
     */
    private final DoubleProperty listingSuccessRate = new SimpleDoubleProperty();
    /**
     * 
     */
    private final DoubleProperty offerAcceptanceRate = new SimpleDoubleProperty();
    /**
     * 
     */
    private final DoubleProperty averageTimeToSell = new SimpleDoubleProperty();
    /**
     * 
     */
    private final DoubleProperty customerSatisfactionRate = new SimpleDoubleProperty();
    /**
     * 
     */
    private final DoubleProperty repeatCustomerRate = new SimpleDoubleProperty();
    /**
     * 
     */
    private final DoubleProperty ratingTrend = new SimpleDoubleProperty();
    /**
     * 
     */
    private final ObservableList<TimeSeriesData> listingStats = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<TimeSeriesData> offerStats = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<TimeSeriesData> reviewStats = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<TimeSeriesData> earningsStats = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<CategoryStats> categoryBreakdown = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<MonthlyStats> monthlyBreakdown = FXCollections.observableArrayList();
    /**
     * @return
     */
    public IntegerProperty totalListingsProperty() { return totalListings; }
    /**
     * @return
     */
    public IntegerProperty activeListingsProperty() { return activeListings; }
    /**
     * @return
     */
    public IntegerProperty completedListingsProperty() { return completedListings; }
    /**
     * @return
     */
    public IntegerProperty totalOffersMadeProperty() { return totalOffersMade; }
    /**
     * @return
     */
    public IntegerProperty totalOffersReceivedProperty() { return totalOffersReceived; }
    /**
     * @return
     */
    public IntegerProperty acceptedOffersProperty() { return acceptedOffers; }
    /**
     * @return
     */
    public DoubleProperty averageRatingProperty() { return averageRating; }
    /**
     * @return
     */
    public IntegerProperty totalReviewsProperty() { return totalReviews; }
    /**
     * @return
     */
    public DoubleProperty totalEarningsProperty() { return totalEarnings; }
    /**
     * @return
     */
    public ObjectProperty<LocalDateTime> memberSinceProperty() { return memberSince; }
    /**
     * @return
     */
    public IntegerProperty totalViewsProperty() { return totalViews; }
    /**
     * @return
     */
    public IntegerProperty totalFavoritesProperty() { return totalFavorites; }
    
    /**
     * @return
     */
    public DoubleProperty listingSuccessRateProperty() { return listingSuccessRate; }
    /**
     * @return
     */
    public DoubleProperty offerAcceptanceRateProperty() { return offerAcceptanceRate; }
    /**
     * @return
     */
    public DoubleProperty averageTimeToSellProperty() { return averageTimeToSell; }
    /**
     * @return
     */
    public DoubleProperty customerSatisfactionRateProperty() { return customerSatisfactionRate; }
    /**
     * @return
     */
    public DoubleProperty repeatCustomerRateProperty() { return repeatCustomerRate; }
    /**
     * @return
     */
    public DoubleProperty ratingTrendProperty() { return ratingTrend; }
    /**
     * @return
     */
    public int getTotalListings() { return totalListings.get(); }
    /**
     * @param totalListings
     */
    public void setTotalListings(int totalListings) { this.totalListings.set(totalListings); }
    
    /**
     * @return
     */
    public int getActiveListings() { return activeListings.get(); }
    /**
     * @param activeListings
     */
    public void setActiveListings(int activeListings) { this.activeListings.set(activeListings); }
    
    /**
     * @return
     */
    public int getCompletedListings() { return completedListings.get(); }
    /**
     * @param completedListings
     */
    public void setCompletedListings(int completedListings) { this.completedListings.set(completedListings); }
    
    /**
     * @return
     */
    public int getTotalOffersMade() { return totalOffersMade.get(); }
    /**
     * @param totalOffersMade
     */
    public void setTotalOffersMade(int totalOffersMade) { this.totalOffersMade.set(totalOffersMade); }
    
    /**
     * @return
     */
    public int getTotalOffersReceived() { return totalOffersReceived.get(); }
    /**
     * @param totalOffersReceived
     */
    public void setTotalOffersReceived(int totalOffersReceived) { this.totalOffersReceived.set(totalOffersReceived); }
    
    /**
     * @return
     */
    public int getAcceptedOffers() { return acceptedOffers.get(); }
    /**
     * @param acceptedOffers
     */
    public void setAcceptedOffers(int acceptedOffers) { this.acceptedOffers.set(acceptedOffers); }
    
    /**
     * @return
     */
    public double getAverageRating() { return averageRating.get(); }
    /**
     * @param averageRating
     */
    public void setAverageRating(double averageRating) { this.averageRating.set(averageRating); }
    
    /**
     * @return
     */
    public int getTotalReviews() { return totalReviews.get(); }
    /**
     * @param totalReviews
     */
    public void setTotalReviews(int totalReviews) { this.totalReviews.set(totalReviews); }
    
    /**
     * @return
     */
    public double getTotalEarnings() { return totalEarnings.get(); }
    /**
     * @param totalEarnings
     */
    public void setTotalEarnings(double totalEarnings) { this.totalEarnings.set(totalEarnings); }
    
    /**
     * @return
     */
    public LocalDateTime getMemberSince() { return memberSince.get(); }
    /**
     * @param memberSince
     */
    public void setMemberSince(LocalDateTime memberSince) { this.memberSince.set(memberSince); }
    
    /**
     * @return
     */
    public int getTotalViews() { return totalViews.get(); }
    /**
     * @param totalViews
     */
    public void setTotalViews(int totalViews) { this.totalViews.set(totalViews); }
    
    /**
     * @return
     */
    public int getTotalFavorites() { return totalFavorites.get(); }
    /**
     * @param totalFavorites
     */
    public void setTotalFavorites(int totalFavorites) { this.totalFavorites.set(totalFavorites); }
    
    /**
     * @return
     */
    public double getListingSuccessRate() { return listingSuccessRate.get(); }
    /**
     * @param listingSuccessRate
     */
    public void setListingSuccessRate(double listingSuccessRate) { this.listingSuccessRate.set(listingSuccessRate); }
    
    /**
     * @return
     */
    public double getOfferAcceptanceRate() { return offerAcceptanceRate.get(); }
    /**
     * @param offerAcceptanceRate
     */
    public void setOfferAcceptanceRate(double offerAcceptanceRate) { this.offerAcceptanceRate.set(offerAcceptanceRate); }
    
    /**
     * @return
     */
    public double getAverageTimeToSell() { return averageTimeToSell.get(); }
    /**
     * @param averageTimeToSell
     */
    public void setAverageTimeToSell(double averageTimeToSell) { this.averageTimeToSell.set(averageTimeToSell); }
    
    /**
     * @return
     */
    public double getCustomerSatisfactionRate() { return customerSatisfactionRate.get(); }
    /**
     * @param customerSatisfactionRate
     */
    public void setCustomerSatisfactionRate(double customerSatisfactionRate) { this.customerSatisfactionRate.set(customerSatisfactionRate); }
    
    /**
     * @return
     */
    public double getRepeatCustomerRate() { return repeatCustomerRate.get(); }
    /**
     * @param repeatCustomerRate
     */
    public void setRepeatCustomerRate(double repeatCustomerRate) { this.repeatCustomerRate.set(repeatCustomerRate); }
    
    /**
     * @return
     */
    public double getRatingTrend() { return ratingTrend.get(); }
    /**
     * @param ratingTrend
     */
    public void setRatingTrend(double ratingTrend) { this.ratingTrend.set(ratingTrend); }
    /**
     * @return
     */
    public ObservableList<TimeSeriesData> getListingStats() { return listingStats; }
    /**
     * @return
     */
    public ObservableList<TimeSeriesData> getOfferStats() { return offerStats; }
    /**
     * @return
     */
    public ObservableList<TimeSeriesData> getReviewStats() { return reviewStats; }
    /**
     * @return
     */
    public ObservableList<TimeSeriesData> getEarningsStats() { return earningsStats; }
    /**
     * @return
     */
    public ObservableList<CategoryStats> getCategoryBreakdown() { return categoryBreakdown; }
    /**
     * @return
     */
    public ObservableList<MonthlyStats> getMonthlyBreakdown() { return monthlyBreakdown; }
    
    /**
     * 
     */
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
    
    /**
     * 
     */
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
    
    /**
     * 
     */
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