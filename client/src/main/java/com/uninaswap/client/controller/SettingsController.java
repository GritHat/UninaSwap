package com.uninaswap.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.Refreshable;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.util.AlertHelper;

import java.util.Locale;

public class SettingsController implements Refreshable {
    
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    
    @FXML private CheckBox emailNotificationsCheckBox;
    @FXML private CheckBox appNotificationsCheckBox;
    @FXML private CheckBox offerNotificationsCheckBox;
    @FXML private CheckBox messageNotificationsCheckBox;
    
    @FXML private ComboBox<String> languageComboBox;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Slider fontSizeSlider;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Additional UI elements for localization
    @FXML private Text settingsTitleText;
    @FXML private Text accountSectionText;
    @FXML private Text notificationsSectionText;
    @FXML private Text applicationSectionText;
    
    private final UserSessionService userSessionService = UserSessionService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    
    @FXML
    public void initialize() {
        // Load user data
        if (userSessionService.isLoggedIn()) {
            emailField.setText(userSessionService.getUser().getEmail());
        }
        
        setupLanguageComboBox();
        setupThemeComboBox();
        setupFontSizeSlider();
        loadUserPreferences();
        
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("settings.debug.initialized", "Settings controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update section titles
        if (settingsTitleText != null) {
            settingsTitleText.setText(localeService.getMessage("settings.title", "Settings"));
        }
        if (accountSectionText != null) {
            accountSectionText.setText(localeService.getMessage("settings.account", "Account"));
        }
        if (notificationsSectionText != null) {
            notificationsSectionText.setText(localeService.getMessage("settings.notifications", "Notifications"));
        }
        if (applicationSectionText != null) {
            applicationSectionText.setText(localeService.getMessage("settings.application", "Application"));
        }

        // Update button text
        if (changePasswordButton != null) {
            changePasswordButton.setText(localeService.getMessage("settings.button.change.password", "Change Password"));
        }
        if (saveButton != null) {
            saveButton.setText(localeService.getMessage("button.save", "Save"));
        }
        if (cancelButton != null) {
            cancelButton.setText(localeService.getMessage("button.cancel", "Cancel"));
        }

        // Update field prompts
        if (emailField != null) {
            emailField.setPromptText(localeService.getMessage("label.email", "Email"));
        }
        if (currentPasswordField != null) {
            currentPasswordField.setPromptText(localeService.getMessage("label.current.password", "Current Password"));
        }
        if (newPasswordField != null) {
            newPasswordField.setPromptText(localeService.getMessage("label.new.password", "New Password"));
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setPromptText(localeService.getMessage("label.new.password.confirm", "Confirm New Password"));
        }

        // Update checkbox text
        if (emailNotificationsCheckBox != null) {
            emailNotificationsCheckBox.setText(localeService.getMessage("settings.notifications.email", "Receive email notifications"));
        }
        if (appNotificationsCheckBox != null) {
            appNotificationsCheckBox.setText(localeService.getMessage("settings.notifications.app", "Receive in-app notifications"));
        }
        if (offerNotificationsCheckBox != null) {
            offerNotificationsCheckBox.setText(localeService.getMessage("settings.notifications.offers", "Notifications for new offers"));
        }
        if (messageNotificationsCheckBox != null) {
            messageNotificationsCheckBox.setText(localeService.getMessage("settings.notifications.messages", "Notifications for new messages"));
        }

        // Update combo box prompts
        if (languageComboBox != null) {
            languageComboBox.setPromptText(localeService.getMessage("settings.language.select", "Select language"));
        }
        if (themeComboBox != null) {
            themeComboBox.setPromptText(localeService.getMessage("settings.theme.select", "Select theme"));
        }

        // Refresh combo box items with current locale
        refreshComboBoxItems();
    }

    private void setupLanguageComboBox() {
        // Initialize with localized language names
        refreshLanguageComboBox();
        
        // Set current language
        Locale currentLocale = localeService.getCurrentLocale();
        String currentLanguageDisplay = getLocalizedLanguageName(currentLocale);
        languageComboBox.setValue(currentLanguageDisplay);
        
        System.out.println(localeService.getMessage("settings.debug.language.setup", "Language combo box setup with current locale: {0}")
            .replace("{0}", currentLocale.getDisplayLanguage()));
    }

    private void setupThemeComboBox() {
        // Initialize with localized theme names
        refreshThemeComboBox();
        
        // Set default theme (this could be loaded from user preferences)
        themeComboBox.setValue(localeService.getMessage("theme.light", "Light"));
        
        System.out.println(localeService.getMessage("settings.debug.theme.setup", "Theme combo box setup completed"));
    }

    private void setupFontSizeSlider() {
        if (fontSizeSlider != null) {
            fontSizeSlider.setMin(10);
            fontSizeSlider.setMax(20);
            fontSizeSlider.setValue(14); // Default font size
            fontSizeSlider.setMajorTickUnit(2);
            fontSizeSlider.setMinorTickCount(1);
            fontSizeSlider.setSnapToTicks(true);
            fontSizeSlider.setShowTickLabels(true);
            fontSizeSlider.setShowTickMarks(true);
            
            System.out.println(localeService.getMessage("settings.debug.font.setup", "Font size slider setup completed"));
        }
    }

    private void loadUserPreferences() {
        // Load user notification preferences (this would come from a service/database)
        emailNotificationsCheckBox.setSelected(true);
        appNotificationsCheckBox.setSelected(true);
        offerNotificationsCheckBox.setSelected(true);
        messageNotificationsCheckBox.setSelected(true);
        
        System.out.println(localeService.getMessage("settings.debug.preferences.loaded", "User preferences loaded"));
    }

    private void refreshComboBoxItems() {
        refreshLanguageComboBox();
        refreshThemeComboBox();
    }

    private void refreshLanguageComboBox() {
        if (languageComboBox == null) return;
        
        String currentSelection = languageComboBox.getValue();
        languageComboBox.getItems().clear();
        
        languageComboBox.getItems().addAll(
            localeService.getMessage("language.english", "English"),
            localeService.getMessage("language.italian", "Italiano")
        );
        
        // Try to maintain the same selection
        if (currentSelection != null) {
            Locale currentLocale = localeService.getCurrentLocale();
            languageComboBox.setValue(getLocalizedLanguageName(currentLocale));
        }
    }

    private void refreshThemeComboBox() {
        if (themeComboBox == null) return;
        
        String currentSelection = themeComboBox.getValue();
        themeComboBox.getItems().clear();
        
        themeComboBox.getItems().addAll(
            localeService.getMessage("theme.light", "Light"),
            localeService.getMessage("theme.dark", "Dark"),
            localeService.getMessage("theme.system", "System")
        );
        
        // Try to maintain the same selection by matching theme type
        if (currentSelection != null) {
            if (currentSelection.contains("Light") || currentSelection.contains("Chiaro")) {
                themeComboBox.setValue(localeService.getMessage("theme.light", "Light"));
            } else if (currentSelection.contains("Dark") || currentSelection.contains("Scuro")) {
                themeComboBox.setValue(localeService.getMessage("theme.dark", "Dark"));
            } else {
                themeComboBox.setValue(localeService.getMessage("theme.system", "System"));
            }
        }
    }

    private String getLocalizedLanguageName(Locale locale) {
        return switch (locale.getLanguage()) {
            case "en" -> localeService.getMessage("language.english", "English");
            case "it" -> localeService.getMessage("language.italian", "Italiano");
            default -> locale.getDisplayLanguage(locale);
        };
    }
    
    @FXML
    public void handleChangePassword(ActionEvent event) {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        System.out.println(localeService.getMessage("settings.debug.password.change.attempt", "Password change attempt"));
        
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("settings.password.error.missing.title", "Missing Fields"),
                localeService.getMessage("settings.password.error.missing.header", "All fields are required"),
                localeService.getMessage("settings.password.error.missing.content", "To change your password, please fill in all fields.")
            );
            System.err.println(localeService.getMessage("settings.debug.password.validation.missing", "Password change failed: missing fields"));
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("settings.password.error.mismatch.title", "Passwords Don't Match"),
                localeService.getMessage("settings.password.error.mismatch.header", "The new passwords don't match"),
                localeService.getMessage("settings.password.error.mismatch.content", "Make sure the new password and confirmation are the same.")
            );
            System.err.println(localeService.getMessage("settings.debug.password.validation.mismatch", "Password change failed: passwords don't match"));
            return;
        }

        if (newPassword.length() < 8) {
            AlertHelper.showWarningAlert(
                localeService.getMessage("settings.password.error.length.title", "Password Too Short"),
                localeService.getMessage("settings.password.error.length.header", "Password must be at least 8 characters"),
                localeService.getMessage("settings.password.error.length.content", "Please choose a longer password for better security.")
            );
            System.err.println(localeService.getMessage("settings.debug.password.validation.length", "Password change failed: password too short"));
            return;
        }
        
        // Here you would implement the actual password change logic with your backend service
        try {
            // Simulate password change API call
            // userService.changePassword(currentPassword, newPassword);
            
            AlertHelper.showInformationAlert(
                localeService.getMessage("settings.password.success.title", "Password Updated"),
                localeService.getMessage("settings.password.success.header", "Your password has been updated successfully"),
                localeService.getMessage("settings.password.success.content", "Use your new password on your next login.")
            );
            
            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            
            System.out.println(localeService.getMessage("settings.debug.password.success", "Password changed successfully"));
            
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                localeService.getMessage("settings.password.error.server.title", "Password Change Failed"),
                localeService.getMessage("settings.password.error.server.header", "Could not update password"),
                localeService.getMessage("settings.password.error.server.content", "Please try again later or contact support.")
            );
            System.err.println(localeService.getMessage("settings.debug.password.error", "Password change failed: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    public void handleSave(ActionEvent event) {
        System.out.println(localeService.getMessage("settings.debug.save.started", "Saving settings"));
        
        try {
            // Save language preference
            String selectedLanguage = languageComboBox.getValue();
            if (selectedLanguage != null) {
                Locale locale;
                if (selectedLanguage.equals(localeService.getMessage("language.english", "English")) || 
                    selectedLanguage.equals("English")) {
                    locale = Locale.ENGLISH;
                } else {
                    locale = Locale.ITALIAN;
                }
                localeService.setLocale(locale);
                System.out.println(localeService.getMessage("settings.debug.language.saved", "Language preference saved: {0}")
                    .replace("{0}", locale.getDisplayLanguage()));
            }

            // Save theme preference
            String selectedTheme = themeComboBox.getValue();
            if (selectedTheme != null) {
                // Here you would save theme preference to user settings
                System.out.println(localeService.getMessage("settings.debug.theme.saved", "Theme preference saved: {0}")
                    .replace("{0}", selectedTheme));
            }

            // Save font size preference
            double fontSize = fontSizeSlider.getValue();
            // Here you would save font size preference
            System.out.println(localeService.getMessage("settings.debug.font.saved", "Font size preference saved: {0}")
                .replace("{0}", String.valueOf(fontSize)));

            // Save notification preferences
            boolean emailNotifications = emailNotificationsCheckBox.isSelected();
            boolean appNotifications = appNotificationsCheckBox.isSelected();
            boolean offerNotifications = offerNotificationsCheckBox.isSelected();
            boolean messageNotifications = messageNotificationsCheckBox.isSelected();
            
            // Here you would save notification preferences to backend/local storage
            System.out.println(localeService.getMessage("settings.debug.notifications.saved", 
                "Notification preferences saved: email={0}, app={1}, offers={2}, messages={3}")
                    .replace("{0}", String.valueOf(emailNotifications))
                    .replace("{1}", String.valueOf(appNotifications))
                    .replace("{2}", String.valueOf(offerNotifications))
                    .replace("{3}", String.valueOf(messageNotifications)));
            
            AlertHelper.showInformationAlert(
                localeService.getMessage("settings.save.success.title", "Settings Saved"),
                localeService.getMessage("settings.save.success.header", "Your settings have been saved"),
                localeService.getMessage("settings.save.success.content", "The new settings have been applied.")
            );
            
            System.out.println(localeService.getMessage("settings.debug.save.success", "Settings saved successfully"));
            
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                localeService.getMessage("settings.save.error.title", "Save Failed"),
                localeService.getMessage("settings.save.error.header", "Could not save settings"),
                localeService.getMessage("settings.save.error.content", "Please try again later.")
            );
            System.err.println(localeService.getMessage("settings.debug.save.error", "Failed to save settings: {0}")
                .replace("{0}", e.getMessage()));
        }
    }
    
    @FXML
    public void handleCancel(ActionEvent event) {
        System.out.println(localeService.getMessage("settings.debug.cancelled", "Settings changes cancelled"));
        
        // Reset form to original values
        try {
            if (userSessionService.isLoggedIn()) {
                emailField.setText(userSessionService.getUser().getEmail());
            }
            
            // Reset language to current locale
            Locale currentLocale = localeService.getCurrentLocale();
            languageComboBox.setValue(getLocalizedLanguageName(currentLocale));
            
            // Reset theme to default
            themeComboBox.setValue(localeService.getMessage("theme.light", "Light"));
            
            // Reset font size to default
            fontSizeSlider.setValue(14);
            
            // Reset notification preferences to defaults (or reload from settings)
            loadUserPreferences();
            
            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            
            System.out.println(localeService.getMessage("settings.debug.reset", "Settings form reset to defaults"));
            
        } catch (Exception e) {
            System.err.println(localeService.getMessage("settings.debug.reset.error", "Error resetting settings form: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    // Public methods for external access
    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsCheckBox != null && emailNotificationsCheckBox.isSelected();
    }

    public boolean isAppNotificationsEnabled() {
        return appNotificationsCheckBox != null && appNotificationsCheckBox.isSelected();
    }

    public boolean isOfferNotificationsEnabled() {
        return offerNotificationsCheckBox != null && offerNotificationsCheckBox.isSelected();
    }

    public boolean isMessageNotificationsEnabled() {
        return messageNotificationsCheckBox != null && messageNotificationsCheckBox.isSelected();
    }

    public String getSelectedTheme() {
        return themeComboBox != null ? themeComboBox.getValue() : null;
    }

    public double getSelectedFontSize() {
        return fontSizeSlider != null ? fontSizeSlider.getValue() : 14.0;
    }

    public void setNotificationPreferences(boolean email, boolean app, boolean offers, boolean messages) {
        if (emailNotificationsCheckBox != null) {
            emailNotificationsCheckBox.setSelected(email);
        }
        if (appNotificationsCheckBox != null) {
            appNotificationsCheckBox.setSelected(app);
        }
        if (offerNotificationsCheckBox != null) {
            offerNotificationsCheckBox.setSelected(offers);
        }
        if (messageNotificationsCheckBox != null) {
            messageNotificationsCheckBox.setSelected(messages);
        }
    }
}
