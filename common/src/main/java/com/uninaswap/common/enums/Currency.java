package com.uninaswap.common.enums;

/**
 * Supported currencies for listings
 */
/**
 * 
 */
public enum Currency {
    /**
     * 
     */
    EUR("€"),
    /**
     * 
     */
    USD("$"),
    /**
     * 
     */
    GBP("£");

    /**
     * 
     */
    private final String symbol;

    /**
     * @param symbol
     */
    Currency(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return
     */
    public String getSymbol() {
        return symbol;
    }
}