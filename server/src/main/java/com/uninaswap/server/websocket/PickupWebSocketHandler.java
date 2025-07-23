package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.common.enums.PickupStatus;
import com.uninaswap.common.message.PickupMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.PickupService;
import com.uninaswap.server.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class PickupWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(PickupWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final PickupService pickupService;
    private final SessionService sessionService;

    @Autowired
    public PickupWebSocketHandler(ObjectMapper objectMapper, PickupService pickupService,
            SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.pickupService = pickupService;
        this.sessionService = sessionService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received pickup message: {}", message.getPayload());

        try {
            PickupMessage pickupMessage = objectMapper.readValue(message.getPayload(), PickupMessage.class);
            PickupMessage response = new PickupMessage();

            try {
                
                UserEntity currentUser = sessionService.validateSession(session);
                if (currentUser == null) {
                    throw new UnauthorizedException("Not authenticated");
                }

                switch (pickupMessage.getType()) {
                    case CREATE_PICKUP_REQUEST:
                        handleCreatePickup(pickupMessage, response, currentUser);
                        break;

                    case ACCEPT_PICKUP_REQUEST:
                        handleAcceptPickup(pickupMessage, response, currentUser);
                        break;

                    case UPDATE_PICKUP_STATUS_REQUEST:
                        handleUpdatePickupStatus(pickupMessage, response, currentUser);
                        break;

                    case UPDATE_PICKUP_REQUEST:
                        handleUpdatePickup(pickupMessage, response, currentUser);
                        break;

                    case GET_PICKUP_REQUEST:
                        handleGetPickup(pickupMessage, response, currentUser);
                        break;

                    case GET_PICKUP_BY_OFFER_REQUEST:
                        handleGetPickupByOffer(pickupMessage, response, currentUser);
                        break;

                    case GET_USER_PICKUPS_REQUEST:
                        handleGetUserPickups(pickupMessage, response, currentUser);
                        break;

                    case GET_UPCOMING_PICKUPS_REQUEST:
                        handleGetUpcomingPickups(pickupMessage, response, currentUser);
                        break;

                    case GET_PAST_PICKUPS_REQUEST:
                        handleGetPastPickups(pickupMessage, response, currentUser);
                        break;

                    case GET_PICKUPS_BY_STATUS_REQUEST:
                        handleGetPickupsByStatus(pickupMessage, response, currentUser);
                        break;

                    case DELETE_PICKUP_REQUEST:
                        handleDeletePickup(pickupMessage, response, currentUser);
                        break;

                    case REJECT_PICKUP_REQUEST:
                        handleRejectPickup(pickupMessage, response, currentUser);
                        break;

                    case CANCEL_PICKUP_REQUEST:
                        handleCancelPickup(pickupMessage, response, currentUser);
                        break;

                    case CANCEL_PICKUP_ARRANGEMENT_REQUEST:
                        handleCancelPickupArrangement(pickupMessage, response, currentUser);
                        break;

                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown pickup message type: " + pickupMessage.getType());
                        break;
                }
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for pickup operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing pickup request: " + e.getMessage());
                logger.error("Error processing pickup message", e);
            }

            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("Error parsing pickup message", e);
            PickupMessage errorResponse = new PickupMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private void handleCreatePickup(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            PickupDTO newPickup = request.getPickup();
            PickupDTO createdPickup = pickupService.createPickup(newPickup, currentUser.getId());

            response.setType(PickupMessage.Type.CREATE_PICKUP_RESPONSE);
            response.setPickup(createdPickup);
            response.setSuccess(true);

            logger.info("Created pickup {} for offer {} by user {}",
                    createdPickup.getId(), createdPickup.getOfferId(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.CREATE_PICKUP_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to create pickup: " + e.getMessage());
            logger.error("Failed to create pickup: {}", e.getMessage());
        }
    }

    private void handleAcceptPickup(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = request.getPickupId();
            LocalDate selectedDate = request.getSelectedDate();
            LocalTime selectedTime = request.getSelectedTime();

            PickupDTO updatedPickup = pickupService.acceptPickup(pickupId, selectedDate, selectedTime,
                    currentUser.getId());

            response.setType(PickupMessage.Type.ACCEPT_PICKUP_RESPONSE);
            response.setPickup(updatedPickup);
            response.setSuccess(true);

            logger.info("Accepted pickup {} with date {} time {} by user {}",
                    pickupId, selectedDate, selectedTime, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.ACCEPT_PICKUP_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to accept pickup: " + e.getMessage());
            logger.error("Failed to accept pickup {}: {}", request.getPickupId(), e.getMessage());
        }
    }

    private void handleUpdatePickupStatus(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = request.getPickupId();
            PickupStatus newStatus = request.getStatus();

            PickupDTO updatedPickup = pickupService.updatePickupStatus(pickupId, newStatus, currentUser.getId());

            response.setType(PickupMessage.Type.UPDATE_PICKUP_STATUS_RESPONSE);
            response.setPickup(updatedPickup);
            response.setSuccess(true);

            logger.info("Updated pickup {} status to {} by user {}",
                    pickupId, newStatus, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.UPDATE_PICKUP_STATUS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to update pickup status: " + e.getMessage());
            logger.error("Failed to update pickup status {}: {}", request.getPickupId(), e.getMessage());
        }
    }

    private void handleUpdatePickup(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = request.getPickupId();
            PickupDTO pickupDTO = request.getPickup();

            PickupDTO updatedPickup = pickupService.updatePickup(pickupId, pickupDTO, currentUser.getId());

            response.setType(PickupMessage.Type.UPDATE_PICKUP_RESPONSE);
            response.setPickup(updatedPickup);
            response.setSuccess(true);

            logger.info("Updated pickup {} by user {}", pickupId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.UPDATE_PICKUP_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to update pickup: " + e.getMessage());
            logger.error("Failed to update pickup {}: {}", request.getPickupId(), e.getMessage());
        }
    }

    private void handleGetPickup(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = request.getPickupId();
            PickupDTO pickup = pickupService.getPickupById(pickupId);

            response.setType(PickupMessage.Type.GET_PICKUP_RESPONSE);
            response.setPickup(pickup);
            response.setSuccess(true);

            logger.info("Retrieved pickup {} for user {}", pickupId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.GET_PICKUP_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get pickup: " + e.getMessage());
            logger.error("Failed to get pickup {}: {}", request.getPickupId(), e.getMessage());
        }
    }

    private void handleGetUserPickups(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            List<PickupDTO> pickups = pickupService.getUserPickups(currentUser.getId());

            response.setType(PickupMessage.Type.GET_USER_PICKUPS_RESPONSE);
            response.setPickups(pickups);
            response.setSuccess(true);

            logger.info("Retrieved {} pickups for user {}", pickups.size(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.GET_USER_PICKUPS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get user pickups: " + e.getMessage());
            logger.error("Failed to get user pickups: {}", e.getMessage());
        }
    }

    private void handleGetUpcomingPickups(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            List<PickupDTO> pickups = pickupService.getUpcomingPickups(currentUser.getId());

            response.setType(PickupMessage.Type.GET_UPCOMING_PICKUPS_RESPONSE);
            response.setPickups(pickups);
            response.setSuccess(true);

            logger.info("Retrieved {} upcoming pickups for user {}", pickups.size(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.GET_UPCOMING_PICKUPS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get upcoming pickups: " + e.getMessage());
            logger.error("Failed to get upcoming pickups: {}", e.getMessage());
        }
    }

    private void handleGetPastPickups(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            List<PickupDTO> pickups = pickupService.getPastPickups(currentUser.getId());

            response.setType(PickupMessage.Type.GET_PAST_PICKUPS_RESPONSE);
            response.setPickups(pickups);
            response.setSuccess(true);

            logger.info("Retrieved {} past pickups for user {}", pickups.size(), currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.GET_PAST_PICKUPS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get past pickups: " + e.getMessage());
            logger.error("Failed to get past pickups: {}", e.getMessage());
        }
    }

    private void handleGetPickupsByStatus(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            PickupStatus status = request.getStatus();
            List<PickupDTO> pickups = pickupService.getPickupsByStatus(currentUser.getId(), status);

            response.setType(PickupMessage.Type.GET_PICKUPS_BY_STATUS_RESPONSE);
            response.setPickups(pickups);
            response.setSuccess(true);

            logger.info("Retrieved {} pickups with status {} for user {}", pickups.size(), status,
                    currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.GET_PICKUPS_BY_STATUS_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to get pickups by status: " + e.getMessage());
            logger.error("Failed to get pickups by status: {}", e.getMessage());
        }
    }

    private void handleDeletePickup(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = request.getPickupId();
            pickupService.deletePickup(pickupId, currentUser.getId());

            response.setType(PickupMessage.Type.DELETE_PICKUP_RESPONSE);
            response.setSuccess(true);

            logger.info("Deleted pickup {} by user {}", pickupId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.DELETE_PICKUP_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to delete pickup: " + e.getMessage());
            logger.error("Failed to delete pickup {}: {}", request.getPickupId(), e.getMessage());
        }
    }

    private void handleRejectPickup(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = request.getPickupId();
            PickupDTO rejectedPickup = pickupService.rejectPickup(pickupId, currentUser.getId());

            response.setType(PickupMessage.Type.REJECT_PICKUP_RESPONSE);
            response.setPickup(rejectedPickup);
            response.setSuccess(true);

            logger.info("Rejected pickup {} by user {}", pickupId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.REJECT_PICKUP_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to reject pickup: " + e.getMessage());
            logger.error("Failed to reject pickup {}: {}", request.getPickupId(), e.getMessage());
        }
    }

    private void handleCancelPickup(PickupMessage request, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = request.getPickupId();
            pickupService.cancelPickupArrangement(pickupId, currentUser.getId());

            response.setType(PickupMessage.Type.CANCEL_PICKUP_RESPONSE);
            response.setSuccess(true);

            logger.info("Cancelled pickup {} by user {}", pickupId, currentUser.getUsername());
        } catch (Exception e) {
            response.setType(PickupMessage.Type.CANCEL_PICKUP_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Failed to cancel pickup: " + e.getMessage());
            logger.error("Failed to cancel pickup {}: {}", request.getPickupId(), e.getMessage());
        }
    }

    private void handleCancelPickupArrangement(PickupMessage message, PickupMessage response, UserEntity currentUser) {
        try {
            Long pickupId = message.getPickupId();
            Long userIdLong = Long.parseLong(currentUser.getId().toString());
            
            
            pickupService.cancelPickupArrangement(pickupId, userIdLong);
            
            
            response.setType(PickupMessage.Type.CANCEL_PICKUP_ARRANGEMENT_RESPONSE);
            response.setPickupId(pickupId);
            response.setSuccess(true);
        } catch (Exception e) {
            logger.error("Error cancelling pickup arrangement: {}", e.getMessage(), e);
            
            response.setType(PickupMessage.Type.CANCEL_PICKUP_ARRANGEMENT_RESPONSE);
            response.setPickupId(message.getPickupId());
            response.setSuccess(false);
            response.setErrorMessage("Failed to cancel pickup arrangement: " + e.getMessage());
        }
    }

    private void handleGetPickupByOffer(PickupMessage message, PickupMessage response, UserEntity currentUser) {
        try {
            String offerId = message.getOfferId();
            
            PickupDTO pickup = pickupService.getPickupByOfferId(offerId, currentUser.getId());
            response.setType(PickupMessage.Type.GET_PICKUP_BY_OFFER_RESPONSE);
            response.setOfferId(offerId);
            response.setSuccess(true);
            
            if (pickup != null) {
                response.setPickup(pickup);
            }
        } catch (Exception e) {
            logger.error("Error getting pickup by offer: {}", e.getMessage(), e);
            response.setType(PickupMessage.Type.GET_PICKUP_BY_OFFER_RESPONSE);
            response.setOfferId(message.getOfferId());
            response.setSuccess(false);
            response.setErrorMessage("Failed to get pickup: " + e.getMessage());
        }
    }
}