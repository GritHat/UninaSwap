package com.uninaswap.client.service;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.message.AnalyticsMessage;
import com.uninaswap.common.dto.AnalyticsDTO;
import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.AnalyticsViewModel;

import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.time.LocalDateTime;

public class AnalyticsService {
    private static AnalyticsService instance;
    
    private final WebSocketClient webSocketClient;
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    
    // Cache for analytics data
    private AnalyticsViewModel cachedAnalytics;
    private LocalDateTime lastRefresh;
    private static final long CACHE_DURATION_MINUTES = 15; // Cache for 15 minutes
    
    // Pending futures for WebSocket responses
    private final Map<String, CompletableFuture<?>> pendingFutures = new ConcurrentHashMap<>();
    
    private AnalyticsService() {
        webSocketClient = WebSocketClient.getInstance();
        // Register message handler
        webSocketClient.registerMessageHandler(AnalyticsMessage.class, this::handleAnalyticsMessage);
    }
    
    public static synchronized AnalyticsService getInstance() {
        if (instance == null) {
            instance = new AnalyticsService();
        }
        return instance;
    }
    
    /**
     * Get analytics data for a specific time period
     */
    public CompletableFuture<AnalyticsViewModel> getAnalytics(String period) {
        // Check cache first
        if (isCacheValid() && cachedAnalytics != null) {
            return CompletableFuture.completedFuture(cachedAnalytics);
        }
        
        CompletableFuture<AnalyticsViewModel> future = new CompletableFuture<>();
        
        AnalyticsMessage message = new AnalyticsMessage();
        message.setType(AnalyticsMessage.AnalyticsMessageType.GET_ANALYTICS_REQUEST);
        message.setPeriod(period);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Get analytics data for a custom date range
     */
    public CompletableFuture<AnalyticsViewModel> getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        CompletableFuture<AnalyticsViewModel> future = new CompletableFuture<>();
        
        AnalyticsMessage message = new AnalyticsMessage();
        message.setType(AnalyticsMessage.AnalyticsMessageType.GET_ANALYTICS_REQUEST);
        message.setStartDate(startDate);
        message.setEndDate(endDate);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Get category-specific analytics
     */
    public CompletableFuture<AnalyticsViewModel> getCategoryAnalytics(String category, String period) {
        CompletableFuture<AnalyticsViewModel> future = new CompletableFuture<>();
        
        AnalyticsMessage message = new AnalyticsMessage();
        message.setType(AnalyticsMessage.AnalyticsMessageType.GET_CATEGORY_ANALYTICS_REQUEST);
        message.setCategory(category);
        message.setPeriod(period);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Get performance comparison with other users
     */
    public CompletableFuture<AnalyticsViewModel> getPerformanceComparison(String period) {
        CompletableFuture<AnalyticsViewModel> future = new CompletableFuture<>();
        
        AnalyticsMessage message = new AnalyticsMessage();
        message.setType(AnalyticsMessage.AnalyticsMessageType.GET_PERFORMANCE_COMPARISON_REQUEST);
        message.setPeriod(period);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Refresh analytics data
     */
    public CompletableFuture<AnalyticsViewModel> refreshAnalytics() {
        clearCache();
        return getAnalytics("all");
    }
    
    /**
     * Export analytics data
     */
    public CompletableFuture<String> exportAnalytics(String format, String period) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        AnalyticsMessage message = new AnalyticsMessage();
        message.setType(AnalyticsMessage.AnalyticsMessageType.EXPORT_ANALYTICS_REQUEST);
        message.setExportFormat(format);
        message.setPeriod(period);
        
        pendingFutures.put(message.getMessageId(), future);
        
        webSocketClient.sendMessage(message);
        return future;
    }
    
    /**
     * Handle incoming WebSocket messages
     */
    @SuppressWarnings("unchecked")
    private void handleAnalyticsMessage(AnalyticsMessage message) {
        switch (message.getType()) {
            case GET_ANALYTICS_RESPONSE -> {
                CompletableFuture<AnalyticsViewModel> future = 
                    (CompletableFuture<AnalyticsViewModel>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    if (message.isSuccess()) {
                        Platform.runLater(() -> {
                            AnalyticsViewModel viewModel = convertToViewModel(message.getAnalytics());
                            updateCache(viewModel);
                            future.complete(viewModel);
                        });
                    } else {
                        future.completeExceptionally(new Exception(message.getErrorMessage()));
                    }
                }
            }
            
            case GET_CATEGORY_ANALYTICS_RESPONSE -> {
                CompletableFuture<AnalyticsViewModel> future = 
                    (CompletableFuture<AnalyticsViewModel>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    if (message.isSuccess()) {
                        Platform.runLater(() -> {
                            AnalyticsViewModel viewModel = convertToViewModel(message.getAnalytics());
                            future.complete(viewModel);
                        });
                    } else {
                        future.completeExceptionally(new Exception(message.getErrorMessage()));
                    }
                }
            }
            
            case GET_PERFORMANCE_COMPARISON_RESPONSE -> {
                CompletableFuture<AnalyticsViewModel> future = 
                    (CompletableFuture<AnalyticsViewModel>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    if (message.isSuccess()) {
                        Platform.runLater(() -> {
                            AnalyticsViewModel viewModel = convertToViewModel(message.getAnalytics());
                            future.complete(viewModel);
                        });
                    } else {
                        future.completeExceptionally(new Exception(message.getErrorMessage()));
                    }
                }
            }
            
            case EXPORT_ANALYTICS_RESPONSE -> {
                CompletableFuture<String> future = 
                    (CompletableFuture<String>) pendingFutures.remove(message.getMessageId());
                
                if (future != null) {
                    if (message.isSuccess()) {
                        future.complete(message.getExportData());
                    } else {
                        future.completeExceptionally(new Exception(message.getErrorMessage()));
                    }
                }
            }
        }
    }
    
    /**
     * Convert DTO to ViewModel
     */
    private AnalyticsViewModel convertToViewModel(AnalyticsDTO dto) {
        if (dto == null) return new AnalyticsViewModel();
        
        AnalyticsViewModel viewModel = new AnalyticsViewModel();
        
        // User stats
        if (dto.getUserStats() != null) {
            var userStats = dto.getUserStats();
            viewModel.setTotalListings(userStats.getTotalListings());
            viewModel.setActiveListings(userStats.getActiveListings());
            viewModel.setCompletedListings(userStats.getCompletedListings());
            viewModel.setTotalOffersMade(userStats.getTotalOffersMade());
            viewModel.setTotalOffersReceived(userStats.getTotalOffersReceived());
            viewModel.setAcceptedOffers(userStats.getAcceptedOffers());
            viewModel.setAverageRating(userStats.getAverageRating());
            viewModel.setTotalReviews(userStats.getTotalReviews());
            viewModel.setTotalEarnings(userStats.getTotalEarnings());
            viewModel.setMemberSince(userStats.getMemberSince());
            viewModel.setTotalViews(userStats.getTotalViews());
            viewModel.setTotalFavorites(userStats.getTotalFavorites());
        }
        
        // Performance metrics
        if (dto.getPerformanceMetrics() != null) {
            var metrics = dto.getPerformanceMetrics();
            viewModel.setListingSuccessRate(metrics.getListingSuccessRate());
            viewModel.setOfferAcceptanceRate(metrics.getOfferAcceptanceRate());
            viewModel.setAverageTimeToSell(metrics.getAverageTimeToSell());
            viewModel.setCustomerSatisfactionRate(metrics.getCustomerSatisfactionRate());
            viewModel.setRepeatCustomerRate(metrics.getRepeatCustomerRate());
            viewModel.setRatingTrend(metrics.getRatingTrend());
        }
        
        // Time series data
        if (dto.getListingStats() != null) {
            viewModel.getListingStats().clear();
            dto.getListingStats().forEach(ts -> 
                viewModel.getListingStats().add(new AnalyticsViewModel.TimeSeriesData(
                    ts.getTimestamp(), ts.getValue(), ts.getLabel())));
        }
        
        if (dto.getOfferStats() != null) {
            viewModel.getOfferStats().clear();
            dto.getOfferStats().forEach(ts -> 
                viewModel.getOfferStats().add(new AnalyticsViewModel.TimeSeriesData(
                    ts.getTimestamp(), ts.getValue(), ts.getLabel())));
        }
        
        if (dto.getReviewStats() != null) {
            viewModel.getReviewStats().clear();
            dto.getReviewStats().forEach(ts -> 
                viewModel.getReviewStats().add(new AnalyticsViewModel.TimeSeriesData(
                    ts.getTimestamp(), ts.getValue(), ts.getLabel())));
        }
        
        if (dto.getEarningsStats() != null) {
            viewModel.getEarningsStats().clear();
            dto.getEarningsStats().forEach(ts -> 
                viewModel.getEarningsStats().add(new AnalyticsViewModel.TimeSeriesData(
                    ts.getTimestamp(), ts.getValue(), ts.getLabel())));
        }
        
        // Category breakdown
        if (dto.getCategoryBreakdown() != null) {
            viewModel.getCategoryBreakdown().clear();
            dto.getCategoryBreakdown().forEach(cat -> 
                viewModel.getCategoryBreakdown().add(new AnalyticsViewModel.CategoryStats(
                    cat.getCategory(), cat.getTotalListings(), cat.getActiveListings(),
                    cat.getCompletedListings(), cat.getTotalEarnings(), cat.getAverageRating())));
        }
        
        // Monthly breakdown
        if (dto.getMonthlyBreakdown() != null) {
            viewModel.getMonthlyBreakdown().clear();
            dto.getMonthlyBreakdown().forEach(month -> 
                viewModel.getMonthlyBreakdown().add(new AnalyticsViewModel.MonthlyStats(
                    month.getYear(), month.getMonth(), month.getTotalListings(),
                    month.getCompletedTransactions(), month.getEarnings(), month.getAverageRating())));
        }
        
        return viewModel;
    }
    
    /**
     * Check if cache is still valid
     */
    private boolean isCacheValid() {
        if (lastRefresh == null) return false;
        return LocalDateTime.now().minusMinutes(CACHE_DURATION_MINUTES).isBefore(lastRefresh);
    }
    
    /**
     * Update cache
     */
    private void updateCache(AnalyticsViewModel analytics) {
        this.cachedAnalytics = analytics;
        this.lastRefresh = LocalDateTime.now();
    }
    
    /**
     * Clear cache
     */
    public void clearCache() {
        this.cachedAnalytics = null;
        this.lastRefresh = null;
    }
    
    /**
     * Get cached analytics if available
     */
    public AnalyticsViewModel getCachedAnalytics() {
        return isCacheValid() ? cachedAnalytics : null;
    }
}