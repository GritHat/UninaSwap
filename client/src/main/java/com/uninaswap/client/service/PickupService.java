package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.PickupViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.common.enums.PickupStatus;
import com.uninaswap.common.message.PickupMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 
 */
public class PickupService {
    /**
     * 
     */
    private static PickupService instance;

    /**
     * 
     */
    private final WebSocketClient webSocketClient;
    /**
     * 
     */
    private final UserSessionService sessionService;

    /**
     * 
     */
    private final ObservableList<PickupDTO> userPickups = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<PickupDTO> upcomingPickups = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<PickupDTO> pastPickups = FXCollections.observableArrayList();

    /**
     * 
     */
    private Consumer<PickupMessage> createPickupHandler;
    /**
     * 
     */
    private Consumer<PickupMessage> acceptPickupHandler;
    /**
     * 
     */
    private Consumer<PickupMessage> updatePickupHandler;
    /**
     * 
     */
    private Consumer<PickupMessage> getPickupHandler;

    /**
     * 
     */
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();

    /**
     * 
     */
    private PickupService() {
        this.webSocketClient = WebSocketClient.getInstance();
        this.sessionService = UserSessionService.getInstance();
        webSocketClient.registerMessageHandler(PickupMessage.class, this::handlePickupMessage);
    }

    /**
     * @return
     */
    public static PickupService getInstance() {
        if (instance == null) {
            instance = new PickupService();
        }
        return instance;
    }

    /**
     * Create a pickup arrangement
     * 
     * @param pickupViewModel The view model containing pickup details
     * @return CompletableFuture with true if successful
     */
    /**
     * @param pickupViewModel
     * @return
     */
    public CompletableFuture<Boolean> createPickup(PickupViewModel pickupViewModel) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        PickupDTO pickupDTO = viewModelMapper.toDTO(pickupViewModel);

        createPickupHandler = response -> {
            future.complete(response.isSuccess());
            if (response.isSuccess() && response.getPickup() != null) {
                Platform.runLater(() -> {
                    userPickups.removeIf(p -> p.getId().equals(response.getPickup().getId()));
                    userPickups.add(response.getPickup());
                });
            }
            createPickupHandler = null;
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
     * 
     * @param pickupId The ID of the pickup to accept
     * @param selectedDate The date selected for the pickup
     * @param selectedTime The time selected for the pickup
     * @return CompletableFuture with true if successful
     */
    /**
     * @param pickupId
     * @param selectedDate
     * @param selectedTime
     * @return
     */
    public CompletableFuture<Boolean> acceptPickup(Long pickupId, LocalDate selectedDate, LocalTime selectedTime) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        acceptPickupHandler = response -> {
            future.complete(response.isSuccess());
            if (response.isSuccess() && response.getPickup() != null) {
                Platform.runLater(() -> updatePickupInLists(response.getPickup()));
            }
            acceptPickupHandler = null;
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
     * 
     * @param pickupId The ID of the pickup to update
     * @param status The new status for the pickup
     * @return CompletableFuture with true if successful
     */
    /**
     * @param pickupId
     * @param status
     * @return
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
     * Cancel pickup arrangement and update offer status to CANCELLED
     * 
     * @param pickupId The ID of the pickup to cancel
     * @return CompletableFuture with true if successful
     */
    /**
     * @param pickupId
     * @return
     */
    public CompletableFuture<Boolean> cancelPickupArrangement(Long pickupId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        updatePickupHandler = response -> {
            future.complete(response.isSuccess());
            if (response.isSuccess() && response.getPickup() != null) {
                Platform.runLater(() -> updatePickupInLists(response.getPickup()));
            }
            updatePickupHandler = null;
        };

        PickupMessage message = new PickupMessage();
        message.setType(PickupMessage.Type.CANCEL_PICKUP_ARRANGEMENT_REQUEST);
        message.setPickupId(pickupId);

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
     * 
     * @return CompletableFuture with user's pickups
     */
    /**
     * @return
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
     * 
     * @return CompletableFuture with upcoming pickups
     */
    /**
     * @return
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

    /**
     * Get pickup by offer ID
     * 
     * @param offerId The ID of the offer to get the pickup for
     * @return CompletableFuture with the PickupViewModel if found, null otherwise
     */
    /**
     * @param offerId
     * @return
     */
    public CompletableFuture<PickupViewModel> getPickupByOfferId(String offerId) {
        CompletableFuture<PickupViewModel> future = new CompletableFuture<>();
        Consumer<PickupMessage> tempHandler = response -> {
            if (response.isSuccess() && response.getPickup() != null) {
                PickupViewModel pickup = ViewModelMapper.getInstance().toViewModel(response.getPickup());
                future.complete(pickup);
            } else {
                future.complete(null);
            }
        };

        getPickupHandler = tempHandler;

        PickupMessage message = new PickupMessage();
        message.setType(PickupMessage.Type.GET_PICKUP_BY_OFFER_REQUEST);
        message.setOfferId(offerId);

        webSocketClient.sendMessage(message)
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    getPickupHandler = null;
                    return null;
                });

        return future;
    }

    /**
     * @param message
     */
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

                case CANCEL_PICKUP_ARRANGEMENT_RESPONSE:
                    if (updatePickupHandler != null) {
                        updatePickupHandler.accept(message);
                    }
                    break;

                case GET_PICKUP_BY_OFFER_RESPONSE:
                    if (getPickupHandler != null) {
                        getPickupHandler.accept(message);
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
                    System.err.println("Unknown pickup message type: " + message.getType());
                    break;
            }
        });
    }

    /**
     * @param message
     */
    private void handleGetUserPickupsResponse(PickupMessage message) {
        if (message.isSuccess() && message.getPickups() != null) {
            userPickups.clear();
            userPickups.addAll(message.getPickups());
        }
    }

    /**
     * @param message
     */
    private void handleGetUpcomingPickupsResponse(PickupMessage message) {
        if (message.isSuccess() && message.getPickups() != null) {
            upcomingPickups.clear();
            upcomingPickups.addAll(message.getPickups());
        }
    }

    /**
     * @param message
     */
    private void handleGetPastPickupsResponse(PickupMessage message) {
        if (message.isSuccess() && message.getPickups() != null) {
            pastPickups.clear();
            pastPickups.addAll(message.getPickups());
        }
    }

    /**
     * @param updatedPickup
     */
    private void updatePickupInLists(PickupDTO updatedPickup) {
        updatePickupInList(userPickups, updatedPickup);
        updatePickupInList(upcomingPickups, updatedPickup);
        updatePickupInList(pastPickups, updatedPickup);
    }

    /**
     * @param list
     * @param updatedPickup
     */
    private void updatePickupInList(ObservableList<PickupDTO> list, PickupDTO updatedPickup) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedPickup.getId())) {
                list.set(i, updatedPickup);
                break;
            }
        }
    }

    /**
     * @return
     */
    public ObservableList<PickupDTO> getUserPickupsList() {
        return userPickups;
    }

    /**
     * @return
     */
    public ObservableList<PickupDTO> getUpcomingPickupsList() {
        return upcomingPickups;
    }

    /**
     * @return
     */
    public ObservableList<PickupDTO> getPastPickupsList() {
        return pastPickups;
    }

    /**
     * 
     */
    public void clearData() {
        userPickups.clear();
        upcomingPickups.clear();
        pastPickups.clear();
    }
}
