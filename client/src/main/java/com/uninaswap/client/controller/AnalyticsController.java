// filepath: /home/hat/Desktop/edu_workspace/UninaSwap/UninaSwap/client/src/main/java/com/uninaswap/client/controller/AnalyticsController.java
package com.uninaswap.client.controller;

import com.uninaswap.client.service.AnalyticsService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.viewmodel.AnalyticsViewModel;
import com.uninaswap.client.util.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for the Analytics view, handling data retrieval, display, and user interactions.
 */
/**
 * 
 */
public class AnalyticsController implements Refreshable {

    // Header Controls
    /**
     * 
     */
    @FXML private ComboBox<String> periodComboBox;
    /**
     * 
     */
    @FXML private ComboBox<String> categoryComboBox;
    /**
     * 
     */
    @FXML private DatePicker startDatePicker;
    /**
     * 
     */
    @FXML private DatePicker endDatePicker;
    /**
     * 
     */
    @FXML private Button refreshButton;
    /**
     * 
     */
    @FXML private Button exportButton;
    /**
     * 
     */
    @FXML private ComboBox<String> exportFormatComboBox;
    
    // Summary Cards
    /**
     * 
     */
    @FXML private Text totalListingsValue;
    /**
     * 
     */
    @FXML private Text activeListingsValue;
    /**
     * 
     */
    @FXML private Text completedListingsValue;
    /**
     * 
     */
    @FXML private Text totalEarningsValue;
    /**
     * 
     */
    @FXML private Text averageRatingValue;
    /**
     * 
     */
    @FXML private Text totalReviewsValue;
    /**
     * 
     */
    @FXML private Text memberSinceValue;
    /**
     * 
     */
    @FXML private Text totalViewsValue;
    /**
     * 
     */
    @FXML private Text totalFavoritesValue;
    
    // Performance Metrics
    /**
     * 
     */
    @FXML private ProgressBar successRateProgress;
    /**
     * 
     */
    @FXML private Text successRateValue;
    /**
     * 
     */
    @FXML private ProgressBar acceptanceRateProgress;
    /**
     * 
     */
    @FXML private Text acceptanceRateValue;
    /**
     * 
     */
    @FXML private Text averageTimeToSellValue;
    /**
     * 
     */
    @FXML private ProgressBar satisfactionRateProgress;
    /**
     * 
     */
    @FXML private Text satisfactionRateValue;
    /**
     * 
     */
    @FXML private Text ratingTrendValue;
    
    // Charts
    /**
     * 
     */
    @FXML private LineChart<String, Number> activityChart;
    /**
     * 
     */
    @FXML private BarChart<String, Number> earningsChart;
    /**
     * 
     */
    @FXML private PieChart categoryPieChart;
    /**
     * 
     */
    @FXML private AreaChart<String, Number> ratingTrendChart;
    
    // Tables
    /**
     * 
     */
    @FXML private TableView<AnalyticsViewModel.CategoryStats> categoryTable;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, String> categoryNameColumn;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, Number> categoryListingsColumn;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, Number> categoryEarningsColumn;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.CategoryStats, Number> categoryRatingColumn;
    
    /**
     * 
     */
    @FXML private TableView<AnalyticsViewModel.MonthlyStats> monthlyTable;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, String> monthColumn;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, Number> monthlyListingsColumn;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, Number> monthlyEarningsColumn;
    /**
     * 
     */
    @FXML private TableColumn<AnalyticsViewModel.MonthlyStats, Number> monthlyRatingColumn;
    
    // Tabs
    /**
     * 
     */
    @FXML private TabPane mainTabPane;
    /**
     * 
     */
    @FXML private Tab overviewTab;
    /**
     * 
     */
    @FXML private Tab chartsTab;
    /**
     * 
     */
    @FXML private Tab detailsTab;
    
    /**
     * 
     */
    @FXML private ProgressIndicator loadingIndicator;
    /**
     * 
     */
    @FXML private VBox contentArea;
    
    /**
     * 
     */
    private final AnalyticsService analyticsService = AnalyticsService.getInstance();
    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private AnalyticsViewModel currentAnalytics;

    /**
     * Default constructor
     */
    /**
     * 
     */
    public AnalyticsController() {
    }
    
    /**
     * 
     */
    @FXML
    public void initialize() {
        setupControls();
        setupCharts();
        setupTables();
        setupEventHandlers();
        loadAnalytics();
    }
    
    /*
     * Sets up the controls for the analytics view, including period and category selection,
     */

    /**
     * 
     */
    private void setupControls() {
        periodComboBox.setItems(FXCollections.observableArrayList(
            "week", "month", "quarter", "year", "all"
        ));
        periodComboBox.setValue("month");
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "all", "electronics", "books", "clothing", "home", "sports", "toys", "other"
        ));
        categoryComboBox.setValue("all");
        exportFormatComboBox.setItems(FXCollections.observableArrayList(
            "PDF", "CSV", "Excel"
        ));
        exportFormatComboBox.setValue("PDF");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
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
                return string;
            }
        });
    }
    
    /*
     * Sets up the charts with titles, legends, and styles.
     */

    /**
     * 
     */
    private void setupCharts() {
        activityChart.setTitle(localeService.getMessage("analytics.chart.activity", "Activity Over Time"));
        activityChart.setLegendSide(Side.BOTTOM);
        activityChart.setCreateSymbols(true);
        activityChart.setAnimated(true);
        earningsChart.setTitle(localeService.getMessage("analytics.chart.earnings", "Earnings Over Time"));
        earningsChart.setLegendSide(Side.BOTTOM);
        earningsChart.setAnimated(true);
        categoryPieChart.setTitle(localeService.getMessage("analytics.chart.category", "Listings by Category"));
        categoryPieChart.setLegendSide(Side.RIGHT);
        categoryPieChart.setAnimated(true);
        ratingTrendChart.setTitle(localeService.getMessage("analytics.chart.rating", "Rating Trend"));
        ratingTrendChart.setLegendSide(Side.BOTTOM);
        ratingTrendChart.setCreateSymbols(true);
        ratingTrendChart.setAnimated(true);
    }
    
    /**
     * 
     */
    private void setupTables() {
        categoryNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCategory()));
        categoryListingsColumn.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getTotalListings()));
        categoryEarningsColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getTotalEarnings()));
        categoryRatingColumn.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getAverageRating()));
        
        categoryEarningsColumn.setCellFactory(_ -> new TableCell<AnalyticsViewModel.CategoryStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item.doubleValue()));
                }
            }
        });
        categoryRatingColumn.setCellFactory(_ -> new TableCell<AnalyticsViewModel.CategoryStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f/5.0", item.doubleValue()));
                }
            }
        });
        monthColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedPeriod()));
        monthlyListingsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTotalListings()));
        monthlyEarningsColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getEarnings()));
        monthlyRatingColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAverageRating()));

        monthlyEarningsColumn.setCellFactory(_ -> new TableCell<AnalyticsViewModel.MonthlyStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("€%.2f", item.doubleValue()));
                }
            }
        });
        
        monthlyRatingColumn.setCellFactory(_ -> new TableCell<AnalyticsViewModel.MonthlyStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f/5.0", item.doubleValue()));
                }
            }
        });
    }
    
    /**
     * 
     */
    private void setupEventHandlers() {
        periodComboBox.setOnAction(_ -> {
            if (!periodComboBox.getValue().equals("custom")) {
                loadAnalytics();
            }
        });
        categoryComboBox.setOnAction(_ -> loadAnalytics());
        startDatePicker.setOnAction(_ -> {
            if (periodComboBox.getValue().equals("custom")) {
                loadAnalytics();
            }
        });
        endDatePicker.setOnAction(_ -> {
            if (periodComboBox.getValue().equals("custom")) {
                loadAnalytics();
            }
        });
        refreshButton.setOnAction(_ -> refreshAnalytics());
        exportButton.setOnAction(_ -> exportAnalytics());
    }
    
    /**
     * 
     */
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
                showError("Failed to load analytics: " + ex.getMessage());
            });
            return null;
        });
    }
    
    /**
     * 
     */
    private void refreshAnalytics() {
        analyticsService.clearCache();
        loadAnalytics();
    }
    
    /**
     * 
     */
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
            .thenAccept(_ -> Platform.runLater(() -> {
                AlertHelper.showInformationAlert(
                    localeService.getMessage("analytics.export.success.title", "Export Success"),
                    localeService.getMessage("analytics.export.success.message", "Export Completed"),
                    localeService.getMessage("analytics.export.success.details", "Analytics data has been exported successfully")
                );
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    showError("Failed to export analytics: " + ex.getMessage());
                });
                return null;
            });
    }
    
    /**
     * @param analytics
     */
    private void updateUI(AnalyticsViewModel analytics) {
        updateSummaryCards(analytics);
        updatePerformanceMetrics(analytics);
        updateCharts(analytics);
        updateTables(analytics);
    }
    
    /**
     * @param analytics
     */
    private void updateSummaryCards(AnalyticsViewModel analytics) {
        totalListingsValue.setText(String.valueOf(analytics.getTotalListings()));
        activeListingsValue.setText(String.valueOf(analytics.getActiveListings()));
        completedListingsValue.setText(String.valueOf(analytics.getCompletedListings()));
        totalEarningsValue.setText(String.format("€%.2f", analytics.getTotalEarnings()));
        averageRatingValue.setText(String.format("%.1f/5.0", analytics.getAverageRating()));
        totalReviewsValue.setText(String.valueOf(analytics.getTotalReviews()));
        totalViewsValue.setText(String.valueOf(analytics.getTotalViews()));
        totalFavoritesValue.setText(String.valueOf(analytics.getTotalFavorites()));
        
        if (analytics.getMemberSince() != null) {
            memberSinceValue.setText(analytics.getMemberSince().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }
    
    /**
     * @param analytics
     */
    private void updatePerformanceMetrics(AnalyticsViewModel analytics) {
        double successRate = analytics.getListingSuccessRate() / 100.0;
        successRateProgress.setProgress(successRate);
        successRateValue.setText(String.format("%.1f%%", analytics.getListingSuccessRate()));
        double acceptanceRate = analytics.getOfferAcceptanceRate() / 100.0;
        acceptanceRateProgress.setProgress(acceptanceRate);
        acceptanceRateValue.setText(String.format("%.1f%%", analytics.getOfferAcceptanceRate()));
        averageTimeToSellValue.setText(String.format("%.1f days", analytics.getAverageTimeToSell()));
        double satisfactionRate = analytics.getCustomerSatisfactionRate() / 100.0;
        satisfactionRateProgress.setProgress(satisfactionRate);
        satisfactionRateValue.setText(String.format("%.1f%%", analytics.getCustomerSatisfactionRate()));
        double trend = analytics.getRatingTrend();
        String trendText = trend > 0 ? String.format("+%.2f", trend) : String.format("%.2f", trend);
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
    
    /**
     * @param analytics
     */
    private void updateCharts(AnalyticsViewModel analytics) {
        updateActivityChart(analytics);
        updateEarningsChart(analytics);
        updateCategoryPieChart(analytics);
        updateRatingTrendChart(analytics);
    }
    
    /**
     * @param analytics
     */
    private void updateActivityChart(AnalyticsViewModel analytics) {
        activityChart.getData().clear();
        XYChart.Series<String, Number> listingSeries = new XYChart.Series<>();
        listingSeries.setName(localeService.getMessage("analytics.chart.listings", "Listings"));
        
        analytics.getListingStats().forEach(data -> {
            listingSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        XYChart.Series<String, Number> offerSeries = new XYChart.Series<>();
        offerSeries.setName(localeService.getMessage("analytics.chart.offers", "Offers"));
        
        analytics.getOfferStats().forEach(data -> {
            offerSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        activityChart.getData().addAll(listingSeries, offerSeries);
    }
    
    /**
     * @param analytics
     */
    private void updateEarningsChart(AnalyticsViewModel analytics) {
        earningsChart.getData().clear();
        
        XYChart.Series<String, Number> earningsSeries = new XYChart.Series<>();
        earningsSeries.setName(localeService.getMessage("analytics.chart.earnings", "Earnings"));
        
        analytics.getEarningsStats().forEach(data -> {
            earningsSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        earningsChart.getData().add(earningsSeries);
    }
    
    /**
     * @param analytics
     */
    private void updateCategoryPieChart(AnalyticsViewModel analytics) {
        categoryPieChart.getData().clear();
        
        analytics.getCategoryBreakdown().forEach(category -> {
            if (category.getTotalListings() > 0) {
                PieChart.Data slice = new PieChart.Data(
                    category.getCategory() + " (" + category.getTotalListings() + ")",
                    category.getTotalListings()
                );
                categoryPieChart.getData().add(slice);
            }
        });
    }
    
    /**
     * @param analytics
     */
    private void updateRatingTrendChart(AnalyticsViewModel analytics) {
        ratingTrendChart.getData().clear();
        
        XYChart.Series<String, Number> ratingSeries = new XYChart.Series<>();
        ratingSeries.setName(localeService.getMessage("analytics.chart.rating.trend", "Rating Trend"));
        
        analytics.getReviewStats().forEach(data -> {
            ratingSeries.getData().add(new XYChart.Data<>(data.getLabel(), data.getValue()));
        });
        
        ratingTrendChart.getData().add(ratingSeries);
    }
    
    /**
     * @param analytics
     */
    private void updateTables(AnalyticsViewModel analytics) {
        categoryTable.setItems(analytics.getCategoryBreakdown());
        monthlyTable.setItems(analytics.getMonthlyBreakdown());
    }
    
    /**
     * @param show
     */
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        contentArea.setDisable(show);
    }
    
    /**
     * @param message
     */
    private void showError(String message) {
        AlertHelper.showErrorAlert(
            localeService.getMessage("analytics.error.title", "Analytics Error"),
            localeService.getMessage("analytics.error.message", "Error loading analytics"),
            message
        );
    }
    
    /**
     *
     */
    @Override
    public void refreshUI() {
        loadAnalytics();
    }
}