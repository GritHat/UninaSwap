package com.uninaswap.common.message;

import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.enums.Category;

import java.util.List;

public class SearchMessage extends Message {
    
    public enum Type {
        SEARCH_REQUEST,
        SEARCH_RESPONSE
    }
    
    private Type type;
    private String query;
    private String listingType;
    private Category category;
    private int page = 0;
    private int size = 50;
    private List<ListingDTO> results;
    private long totalElements;
    private int totalPages;
    private boolean hasMore;
    
    // Constructors
    public SearchMessage() {
        super();
        setMessageType("search");
    }
    
    // Getters and setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public List<ListingDTO> getResults() { return results; }
    public void setResults(List<ListingDTO> results) { this.results = results; }
    
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public boolean isHasMore() { return hasMore; }
    public void setHasMore(boolean hasMore) { this.hasMore = hasMore; }
    
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
