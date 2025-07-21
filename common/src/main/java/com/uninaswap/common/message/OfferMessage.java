package com.uninaswap.common.message;

import com.uninaswap.common.dto.OfferDTO;
import java.util.List;

public class OfferMessage extends Message {

    public enum Type {
        CREATE_OFFER_REQUEST,
        CREATE_OFFER_RESPONSE,
        GET_RECEIVED_OFFERS_REQUEST,
        GET_RECEIVED_OFFERS_RESPONSE,
        GET_SENT_OFFERS_REQUEST,
        GET_SENT_OFFERS_RESPONSE,
        UPDATE_OFFER_STATUS_REQUEST,
        UPDATE_OFFER_STATUS_RESPONSE,
        GET_LISTING_OFFERS_REQUEST,
        GET_LISTING_OFFERS_RESPONSE,
        GET_OFFER_HISTORY_REQUEST,
        GET_OFFER_HISTORY_RESPONSE,
        ACCEPT_OFFER_REQUEST,
        ACCEPT_OFFER_RESPONSE,
        REJECT_OFFER_REQUEST,
        REJECT_OFFER_RESPONSE,
        WITHDRAW_OFFER_REQUEST,
        WITHDRAW_OFFER_RESPONSE,
        CONFIRM_TRANSACTION_REQUEST,
        CONFIRM_TRANSACTION_RESPONSE,
        CANCEL_TRANSACTION_REQUEST,
        CANCEL_TRANSACTION_RESPONSE
    }

    private Type type;
    private OfferDTO offer;
    private List<OfferDTO> offers;
    private String listingId;
    private String offerId;
    private String errorMessage;

    // For pagination
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    // Default constructor
    public OfferMessage() {
        setMessageType("offer");
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public OfferDTO getOffer() {
        return offer;
    }

    public void setOffer(OfferDTO offer) {
        this.offer = offer;
    }

    public List<OfferDTO> getOffers() {
        return offers;
    }

    public void setOffers(List<OfferDTO> offers) {
        this.offers = offers;
    }

    public String getListingId() {
        return listingId;
    }

    public void setListingId(String listingId) {
        this.listingId = listingId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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