package com.uninaswap.client.controller;

/**
 * Interface for controllers that can refresh their UI
 */
public interface Refreshable {
    /**
     * Refreshes the UI elements with current data and localization
     */
    void refreshUI();
}