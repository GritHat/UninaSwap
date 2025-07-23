package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.dto.OfferDTO;
import com.uninaswap.common.enums.OfferStatus;
import com.uninaswap.common.message.OfferMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 
 */
public class OfferService {
    /**
     * 
     */
    private static OfferService instance;
    /**
     * 
     */
    private final WebSocketClient webSocketClient = WebSocketClient.getInstance();
    /**
     * 
     */
    private final ViewModelMapper viewModelMapper = ViewModelMapper.getInstance();

    /**
     * 
     */
    private CompletableFuture<?> futureToComplete;
    /**
     * 
     */
    private Consumer<OfferMessage> messageCallback;

    /**
     * 
     */
    private final ObservableList<OfferViewModel> userOffers = FXCollections.observableArrayList();
    /**
     * 
     */
    private final ObservableList<OfferViewModel> receivedOffers = FXCollections.observableArrayList();

    /**
     * 
     */
    private OfferService() {
        this.webSocketClient.registerMessageHandler(OfferMessage.class, this::handleOfferMessage);
    }

    /**
     * @return
     */
    public static synchronized OfferService getInstance() {
        if (instance == null) {
            instance = new OfferService();
        }
        return instance;
    }

    /**
     * Create a new offer - accepts ViewModel, converts to DTO internally
     * 
     * @param offerViewModel The offer to create
     * @return CompletableFuture with the created OfferViewModel
     */
    /**
     * @param offerViewModel
     * @return
     */
    public CompletableFuture<OfferViewModel> createOffer(OfferViewModel offerViewModel) {
        CompletableFuture<OfferViewModel> future = new CompletableFuture<>();

        OfferDTO offerDTO = viewModelMapper.toDTO(offerViewModel);

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.CREATE_OFFER_REQUEST);
        message.setOffer(offerDTO);

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
     * Get offers for a specific listing - returns ViewModels
     * 
     * @param listingId The ID of the listing to get offers for
     * @return CompletableFuture with a list of OfferViewModels
     */
    /**
     * @param listingId
     * @return
     */
    public CompletableFuture<List<OfferViewModel>> getListingOffers(String listingId) {
        CompletableFuture<List<OfferViewModel>> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.GET_LISTING_OFFERS_REQUEST);
        message.setListingId(listingId);

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
     * Update offer status
     * 
     * @param offerId The ID of the offer to update
     * @param status The new status for the offer
     * @return CompletableFuture with the updated OfferViewModel
     */
    /**
     * @param offerId
     * @param status
     * @return
     */
    public CompletableFuture<OfferViewModel> updateOfferStatus(String offerId, OfferStatus status) {
        CompletableFuture<OfferViewModel> future = new CompletableFuture<>();

        OfferDTO offerData = new OfferDTO();
        offerData.setStatus(status);

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.UPDATE_OFFER_STATUS_REQUEST);
        message.setOfferId(offerId);
        message.setOffer(offerData);

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
     * Get received offers for the current user
     * 
     * @return CompletableFuture with a list of OfferDTOs
     */
    /**
     * @return
     */
    public CompletableFuture<List<OfferDTO>> getReceivedOffers() {
        CompletableFuture<List<OfferDTO>> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.GET_RECEIVED_OFFERS_REQUEST);

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
     * Get sent offers for the current user
     * 
     * @return CompletableFuture with a list of OfferDTOs
     */
    /**
     * @return
     */
    public CompletableFuture<List<OfferDTO>> getSentOffers() {
        CompletableFuture<List<OfferDTO>> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.GET_SENT_OFFERS_REQUEST);

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
     * Get offer history for the current user
     * 
     * @return CompletableFuture with a list of OfferDTOs
     */
    /**
     * @return
     */
    public CompletableFuture<List<OfferDTO>> getOfferHistory() {
        CompletableFuture<List<OfferDTO>> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.GET_OFFER_HISTORY_REQUEST);

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
     * Accept an offer
     * 
     * @param offerId The ID of the offer to accept
     * @return CompletableFuture with true if successful
     */
    public CompletableFuture<Boolean> acceptOffer(String offerId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.ACCEPT_OFFER_REQUEST);
        message.setOfferId(offerId);

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
     * Reject an offer
     * 
     * @param offerId The ID of the offer to reject
     * @return CompletableFuture with true if successful
     */
    /**
     * @param offerId
     * @return
     */
    public CompletableFuture<Boolean> rejectOffer(String offerId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.REJECT_OFFER_REQUEST);
        message.setOfferId(offerId);

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
     * Withdraw an offer
     * 
     * @param offerId The ID of the offer to withdraw
     * @return CompletableFuture with true if successful
     */
    /**
     * @param offerId
     * @return
     */
    public CompletableFuture<Boolean> withdrawOffer(String offerId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.WITHDRAW_OFFER_REQUEST);
        message.setOfferId(offerId);

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
     * Confirm a transaction (buyer or seller verification)
     * 
     * @param offerId The ID of the offer to confirm
     * @return CompletableFuture with true if successful
     */
    public CompletableFuture<Boolean> confirmTransaction(String offerId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.CONFIRM_TRANSACTION_REQUEST);
        message.setOfferId(offerId);

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
     * Cancel a transaction
     * 
     * @param offerId The ID of the offer to cancel
     * @return CompletableFuture with true if successful
     */
    /**
     * @param offerId
     * @return
     */
    public CompletableFuture<Boolean> cancelTransaction(String offerId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        OfferMessage message = new OfferMessage();
        message.setType(OfferMessage.Type.CANCEL_TRANSACTION_REQUEST);
        message.setOfferId(offerId);

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
     * @param message
     */
    @SuppressWarnings("unchecked")
    private void handleOfferMessage(OfferMessage message) {
        if (message.getType() == null) {
            System.err.println("Received offer message with null type: " + message.getErrorMessage());
            if (!message.isSuccess() && futureToComplete != null) {
                futureToComplete.completeExceptionally(
                        new Exception("Server error: " + message.getErrorMessage()));
                futureToComplete = null;
            }
            return;
        }

        switch (message.getType()) {
            case CREATE_OFFER_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        OfferViewModel offerViewModel = viewModelMapper.toViewModel(message.getOffer());
                        userOffers.add(offerViewModel);
                        if (futureToComplete != null) {
                            ((CompletableFuture<OfferViewModel>) futureToComplete).complete(offerViewModel);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to create offer: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_SENT_OFFERS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<OfferDTO> offers = message.getOffers() != null ? message.getOffers() : new ArrayList<>();
                        List<OfferViewModel> offerViewModels = offers.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        userOffers.setAll(offerViewModels);
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<OfferDTO>>) futureToComplete).complete(offers);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get sent offers: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_RECEIVED_OFFERS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<OfferDTO> offers = message.getOffers() != null ? message.getOffers() : new ArrayList<>();

                        List<OfferViewModel> offerViewModels = offers.stream()
                                .map(viewModelMapper::toViewModel)
                                .collect(Collectors.toList());
                        receivedOffers.setAll(offerViewModels);

                        if (futureToComplete != null) {
                            ((CompletableFuture<List<OfferDTO>>) futureToComplete).complete(offers);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get received offers: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_OFFER_HISTORY_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<OfferDTO> offers = message.getOffers() != null ? message.getOffers() : new ArrayList<>();
                        if (futureToComplete != null) {
                            ((CompletableFuture<List<OfferDTO>>) futureToComplete).complete(offers);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get offer history: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case ACCEPT_OFFER_RESPONSE:
            case REJECT_OFFER_RESPONSE:
            case WITHDRAW_OFFER_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(true);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to update offer: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case GET_LISTING_OFFERS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        List<OfferViewModel> offerViewModels = message.getOffers() != null
                                ? message.getOffers().stream()
                                        .map(viewModelMapper::toViewModel)
                                        .collect(Collectors.toList())
                                : new ArrayList<>();

                        if (futureToComplete != null) {
                            ((CompletableFuture<List<OfferViewModel>>) futureToComplete).complete(offerViewModels);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to get listing offers: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case UPDATE_OFFER_STATUS_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        OfferViewModel updatedOffer = viewModelMapper.toViewModel(message.getOffer());
                        updateOfferInList(userOffers, updatedOffer);
                        updateOfferInList(receivedOffers, updatedOffer);

                        if (futureToComplete != null) {
                            ((CompletableFuture<OfferViewModel>) futureToComplete).complete(updatedOffer);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to update offer status: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            case CONFIRM_TRANSACTION_RESPONSE:
            case CANCEL_TRANSACTION_RESPONSE:
                Platform.runLater(() -> {
                    if (message.isSuccess()) {
                        if (futureToComplete != null) {
                            ((CompletableFuture<Boolean>) futureToComplete).complete(true);
                            futureToComplete = null;
                        }
                    } else {
                        if (futureToComplete != null) {
                            futureToComplete.completeExceptionally(
                                    new Exception("Failed to process transaction: " + message.getErrorMessage()));
                            futureToComplete = null;
                        }
                    }
                });
                break;

            default:
                System.out.println("Unknown offer message type: " + message.getType());
                break;
        }
        if (messageCallback != null) {
            messageCallback.accept(message);
        }
    }

    /**
     * @param list
     * @param updatedOffer
     */
    private void updateOfferInList(ObservableList<OfferViewModel> list, OfferViewModel updatedOffer) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(updatedOffer.getId())) {
                list.set(i, updatedOffer);
                break;
            }
        }
    }

    /**
     * @return
     */
    public ObservableList<OfferViewModel> getUserOffersList() {
        return userOffers;
    }

    /**
     * @return
     */
    public ObservableList<OfferViewModel> getReceivedOffersList() {
        return receivedOffers;
    }
    
    /**
     * @param callback
     */
    public void setMessageCallback(Consumer<OfferMessage> callback) {
        this.messageCallback = callback;
    }
}
