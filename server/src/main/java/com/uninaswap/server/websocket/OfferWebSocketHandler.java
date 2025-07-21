package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.OfferDTO;
import com.uninaswap.common.enums.OfferStatus;
import com.uninaswap.common.message.OfferMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.OfferService;
import com.uninaswap.server.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OfferWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OfferWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final OfferService offerService;
    private final SessionService sessionService;

    @Autowired
    public OfferWebSocketHandler(ObjectMapper objectMapper, OfferService offerService, SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.offerService = offerService;
        this.sessionService = sessionService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received offer message: {}", message.getPayload());

        try {
            OfferMessage offerMessage = objectMapper.readValue(message.getPayload(), OfferMessage.class);
            OfferMessage response = new OfferMessage();

            try {
                // All offer operations require authentication
                UserEntity currentUser = sessionService.validateSession(session);
                if (currentUser == null) {
                    throw new UnauthorizedException("Not authenticated");
                }

                switch (offerMessage.getType()) {
                    case CREATE_OFFER_REQUEST:
                        handleCreateOffer(offerMessage, response, currentUser);
                        break;

                    case GET_SENT_OFFERS_REQUEST:
                        handleGetSentOffers(offerMessage, response, currentUser);
                        break;

                    case GET_RECEIVED_OFFERS_REQUEST:
                        handleGetReceivedOffers(offerMessage, response, currentUser);
                        break;

                    case GET_LISTING_OFFERS_REQUEST:
                        handleGetListingOffers(offerMessage, response, currentUser);
                        break;

                    case GET_OFFER_HISTORY_REQUEST:
                        handleGetOfferHistory(offerMessage, response, currentUser);
                        break;

                    case ACCEPT_OFFER_REQUEST:
                        handleAcceptOffer(offerMessage, response, currentUser);
                        break;

                    case REJECT_OFFER_REQUEST:
                        handleRejectOffer(offerMessage, response, currentUser);
                        break;

                    case WITHDRAW_OFFER_REQUEST:
                        handleWithdrawOffer(offerMessage, response, currentUser);
                        break;

                    case UPDATE_OFFER_STATUS_REQUEST:
                        handleUpdateOfferStatus(offerMessage, response, currentUser);
                        break;

                    case CONFIRM_TRANSACTION_REQUEST:
                        handleConfirmTransaction(offerMessage, response, currentUser);
                        break;
                        
                    case CANCEL_TRANSACTION_REQUEST:
                        handleCancelTransaction(offerMessage, response, currentUser);
                        break;

                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown offer message type: " + offerMessage.getType());
                        break;
                }
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for offer operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing offer request: " + e.getMessage());
                logger.error("Error processing offer message", e);
            }

            // Send the response back to the client
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("Error parsing offer message", e);
            OfferMessage errorResponse = new OfferMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private void handleCreateOffer(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        OfferDTO newOffer = request.getOffer();

        // Create the offer
        OfferDTO createdOffer = offerService.createOffer(newOffer, currentUser.getId());

        response.setType(OfferMessage.Type.CREATE_OFFER_RESPONSE);
        response.setOffer(createdOffer);
        response.setSuccess(true);

        logger.info("Created new offer: {} for listing {} by user {}",
                createdOffer.getId(), createdOffer.getListingId(), currentUser.getUsername());
    }

    private void handleGetSentOffers(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        List<OfferDTO> userOffers = offerService.getUserOffers(currentUser.getId());

        response.setType(OfferMessage.Type.GET_SENT_OFFERS_RESPONSE);
        response.setOffers(userOffers);
        response.setSuccess(true);

        logger.info("Retrieved {} offers for user {}", userOffers.size(), currentUser.getUsername());
    }

    private void handleGetReceivedOffers(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        List<OfferDTO> receivedOffers = offerService.getReceivedOffers(currentUser.getId());

        response.setType(OfferMessage.Type.GET_RECEIVED_OFFERS_RESPONSE);
        response.setOffers(receivedOffers);
        response.setSuccess(true);

        logger.info("Retrieved {} received offers for user {}", receivedOffers.size(), currentUser.getUsername());
    }

    private void handleGetListingOffers(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        String listingId = request.getListingId();

        // TODO: Add permission check - user should own the listing to see its offers

        List<OfferDTO> listingOffers = offerService.getOffersForListing(listingId);

        response.setType(OfferMessage.Type.GET_LISTING_OFFERS_RESPONSE);
        response.setOffers(listingOffers);
        response.setListingId(listingId);
        response.setSuccess(true);

        logger.info("Retrieved {} offers for listing {} by user {}",
                listingOffers.size(), listingId, currentUser.getUsername());
    }

    private void handleGetOfferHistory(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        // Get all offers where the user was involved (sent or received)
        List<OfferDTO> userOffers = offerService.getUserOffers(currentUser.getId());
        List<OfferDTO> receivedOffers = offerService.getReceivedOffers(currentUser.getId());

        // Combine and filter for completed/rejected/withdrawn offers only
        List<OfferDTO> historyOffers = new ArrayList<>();

        userOffers.stream()
                .filter(offer -> offer.getStatus() == OfferStatus.COMPLETED ||
                        offer.getStatus() == OfferStatus.REJECTED ||
                        offer.getStatus() == OfferStatus.WITHDRAWN)
                .forEach(historyOffers::add);

        receivedOffers.stream()
                .filter(offer -> offer.getStatus() == OfferStatus.COMPLETED ||
                        offer.getStatus() == OfferStatus.REJECTED ||
                        offer.getStatus() == OfferStatus.WITHDRAWN)
                .forEach(historyOffers::add);

        response.setType(OfferMessage.Type.GET_OFFER_HISTORY_RESPONSE);
        response.setOffers(historyOffers);
        response.setSuccess(true);

        logger.info("Retrieved {} history offers for user {}", historyOffers.size(), currentUser.getUsername());
    }

    private void handleAcceptOffer(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        String offerId = request.getOfferId();

        try {
            OfferDTO updatedOffer = offerService.updateOfferStatus(offerId, OfferStatus.ACCEPTED,
                    currentUser.getId());

            response.setType(OfferMessage.Type.ACCEPT_OFFER_RESPONSE);
            response.setOffer(updatedOffer);
            response.setSuccess(true);

            logger.info("Accepted offer {} by user {}", offerId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(OfferMessage.Type.ACCEPT_OFFER_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to accept offer: " + e.getMessage());
            logger.error("Failed to accept offer {}: {}", offerId, e.getMessage());
        }
    }

    private void handleRejectOffer(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        String offerId = request.getOfferId();

        try {
            OfferDTO updatedOffer = offerService.updateOfferStatus(offerId, OfferStatus.REJECTED,
                    currentUser.getId());

            response.setType(OfferMessage.Type.REJECT_OFFER_RESPONSE);
            response.setOffer(updatedOffer);
            response.setSuccess(true);

            logger.info("Rejected offer {} by user {}", offerId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(OfferMessage.Type.REJECT_OFFER_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to reject offer: " + e.getMessage());
            logger.error("Failed to reject offer {}: {}", offerId, e.getMessage());
        }
    }

    private void handleConfirmTransaction(OfferMessage message, OfferMessage response, UserEntity user) {
        try {
            Long userId = user.getId();
            OfferDTO result = offerService.confirmTransaction(message.getOfferId(), userId);
            
            response.setMessageId(UUID.randomUUID().toString());
            response.setTimestamp(System.currentTimeMillis());
            response.setType(OfferMessage.Type.CONFIRM_TRANSACTION_RESPONSE);
            response.setSuccess(true);
            response.setOffer(result);
            logger.info("Transaction confirmation successful for offer: {}", message.getOfferId());
            
        } catch (Exception e) {
            logger.error("Failed to confirm transaction for offer: {}", message.getOfferId(), e);
            response.setErrorMessage("Failed to confirm transaction: " + e.getMessage());
        }
    }

    private void handleCancelTransaction(OfferMessage message, OfferMessage response, UserEntity user) {
        try {
            Long userId = user.getId();
            OfferDTO result = offerService.cancelTransaction(message.getOfferId(), userId);
            response.setMessageId(UUID.randomUUID().toString());
            response.setTimestamp(System.currentTimeMillis());
            response.setType(OfferMessage.Type.CANCEL_TRANSACTION_RESPONSE);
            response.setSuccess(true);
            response.setOffer(result);
            logger.info("Transaction cancellation successful for offer: {}", message.getOfferId());
        } catch (Exception e) {
            logger.error("Failed to cancel transaction for offer: {}", message.getOfferId(), e);
            response.setErrorMessage("Failed to cancel transaction: " + e.getMessage());
        }
    }

    private void handleWithdrawOffer(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        String offerId = request.getOfferId();

        try {
            OfferDTO updatedOffer = offerService.updateOfferStatus(offerId, OfferStatus.WITHDRAWN,
                    currentUser.getId());

            response.setType(OfferMessage.Type.WITHDRAW_OFFER_RESPONSE);
            response.setOffer(updatedOffer);
            response.setSuccess(true);

            logger.info("Withdrew offer {} by user {}", offerId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(OfferMessage.Type.WITHDRAW_OFFER_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to withdraw offer: " + e.getMessage());
            logger.error("Failed to withdraw offer {}: {}", offerId, e.getMessage());
        }
    }

    private void handleUpdateOfferStatus(OfferMessage request, OfferMessage response, UserEntity currentUser) {
        String offerId = request.getOfferId();
        OfferDTO offerData = request.getOffer();

        if (offerData == null || offerData.getStatus() == null) {
            response.setType(OfferMessage.Type.UPDATE_OFFER_STATUS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Offer status is required");
            return;
        }

        OfferDTO updatedOffer = offerService.updateOfferStatus(offerId, offerData.getStatus(), currentUser.getId());

        response.setType(OfferMessage.Type.UPDATE_OFFER_STATUS_RESPONSE);
        response.setOffer(updatedOffer);
        response.setSuccess(true);

        logger.info("Updated offer {} status to {} by user {}",
                offerId, offerData.getStatus(), currentUser.getUsername());
    }
}