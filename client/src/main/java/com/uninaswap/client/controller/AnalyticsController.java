// filepath: /home/hat/Desktop/edu_workspace/UninaSwap/UninaSwap/client/src/main/java/com/uninaswap/client/controller/AnalyticsController.java
package com.uninaswap.client.controller;

import com.uninaswap.client.service.AnalyticsService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.viewmodel.AnalyticsViewModel;
import com.uninaswap.client.util.AlertHelper;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AnalyticsController implements Refreshable {

    // Header Controls
    @FXML private ComboBox<String> periodComboBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private ComboBox<String> exportFormatComboBox;
    
    // Summary Cards
    @FXML private Text totalListingsValue;
    @FXML private Text activeListingsValue;
    @FXML private Text completedListingsValue;
    @FXML private Text totalEarningsValue;
    @FXML private Text averageRatingValue;
    @FXML private Text totalReviewsValue;
    @FXML private Text memberSinceValue;
    @FXML private Text totalViewsValue;
    @FXML private Text totalFavoritesValue;
    
    // Performance Metrics
    @FXML private ProgressBar successRateProgress;
    @FXML private Text successRateValue;
    @FXML private ProgressBar acceptanceRateProgress;
    @FXML private Text acceptanceRateValue;
    @FXML private Text averageTimeToSellValue;
    @FXML private ProgressBar satisfactionRateProgress;
    @FXML private Text satisfactionRateValue;
    @FXML private Text ratingTrendValue;
    
    // Charts
    @FXML private LineChart<String, Number> activityChart;
    @FXML private BarChart<String, Number> earningsChart;
    @FXML private PieChart categoryPieChart;
    @FXML private AreaChart<String, Number> ratingTrendChart;
    
    // Tables
    @FXML private TableView<AnalyticsViewModel.CategoryStats> categoryTable;
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, String> categoryNameColumn;
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, Number> categoryListingsColumn;
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, Number> categoryEarningsColumn;
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, Number> categoryRatingColumn;
    
    @FXML private TableView<AnalyticsViewModel.MonthlyStats> monthlyTable;
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, String> monthColumn;
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, Number> monthlyListingsColumn;
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, Number> monthlyEarningsColumn;
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, Number> monthlyRatingColumn;
    
    // Tabs
    @FXML private TabPane mainTabPane;
    @FXML private Tab overviewTab;
    @FXML private Tab chartsTab;
    @FXML private Tab detailsTab;
    
    // Loading
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox contentArea;
    
    // Services
    private final AnalyticsService analyticsService = AnalyticsService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    
    // Data
    private AnalyticsViewModel currentAnalytics;
    
    @FXML
    public void initialize() {
        setupControls();
        setupCharts();
        setupTables();
        setupEventHandlers();
        refreshUI();
        loadAnalytics();
    }
    
    private void setupControls() {
        // Period combo box - using internal keys that will be converted by StringConverter
        periodComboBox.setItems(FXCollections.observableArrayList(
            "week", "month", "quarter", "year", "all"
        ));
        periodComboBox.setValue("month");
        
        // Category combo box - using internal keys
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "all", "electronics", "books", "clothing", "home", "sports", "toys", "other"
        ));
        categoryComboBox.setValue("all");
        
        // Export format combo box
        exportFormatComboBox.setItems(FXCollections.observableArrayList(
            "PDF", "CSV", "Excel"
        ));
        exportFormatComboBox.setValue("PDF");
        
        // Date pickers
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        
        // Setup period combo box converter
        periodComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String period) {
                return switch (period) {
                    case "week" -> localeService.getMessage("analytics.period.week", "Last Week");
                    case "month" -> localeService.getMessage("analytics.period.month", "Last Month");
                    case "quarter" -> localeService.getMessage("analytics.period.quarter", "Last Quarter");
                    case "year" -> localeService.getMessage("analytics.period.year", "Last Year");
                    case "all" -> localeService.getMessage("analytics.period.all", "All Time");
                    default -> period;
                };
            }
            
            @Override
            public String fromString(String string) {
                // Reverse mapping if needed
                return string;
            }
        });
        
        // Setup category combo box converter
        categoryComboBox.setConverter(new StringConverter<String>() {
            @Override
            public String toString(String category) {
                return switch (category) {
                    case "all" -> localeService.getMessage("analytics.category.all", "All Categories");
                    case "electronics" -> localeService.getMessage("category.electronics", "Electronics");
                    case "books" -> localeService.getMessage("category.books", "Books");
                    case "clothing" -> localeService.getMessage("category.clothing", "Clothing");
                    case "home" -> localeService.getMessage("category.home", "Home & Garden");
                    case "sports" -> localeService.getMessage("category.sports", "Sports");
                    case "toys" -> localeService.getMessage("category.toys.games", "Toys & Games");
                    case "other" -> localeService.getMessage("category.other", "Other");
                    default -> category;
                };
            }
            
            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }
    
    private void setupCharts() {
        // Activity Chart Setup
        activityChart.setTitle(localeService.getMessage("analytics.chart.activity", "Activity Over Time"));
        activityChart.setLegendSide(Side.BOTTOM);
        activityChart.setCreateSymbols(true);
        activityChart.setAnimated(true);
        
        // Earnings Chart Setup
        earningsChart.setTitle(localeService.getMessage("analytics.chart.earnings", "Earnings Over Time"));
        earningsChart.setLegendSide(Side.BOTTOM);
        earningsChart.setAnimated(true);
        
        // Category Pie Chart Setup
        categoryPieChart.setTitle(localeService.getMessage("analytics.chart.category", "Listings by Category"));
        categoryPieChart.setLegendSide(Side.RIGHT);
        categoryPieChart.setAnimated(true);
        
        // Rating Trend Chart Setup
        ratingTrendChart.setTitle(localeService.getMessage("analytics.chart.rating", "Rating Trend"));
        ratingTrendChart.setLegendSide(Side.BOTTOM);
        ratingTrendChart.setCreateSymbols(true);
        ratingTrendChart.setAnimated(true);
    }
    
    private void setupTables() {
        // Category Table
        categoryNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory()));
        categoryListingsColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getTotalListings()));
        categoryEarningsColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getTotalEarnings()));
        categoryRatingColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getAverageRating()));
        
        // Format earnings column
        categoryEarningsColumn.setCellFactory(col -> new TableCell<AnalyticsViewModel.CategoryStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(localeService.getMessage("analytics.currency.format", "€%.2f").formatted(item.doubleValue()));
                }
            }
        });
        
        // Format rating column
        categoryRatingColumn.setCellFactory(col -> new TableCell<AnalyticsViewModel.CategoryStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(localeService.getMessage("analytics.rating.format", "%.1f/5.0").formatted(item.doubleValue()));
                }
            }
        });
        
        // Monthly Table
        monthColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedPeriod()));
        monthlyListingsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTotalListings()));
        monthlyEarningsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getEarnings()));
        monthlyRatingColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAverageRating()));
        
        // Format monthly earnings column
        monthlyEarningsColumn.setCellFactory(col -> new TableCell<AnalyticsViewModel.MonthlyStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(localeService.getMessage("analytics.currency.format", "€%.2f").formatted(item.doubleValue()));
                }
            }
        });
        
        // Format monthly rating column
        monthlyRatingColumn.setCellFactory(col -> new TableCell<AnalyticsViewModel.MonthlyStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(localeService.getMessage("analytics.rating.format", "%.1f/5.0").formatted(item.doubleValue()));
                }
            }
        });
    }
    
    private void setupEventHandlers() {
        // Period change
        periodComboBox.setOnAction(e -> {
            if (!periodComboBox.getValue().equals("custom")) {
                loadAnalytics();
            }
        });
        
        // Category change
        categoryComboBox.setOnAction(e -> loadAnalytics());
        
        // Date picker changes (for custom range)
        startDatePicker.setOnAction(e -> {
            if (periodComboBox.getValue().equals("custom")) {
                loadAnalytics();
            }
        });
        
        endDatePicker.setOnAction(e -> {
            if (periodComboBox.getValue().equals("custom")) {
                loadAnalytics();
            }
        });
        
        // Refresh button
        refreshButton.setOnAction(e -> refreshAnalytics());
        
        // Export button
        exportButton.setOnAction(e -> exportAnalytics());
    }
    
    private void loadAnalytics() {
        showLoading(true);
        
        String period = periodComboBox.getValue();
        String category = categoryComboBox.getValue();
        
        CompletableFuture<AnalyticsViewModel> future;
        
        if ("custom".equals(period)) {
            LocalDateTime start = startDatePicker.getValue().atStartOfDay();
            LocalDateTime end = endDatePicker.getValue().atTime(23, 59, 59);
            future = analyticsService.getAnalytics(start, end);
        } else if (!"all".equals(category)) {
            future = analyticsService.getCategoryAnalytics(category, period);
        } else {
            future = analyticsService.getAnalytics(period);
        }
        
        future.thenAccept(analytics -> Platform.runLater(() -> {
            currentAnalytics = analytics;
            updateUI(analytics);
            showLoading(false);
        }))
        .exceptionally(ex -> {
            Platform.runLater(() -> {
                showLoading(false);
                showError(localeService.getMessage("analytics.error.load.failed", "Failed to load analytics: {0}"), ex.getMessage());
            });
            return null;
        });
    }
    
    private void refreshAnalytics() {
        analyticsService.clearCache();
        loadAnalytics();
    }
    
    private void exportAnalytics() {
        if (currentAnalytics == null) {
            AlertHelper.showErrorAlert(
                localeService.getMessage("analytics.export.error.title", "Export Error"),
                localeService.getMessage("analytics.export.error.no.data", "No Data"),
                localeService.getMessage("analytics.export.error.no.data.message", "No analytics data to export")
            );
            return;
        }
        
        String format = exportFormatComboBox.getValue().toLowerCase();
        String period = periodComboBox.getValue();
        
        analyticsService.exportAnalytics(format, period)
            .thenAccept(exportData -> Platform.runLater(() -> {
                // Handle export success - could save file or show download link
                AlertHelper.showInformationAlert(
                    localeService.getMessage("analytics.export.success.title", "Export Success"),
                    localeService.getMessage("analytics.export.success.message", "Export Completed"),
                    localeService.getMessage("analytics.export.success.details", "Analytics data has been exported successfully")
                );
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError(localeService.getMessage("analytics.error.export.failed", "Failed to export analytics: {0}"), ex.getMessage());
                });
                return null;
            });
    }
    
    private void updateUI(AnalyticsViewModel analytics) {
        updateSummaryCards(analytics);
        updatePerformanceMetrics(analytics);
        updateCharts(analytics);
        updateTables(analytics);
    }
    
    private void updateSummaryCards(AnalyticsViewModel analytics) {
        totalListingsValue.setText(String.valueOf(analytics.getTotalListings()));
        activeListingsValue.setText(String.valueOf(analytics.getActiveListings()));
        completedListingsValue.setText(String.valueOf(analytics.getCompletedListings()));
        totalEarningsValue.setText(localeService.getMessage("analytics.currency.format", "€%.2f").formatted(analytics.getTotalEarnings()));
        averageRatingValue.setText(localeService.getMessage("analytics.rating.format", "%.1f/5.0").formatted(analytics.getAverageRating()));
        totalReviewsValue.setText(String.valueOf(analytics.getTotalReviews()));
        totalViewsValue.setText(String.valueOf(analytics.getTotalViews()));
        totalFavoritesValue.setText(String.valueOf(analytics.getTotalFavorites()));
        
        if (analytics.getMemberSince() != null) {
            memberSinceValue.setText(analytics.getMemberSince().format(
                DateTimeFormatter.ofPattern(localeService.getMessage("analytics.date.format", "dd/MM/yyyy"))));
        }
    }
    
    private void updatePerformanceMetrics(AnalyticsViewModel analytics) {
        // Success Rate
        double successRate = analytics.getListingSuccessRate() / 100.0;
        successRateProgress.setProgress(successRate);
        successRateValue.setText(localeService.getMessage("analytics.percentage.format", "%.1f%%").formatted(analytics.getListingSuccessRate()));
        
        // Acceptance Rate
        double acceptanceRate = analytics.getOfferAcceptanceRate() / 100.0;
        acceptanceRateProgress.setProgress(acceptanceRate);
        acceptanceRateValue.setText(localeService.getMessage("analytics.percentage.format", "%.1f%%").formatted(analytics.getOfferAcceptanceRate()));
        
        // Average Time to Sell
        averageTimeToSellValue.setText(localeService.getMessage("analytics.days.format", "%.1f days").formatted(analytics.getAverageTimeToSell()));
        
        // Satisfaction Rate
        double satisfactionRate = analytics.getCustomerSatisfactionRate() / 100.0;
        satisfactionRateProgress.setProgress(satisfactionRate);
        satisfactionRateValue.setText(localeService.getMessage("analytics.percentage.format", "%.1f%%").formatted(analytics.getCustomerSatisfactionRate()));
        
        // Rating Trend
        double trend = analytics.getRatingTrend();
        String trendText = trend > 0 ? 
            localeService.getMessage("analytics.trend.positive.format", "+%.2f").formatted(trend) : 
            localeService.getMessage("analytics.trend.format", "%.2f").formatted(trend);
        ratingTrendValue.setText(trendText);
        ratingTrendValue.getStyleClass().removeAll("positive-trend", "negative-trend", "neutral-trend");
        if (trend > 0) {
            ratingTrendValue.getStyleClass().add("positive-trend");
        } else if (trend < 0) {
            ratingTrendValue.getStyleClass().add("negative-trend");
        } else {
            ratingTrendValue.getStyleClass().add("neutral-trend");
        }
    }
    
    private void updateCharts(AnalyticsViewModel analytics) {
        updateActivityChart(analytics);
        updateEarningsChart(analytics);
        updateCategoryPieChart(analytics);
        updateRatingTrendChart(analytics);
    }
    
    private void updateActivityChart(AnalyticsViewModel analytics) {
        activityChart.getData().clear();
        
        // Listings series
        XYChart.Series<String, Number> listingSeries = new XYChart.Series<>();
        listingSeries.setName(localeService.getMessage("analytics.chart.listings", "Listings"));
        
        analytics.getListingStats().forEach(data -> {
            listingSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        // Offers series
        XYChart.Series<String, Number> offerSeries = new XYChart.Series<>();
        offerSeries.setName(localeService.getMessage("analytics.chart.offers", "Offers"));
        
        analytics.getOfferStats().forEach(data -> {
            offerSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        activityChart.getData().addAll(listingSeries, offerSeries);
    }
    
    private void updateEarningsChart(AnalyticsViewModel analytics) {
        earningsChart.getData().clear();
        
        XYChart.Series<String, Number> earningsSeries = new XYChart.Series<>();
        earningsSeries.setName(localeService.getMessage("analytics.chart.earnings", "Earnings"));
        
        analytics.getEarningsStats().forEach(data -> {
            earningsSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        earningsChart.getData().add(earningsSeries);
    }
    
    private void updateCategoryPieChart(AnalyticsViewModel analytics) {
        categoryPieChart.getData().clear();
        
        analytics.getCategoryBreakdown().forEach(category -> {
            if (category.getTotalListings() > 0) {
                String categoryName = getCategoryDisplayName(category.getCategory());
                PieChart.Data slice = new PieChart.Data(
                    localeService.getMessage("analytics.pie.slice.format", "{0} ({1})").replace("{0}", categoryName).replace("{1}", String.valueOf(category.getTotalListings())),
                    category.getTotalListings()
                );
                categoryPieChart.getData().add(slice);
            }
        });
    }
    
    private void updateRatingTrendChart(AnalyticsViewModel analytics) {
        ratingTrendChart.getData().clear();
        
        XYChart.Series<String, Number> ratingSeries = new XYChart.Series<>();
        ratingSeries.setName(localeService.getMessage("analytics.chart.rating.trend", "Rating Trend"));
        
        analytics.getReviewStats().forEach(data -> {
            ratingSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        ratingTrendChart.getData().add(ratingSeries);
    }
    
    private void updateTables(AnalyticsViewModel analytics) {
        categoryTable.setItems(analytics.getCategoryBreakdown());
        monthlyTable.setItems(analytics.getMonthlyBreakdown());
    }
    
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        contentArea.setDisable(show);
    }
    
    private void showError(String messageTemplate, String... params) {
        String message = params.length > 0 ? messageTemplate.replace("{0}", params[0]) : messageTemplate;
        AlertHelper.showErrorAlert(
            localeService.getMessage("analytics.error.title", "Analytics Error"),
            localeService.getMessage("analytics.error.message", "Error loading analytics"),
            message
        );
    }
    
    private String getCategoryDisplayName(String categoryKey) {
        return switch (categoryKey) {
            case "electronics" -> localeService.getMessage("category.electronics", "Electronics");
            case "books" -> localeService.getMessage("category.books", "Books");
            case "clothing" -> localeService.getMessage("category.clothing", "Clothing");
            case "home" -> localeService.getMessage("category.home", "Home & Garden");
            case "sports" -> localeService.getMessage("category.sports", "Sports");
            case "toys" -> localeService.getMessage("category.toys.games", "Toys & Games");
            case "other" -> localeService.getMessage("category.other", "Other");
            default -> categoryKey;
        };
    }
    
    @Override
    public void refreshUI() {
        // Update button labels
        if (refreshButton != null) {
            refreshButton.setText(localeService.getMessage("analytics.refresh", "Refresh"));
        }
        if (exportButton != null) {
            exportButton.setText(localeService.getMessage("analytics.export", "Export"));
        }
        
        // Update tab labels
        if (overviewTab != null) {
            overviewTab.setText(localeService.getMessage("analytics.tab.overview", "Overview"));
        }
        if (chartsTab != null) {
            chartsTab.setText(localeService.getMessage("analytics.tab.charts", "Charts"));
        }
        if (detailsTab != null) {
            detailsTab.setText(localeService.getMessage("analytics.tab.details", "Details"));
        }
        
        // Update table column headers
        if (categoryNameColumn != null) {
            categoryNameColumn.setText(localeService.getMessage("analytics.table.category", "Category"));
        }
        if (categoryListingsColumn != null) {
            categoryListingsColumn.setText(localeService.getMessage("analytics.table.listings", "Listings"));
        }
        if (categoryEarningsColumn != null) {
            categoryEarningsColumn.setText(localeService.getMessage("analytics.table.earnings", "Earnings"));
        }
        if (categoryRatingColumn != null) {
            categoryRatingColumn.setText(localeService.getMessage("analytics.table.rating", "Avg Rating"));
        }
        if (monthColumn != null) {
            monthColumn.setText(localeService.getMessage("analytics.table.month", "Month"));
        }
        if (monthlyListingsColumn != null) {
            monthlyListingsColumn.setText(localeService.getMessage("analytics.table.listings", "Listings"));
        }
        if (monthlyEarningsColumn != null) {
            monthlyEarningsColumn.setText(localeService.getMessage("analytics.table.earnings", "Earnings"));
        }
        if (monthlyRatingColumn != null) {
            monthlyRatingColumn.setText(localeService.getMessage("analytics.table.rating", "Avg Rating"));
        }
        
        // Refresh chart titles
        setupCharts();
        
        // Refresh combo box display values
        if (periodComboBox != null) {
            String currentPeriod = periodComboBox.getValue();
            periodComboBox.setConverter(periodComboBox.getConverter()); // This will trigger refresh
        }
        if (categoryComboBox != null) {
            String currentCategory = categoryComboBox.getValue();
            categoryComboBox.setConverter(categoryComboBox.getConverter()); // This will trigger refresh
        }
        
        // If we have current analytics, update the display
        if (currentAnalytics != null) {
            updateUI(currentAnalytics);
        }
    }
}