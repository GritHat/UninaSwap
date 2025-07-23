package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.common.message.ItemMessage;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.exception.UnauthorizedException;
import com.uninaswap.server.service.ItemService;
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
public class ItemWebSocketHandler extends TextWebSocketHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final ItemService itemService;
    private final SessionService sessionService;
    
    @Autowired
    public ItemWebSocketHandler(ObjectMapper objectMapper, ItemService itemService, SessionService sessionService) {
        this.objectMapper = objectMapper;
        this.itemService = itemService;
        this.sessionService = sessionService;
    }
    
    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received item message: {}", message.getPayload());
        
        try {
            ItemMessage itemMessage = objectMapper.readValue(message.getPayload(), ItemMessage.class);
            ItemMessage response = new ItemMessage();
            
            try {
                // Check if session is authenticated for operations that require it
                UserEntity currentUser = null;
                if (requiresAuthentication(itemMessage.getType())) {
                    currentUser = sessionService.validateSession(session);
                    if (currentUser == null) {
                        throw new UnauthorizedException("Not authenticated");
                    }
                }
                
                switch (itemMessage.getType()) {
                    case GET_ITEMS_REQUEST:
                        handleGetItems(itemMessage, response, currentUser);
                        break;
                        
                    case ADD_ITEM_REQUEST:
                        handleAddItem(itemMessage, response, currentUser);
                        break;
                        
                    case UPDATE_ITEM_REQUEST:
                        handleUpdateItem(itemMessage, response, currentUser);
                        break;
                        
                    case DELETE_ITEM_REQUEST:
                        handleDeleteItem(itemMessage, response, currentUser);
                        break;
                        
                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown item message type: " + itemMessage.getType());
                        break;
                }
            } catch (UnauthorizedException e) {
                response.setSuccess(false);
                response.setErrorMessage("Authentication required: " + e.getMessage());
                logger.warn("Authentication failed for item operation: {}", e.getMessage());
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing item request: " + e.getMessage());
                logger.error("Error processing item message", e);
            }
            
            // Send the response back to the client
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
            
        } catch (Exception e) {
            logger.error("Error parsing item message", e);
            ItemMessage errorResponse = new ItemMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }
    
    private boolean requiresAuthentication(ItemMessage.Type type) {
        // All item operations require authentication
        return true;
    }
    
    private void handleGetItems(ItemMessage request, ItemMessage response, UserEntity currentUser) {
        List<ItemDTO> items = itemService.getUserItems(currentUser.getId());
        response.setType(ItemMessage.Type.GET_ITEMS_RESPONSE);
        response.setItems(items);
        response.setSuccess(true);
        
        logger.info("Retrieved {} items for user {}", items.size(), currentUser.getUsername());
    }
    
    private void handleAddItem(ItemMessage request, ItemMessage response, UserEntity currentUser) {
        ItemDTO newItem = request.getItem();
        
        // Set owner to current user
        newItem.setOwnerId(currentUser.getId());
        
        ItemDTO savedItem = itemService.addItem(newItem);
        
        response.setType(ItemMessage.Type.ADD_ITEM_RESPONSE);
        response.setItem(savedItem);
        response.setSuccess(true);
        
        logger.info("Added new item: {} for user {}", savedItem.getId(), currentUser.getUsername());
    }
    
    private void handleUpdateItem(ItemMessage request, ItemMessage response, UserEntity currentUser) {
        ItemDTO itemToUpdate = request.getItem();
        
        // Verify ownership
        if (!itemService.isItemOwnedByUser(itemToUpdate.getId(), currentUser.getId())) {
            response.setSuccess(false);
            response.setErrorMessage("You don't own this item");
            response.setType(ItemMessage.Type.UPDATE_ITEM_RESPONSE);
            return;
        }
        
        // Preserve owner ID
        itemToUpdate.setOwnerId(currentUser.getId());
        
        ItemDTO updatedItem = itemService.updateItem(itemToUpdate);
        
        response.setType(ItemMessage.Type.UPDATE_ITEM_RESPONSE);
        response.setItem(updatedItem);
        response.setSuccess(true);
        
        logger.info("Updated item: {} for user {}", updatedItem.getId(), currentUser.getUsername());
    }
    
    private void handleDeleteItem(ItemMessage request, ItemMessage response, UserEntity currentUser) {
        String itemId = request.getItem().getId();
        
        // Verify ownership
        if (!itemService.isItemOwnedByUser(itemId, currentUser.getId())) {
            response.setSuccess(false);
            response.setErrorMessage("You don't own this item");
            response.setType(ItemMessage.Type.DELETE_ITEM_RESPONSE);
            return;
        }
        
        // Check if item is used in any active listings
        if (itemService.isItemUsedInActiveListing(itemId)) {
            response.setSuccess(false);
            response.setErrorMessage("Item cannot be deleted because it's part of an active listing");
            response.setType(ItemMessage.Type.DELETE_ITEM_RESPONSE);
            return;
        }
        
        itemService.deleteItem(itemId);
        ItemDTO deletedItem = new ItemDTO();
        deletedItem.setId(itemId);
        response.setType(ItemMessage.Type.DELETE_ITEM_RESPONSE);
        response.setSuccess(true);
        response.setItem(deletedItem);

        logger.info("Deleted item: {} for user {}", itemId, currentUser.getUsername());
    }
}