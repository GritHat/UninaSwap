package com.uninaswap.common.message;

import java.util.List;

import com.uninaswap.common.dto.ListingDTO;

/**
 * 
 */
public class ListingMessage extends Message {
    /**
     * 
     */
    public enum Type {
        GET_LISTINGS_REQUEST,
        GET_LISTINGS_RESPONSE,
        GET_MY_LISTINGS_REQUEST,
        GET_MY_LISTINGS_RESPONSE,
        CREATE_LISTING_REQUEST,
        CREATE_LISTING_RESPONSE,
        UPDATE_LISTING_REQUEST,
        UPDATE_LISTING_RESPONSE,
        DELETE_LISTING_REQUEST,
        DELETE_LISTING_RESPONSE,
        GET_LISTING_DETAIL_REQUEST,
        GET_LISTING_DETAIL_RESPONSE,
        GET_USER_LISTINGS_REQUEST,
        GET_USER_LISTINGS_RESPONSE
    }

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private String listingTypeValue; 
    /**
     * 
     */
    private ListingDTO listing;
    /**
     * 
     */
    private List<ListingDTO> listings;
    /**
     * 
     */
    private String errorMessage;
    /**
     * 
     */
    private String listingId;
    /**
     * 
     */
    private Long userId;

    
    /**
     * 
     */
    private int page;
    /**
     * 
     */
    private int size;
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
    public ListingMessage() {
        setMessageType("listing");
    }

    
    /**
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return
     */
    public String getListingTypeValue() {
        return listingTypeValue;
    }

    /**
     * @param listingTypeValue
     */
    public void setListingTypeValue(String listingTypeValue) {
        this.listingTypeValue = listingTypeValue;
    }

    /**
     * @return
     */
    public ListingDTO getListing() {
        return listing;
    }

    /**
     * @param listing
     */
    public void setListing(ListingDTO listing) {
        this.listing = listing;
    }

    /**
     * @return
     */
    public List<ListingDTO> getListings() {
        return listings;
    }

    /**
     * @param listings
     */
    public void setListings(List<ListingDTO> listings) {
        this.listings = listings;
    }

    /**
     *
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     *
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return
     */
    public String getListingId() {
        return listingId;
    }

    /**
     * @param listingId
     */
    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    /**
     * @return
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @param userId
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * @return
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * @param totalElements
     */
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * @return
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * @param totalPages
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}