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
        
        
        AnalyticsDTO.PerformanceMetricsDTO userMetrics = analytics.getPerformanceMetrics();
        AnalyticsDTO.PerformanceMetricsDTO platformAverages = calculatePlatformAverages(startDate, endDate);
        
        
        
        
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
    
    
    
    private LocalDateTime calculateStartDate(String period, LocalDateTime endDate) {
        return switch (period.toLowerCase()) {
            case "week" -> endDate.minus(1, ChronoUnit.WEEKS);
            case "month" -> endDate.minus(1, ChronoUnit.MONTHS);
            case "quarter" -> endDate.minus(3, ChronoUnit.MONTHS);
            case "year" -> endDate.minus(1, ChronoUnit.YEARS);
            case "all" -> LocalDateTime.of(2020, 1, 1, 0, 0); 
            default -> endDate.minus(1, ChronoUnit.MONTHS);
        };
    }
    
    private AnalyticsDTO.UserStatsDTO generateUserStats(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        AnalyticsDTO.UserStatsDTO stats = new AnalyticsDTO.UserStatsDTO();
        
        
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            stats.setMemberSince(userOpt.get().getCreatedAt());
        }
        
        
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        stats.setTotalListings(userListings.size());
        stats.setActiveListings((int) userListings.stream()
            .filter(l -> l.getStatus() == ListingStatus.ACTIVE)
            .count());
        stats.setCompletedListings((int) userListings.stream()
            .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
            .count());
        
        
        List<OfferEntity> offersMade = offerRepository.findByUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        stats.setTotalOffersMade(offersMade.size());
        stats.setAcceptedOffers((int) offersMade.stream()
            .filter(o -> o.getStatus() == OfferStatus.ACCEPTED)
            .count());
        
        List<OfferEntity> offersReceived = offerRepository.findByListingCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        stats.setTotalOffersReceived(offersReceived.size());
        
        
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
        
        
        double totalEarnings = calculateTotalEarnings(userId, startDate, endDate);
        stats.setTotalEarnings(totalEarnings);
        
        
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
        
        
        if (!userListings.isEmpty()) {
            long completedListings = userListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
                .count();
            metrics.setListingSuccessRate((double) completedListings / userListings.size() * 100);
        }
        
        
        if (!offersReceived.isEmpty()) {
            long acceptedOffers = offersReceived.stream()
                .filter(o -> o.getStatus() == OfferStatus.ACCEPTED)
                .count();
            metrics.setOfferAcceptanceRate((double) acceptedOffers / offersReceived.size() * 100);
        }
        
        
        metrics.setAverageTimeToSell(calculateAverageTimeToSell(userListings));
        
        
        List<ReviewEntity> reviews = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        if (!reviews.isEmpty()) {
            double satisfactionRate = reviews.stream()
                .mapToDouble(ReviewEntity::getScore)
                .filter(score -> score >= 4) 
                .count() * 100.0 / reviews.size();
            metrics.setCustomerSatisfactionRate(satisfactionRate);
        }
        
        
        metrics.setRatingTrend(calculateRatingTrend(userId, startDate, endDate));
        
        
        metrics.setRepeatCustomerRate(calculateRepeatCustomerRate(userId, startDate, endDate));
        
        return metrics;
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateListingTimeSeriesData(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<AnalyticsDTO.TimeSeriesDataDTO> data = new ArrayList<>();
        
        
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
            
            
            monthStats.setEarnings(calculateMonthEarnings(userId, current, monthEnd));
            monthStats.setAverageRating(calculateMonthAverageRating(userId, current, monthEnd));
            
            breakdown.add(monthStats);
            current = current.plusMonths(1);
        }
        
        return breakdown;
    }
    
    
    
    private AnalyticsDTO.UserStatsDTO generateCategoryUserStats(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        
        
        return generateUserStats(userId, startDate, endDate); 
    }
    
    private AnalyticsDTO.PerformanceMetricsDTO generateCategoryPerformanceMetrics(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        
        return generatePerformanceMetrics(userId, startDate, endDate); 
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateCategoryListingTimeSeriesData(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        
        return generateListingTimeSeriesData(userId, startDate, endDate); 
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateCategoryOfferTimeSeriesData(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        
        return generateOfferTimeSeriesData(userId, startDate, endDate); 
    }
    
    private List<AnalyticsDTO.TimeSeriesDataDTO> generateCategoryEarningsTimeSeriesData(Long userId, String category, LocalDateTime startDate, LocalDateTime endDate) {
        
        return generateEarningsTimeSeriesData(userId, startDate, endDate); 
    }
    
    
    
    private double calculateTotalEarnings(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        
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
        
        
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        
        
        return userListings.size() * 10; 
    }
    
    private int calculateTotalFavorites(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        
        List<ListingEntity> userListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        
        
        long completedListings = userListings.stream()
            .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
            .count();
        
        return (int) (completedListings * 2); 
    }
    
    private double calculateAverageTimeToSell(List<ListingEntity> listings) {
        if (listings.isEmpty()) {
            return 0.0;
        }
        
        
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
        
        List<ReviewEntity> reviews = reviewRepository.findByReviewedUserIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        if (reviews.size() < 2) {
            return 0.0; 
        }
        
        
        reviews.sort((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));
        
        
        int midPoint = reviews.size() / 2;
        double firstHalfAvg = reviews.subList(0, midPoint).stream()
            .mapToDouble(ReviewEntity::getScore)
            .average()
            .orElse(0.0);
        
        double secondHalfAvg = reviews.subList(midPoint, reviews.size()).stream()
            .mapToDouble(ReviewEntity::getScore)
            .average()
            .orElse(0.0);
        
        return secondHalfAvg - firstHalfAvg; 
    }
    
    private double calculateRepeatCustomerRate(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        
        List<OfferEntity> offersReceived = offerRepository.findByListingCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate);
        
        if (offersReceived.isEmpty()) {
            return 0.0;
        }
        
        
        Map<Long, Long> offersByUser = offersReceived.stream()
            .collect(Collectors.groupingBy(
                offer -> offer.getUser().getId(),
                Collectors.counting()
            ));
        
        
        long repeatCustomers = offersByUser.values().stream()
            .filter(count -> count > 1)
            .count();
        
        return (double) repeatCustomers / offersByUser.size() * 100.0;
    }
    
    private double calculateDayEarnings(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        
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
        
        List<ListingEntity> categoryListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate).stream()
            .filter(listing -> {
                if (!listing.getStatus().equals(ListingStatus.COMPLETED) || 
                    !"SELL".equals(listing.getListingType())) {
                    return false;
                }
                
                
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
        
        List<ListingEntity> categoryListings = listingRepository.findByCreatorIdAndCreatedAtBetween(
            userId, startDate, endDate).stream()
            .filter(listing -> listing.getItems().stream()
                .anyMatch(item -> category.equalsIgnoreCase(item.getCategory())))
            .collect(Collectors.toList());
        
        if (categoryListings.isEmpty()) {
            return 0.0;
        }
        
        
        
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
        
        
        List<ListingEntity> allListings = listingRepository.findByCreatedAtBetween(startDate, endDate);
        List<OfferEntity> allOffers = offerRepository.findByCreatedAtBetween(startDate, endDate);
        List<ReviewEntity> allReviews = reviewRepository.findByCreatedAtBetween(startDate, endDate);
        
        
        if (!allListings.isEmpty()) {
            long completedListings = allListings.stream()
                .filter(l -> l.getStatus() == ListingStatus.COMPLETED)
                .count();
            averages.setListingSuccessRate((double) completedListings / allListings.size() * 100);
        } else {
            averages.setListingSuccessRate(0.0);
        }
        
        
        if (!allOffers.isEmpty()) {
            long acceptedOffers = allOffers.stream()
                .filter(o -> o.getStatus() == OfferStatus.ACCEPTED || 
                           o.getStatus() == OfferStatus.COMPLETED)
                .count();
            averages.setOfferAcceptanceRate((double) acceptedOffers / allOffers.size() * 100);
        } else {
            averages.setOfferAcceptanceRate(0.0);
        }
        
        
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
        
        
        if (!allReviews.isEmpty()) {
            double satisfactionRate = allReviews.stream()
                .mapToDouble(ReviewEntity::getScore)
                .filter(score -> score >= 4) 
                .count() * 100.0 / allReviews.size();
            averages.setCustomerSatisfactionRate(satisfactionRate);
        } else {
            averages.setCustomerSatisfactionRate(0.0);
        }
        
        
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
    
    
    
    private String generateCSVExport(AnalyticsDTO analytics) {
        StringBuilder csv = new StringBuilder();
        csv.append("Analytics Export\n");
        csv.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
        
        
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
        
        return "PDF export data for analytics"; 
    }
    
    private String generateExcelExport(AnalyticsDTO analytics) {
        
        return "Excel export data for analytics"; 
    }
}