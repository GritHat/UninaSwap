package com.uninaswap.client.service;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.websocket.WebSocketClient;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.enums.Category;
import com.uninaswap.common.message.SearchMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.CompletableFuture;

/**
 * 
 */
public class SearchService {
    
    /**
     * 
     */
    private static SearchService instance;
    /**
     * 
     */
    private final WebSocketClient webSocketClient;
    /**
     * 
     */
    private final ObservableList<ListingViewModel> searchResults = FXCollections.observableArrayList();
    /**
     * 
     */
    private CompletableFuture<SearchResult> currentSearchFuture;
    /**
     * 
     */
    private boolean isSearching = false;
    /**
     * 
     */
    private String lastQuery = "";
    /**
     * 
     */
    private String lastListingType = "all";
    /**
     * 
     */
    private Category lastCategory = Category.ALL;
    /**
     * 
     */
    public static class SearchResult {
        private final ObservableList<ListingViewModel> results;
        private final long totalResults;
        private final boolean hasMore;
        
        public SearchResult(ObservableList<ListingViewModel> results, long totalResults, boolean hasMore) {
            this.results = results;
            this.totalResults = totalResults;
            this.hasMore = hasMore;
        }
        
        public ObservableList<ListingViewModel> getResults() { return results; }
        public long getTotalResults() { return totalResults; }
        public boolean hasMore() { return hasMore; }
    }
    
    /**
     * @return
     */
    public static SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
        }
        return instance;
    }
    
    /**
     * 
     */
    private SearchService() {
        this.webSocketClient = WebSocketClient.getInstance();
        this.webSocketClient.registerMessageHandler(SearchMessage.class, this::handleSearchResponse);
    }
    
    /**
     * Perform search with given parameters
     * 
     * @param query The search query string
     * @param listingType The type of listing to search for (e.g., "all", "offer", "request")
     * @param category The category to filter results by
     * @return CompletableFuture with SearchResult containing search results
     *         or an exception if the search fails
     */
    /**
     * @param query
     * @param listingType
     * @param category
     * @return
     */
    public CompletableFuture<SearchResult> search(String query, String listingType, Category category) {
        return search(query, listingType, category, 0, 50); // Default pagination
    }
    
    /**
     * Perform search with pagination
     * 
     * @param query The search query string
     * @param listingType The type of listing to search for (e.g., "all", "offer", "request")
     * @param category The category to filter results by
     * @param page The page number to retrieve (0-based)
     * @param size The number of results per page
     * @return CompletableFuture with SearchResult containing search results
     *         or an exception if the search fails
     */
    /**
     * @param query
     * @param listingType
     * @param category
     * @param page
     * @param size
     * @return
     */
    public CompletableFuture<SearchResult> search(String query, String listingType, Category category, int page, int size) {
        if (currentSearchFuture != null && !currentSearchFuture.isDone()) {
            currentSearchFuture.cancel(true);
        }
        currentSearchFuture = new CompletableFuture<>();
        isSearching = true;
        lastQuery = query != null ? query.trim() : "";
        lastListingType = listingType != null ? listingType : "all";
        lastCategory = category != null ? category : Category.ALL;
        
        try {
            SearchMessage searchMessage = new SearchMessage();
            searchMessage.setType(SearchMessage.Type.SEARCH_REQUEST);
            searchMessage.setQuery(lastQuery);
            searchMessage.setListingType(lastListingType);
            searchMessage.setCategory(lastCategory);
            searchMessage.setPage(page);
            searchMessage.setSize(size);
            
            System.out.println("Sending search request: " + searchMessage);
            
            webSocketClient.sendMessage(searchMessage)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        isSearching = false;
                        if (currentSearchFuture != null && !currentSearchFuture.isDone()) {
                            currentSearchFuture.completeExceptionally(ex);
                        }
                    });
                    return null;
                });
                
        } catch (Exception e) {
            isSearching = false;
            currentSearchFuture.completeExceptionally(e);
        }
        
        return currentSearchFuture;
    }
    
    /**
     * Clear search results and return to normal view
     */
    /**
     * 
     */
    public void clearSearch() {
        Platform.runLater(() -> {
            searchResults.clear();
            lastQuery = "";
            lastListingType = "all";
            lastCategory = Category.ALL;
            isSearching = false;
        });
    }
    
    /**
     * Check if currently in search mode
     */
    /**
     * @return
     */
    public boolean isInSearchMode() {
        return !lastQuery.isEmpty() || !lastListingType.equals("all") || lastCategory != Category.ALL;
    }
    
    /**
     * Get current search results
     * 
     * @return ObservableList of ListingViewModel containing current search results
     */
    /**
     * @return
     */
    public ObservableList<ListingViewModel> getSearchResults() {
        return searchResults;
    }
    
    /**
     * Get last search parameters for reference
     */
    /**
     * @return
     */
    public String getLastQuery() { return lastQuery; }
    /**
     * @return
     */
    public String getLastListingType() { return lastListingType; }
    /**
     * @return
     */
    public Category getLastCategory() { return lastCategory; }
    /**
     * @return
     */
    public boolean isSearching() { return isSearching; }
    
    /**
     * @param message
     */
    private void handleSearchResponse(SearchMessage message) {
        if (message.getType() == SearchMessage.Type.SEARCH_RESPONSE) {
            Platform.runLater(() -> {
                isSearching = false;
                
                if (message.isSuccess()) {
                    ObservableList<ListingViewModel> results = FXCollections.observableArrayList();
                    if (message.getResults() != null) {
                        for (ListingDTO dto : message.getResults()) {
                            results.add(ViewModelMapper.getInstance().toViewModel(dto));
                        }
                    }
                    searchResults.setAll(results);
                    SearchResult searchResult = new SearchResult(results, message.getTotalElements(), message.isHasMore());
                    if (currentSearchFuture != null && !currentSearchFuture.isDone()) {
                        currentSearchFuture.complete(searchResult);
                    }
                    
                    System.out.println("Search completed: " + results.size() + " results, total: " + message.getTotalElements());
                    
                } else {
                    String errorMessage = message.getErrorMessage() != null ? message.getErrorMessage() : "Search failed";
                    Exception searchException = new Exception(errorMessage);
                    
                    if (currentSearchFuture != null && !currentSearchFuture.isDone()) {
                        currentSearchFuture.completeExceptionally(searchException);
                    }
                    
                    System.err.println("Search failed: " + errorMessage);
                }
            });
        }
    }
}
