package com.uninaswap.server.service;

import com.uninaswap.common.dto.AnalyticsDTO;
import com.uninaswap.common.enums.ListingStatus;
import com.uninaswap.common.enums.OfferStatus;
import com.uninaswap.server.entity.*;
import com.uninaswap.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private OfferRepository offerRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    /**
     * Get comprehensive analytics for a user within a specific time period
     */
    public AnalyticsDTO getUserAnalytics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Generating analytics for user {} from {} to {}", userId, startDate, endDate);
        
        AnalyticsDTO analytics = new AnalyticsDTO();
        
        // Generate all analytics components
        analytics.setUserStats(generateUserStats(userId, startDate, endDate));
        analytics.setPerformanceMetrics(generatePerformanceMetrics(userId, startDate, endDate));
        analytics.setListingStats(generateListingTimeSeriesData(userId, startDate, endDate));
        analytics.setOfferStats(generateOfferTimeSeriesData(userId, startDate, endDate));
        analytics.setReviewStats(generateReviewTimeSeriesData(userId, startDate, endDate));
        analytics.setEarningsStats(generateEarningsTimeSeriesData(userId, startDate, endDate));
        analytics.setCategoryBreakdown(generateCategoryBreakdown(userId, startDate, endDate));
        analytics.setMonthlyBreakdown(generateMonthlyBreakdown(userId, startDate, endDate));
        
        return analytics;
    }
    
    /**
     * Get analytics for a user by period (week, month, quarter, year, all)
     */
    public AnalyticsDTO getUserAnalyticsByPeriod(Long userId, String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(period, endDate);
        
        return getUserAnalytics(userId, startDate, endDate);
    }
    
    /**
     * Get category-specific analytics for a user
     */
    public AnalyticsDTO getUserCategoryAnalytics(Long userId, String category, String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(period, endDate);
        
        AnalyticsDTO analytics = new AnalyticsDTO();
        
        // Generate category-filtered analytics
        analytics.setUserStats(generateCategoryUserStats(userId, category, startDate, endDate));
        analytics.setPerformanceMetrics(generateCategoryPerformanceMetrics(userId, category, startDate, endDate));
        analytics.setListingStats(generateCategoryListingTimeSeriesData(userId, category, startDate, endDate));
        analytics.setOfferStats(generateCategoryOfferTimeSeriesData(userId, category, startDate, endDate));
        analytics.setEarningsStats(generateCategoryEarningsTimeSeriesData(userId, category, startDate, endDate));
        
        return analytics;
    }
    
    /**
     * Get performance comparison with platform averages
     */
    public AnalyticsDTO getUserPerformanceComparison(Long userId, String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(period, endDate);
        
        AnalyticsDTO analytics = getUserAnalytics(userId, startDate, endDate);
        
        // Add comparison metrics (platform averages)
        AnalyticsDTO.PerformanceMetricsDTO userMetrics = analytics.getPerformanceMetrics();
        AnalyticsDTO.PerformanceMetricsDTO platformAverages = calculatePlatformAverages(startDate, endDate);
        
        // You could enhance this to include comparison data
        // For now, we'll just return the user analytics
        
        return analytics;
    }
    
    /**
     * Export analytics data in specified format
     */
    public String exportUserAnalytics(Long userId, String format, String period) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(period, endDate);
        
        AnalyticsDTO analytics = getUserAnalytics(userId, startDate, endDate);
        
        switch (format.toLowerCase()) {
            case "csv":
                return generateCSVExport(analytics);
            case "pdf":
                return generatePDFExport(analytics);
            case "excel":
                return generateExcelExport(analytics);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }
    
    // Private helper methods
    
    private LocalDateTime calculateStartDate(String period, LocalDateTime endDate) {
        return switch (period.toLowerCase()) {
            case "week" -> endDate.minus(1, ChronoUnit.WEEKS);
            case "month" -> endDate.minus(1, ChronoUnit.MONTHS);
            case "quarter" -> endDate.minus(3, ChronoUnit.MONTHS);
            case "year" -> endDate.minus(1, ChronoUnit.YEARS);
            case "all" -> LocalDateTime.of(2020, 1, 1, 0, 0); // Platform start date
            default -> endDate.minus(1, ChronoUnit.MONTHS);
        };
    }
    
    private AnalyticsDTO.UserStatsDTO generateUserStats(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        AnalyticsDTO.UserStatsDTO stats = new AnalyticsDTO.UserStatsDTO();
        
        // Get user registration date
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            stats.setMemberSince(userOpt.get().getCreatedAt());
        }
        
        // Listing statistics
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        stats.setTotalListings(userListings.size());
        stats.setActiveListings((int) userListings.stream()
            .filter(l -> l.getStatus() == ListingStatus.ACTIVE)
            .count());
        stats.setCompletedListings((int) userListings.stream()
            .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
            .count());
        
        // Offer statistics
        List<OfferEntity> offersMade = offerRepository.findByUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        stats.setTotalOffersMade(offersMade.size());
        stats.setAcceptedOffers((int) offersMade.stream()
            .filter(o -> o.getStatus() == OfferStatus.ACCEPTED)
            .count());
        
        List<OfferEntity> offersReceived = offerRepository.findByListingCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        stats.setTotalOffersReceived(offersReceived.size());
        
        // Review and rating statistics
        List<ReviewEntity> reviewsReceived = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        stats.setTotalReviews(reviewsReceived.size());
        
        if (!reviewsReceived.isEmpty()) {
            double averageRating = reviewsReceived.stream()
                .mapToDouble(ReviewEntity::getScore)
                .average()
                .orElse(0.0);
            stats.setAverageRating(averageRating);
        }
        
        // Calculate earnings (mock calculation - you'd implement based on your business logic)
        double totalEarnings = calculateTotalEarnings(userId, startDate, endDate);
        stats.setTotalEarnings(totalEarnings);
        
        // Views and favorites (mock data - you'd implement based on your tracking)
        stats.setTotalViews(calculateTotalViews(userId, startDate, endDate));
        stats.setTotalFavorites(calculateTotalFavorites(userId, startDate, endDate));
        
        return stats;
    }
    
    private AnalyticsDTO.PerformanceMetricsDTO generatePerformanceMetrics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        AnalyticsDTO.PerformanceMetricsDTO metrics = new AnalyticsDTO.PerformanceMetricsDTO();
        
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        List<OfferEntity> userOffers = offerRepository.findByUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        List<OfferEntity> offersReceived = offerRepository.findByListingCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        // Listing success rate (completed / total)
        if (!userListings.isEmpty()) {
            long completedListings = userListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
                .count();
            metrics.setListingSuccessRate((double) completedListings / userListings.size() * 100);
        }
        
        // Offer acceptance rate
        if (!offersReceived.isEmpty()) {
            long acceptedOffers = offersReceived.stream()
                .filter(o -> o.getStatus() == OfferStatus.ACCEPTED)
                .count();
            metrics.setOfferAcceptanceRate((double) acceptedOffers / offersReceived.size() * 100);
        }
        
        // Average time to sell (mock calculation)
        metrics.setAverageTimeToSell(calculateAverageTimeToSell(userListings));
        
        // Customer satisfaction rate (based on reviews)
        List<ReviewEntity> reviews = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        if (!reviews.isEmpty()) {
            double satisfactionRate = reviews.stream()
                .mapToDouble(ReviewEntity::getScore)
                .filter(score -> score >= 4) // 4+ stars considered satisfied
                .count() * 100.0 / reviews.size();
            metrics.setCustomerSatisfactionRate(satisfactionRate);
        }
        
        // Rating trend (mock calculation)
        metrics.setRatingTrend(calculateRatingTrend(userId, startDate, endDate));
        
        // Repeat customer rate (mock calculation)
        metrics.setRepeatCustomerRate(calculateRepeatCustomerRate(userId, startDate, endDate));
        
        return metrics;
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateListingTimeSeriesData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AnalyticsDTO.TimeSeriesDataDTO> data = new ArrayList<>();
        
        // Generate daily data points
        LocalDateTime current = startDate;
        while (current.isBefore(endDate)) {
            LocalDateTime dayEnd = current.plusDays(1);
            
            long listingsCount = listingRepository.countByCreatorIdAndCreatedAtBetween(
                userId, current, dayEnd);
            
            data.add(new AnalyticsDTO.TimeSeriesDataDTO(
                current, 
                listingsCount, 
                current.toLocalDate().toString()
            ));
            
            current = current.plusDays(1);
        }
        
        return data;
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateOfferTimeSeriesData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AnalyticsDTO.TimeSeriesDataDTO> data = new ArrayList<>();
        
        LocalDateTime current = startDate;
        while (current.isBefore(endDate)) {
            LocalDateTime dayEnd = current.plusDays(1);
            
            long offersCount = offerRepository.countByUserIdAndCreatedAtBetween(
                userId, current, dayEnd);
            
            data.add(new AnalyticsDTO.TimeSeriesDataDTO(
                current, 
                offersCount, 
                current.toLocalDate().toString()
            ));
            
            current = current.plusDays(1);
        }
        
        return data;
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateReviewTimeSeriesData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AnalyticsDTO.TimeSeriesDataDTO> data = new ArrayList<>();
        
        LocalDateTime current = startDate;
        while (current.isBefore(endDate)) {
            LocalDateTime dayEnd = current.plusDays(1);
            
            List<ReviewEntity> dayReviews = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
                userId, current, dayEnd);
            
            double averageRating = dayReviews.stream()
                .mapToDouble(ReviewEntity::getScore)
                .average()
                .orElse(0.0);
            
            data.add(new AnalyticsDTO.TimeSeriesDataDTO(
                current, 
                averageRating, 
                current.toLocalDate().toString()
            ));
            
            current = current.plusDays(1);
        }
        
        return data;
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateEarningsTimeSeriesData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AnalyticsDTO.TimeSeriesDataDTO> data = new ArrayList<>();
        
        LocalDateTime current = startDate;
        while (current.isBefore(endDate)) {
            LocalDateTime dayEnd = current.plusDays(1);
            
            // Mock earnings calculation - implement based on your business logic
            double dayEarnings = calculateDayEarnings(userId, current, dayEnd);
            
            data.add(new AnalyticsDTO.TimeSeriesDataDTO(
                current, 
                dayEarnings, 
                current.toLocalDate().toString()
            ));
            
            current = current.plusDays(1);
        }
        
        return data;
    }
    
    private List<AnalyticsDTO.CategoryStatsDTO> generateCategoryBreakdown(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AnalyticsDTO.CategoryStatsDTO> breakdown = new ArrayList<>();
        
        // Get all listings grouped by category
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        Map<String, List<ListingEntity>> categoryGroups = userListings.stream()
        .collect(Collectors.groupingBy((Function<ListingEntity, String>) listing -> 
            listing.getItems().isEmpty() ? "other" : 
            listing.getItems().get(0).getCategory().toLowerCase()));
        
        for (Map.Entry<String, List<ListingEntity>> entry : categoryGroups.entrySet()) {
            String category = entry.getKey();
            List<ListingEntity> categoryListings = entry.getValue();
            
            AnalyticsDTO.CategoryStatsDTO categoryStats = new AnalyticsDTO.CategoryStatsDTO();
            categoryStats.setCategory(category);
            categoryStats.setTotalListings(categoryListings.size());
            categoryStats.setActiveListings((int) categoryListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.ACTIVE)
                .count());
            categoryStats.setCompletedListings((int) categoryListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
                .count());
            
            // Mock calculations
            categoryStats.setTotalEarnings(calculateCategoryEarnings(userId, category, startDate, endDate));
            categoryStats.setAverageRating(calculateCategoryAverageRating(userId, category, startDate, endDate));
            
            breakdown.add(categoryStats);
        }
        
        return breakdown;
    }
    
    private List<AnalyticsDTO.MonthlyStatsDTO> generateMonthlyBreakdown(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AnalyticsDTO.MonthlyStatsDTO> breakdown = new ArrayList<>();
        
        LocalDateTime current = startDate.withDayOfMonth(1);
        while (current.isBefore(endDate)) {
            LocalDateTime monthEnd = current.plusMonths(1);
            
            List<ListingEntity> monthListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
                userId, current, monthEnd);
            
            AnalyticsDTO.MonthlyStatsDTO monthStats = new AnalyticsDTO.MonthlyStatsDTO();
            monthStats.setYear(current.getYear());
            monthStats.setMonth(current.getMonthValue());
            monthStats.setTotalListings(monthListings.size());
            monthStats.setCompletedTransactions((int) monthListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
                .count());
            
            // Mock calculations
            monthStats.setEarnings(calculateMonthEarnings(userId, current, monthEnd));
            monthStats.setAverageRating(calculateMonthAverageRating(userId, current, monthEnd));
            
            breakdown.add(monthStats);
            current = current.plusMonths(1);
        }
        
        return breakdown;
    }
    
    // Category-specific methods (simplified implementations)
    
    private AnalyticsDTO.UserStatsDTO generateCategoryUserStats(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Similar to generateUserStats but filtered by category
        // Implementation would filter queries by category
        return generateUserStats(userId, startDate, endDate); // Simplified
    }
    
    private AnalyticsDTO.PerformanceMetricsDTO generateCategoryPerformanceMetrics(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Similar to generatePerformanceMetrics but filtered by category
        return generatePerformanceMetrics(userId, startDate, endDate); // Simplified
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateCategoryListingTimeSeriesData(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Similar to generateListingTimeSeriesData but filtered by category
        return generateListingTimeSeriesData(userId, startDate, endDate); // Simplified
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateCategoryOfferTimeSeriesData(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Similar to generateOfferTimeSeriesData but filtered by category
        return generateOfferTimeSeriesData(userId, startDate, endDate); // Simplified
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateCategoryEarningsTimeSeriesData(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Similar to generateEarningsTimeSeriesData but filtered by category
        return generateEarningsTimeSeriesData(userId, startDate, endDate); // Simplified
    }
    
    // Replace mock calculation methods with actual implementations
    
    private double calculateTotalEarnings(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate earnings from completed sell listings
        List<ListingEntity> completedSellListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate).stream()
            .filter(listing -> listing.getStatus() == ListingStatus.COMPLETED && 
                             "SELL".equals(listing.getListingType()))
            .collect(Collectors.toList());
        
        return completedSellListings.stream()
            .mapToDouble(listing -> {
                try {
                    if (listing instanceof SellListingEntity sellListing) {
                        return sellListing.getPrice().doubleValue();
                    }
                } catch (Exception e) {
                    logger.warn("Error calculating earnings for listing {}: {}", listing.getId(), e.getMessage());
                }
                return 0.0;
            })
            .sum();
    }
    
    private int calculateTotalViews(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Sum up view counts from user's listings
        // Assuming you have a views/analytics table or field in listings
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        // If you have a separate analytics/views table, implement accordingly
        // For now, return the count of listings as a proxy for engagement
        return userListings.size() * 10; // Rough estimate - replace with actual view tracking
    }
    
    private int calculateTotalFavorites(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Count favorites on user's listings within the date range
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        // If you have a favorites table/repository, use it here
        // For now, use a heuristic based on completed listings
        long completedListings = userListings.stream()
            .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
            .count();
        
        return (int) (completedListings * 2); // Estimate: 2 favorites per completed listing
    }
    
    private double calculateAverageTimeToSell(List<ListingEntity> listings) {
        if (listings.isEmpty()) {
            return 0.0;
        }
        
        // Calculate average time between creation and completion
        return listings.stream()
            .filter(listing -> listing.getStatus() == ListingStatus.COMPLETED)
            .mapToLong(listing -> {
                if (listing.getUpdatedAt() != null && listing.getCreatedAt() != null) {
                    return ChronoUnit.DAYS.between(listing.getCreatedAt(), listing.getUpdatedAt());
                }
                return 0L;
            })
            .average()
            .orElse(0.0);
    }
    
    private double calculateRatingTrend(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Calculate the trend in user ratings over time
        List<ReviewEntity> reviews = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        if (reviews.size() < 2) {
            return 0.0; // No trend with less than 2 reviews
        }
        
        // Sort by creation date
        reviews.sort((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));
        
        // Calculate trend using first half vs second half of reviews
        int midPoint = reviews.size() / 2;
        double firstHalfAvg = reviews.subList(0, midPoint).stream()
            .mapToDouble(ReviewEntity::getScore)
            .average()
            .orElse(0.0);
        
        double secondHalfAvg = reviews.subList(midPoint, reviews.size()).stream()
            .mapToDouble(ReviewEntity::getScore)
            .average()
            .orElse(0.0);
        
        return secondHalfAvg - firstHalfAvg; // Positive = improving, negative = declining
    }
    
    private double calculateRepeatCustomerRate(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Find users who made multiple offers to this user's listings
        List<OfferEntity> offersReceived = offerRepository.findByListingCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        if (offersReceived.isEmpty()) {
            return 0.0;
        }
        
        // Group by offering user
        Map<Long, Long> offersByUser = offersReceived.stream()
            .collect(Collectors.groupingBy(
                offer -> offer.getUser().getId(),
                Collectors.counting()
            ));
        
        // Count users who made more than one offer
        long repeatCustomers = offersByUser.values().stream()
            .filter(count -> count > 1)
            .count();
        
        return (double) repeatCustomers / offersByUser.size() * 100.0;
    }
    
    private double calculateDayEarnings(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        // Get completed sell listings for this specific day
        List<ListingEntity> dayListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate).stream()
            .filter(listing -> listing.getStatus() == ListingStatus.COMPLETED && 
                             "SELL".equals(listing.getListingType()))
            .collect(Collectors.toList());
        
        return dayListings.stream()
            .mapToDouble(listing -> {
                try {
                    if (listing instanceof SellListingEntity sellListing) {
                        return sellListing.getPrice().doubleValue();
                    }
                } catch (Exception e) {
                    logger.warn("Error calculating day earnings for listing {}: {}", listing.getId(), e.getMessage());
                }
                return 0.0;
            })
            .sum();
    }
    
    private double calculateCategoryEarnings(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Get completed sell listings in specific category
        List<ListingEntity> categoryListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate).stream()
            .filter(listing -> {
                if (!listing.getStatus().equals(ListingStatus.COMPLETED) || 
                    !"SELL".equals(listing.getListingType())) {
                    return false;
                }
                
                // Check if listing belongs to the category
                return listing.getItems().stream()
                    .anyMatch(item -> category.equalsIgnoreCase(item.getCategory()));
            })
            .collect(Collectors.toList());
        
        return categoryListings.stream()
            .mapToDouble(listing -> {
                try {
                    if (listing instanceof SellListingEntity sellListing) {
                        return sellListing.getPrice().doubleValue();
                    }
                } catch (Exception e) {
                    logger.warn("Error calculating category earnings for listing {}: {}", listing.getId(), e.getMessage());
                }
                return 0.0;
            })
            .sum();
    }
    
    private double calculateCategoryAverageRating(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        // Get reviews for listings in specific category
        List<ListingEntity> categoryListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate).stream()
            .filter(listing -> listing.getItems().stream()
                .anyMatch(item -> category.equalsIgnoreCase(item.getCategory())))
            .collect(Collectors.toList());
        
        if (categoryListings.isEmpty()) {
            return 0.0;
        }
        
        // For each listing, find associated reviews (this would require linking reviews to listings)
        // For now, use overall user rating as approximation
        List<ReviewEntity> userReviews = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        return userReviews.stream()
            .mapToDouble(ReviewEntity::getScore)
            .average()
            .orElse(0.0);
    }
    
    private double calculateMonthEarnings(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return calculateTotalEarnings(userId, startDate, endDate);
    }
    
    private double calculateMonthAverageRating(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ReviewEntity> monthReviews = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        return monthReviews.stream()
            .mapToDouble(ReviewEntity::getScore)
            .average()
            .orElse(0.0);
    }
    
    private AnalyticsDTO.PerformanceMetricsDTO calculatePlatformAverages(LocalDateTime startDate, LocalDateTime endDate) {
        AnalyticsDTO.PerformanceMetricsDTO averages = new AnalyticsDTO.PerformanceMetricsDTO();
        
        // Calculate actual platform averages
        List<ListingEntity> allListings = listingRepository.findByCreatedAtBetween(startDate, endDate);
        List<OfferEntity> allOffers = offerRepository.findByCreatedAtBetween(startDate, endDate);
        List<ReviewEntity> allReviews = reviewRepository.findByCreatedAtBetween(startDate, endDate);
        
        // Platform listing success rate
        if (!allListings.isEmpty()) {
            long completedListings = allListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
                .count();
            averages.setListingSuccessRate((double) completedListings / allListings.size() * 100);
        } else {
            averages.setListingSuccessRate(0.0);
        }
        
        // Platform offer acceptance rate
        if (!allOffers.isEmpty()) {
            long acceptedOffers = allOffers.stream()
                .filter(o -> o.getStatus() == OfferStatus.ACCEPTED || 
                           o.getStatus() == OfferStatus.COMPLETED)
                .count();
            averages.setOfferAcceptanceRate((double) acceptedOffers / allOffers.size() * 100);
        } else {
            averages.setOfferAcceptanceRate(0.0);
        }
        
        // Platform average time to sell
        double avgTimeToSell = allListings.stream()
            .filter(listing -> listing.getStatus() == ListingStatus.COMPLETED)
            .mapToLong(listing -> {
                if (listing.getUpdatedAt() != null && listing.getCreatedAt() != null) {
                    return ChronoUnit.DAYS.between(listing.getCreatedAt(), listing.getUpdatedAt());
                }
                return 0L;
            })
            .average()
            .orElse(0.0);
        averages.setAverageTimeToSell(avgTimeToSell);
        
        // Platform customer satisfaction rate
        if (!allReviews.isEmpty()) {
            double satisfactionRate = allReviews.stream()
                .mapToDouble(ReviewEntity::getScore)
                .filter(score -> score >= 4) // 4+ stars considered satisfied
                .count() * 100.0 / allReviews.size();
            averages.setCustomerSatisfactionRate(satisfactionRate);
        } else {
            averages.setCustomerSatisfactionRate(0.0);
        }
        
        // Platform repeat customer rate (simplified calculation)
        Map<Long, Set<Long>> userInteractions = new HashMap<>();
        allOffers.forEach(offer -> {
            Long listingCreator = offer.getListing().getCreator().getId();
            Long offerUser = offer.getUser().getId();
            userInteractions.computeIfAbsent(listingCreator, k -> new HashSet<>()).add(offerUser);
        });
        
        double repeatRate = userInteractions.values().stream()
            .mapToDouble(users -> users.size() > 1 ? 1.0 : 0.0)
            .average()
            .orElse(0.0) * 100;
        averages.setRepeatCustomerRate(repeatRate);
        
        // Platform rating trend (compare first half vs second half of period)
        if (allReviews.size() >= 2) {
            allReviews.sort((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));
            int midPoint = allReviews.size() / 2;
            
            double firstHalfAvg = allReviews.subList(0, midPoint).stream()
                .mapToDouble(ReviewEntity::getScore)
                .average()
                .orElse(0.0);
            
            double secondHalfAvg = allReviews.subList(midPoint, allReviews.size()).stream()
                .mapToDouble(ReviewEntity::getScore)
                .average()
                .orElse(0.0);
            
            averages.setRatingTrend(secondHalfAvg - firstHalfAvg);
        } else {
            averages.setRatingTrend(0.0);
        }
        
        return averages;
    }
    
    // Export methods (simplified implementations)
    
    private String generateCSVExport(AnalyticsDTO analytics) {
        StringBuilder csv = new StringBuilder();
        csv.append("Analytics Export\n");
        csv.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        // Add user stats
        AnalyticsDTO.UserStatsDTO userStats = analytics.getUserStats();
        if (userStats != null) {
            csv.append("User Statistics\n");
            csv.append("Total Listings,").append(userStats.getTotalListings()).append("\n");
            csv.append("Active Listings,").append(userStats.getActiveListings()).append("\n");
            csv.append("Completed Listings,").append(userStats.getCompletedListings()).append("\n");
            csv.append("Total Earnings,").append(userStats.getTotalEarnings()).append("\n");
            csv.append("Average Rating,").append(userStats.getAverageRating()).append("\n");
        }
        
        return csv.toString();
    }
    
    private String generatePDFExport(AnalyticsDTO analytics) {
        // Mock implementation - in reality, you'd use a PDF library like iText
        return "PDF export data for analytics"; // Base64 encoded PDF would go here
    }
    
    private String generateExcelExport(AnalyticsDTO analytics) {
        // Mock implementation - in reality, you'd use Apache POI
        return "Excel export data for analytics"; // Base64 encoded Excel would go here
    }
}