package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import com.uninaswap.client.service.LocaleService;
import javafx.util.StringConverter;
import java.util.Locale;
import java.util.Map;

public class FooterController implements Refreshable {
    @FXML
    private HBox footer;
    @FXML
    private Text connectionStatusLabel;
    @FXML
    private ImageView statusIcon;
    @FXML
    private ComboBox<String> languageSelector;
    @FXML
    private ComboBox<Locale> languageComboBox;
    @FXML
    private Label languageLabel;

    private final LocaleService localeService = LocaleService.getInstance();

    private static final Map<String, Locale> SUPPORTED_LANGUAGES = Map.of(
        "English", Locale.ENGLISH,
        "Italiano", Locale.ITALIAN
    );

    // Track current connection status for refreshUI
    private boolean currentConnectionStatus = true;

    @FXML
    public void initialize() {    
        setupLanguageComboBox();
        refreshUI();
        updateConnectionStatus(true);
    }

    private void setupLanguageComboBox() {
        // Populate ComboBox with language options
        languageComboBox.setItems(FXCollections.observableArrayList(SUPPORTED_LANGUAGES.values()));
        
        // Set a custom string converter to display language names
        languageComboBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale locale) {
                if (locale == null) return null;
                return getLocalizedLanguageName(locale);
            }
            
            @Override
            public Locale fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        
        // Set current locale
        languageComboBox.setValue(localeService.getCurrentLocale());
        
        // Add listener to change language when selection changes
        languageComboBox.valueProperty().addListener((_, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                localeService.setLocale(newValue);
            }
        });
    }

    /**
     * Get localized language name for display
     */
    private String getLocalizedLanguageName(Locale locale) {
        return switch (locale.getLanguage()) {
            case "en" -> localeService.getMessage("language.english", "English");
            case "it" -> localeService.getMessage("language.italian", "Italiano");
            default -> locale.getDisplayLanguage(locale);
        };
    }

    public void updateConnectionStatus(boolean isConnected) {
        this.currentConnectionStatus = isConnected;
        
        if (isConnected) {
            connectionStatusLabel.setText(localeService.getMessage("footer.status.online", "Online"));
            connectionStatusLabel.getStyleClass().setAll("status-text-success");
            setStatusIcon("/images/icons/online.png");
        } else {
            connectionStatusLabel.setText(localeService.getMessage("footer.status.offline", "Offline"));
            connectionStatusLabel.getStyleClass().setAll("status-text-warning");
            setStatusIcon("/images/icons/offline.png");
        }
    }

    /**
     * Set status icon with error handling
     */
    private void setStatusIcon(String iconPath) {
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            if (!icon.isError()) {
                statusIcon.setImage(icon);
            } else {
                System.err.println(localeService.getMessage("footer.error.icon.load", "Failed to load status icon: {0}").replace("{0}", iconPath));
                setDefaultStatusIcon();
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("footer.error.icon.exception", "Exception loading status icon: {0}").replace("{0}", e.getMessage()));
            setDefaultStatusIcon();
        }
    }

    /**
     * Set a default status icon when the main icon fails to load
     */
    private void setDefaultStatusIcon() {
        try {
            // Try to load a default/fallback icon
            Image defaultIcon = new Image(getClass().getResourceAsStream("/images/icons/default_status.png"));
            if (!defaultIcon.isError()) {
                statusIcon.setImage(defaultIcon);
            } else {
                // If even the default fails, clear the icon
                statusIcon.setImage(null);
            }
        } catch (Exception e) {
            // Clear icon if all attempts fail
            statusIcon.setImage(null);
        }
    }

    /**
     * Update the footer status message
     */
    public void updateFooterStatus(String messageKey, String defaultMessage) {
        if (footer != null && footer.getChildren().size() > 0) {
            // The first child should be the status text
            if (footer.getChildren().get(0) instanceof Text statusText) {
                statusText.setText(localeService.getMessage(messageKey, defaultMessage));
            }
        }
    }

    /**
     * Update the footer status with a custom message
     */
    public void updateFooterStatusWithMessage(String message) {
        if (footer != null && footer.getChildren().size() > 0) {
            if (footer.getChildren().get(0) instanceof Text statusText) {
                statusText.setText(message);
            }
        }
    }

    /**
     * Set footer to ready state
     */
    public void setReady() {
        updateFooterStatus("footer.status.ready", "Ready");
    }

    /**
     * Set footer to loading state
     */
    public void setLoading(String operation) {
        String message = localeService.getMessage("footer.status.loading", "Loading {0}...").replace("{0}", operation);
        updateFooterStatusWithMessage(message);
    }

    /**
     * Set footer to error state
     */
    public void setError(String errorMessage) {
        String message = localeService.getMessage("footer.status.error", "Error: {0}").replace("{0}", errorMessage);
        updateFooterStatusWithMessage(message);
    }

    /**
     * Set footer to connected state
     */
    public void setConnected() {
        updateFooterStatus("footer.status.connected", "Connected");
    }

    @Override
    public void refreshUI() {
        // Update language label
        if (languageLabel != null) {
            languageLabel.setText(localeService.getMessage("footer.language.label", "Language:"));
        }

        // Refresh language combo box display
        if (languageComboBox != null) {
            Locale currentLocale = languageComboBox.getValue();
            // Force refresh of the combo box converter to update display text
            languageComboBox.setConverter(languageComboBox.getConverter());
            // Refresh the selected value to show updated text
            if (currentLocale != null) {
                languageComboBox.setValue(null);
                languageComboBox.setValue(currentLocale);
            }
        }

        // Update connection status with current language
        updateConnectionStatus(currentConnectionStatus);

        // Update footer status text
        setReady();

        // Update tooltips if they exist
        updateTooltips();
    }

    /**
     * Update tooltips with localized text
     */
    private void updateTooltips() {
        if (languageComboBox != null) {
            // Create localized tooltip for language selector
            String tooltipText = localeService.getMessage("footer.language.tooltip", "Select application language");
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(tooltipText);
            languageComboBox.setTooltip(tooltip);
        }

        if (statusIcon != null) {
            // Create localized tooltip for connection status
            String tooltipText = currentConnectionStatus ? 
                localeService.getMessage("footer.status.tooltip.online", "Connection is active") :
                localeService.getMessage("footer.status.tooltip.offline", "Connection is lost");
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(tooltipText);
            javafx.scene.control.Tooltip.install(statusIcon, tooltip);
        }
    }

    /**
     * Get the current connection status
     */
    public boolean isConnected() {
        return currentConnectionStatus;
    }

    /**
     * Get supported languages map
     */
    public Map<String, Locale> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }
}
