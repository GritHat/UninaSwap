package com.uninaswap.common.message;

import java.util.List;

import com.uninaswap.common.dto.ListingDTO;

public class ListingMessage extends Message {
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
        GET_LISTING_DETAIL_RESPONSE
    }

    private Type type;
    private String listingTypeValue; // "SELL", "TRADE", "GIFT", "AUCTION"
    private ListingDTO listing;
    private List<ListingDTO> listings;
    private String errorMessage;
    private String listingId;

    // For pagination
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    // Default constructor
    public ListingMessage() {
        setMessageType("listing");
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getListingTypeValue() {
        return listingTypeValue;
    }

    public void setListingTypeValue(String listingTypeValue) {
        this.listingTypeValue = listingTypeValue;
    }

    public ListingDTO getListing() {
        return listing;
    }

    public void setListing(ListingDTO listing) {
        this.listing = listing;
    }

    public List<ListingDTO> getListings() {
        return listings;
    }

    public void setListings(List<ListingDTO> listings) {
        this.listings = listings;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}