package com.uninaswap.common.message;

import com.uninaswap.common.dto.OfferDTO;
import java.util.List;

/**
 * 
 */
public class OfferMessage extends Message {

    /**
     * 
     */
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

    /**
     * 
     */
    private Type type;
    /**
     * 
     */
    private OfferDTO offer;
    /**
     * 
     */
    private List<OfferDTO> offers;
    /**
     * 
     */
    private String listingId;
    /**
     * 
     */
    private String offerId;
    /**
     * 
     */
    private String errorMessage;

    
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
    public OfferMessage() {
        setMessageType("offer");
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
    public OfferDTO getOffer() {
        return offer;
    }

    /**
     * @param offer
     */
    public void setOffer(OfferDTO offer) {
        this.offer = offer;
    }

    /**
     * @return
     */
    public List<OfferDTO> getOffers() {
        return offers;
    }

    /**
     * @param offers
     */
    public void setOffers(List<OfferDTO> offers) {
        this.offers = offers;
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
    public String getOfferId() {
        return offerId;
    }

    /**
     * @param offerId
     */
    public void setOfferId(String offerId) {
        this.offerId = offerId;
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