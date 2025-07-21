package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.ListingReportViewModel;
import com.uninaswap.client.viewmodel.UserReportViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.dto.ListingReportDTO;
import com.uninaswap.common.dto.UserReportDTO;
import com.uninaswap.common.message.ReportMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReportService {
    private static ReportService instance;

    private final WebSocketClient webSocketClient = WebSocketClient.getInstance();
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();

    private CompletableFuture<?> futureToComplete;
    private Consumer<ReportMessage> messageCallback;

    // Observable lists for UI binding
    private final ObservableList<UserReportViewModel> userReports = FXCollections.observableArrayList();
    private final ObservableList<ListingReportViewModel> listingReports = FXCollections.observableArrayList();

    private ReportService() {
        // Register message handler
        webSocketClient.registerMessageHandler(ReportMessage.class, this::handleReportMessage);
    }

    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    /**
     * Create a user report
     */
    public CompletableFuture<UserReportViewModel> createUserReport(UserReportViewModel reportViewModel) {
        CompletableFuture<UserReportViewModel> future = new CompletableFuture<>();

        // Convert ViewModel to DTO for service communication
        UserReportDTO reportDTO = viewModelMapper.toDTO(reportViewModel);

        ReportMessage message = new ReportMessage();
        message.setType(ReportMessage.Type.CREATE_USER_REPORT_REQUEST);
        message.setUserReport(reportDTO);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Create a listing report
     */
    public CompletableFuture<ListingReportViewModel> createListingReport(ListingReportViewModel reportViewModel) {
        CompletableFuture<ListingReportViewModel> future = new CompletableFuture<>();

        // Convert ViewModel to DTO for service communication
        ListingReportDTO reportDTO = viewModelMapper.toDTO(reportViewModel);

        ReportMessage message = new ReportMessage();
        message.setType(ReportMessage.Type.CREATE_LISTING_REPORT_REQUEST);
        message.setListingReport(reportDTO);

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get user reports
     */
    public CompletableFuture<List<UserReportDTO>> getUserReports() {
        CompletableFuture<List<UserReportDTO>> future = new CompletableFuture<>();

        ReportMessage message = new ReportMessage();
        message.setType(ReportMessage.Type.GET_USER_REPORTS_REQUEST);
        message.setUserId(sessionService.getUser().getId().toString());

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    /**
     * Get listing reports
     */
    public CompletableFuture<List<ListingReportDTO>> getListingReports() {
        CompletableFuture<List<ListingReportDTO>> future = new CompletableFuture<>();

        ReportMessage message = new ReportMessage();
        message.setType(ReportMessage.Type.GET_LISTING_REPORTS_REQUEST);
        message.setUserId(sessionService.getUser().getId().toString());

        this.futureToComplete = future;

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    this.futureToComplete = null;
                    return null;
                });

        return future;
    }

    // Handle incoming messages
    @SuppressWarnings("unchecked")
    private void handleReportMessage(ReportMessage message) {
        if (message.getType() == null) {
            System.err.println("Received report message with null type: " + message.getErrorMessage());
            if (!message.isSuccess() && futureToComplete != null) {
                futureToComplete.completeExceptionally(
                        new Exception("Server error: " + message.getErrorMessage()));
                futureToComplete = null;
            }
            return;
        }

        switch (message.getType()) {
            case CREATE_USER_REPORT_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        UserReportViewModel reportViewModel = viewModelMapper.toViewModel(message.getUserReport());
                        userReports.add(reportViewModel);
                        if (futureToComplete != null) {
                            ((CompletableFuture<UserReportViewModel>) futureToComplete).complete(reportViewModel);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to create user report: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case CREATE_LISTING_REPORT_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        ListingReportViewModel reportViewModel = viewModelMapper
                                .toViewModel(message.getListingReport());
                        listingReports.add(reportViewModel);
                        if (futureToComplete != null) {
                            ((CompletableFuture<ListingReportViewModel>) futureToComplete).complete(reportViewModel);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to create listing report: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_USER_REPORTS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<UserReportDTO> reports = message.getUserReports() != null ? message.getUserReports()
                                : new ArrayList<>();
                        List<UserReportViewModel> reportViewModels = reports.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        userReports.setAll(reportViewModels);
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<UserReportDTO>>) futureToComplete).complete(reports);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get user reports: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_LISTING_REPORTS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<ListingReportDTO> reports = message.getListingReports() != null
                                ? message.getListingReports()
                                : new ArrayList<>();
                        List<ListingReportViewModel> reportViewModels = reports.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        listingReports.setAll(reportViewModels);
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<ListingReportDTO>>) futureToComplete).complete(reports);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get listing reports: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            default:
                System.out.println("Unhandled report message type: " + message.getType());
                break;
        }

        // Call any registered callback
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    // Getters for observable lists
    public ObservableList<UserReportViewModel> getUserReportsList() {
        return userReports;
    }

    public ObservableList<ListingReportViewModel> getListingReportsList() {
        return listingReports;
    }

    public void clearData() {
        userReports.clear();
        listingReports.clear();
    }

    // Set a callback for incoming messages
    public void setMessageCallback(Consumer<ReportMessage> callback) {
        this.messageCallback = callback;
    }
}