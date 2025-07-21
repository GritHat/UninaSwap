package com.uninaswap.common.enums;

/**
 * Supported currencies for listings
 */
public enum Currency {
    EUR("€"),
    USD("$"),
    GBP("£");

    private final String symbol;

    Currency(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}