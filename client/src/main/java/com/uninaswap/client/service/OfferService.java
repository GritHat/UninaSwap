package com.uninaswap.client.service;

import com.uninaswap.common.dto.OfferDTO;
import java.util.concurrent.CompletableFuture;

public class OfferService {
    private static OfferService instance;

    private OfferService() {
    }

    public static synchronized OfferService getInstance() {
        if (instance == null) {
            instance = new OfferService();
        }
        return instance;
    }

    public CompletableFuture<Boolean> createOffer(OfferDTO offer) {
        // TODO: Implement offer creation via WebSocket
        return CompletableFuture.completedFuture(true);
    }
}
