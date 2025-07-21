package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;

import java.net.URL;
import java.util.ResourceBundle;

public class SearchBarController implements Initializable, Refreshable {
    
    @FXML
    private HBox searchBar;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button searchButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("searchbar.debug.initialized", "SearchBar controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update search field prompt text
        if (searchField != null) {
            searchField.setPromptText(localeService.getMessage("searchbar.prompt", "Search items, users, or auctions..."));
        }

        // Update search button text
        if (searchButton != null) {
            searchButton.setText(localeService.getMessage("searchbar.button.search", "Search"));
        }
    }
    
    @FXML
    public void handleSearch(ActionEvent event) {
        String searchQuery = searchField.getText();
        
        // Execute search when query is not empty
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String trimmedQuery = searchQuery.trim();
            System.out.println(localeService.getMessage("searchbar.debug.searching", "Searching for: {0}")
                .replace("{0}", trimmedQuery));
            
            // Publish search event through EventBus
            try {
                EventBusService.getInstance().publishEvent(EventTypes.SEARCH_REQUESTED, trimmedQuery);
                System.out.println(localeService.getMessage("searchbar.debug.event.published", 
                    "Search event published for query: {0}").replace("{0}", trimmedQuery));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("searchbar.debug.event.error", 
                    "Error publishing search event: {0}").replace("{0}", e.getMessage()));
            }
        } else {
            System.out.println(localeService.getMessage("searchbar.debug.empty.query", 
                "Search attempted with empty query"));
        }
    }

    /**
     * Clear the search field
     */
    public void clearSearch() {
        if (searchField != null) {
            searchField.clear();
            System.out.println(localeService.getMessage("searchbar.debug.cleared", "Search field cleared"));
        }
    }

    /**
     * Set search query programmatically
     */
    public void setSearchQuery(String query) {
        if (searchField != null) {
            searchField.setText(query != null ? query : "");
            System.out.println(localeService.getMessage("searchbar.debug.query.set", 
                "Search query set to: {0}").replace("{0}", query != null ? query : "empty"));
        }
    }

    /**
     * Get current search query
     */
    public String getSearchQuery() {
        return searchField != null ? searchField.getText().trim() : "";
    }

    /**
     * Focus on search field
     */
    public void focusSearchField() {
        if (searchField != null) {
            Platform.runLater(() -> {
                searchField.requestFocus();
                System.out.println(localeService.getMessage("searchbar.debug.focused", "Search field focused"));
            });
        }
    }

    /**
     * Set search field enabled/disabled state
     */
    public void setSearchEnabled(boolean enabled) {
        if (searchField != null) {
            searchField.setDisable(!enabled);
        }
        if (searchButton != null) {
            searchButton.setDisable(!enabled);
        }
        System.out.println(localeService.getMessage("searchbar.debug.enabled.changed", 
            "Search enabled state changed to: {0}").replace("{0}", String.valueOf(enabled)));
    }

    /**
     * Check if search field is empty
     */
    public boolean isSearchEmpty() {
        return getSearchQuery().isEmpty();
    }
}