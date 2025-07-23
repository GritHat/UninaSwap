package com.uninaswap.common.message;

import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.enums.Category;

import java.util.List;

/**
 * 
 */
public class SearchMessage extends Message {
    
    /**
     * 
     */
    public enum Type {
        SEARCH_REQUEST,
        SEARCH_RESPONSE
    }
    
    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private String query;
    /**
     * 
     */
    private String listingType;
    /**
     * 
     */
    private Category category;
    /**
     * 
     */
    private int page = 0;
    /**
     * 
     */
    private int size = 50;
    /**
     * 
     */
    private List<ListingDTO> results;
    /**
     * 
     */
    private long totalElements;
    /**
     * 
     */
    private int totalPages;
    /**
     * 
     */
    private boolean hasMore;
    
    // Constructors
    /**
     * 
     */
    public SearchMessage() {
        super();
        setMessageType("search");
    }
    
    // Getters and setters
    /**
     * @return
     */
    public Type getType() { return type; }
    /**
     * @param type
     */
    public void setType(Type type) { this.type = type; }
    
    /**
     * @return
     */
    public String getQuery() { return query; }
    /**
     * @param query
     */
    public void setQuery(String query) { this.query = query; }
    
    /**
     * @return
     */
    public String getListingType() { return listingType; }
    /**
     * @param listingType
     */
    public void setListingType(String listingType) { this.listingType = listingType; }
    
    /**
     * @return
     */
    public Category getCategory() { return category; }
    /**
     * @param category
     */
    public void setCategory(Category category) { this.category = category; }
    
    /**
     * @return
     */
    public int getPage() { return page; }
    /**
     * @param page
     */
    public void setPage(int page) { this.page = page; }
    
    /**
     * @return
     */
    public int getSize() { return size; }
    /**
     * @param size
     */
    public void setSize(int size) { this.size = size; }
    
    /**
     * @return
     */
    public List<ListingDTO> getResults() { return results; }
    /**
     * @param results
     */
    public void setResults(List<ListingDTO> results) { this.results = results; }
    
    /**
     * @return
     */
    public long getTotalElements() { return totalElements; }
    /**
     * @param totalElements
     */
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    
    /**
     * @return
     */
    public int getTotalPages() { return totalPages; }
    /**
     * @param totalPages
     */
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    /**
     * @return
     */
    public boolean isHasMore() { return hasMore; }
    /**
     * @param hasMore
     */
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
    
    /**
     *
     */
    @Override
    public String toString() {
        return "SearchMessage{" +
                "type=" + type +
                ", query='" + query + '\'' +
                ", listingType='" + listingType + '\'' +
                ", category=" + category +
                ", page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", hasMore=" + hasMore +
                '}';
    }
}
