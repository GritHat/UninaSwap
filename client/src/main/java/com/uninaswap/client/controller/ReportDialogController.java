package com.uninaswap.client.controller;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ReportService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.ListingReportViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.UserReportViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.common.enums.ListingReportReason;
import com.uninaswap.common.enums.UserReportReason;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ReportDialogController implements Refreshable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label instructionsLabel;

    @FXML
    private Label targetLabel;

    @FXML
    private Label reasonLabel;

    @FXML
    private Label detailsLabel;

    @FXML
    private Label helpLabel;

    @FXML
    private ComboBox<UserReportReason> userReasonComboBox;

    @FXML
    private ComboBox<ListingReportReason> listingReasonComboBox;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private Label characterCountLabel;

    @FXML
    private Button submitButton;

    @FXML
    private Button cancelButton;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final ReportService reportService = ReportService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();

    // Data
    private UserViewModel reportedUser;
    private ListingViewModel reportedListing;
    private boolean isUserReport;

    @FXML
    public void initialize() {
        setupReasonComboBoxes();
        setupDescriptionArea();
        updateSubmitButton();
        
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("report.debug.initialized", "ReportDialog controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update static labels
        if (titleLabel != null) {
            titleLabel.setText(localeService.getMessage("report.dialog.title", "Report"));
        }
        if (instructionsLabel != null) {
            instructionsLabel.setText(localeService.getMessage("report.dialog.instructions",
                    "Please select a reason for reporting and provide additional details"));
        }
        if (reasonLabel != null) {
            reasonLabel.setText(localeService.getMessage("report.dialog.reason.label", "Reason for Report"));
        }
        if (detailsLabel != null) {
            detailsLabel.setText(localeService.getMessage("report.dialog.details.label", "Additional Details (Optional)"));
        }
        if (helpLabel != null) {
            helpLabel.setText(localeService.getMessage("report.dialog.help.text", 
                    "Please provide any additional context that might help us review this report"));
        }

        // Update button labels
        if (submitButton != null) {
            submitButton.setText(localeService.getMessage("report.dialog.submit", "Submit Report"));
        }
        if (cancelButton != null) {
            cancelButton.setText(localeService.getMessage("report.dialog.cancel", "Cancel"));
        }

        // Update combo box prompt texts
        if (userReasonComboBox != null) {
            userReasonComboBox.setPromptText(localeService.getMessage("report.dialog.reason.prompt", "Select a reason..."));
        }
        if (listingReasonComboBox != null) {
            listingReasonComboBox.setPromptText(localeService.getMessage("report.dialog.reason.prompt", "Select a reason..."));
        }

        // Update description area prompt
        if (descriptionTextArea != null) {
            descriptionTextArea.setPromptText(localeService.getMessage("report.dialog.description.prompt",
                    "Provide additional details about this report (optional)"));
        }

        // Refresh combo box converters to update display names
        refreshComboBoxConverters();

        // Update target label if data is set
        if (reportedUser != null && isUserReport) {
            targetLabel.setText(localeService.getMessage("report.dialog.target.user", "Reporting user: {0}")
                .replace("{0}", reportedUser.getDisplayName()));
        } else if (reportedListing != null && !isUserReport) {
            targetLabel.setText(localeService.getMessage("report.dialog.target.listing", "Reporting listing: \"{0}\"")
                .replace("{0}", reportedListing.getTitle()));
        }

        // Update character count display
        updateCharacterCount();
    }

    private void refreshComboBoxConverters() {
        // Refresh user reason combo box converter
        if (userReasonComboBox != null) {
            UserReportReason currentSelection = userReasonComboBox.getValue();
            userReasonComboBox.setConverter(new javafx.util.StringConverter<UserReportReason>() {
                @Override
                public String toString(UserReportReason reason) {
                    return reason != null ? localeService.getMessage(reason.getMessageKey(), reason.name()) : "";
                }

                @Override
                public UserReportReason fromString(String string) {
                    return null;
                }
            });
            // Maintain selection
            if (currentSelection != null) {
                userReasonComboBox.setValue(null);
                userReasonComboBox.setValue(currentSelection);
            }
        }

        // Refresh listing reason combo box converter
        if (listingReasonComboBox != null) {
            ListingReportReason currentSelection = listingReasonComboBox.getValue();
            listingReasonComboBox.setConverter(new javafx.util.StringConverter<ListingReportReason>() {
                @Override
                public String toString(ListingReportReason reason) {
                    return reason != null ? localeService.getMessage(reason.getMessageKey(), reason.name()) : "";
                }

                @Override
                public ListingReportReason fromString(String string) {
                    return null;
                }
            });
            // Maintain selection
            if (currentSelection != null) {
                listingReasonComboBox.setValue(null);
                listingReasonComboBox.setValue(currentSelection);
            }
        }
    }

    private void setupReasonComboBoxes() {
        // Setup user report reasons
        userReasonComboBox.getItems().addAll(UserReportReason.values());
        userReasonComboBox.setConverter(new javafx.util.StringConverter<UserReportReason>() {
            @Override
            public String toString(UserReportReason reason) {
                return reason != null ? localeService.getMessage(reason.getMessageKey(), reason.name()) : "";
            }

            @Override
            public UserReportReason fromString(String string) {
                return null;
            }
        });

        // Setup listing report reasons
        listingReasonComboBox.getItems().addAll(ListingReportReason.values());
        listingReasonComboBox.setConverter(new javafx.util.StringConverter<ListingReportReason>() {
            @Override
            public String toString(ListingReportReason reason) {
                return reason != null ? localeService.getMessage(reason.getMessageKey(), reason.name()) : "";
            }

            @Override
            public ListingReportReason fromString(String string) {
                return null;
            }
        });

        // Add listeners for validation
        userReasonComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSubmitButton();
            System.out.println(localeService.getMessage("report.debug.user.reason.changed", 
                "User report reason changed: {0}").replace("{0}", newVal != null ? newVal.name() : "null"));
        });
        
        listingReasonComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSubmitButton();
            System.out.println(localeService.getMessage("report.debug.listing.reason.changed", 
                "Listing report reason changed: {0}").replace("{0}", newVal != null ? newVal.name() : "null"));
        });
    }

    private void setupDescriptionArea() {
        descriptionTextArea.setPromptText(localeService.getMessage("report.dialog.description.prompt",
                "Provide additional details about this report (optional)"));
        descriptionTextArea.setWrapText(true);

        // Add character count listener
        descriptionTextArea.textProperty().addListener((obs, oldText, newText) -> {
            updateCharacterCount();
            updateSubmitButton();
        });

        updateCharacterCount();
    }

    public void setReportedUser(UserViewModel user) {
        this.reportedUser = user;
        this.isUserReport = true;

        if (user != null) {
            Platform.runLater(() -> {
                targetLabel.setText(localeService.getMessage("report.dialog.target.user", "Reporting user: {0}")
                    .replace("{0}", user.getDisplayName()));

                userReasonComboBox.setVisible(true);
                userReasonComboBox.setManaged(true);
                listingReasonComboBox.setVisible(false);
                listingReasonComboBox.setManaged(false);
                
                System.out.println(localeService.getMessage("report.debug.user.set", 
                    "Report target set to user: {0}").replace("{0}", user.getDisplayName()));
            });
        }
    }

    public void setReportedListing(ListingViewModel listing) {
        this.reportedListing = listing;
        this.isUserReport = false;

        if (listing != null) {
            Platform.runLater(() -> {
                targetLabel.setText(localeService.getMessage("report.dialog.target.listing", "Reporting listing: \"{0}\"")
                    .replace("{0}", listing.getTitle()));

                userReasonComboBox.setVisible(false);
                userReasonComboBox.setManaged(false);
                listingReasonComboBox.setVisible(true);
                listingReasonComboBox.setManaged(true);
                
                System.out.println(localeService.getMessage("report.debug.listing.set", 
                    "Report target set to listing: {0}").replace("{0}", listing.getTitle()));
            });
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateReport()) {
            return;
        }

        submitButton.setDisable(true);
        System.out.println(localeService.getMessage("report.debug.submit.started", "Report submission started"));

        if (isUserReport) {
            submitUserReport();
        } else {
            submitListingReport();
        }
    }

    private void submitUserReport() {
        UserViewModel currentUser = ViewModelMapper.getInstance().toViewModel(sessionService.getUser());
        UserReportViewModel report = new UserReportViewModel(
                currentUser,
                reportedUser,
                userReasonComboBox.getValue(),
                descriptionTextArea.getText().trim());

        System.out.println(localeService.getMessage("report.debug.user.report.creating", 
            "Creating user report for: {0}, reason: {1}")
                .replace("{0}", reportedUser.getDisplayName())
                .replace("{1}", userReasonComboBox.getValue().name()));

        reportService.createUserReport(report)
                .thenAccept(createdReport -> Platform.runLater(() -> {
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("report.submit.success.title", "Report Submitted"),
                            localeService.getMessage("report.submit.success.header", "Thank you!"),
                            localeService.getMessage("report.submit.success.message",
                                    "Your report has been submitted and will be reviewed by our team."));
                    System.out.println(localeService.getMessage("report.debug.user.report.success", 
                        "User report submitted successfully"));
                    closeWindow();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("report.submit.error.title", "Error"),
                                localeService.getMessage("report.submit.error.header", "Failed to submit report"),
                                ex.getMessage());
                        submitButton.setDisable(false);
                        System.err.println(localeService.getMessage("report.debug.user.report.error", 
                            "Failed to submit user report: {0}").replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    private void submitListingReport() {
        UserViewModel currentUser = ViewModelMapper.getInstance().toViewModel(sessionService.getUser());
        ListingReportViewModel report = new ListingReportViewModel(
                currentUser,
                reportedListing,
                listingReasonComboBox.getValue(),
                descriptionTextArea.getText().trim());

        System.out.println(localeService.getMessage("report.debug.listing.report.creating", 
            "Creating listing report for: {0}, reason: {1}")
                .replace("{0}", reportedListing.getTitle())
                .replace("{1}", listingReasonComboBox.getValue().name()));

        reportService.createListingReport(report)
                .thenAccept(createdReport -> Platform.runLater(() -> {
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("report.submit.success.title", "Report Submitted"),
                            localeService.getMessage("report.submit.success.header", "Thank you!"),
                            localeService.getMessage("report.submit.success.message",
                                    "Your report has been submitted and will be reviewed by our team."));
                    System.out.println(localeService.getMessage("report.debug.listing.report.success", 
                        "Listing report submitted successfully"));
                    closeWindow();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("report.submit.error.title", "Error"),
                                localeService.getMessage("report.submit.error.header", "Failed to submit report"),
                                ex.getMessage());
                        submitButton.setDisable(false);
                        System.err.println(localeService.getMessage("report.debug.listing.report.error", 
                            "Failed to submit listing report: {0}").replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        System.out.println(localeService.getMessage("report.debug.cancelled", "Report dialog cancelled by user"));
        closeWindow();
    }

    private boolean validateReport() {
        // Check if reason is selected
        if (isUserReport && userReasonComboBox.getValue() == null) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("report.validation.title", "Validation Error"),
                    localeService.getMessage("report.validation.header", "Invalid Input"),
                    localeService.getMessage("report.validation.reason.required",
                            "Please select a reason for the report"));
            System.err.println(localeService.getMessage("report.debug.validation.user.reason.missing", 
                "User report validation failed: no reason selected"));
            return false;
        }

        if (!isUserReport && listingReasonComboBox.getValue() == null) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("report.validation.title", "Validation Error"),
                    localeService.getMessage("report.validation.header", "Invalid Input"),
                    localeService.getMessage("report.validation.reason.required",
                            "Please select a reason for the report"));
            System.err.println(localeService.getMessage("report.debug.validation.listing.reason.missing", 
                "Listing report validation failed: no reason selected"));
            return false;
        }

        // Check description length
        if (descriptionTextArea.getText().length() > 1000) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("report.validation.title", "Validation Error"),
                    localeService.getMessage("report.validation.header", "Invalid Input"),
                    localeService.getMessage("report.validation.description.too.long",
                            "Description is too long (max 1000 characters)"));
            System.err.println(localeService.getMessage("report.debug.validation.description.too.long", 
                "Report validation failed: description too long ({0} characters)")
                    .replace("{0}", String.valueOf(descriptionTextArea.getText().length())));
            return false;
        }

        System.out.println(localeService.getMessage("report.debug.validation.passed", "Report validation passed"));
        return true;
    }

    private void updateCharacterCount() {
        int length = descriptionTextArea.getText().length();
        int maxLength = 1000;
        
        if (characterCountLabel != null) {
            characterCountLabel.setText(localeService.getMessage("report.dialog.character.count", "{0}/{1}")
                .replace("{0}", String.valueOf(length))
                .replace("{1}", String.valueOf(maxLength)));

            if (length > maxLength) {
                characterCountLabel.getStyleClass().add("character-count-exceeded");
            } else {
                characterCountLabel.getStyleClass().remove("character-count-exceeded");
            }
        }
    }

    private void updateSubmitButton() {
        boolean hasValidReason = (isUserReport && userReasonComboBox.getValue() != null) ||
                (!isUserReport && listingReasonComboBox.getValue() != null);
        boolean hasValidDescription = descriptionTextArea.getText().length() <= 1000;

        if (submitButton != null) {
            submitButton.setDisable(!hasValidReason || !hasValidDescription);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}