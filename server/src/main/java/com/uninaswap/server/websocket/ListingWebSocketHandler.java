package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.message.ListingMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.ListingService;
import com.uninaswap.server.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component
public class ListingWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ListingWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final ListingService listingService;
    private final SessionService sessionService;

    @Autowired
    public ListingWebSocketHandler(ObjectMapper objectMapper, ListingService listingService,
            SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.listingService = listingService;
        this.sessionService = sessionService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received listing message: {}", message.getPayload());

        try {
            ListingMessage listingMessage = objectMapper.readValue(message.getPayload(), ListingMessage.class);
            ListingMessage response = new ListingMessage();

            try {
                // Check if session is authenticated for operations that require it
                UserEntity currentUser = null;
                if (requiresAuthentication(listingMessage.getType())) {
                    currentUser = sessionService.validateSession(session);
                    if (currentUser == null) {
                        throw new UnauthorizedException("Not authenticated");
                    }
                }

                switch (listingMessage.getType()) {
                    case GET_LISTINGS_REQUEST:
                        handleGetListings(listingMessage, response);
                        break;

                    case GET_MY_LISTINGS_REQUEST:
                        handleGetMyListings(listingMessage, response, currentUser);
                        break;

                    case CREATE_LISTING_REQUEST:
                        handleCreateListing(listingMessage, response, currentUser);
                        break;

                    case UPDATE_LISTING_REQUEST:
                        handleUpdateListing(listingMessage, response, currentUser);
                        break;

                    case DELETE_LISTING_REQUEST:
                        handleDeleteListing(listingMessage, response, currentUser);
                        break;

                    case GET_LISTING_DETAIL_REQUEST:
                        handleGetListingDetail(listingMessage, response);
                        break;

                    case GET_USER_LISTINGS_REQUEST:
                        handleGetUserListings(listingMessage, response, currentUser);
                        break;

                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown listing message type: " + listingMessage.getType());
                        break;
                }
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for listing operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing listing request: " + e.getMessage());
                logger.error("Error processing listing message", e);
            }

            // Send the response back to the client
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("Error parsing listing message", e);
            ListingMessage errorResponse = new ListingMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private boolean requiresAuthentication(ListingMessage.Type type) {
        // Public endpoints don't require authentication
        return type != ListingMessage.Type.GET_LISTINGS_REQUEST &&
                type != ListingMessage.Type.GET_LISTING_DETAIL_REQUEST;
    }

    private void handleGetListings(ListingMessage request, ListingMessage response) {
        try {
            int page = Math.max(0, request.getPage());
            int size = Math.min(50, Math.max(1, request.getSize()));

            Pageable pageable = PageRequest.of(page, size);
            Page<ListingDTO> listings = listingService.getActiveListings(pageable);

            response.setType(ListingMessage.Type.GET_LISTINGS_RESPONSE);
            response.setListings(listings.getContent());
            response.setPage(page);
            response.setSize(size);
            response.setTotalElements(listings.getTotalElements());
            response.setTotalPages(listings.getTotalPages());
            response.setSuccess(true);

            logger.info("Retrieved {} listings (page {}/{})",
                    listings.getContent().size(), page + 1, listings.getTotalPages());

        } catch (Exception e) {
            logger.error("Error retrieving listings", e);
            response.setType(ListingMessage.Type.GET_LISTINGS_RESPONSE); // IMPORTANT: Set type even on error
            response.setSuccess(false);
            response.setErrorMessage("Error retrieving listings: " + e.getMessage());
        }
    }

    private void handleGetMyListings(ListingMessage request, ListingMessage response, UserEntity currentUser) {
        List<ListingDTO> userListings = listingService.getUserListings(currentUser.getId());

        response.setType(ListingMessage.Type.GET_MY_LISTINGS_RESPONSE);
        response.setListings(userListings);
        response.setSuccess(true);

        logger.info("Retrieved {} listings for user {}", userListings.size(), currentUser.getUsername());
    }

    private void handleCreateListing(ListingMessage request, ListingMessage response, UserEntity currentUser) {
        ListingDTO newListing = request.getListing();
        String listingTypeValue = request.getListingTypeValue();

        // Set creator to current user
        if (newListing.getCreator() == null ||
                !newListing.getCreator().getId().equals(currentUser.getId())) {
            newListing.getCreator().setId(currentUser.getId());
        }

        // Create listing based on type
        ListingDTO createdListing = listingService.createListing(newListing, listingTypeValue);

        response.setType(ListingMessage.Type.CREATE_LISTING_RESPONSE);
        response.setListing(createdListing);
        response.setSuccess(true);

        logger.info("Created new {} listing: {} for user {}",
                listingTypeValue, createdListing.getId(), currentUser.getUsername());
    }

    private void handleUpdateListing(ListingMessage request, ListingMessage response, UserEntity currentUser) {
        ListingDTO listingToUpdate = request.getListing();
        String listingTypeValue = request.getListingTypeValue();

        // Verify ownership
        if (!listingService.isListingOwnedByUser(listingToUpdate.getId(), currentUser.getId())) {
            response.setSuccess(false);
            response.setErrorMessage("You don't own this listing");
            response.setType(ListingMessage.Type.UPDATE_LISTING_RESPONSE);
            return;
        }

        // Update listing
        ListingDTO updatedListing = listingService.updateListing(listingToUpdate, listingTypeValue);

        response.setType(ListingMessage.Type.UPDATE_LISTING_RESPONSE);
        response.setListing(updatedListing);
        response.setSuccess(true);

        logger.info("Updated {} listing: {} for user {}",
                listingTypeValue, updatedListing.getId(), currentUser.getUsername());
    }

    private void handleDeleteListing(ListingMessage request, ListingMessage response, UserEntity currentUser) {
        String listingId = request.getListingId();

        // Verify ownership
        if (!listingService.isListingOwnedByUser(listingId, currentUser.getId())) {
            response.setSuccess(false);
            response.setErrorMessage("You don't own this listing");
            response.setType(ListingMessage.Type.DELETE_LISTING_RESPONSE);
            return;
        }

        listingService.deleteListing(listingId);

        response.setType(ListingMessage.Type.DELETE_LISTING_RESPONSE);
        response.setListingId(listingId);
        response.setSuccess(true);

        logger.info("Deleted listing: {} for user {}", listingId, currentUser.getUsername());
    }

    private void handleGetListingDetail(ListingMessage request, ListingMessage response) {
        String listingId = request.getListingId();
        ListingDTO listing = listingService.getListingById(listingId);

        if (listing == null) {
            response.setSuccess(false);
            response.setErrorMessage("Listing not found: " + listingId);
        } else {
            response.setSuccess(true);
            response.setListing(listing);
        }

        response.setType(ListingMessage.Type.GET_LISTING_DETAIL_RESPONSE);
        logger.info("Retrieved listing details for: {}", listingId);
    }

    private void handleGetUserListings(ListingMessage request, ListingMessage response, UserEntity currentUser) {
        Long userId = request.getUserId();
        
        if (userId == null) {
            response.setSuccess(false);
            response.setErrorMessage("User ID is required");
            response.setType(ListingMessage.Type.GET_USER_LISTINGS_RESPONSE);
            return;
        }
        
        try {
            List<ListingDTO> userListings = listingService.getUserListings(userId);
            
            response.setType(ListingMessage.Type.GET_USER_LISTINGS_RESPONSE);
            response.setListings(userListings);
            response.setSuccess(true);
            
            logger.info("Retrieved {} listings for user {}", userListings.size(), userId);
        } catch (Exception e) {
            logger.error("Error retrieving listings for user {}: {}", userId, e.getMessage());
            response.setSuccess(false);
            response.setErrorMessage("Failed to retrieve user listings: " + e.getMessage());
            response.setType(ListingMessage.Type.GET_USER_LISTINGS_RESPONSE);
        }
    }
}