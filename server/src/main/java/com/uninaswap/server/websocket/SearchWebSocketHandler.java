package com.uninaswap.server.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.enums.Category;
import com.uninaswap.common.message.SearchMessage;
import com.uninaswap.server.service.ListingService;
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

@Component
public class SearchWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SearchWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final ListingService listingService;

    @Autowired
    public SearchWebSocketHandler(ObjectMapper objectMapper, ListingService listingService) {
        this.objectMapper = objectMapper;
        this.listingService = listingService;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        logger.debug("Received search message: {}", message.getPayload());

        try {
            SearchMessage searchMessage = objectMapper.readValue(message.getPayload(), SearchMessage.class);
            SearchMessage response = new SearchMessage();

            try {
                // Search operations are public - no authentication required
                switch (searchMessage.getType()) {
                    case SEARCH_REQUEST:
                        handleSearchRequest(searchMessage, response);
                        break;

                    default:
                        response.setSuccess(false);
                        response.setErrorMessage("Unknown search message type: " + searchMessage.getType());
                        break;
                }
            } catch (Exception e) {
                response.setSuccess(false);
                response.setErrorMessage("Error processing search request: " + e.getMessage());
                logger.error("Error processing search message", e);
            }

            // Send the response back to the client
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));

        } catch (Exception e) {
            logger.error("Error parsing search message", e);
            SearchMessage errorResponse = new SearchMessage();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Error processing request: " + e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    private void handleSearchRequest(SearchMessage request, SearchMessage response) {
        try {
            // Validate and sanitize parameters
            int page = Math.max(0, request.getPage());
            int size = Math.min(100, Math.max(1, request.getSize())); // Limit max size to 100
            
            String query = request.getQuery();
            String listingType = request.getListingType();
            Category category = request.getCategory();

            // Create pageable
            Pageable pageable = PageRequest.of(page, size);

            // Perform search based on parameters
            Page<ListingDTO> searchResults = performSearch(query, listingType, category, pageable);

            // Set response data
            response.setType(SearchMessage.Type.SEARCH_RESPONSE);
            response.setResults(searchResults.getContent());
            response.setPage(page);
            response.setSize(size);
            response.setTotalElements(searchResults.getTotalElements());
            response.setTotalPages(searchResults.getTotalPages());
            response.setHasMore(searchResults.hasNext());
            response.setSuccess(true);

            logger.info("Search completed: query='{}', type='{}', category={}, page={}, results={}, total={}",
                    query, listingType, category, page, searchResults.getContent().size(), searchResults.getTotalElements());

        } catch (Exception e) {
            logger.error("Error performing search", e);
            response.setType(SearchMessage.Type.SEARCH_RESPONSE);
            response.setSuccess(false);
            response.setErrorMessage("Error performing search: " + e.getMessage());
        }
    }

    private Page<ListingDTO> performSearch(String query, String listingType, Category category, Pageable pageable) {
        // Determine search strategy based on provided parameters
        boolean hasQuery = query != null && !query.trim().isEmpty();
        boolean hasListingType = listingType != null && !listingType.trim().isEmpty() && !listingType.equalsIgnoreCase("all");
        boolean hasCategory = category != null && category != Category.ALL;

        if (!hasQuery && !hasListingType && !hasCategory) {
            // No filters - return all active listings
            return listingService.getActiveListings(pageable);
        }

        if (hasQuery && !hasListingType && !hasCategory) {
            // Text search only
            return listingService.searchListingsByText(query.trim(), pageable);
        }

        if (!hasQuery && hasListingType && !hasCategory) {
            // Filter by listing type only
            return listingService.getListingsByType(listingType, pageable);
        }

        if (!hasQuery && !hasListingType && hasCategory) {
            // Filter by category only
            return listingService.getListingsByCategory(category, pageable);
        }

        if (hasQuery && hasListingType && !hasCategory) {
            // Text search + listing type filter
            return listingService.searchListingsByTextAndType(query.trim(), listingType, pageable);
        }

        if (hasQuery && !hasListingType && hasCategory) {
            // Text search + category filter
            return listingService.searchListingsByTextAndCategory(query.trim(), category, pageable);
        }

        if (!hasQuery && hasListingType && hasCategory) {
            // Listing type + category filter
            return listingService.getListingsByTypeAndCategory(listingType, category, pageable);
        }

        // All filters: text search + listing type + category
        return listingService.searchListingsByTextTypeAndCategory(query.trim(), listingType, category, pageable);
    }
}
