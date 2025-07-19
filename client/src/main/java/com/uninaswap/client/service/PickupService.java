package com.uninaswap.client.service;

import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.client.util.WebSocketManager;
import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.common.enums.PickupStatus;
import com.uninaswap.common.message.PickupMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PickupService {
    private static PickupService instance;

    private final WebSocketClient webSocketClient;
    private final UserSessionService sessionService;

    private final ObservableList<PickupDTO> userPickups = FXCollections.observableArrayList();
    private final ObservableList<PickupDTO> upcomingPickups = FXCollections.observableArrayList();
    private final ObservableList<PickupDTO> pastPickups = FXCollections.observableArrayList();

    // Response handlers for async operations
    private Consumer<PickupMessage> createPickupHandler;
    private Consumer<PickupMessage> acceptPickupHandler;
    private Consumer<PickupMessage> updatePickupHandler;

    private PickupService() {
        this.webSocketClient = WebSocketClient.getInstance();
        this.sessionService = UserSessionService.getInstance();

        // Register message handler
        webSocketClient.registerMessageHandler(PickupMessage.class, this::handlePickupMessage);
    }

    public static PickupService getInstance() {
        if (instance == null) {
            instance = new PickupService();
        }
        return instance;
    }

    /**
     * Create a pickup arrangement
     */
    public CompletableFuture<Boolean> createPickup(PickupDTO pickupDTO) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Set up response handler
        createPickupHandler = response -> {
            future.complete(response.isSuccess());
            if (response.isSuccess() && response.getPickup() != null) {
                Platform.runLater(() -> {
                    // Add to user pickups if not already present
                    userPickups.removeIf(p -> p.getId().equals(response.getPickup().getId()));
                    userPickups.add(response.getPickup());
                });
            }
            createPickupHandler = null; // Clear handler
        };

        PickupMessage message = new PickupMessage();
        message.setType(PickupMessage.Type.CREATE_PICKUP_REQUEST);
        message.setPickup(pickupDTO);

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    createPickupHandler = null;
                    return null;
                });

        return future;
    }

    /**
     * Accept a pickup with selected date and time
     */
    public CompletableFuture<Boolean> acceptPickup(Long pickupId, LocalDate selectedDate, LocalTime selectedTime) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Set up response handler
        acceptPickupHandler = response -> {
            future.complete(response.isSuccess());
            if (response.isSuccess() && response.getPickup() != null) {
                Platform.runLater(() -> updatePickupInLists(response.getPickup()));
            }
            acceptPickupHandler = null; // Clear handler
        };

        PickupMessage message = new PickupMessage();
        message.setType(PickupMessage.Type.ACCEPT_PICKUP_REQUEST);
        message.setPickupId(pickupId);
        message.setSelectedDate(selectedDate);
        message.setSelectedTime(selectedTime);

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    acceptPickupHandler = null;
                    return null;
                });

        return future;
    }

    /**
     * Update pickup status
     */
    public CompletableFuture<Boolean> updatePickupStatus(Long pickupId, PickupStatus status) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        updatePickupHandler = response -> {
            future.complete(response.isSuccess());
            if (response.isSuccess() && response.getPickup() != null) {
                Platform.runLater(() -> updatePickupInLists(response.getPickup()));
            }
            updatePickupHandler = null;
        };

        PickupMessage message = new PickupMessage();
        message.setType(PickupMessage.Type.UPDATE_PICKUP_STATUS_REQUEST);
        message.setPickupId(pickupId);
        message.setStatus(status);

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    updatePickupHandler = null;
                    return null;
                });

        return future;
    }

    /**
     * Get user's pickups
     */
    public CompletableFuture<Void> getUserPickups() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        PickupMessage message = new PickupMessage();
        message.setType(PickupMessage.Type.GET_USER_PICKUPS_REQUEST);
        message.setUserId(sessionService.getUser().getId());

        webSocketClient.sendMessage(message)
                .thenRun(() -> future.complete(null))
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });

        return future;
    }

    /**
     * Get upcoming pickups
     */
    public CompletableFuture<Void> getUpcomingPickups() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        PickupMessage message = new PickupMessage();
        message.setType(PickupMessage.Type.GET_UPCOMING_PICKUPS_REQUEST);
        message.setUserId(sessionService.getUser().getId());

        webSocketClient.sendMessage(message)
                .thenRun(() -> future.complete(null))
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });

        return future;
    }

    private void handlePickupMessage(PickupMessage message) {
        Platform.runLater(() -> {
            switch (message.getType()) {
                case CREATE_PICKUP_RESPONSE:
                    if (createPickupHandler != null) {
                        createPickupHandler.accept(message);
                    }
                    break;

                case ACCEPT_PICKUP_RESPONSE:
                    if (acceptPickupHandler != null) {
                        acceptPickupHandler.accept(message);
                    }
                    break;

                case UPDATE_PICKUP_STATUS_RESPONSE:
                case UPDATE_PICKUP_RESPONSE:
                    if (updatePickupHandler != null) {
                        updatePickupHandler.accept(message);
                    }
                    break;

                case GET_USER_PICKUPS_RESPONSE:
                    handleGetUserPickupsResponse(message);
                    break;

                case GET_UPCOMING_PICKUPS_RESPONSE:
                    handleGetUpcomingPickupsResponse(message);
                    break;

                case GET_PAST_PICKUPS_RESPONSE:
                    handleGetPastPickupsResponse(message);
                    break;

                default:
                    System.out.println("Unhandled pickup message type: " + message.getType());
                    break;
            }
        });
    }

    private void handleGetUserPickupsResponse(PickupMessage message) {
        if (message.isSuccess() && message.getPickups() != null) {
            userPickups.clear();
            userPickups.addAll(message.getPickups());
        }
    }

    private void handleGetUpcomingPickupsResponse(PickupMessage message) {
        if (message.isSuccess() && message.getPickups() != null) {
            upcomingPickups.clear();
            upcomingPickups.addAll(message.getPickups());
        }
    }

    private void handleGetPastPickupsResponse(PickupMessage message) {
        if (message.isSuccess() && message.getPickups() != null) {
            pastPickups.clear();
            pastPickups.addAll(message.getPickups());
        }
    }

    private void updatePickupInLists(PickupDTO updatedPickup) {
        // Update in all lists where the pickup exists
        updatePickupInList(userPickups, updatedPickup);
        updatePickupInList(upcomingPickups, updatedPickup);
        updatePickupInList(pastPickups, updatedPickup);
    }

    private void updatePickupInList(ObservableList<PickupDTO> list, PickupDTO updatedPickup) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedPickup.getId())) {
                list.set(i, updatedPickup);
                break;
            }
        }
    }

    // Observable list getters
    public ObservableList<PickupDTO> getUserPickupsList() {
        return userPickups;
    }

    public ObservableList<PickupDTO> getUpcomingPickupsList() {
        return upcomingPickups;
    }

    public ObservableList<PickupDTO> getPastPickupsList() {
        return pastPickups;
    }

    public void clearData() {
        userPickups.clear();
        upcomingPickups.clear();
        pastPickups.clear();
    }
}
