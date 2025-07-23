package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.FavoriteDTO;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.message.FavoriteMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.FavoriteService;
import com.uninaswap.server.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;

@Component
public class FavoriteWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final FavoriteService favoriteService;
    private final SessionService sessionService;

    @Autowired
    public FavoriteWebSocketHandler(ObjectMapper objectMapper, FavoriteService favoriteService,
            SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.favoriteService = favoriteService;
        this.sessionService = sessionService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received favorite message: {}", message.getPayload());

        try {
            FavoriteMessage favoriteMessage = objectMapper.readValue(message.getPayload(), FavoriteMessage.class);
            FavoriteMessage response = new FavoriteMessage();

            try {
                UserEntity currentUser = sessionService.validateSession(session);
                if (currentUser == null) {
                    throw new UnauthorizedException("Not authenticated");
                }

                switch (favoriteMessage.getType()) {
                    case ADD_FAVORITE_REQUEST:
                        handleAddFavorite(favoriteMessage, response, currentUser);
                        break;

                    case REMOVE_FAVORITE_REQUEST:
                        handleRemoveFavorite(favoriteMessage, response, currentUser);
                        break;

                    case GET_USER_FAVORITES_REQUEST:
                        handleGetUserFavorites(favoriteMessage, response, currentUser);
                        break;

                    case IS_FAVORITE_REQUEST:
                        handleIsFavorite(favoriteMessage, response, currentUser);
                        break;

                    case TOGGLE_FAVORITE_REQUEST:
                        handleToggleFavorite(favoriteMessage, response, currentUser);
                        break;

                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown favorite message type: " + favoriteMessage.getType());
                        break;
                }
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for favorite operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing favorite request: " + e.getMessage());
                logger.error("Error processing favorite message", e);
            }

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("Error parsing favorite message", e);
            FavoriteMessage errorResponse = new FavoriteMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private void handleAddFavorite(FavoriteMessage request, FavoriteMessage response, UserEntity currentUser) {
        String listingId = request.getListingId();

        try {
            FavoriteDTO favorite = favoriteService.addFavorite(currentUser.getId(), listingId);

            response.setType(FavoriteMessage.Type.ADD_FAVORITE_RESPONSE);
            response.setFavorite(favorite);
            response.setSuccess(true);

            logger.info("Added favorite: user {} listing {}", currentUser.getUsername(), listingId);
        } catch (IllegalStateException e) {
            
            response.setType(FavoriteMessage.Type.ADD_FAVORITE_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Listing already in favorites");
            logger.warn("Attempt to add duplicate favorite: user {} listing {}", currentUser.getUsername(), listingId);
        }
    }

    private void handleRemoveFavorite(FavoriteMessage request, FavoriteMessage response, UserEntity currentUser) {
        String listingId = request.getListingId();

        favoriteService.removeFavorite(currentUser.getId(), listingId);

        response.setType(FavoriteMessage.Type.REMOVE_FAVORITE_RESPONSE);
        response.setListingId(listingId);
        response.setSuccess(true);

        logger.info("Removed favorite: user {} listing {}", currentUser.getUsername(), listingId);
    }

    private void handleGetUserFavorites(FavoriteMessage request, FavoriteMessage response, UserEntity currentUser) {
        List<FavoriteDTO> favorites = favoriteService.getUserFavorites(currentUser.getId());
        List<ListingDTO> favoriteListings = favoriteService.getUserFavoriteListings(currentUser.getId());

        response.setType(FavoriteMessage.Type.GET_USER_FAVORITES_RESPONSE);
        response.setFavorites(favorites);
        response.setFavoriteListings(favoriteListings);
        response.setSuccess(true);

        logger.info("Retrieved {} favorites for user {}", favorites.size(), currentUser.getUsername());
    }

    private void handleIsFavorite(FavoriteMessage request, FavoriteMessage response, UserEntity currentUser) {
        String listingId = request.getListingId();

        boolean isFavorite = favoriteService.isFavorite(currentUser.getId(), listingId);

        response.setType(FavoriteMessage.Type.IS_FAVORITE_RESPONSE);
        response.setFavoriteEnabled(isFavorite);
        response.setSuccess(true);

        logger.debug("Checked favorite status: user {} listing {} -> {}",
                currentUser.getUsername(), listingId, isFavorite);
    }

    private void handleToggleFavorite(FavoriteMessage request, FavoriteMessage response, UserEntity currentUser) {
        String listingId = request.getListingId();

        boolean isFavorite = favoriteService.toggleFavorite(currentUser.getId(), listingId);

        response.setType(FavoriteMessage.Type.TOGGLE_FAVORITE_RESPONSE);
        response.setFavoriteEnabled(isFavorite);
        response.setSuccess(true);

        logger.info("Toggled favorite: user {} listing {} -> {}",
                currentUser.getUsername(), listingId, isFavorite);
    }
}
