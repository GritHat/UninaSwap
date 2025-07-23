package com.uninaswap.client.controller;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.ReportService;
import com.uninaswap.client.service.UserSessionService;
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

/**
 * 
 */
public class ReportDialogController {

    /**
     * 
     */
    @FXML
    private Label titleLabel;

    /**
     * 
     */
    @FXML
    private Label instructionsLabel;

    /**
     * 
     */
    @FXML
    private Label targetLabel;

    /**
     * 
     */
    @FXML
    private ComboBox<UserReportReason> userReasonComboBox;

    /**
     * 
     */
    @FXML
    private ComboBox<ListingReportReason> listingReasonComboBox;

    /**
     * 
     */
    @FXML
    private TextArea descriptionTextArea;

    /**
     * 
     */
    @FXML
    private Label characterCountLabel;

    /**
     * 
     */
    @FXML
    private Button submitButton;

    /**
     * 
     */
    @FXML
    private Button cancelButton;

    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final ReportService reportService = ReportService.getInstance();
    /**
     * 
     */
    private final UserSessionService sessionService = UserSessionService.getInstance();

    /**
     * 
     */
    private UserViewModel reportedUser;
    /**
     * 
     */
    private ListingViewModel reportedListing;
    /**
     * 
     */
    private boolean isUserReport;

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupLabels();
        setupReasonComboBoxes();
        setupDescriptionArea();
        updateSubmitButton();
    }

    /**
     * 
     */
    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("report.dialog.title", "Report"));
        instructionsLabel.setText(localeService.getMessage("report.dialog.instructions",
                "Please select a reason for reporting and provide additional details"));

        submitButton.setText(localeService.getMessage("report.dialog.submit", "Submit Report"));
        cancelButton.setText(localeService.getMessage("report.dialog.cancel", "Cancel"));
    }

    /**
     * 
     */
    private void setupReasonComboBoxes() {
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

        userReasonComboBox.valueProperty().addListener((_, _, _) -> updateSubmitButton());
        listingReasonComboBox.valueProperty().addListener((_, _, _) -> updateSubmitButton());
    }

    /**
     * 
     */
    private void setupDescriptionArea() {
        descriptionTextArea.setPromptText(localeService.getMessage("report.dialog.description.prompt",
                "Provide additional details about this report (optional)"));
        descriptionTextArea.setWrapText(true);

        descriptionTextArea.textProperty().addListener((_, _, _) -> {
            updateCharacterCount();
            updateSubmitButton();
        });

        updateCharacterCount();
    }

    /**
     * @param user
     */
    public void setReportedUser(UserViewModel user) {
        this.reportedUser = user;
        this.isUserReport = true;

        if (user != null) {
            Platform.runLater(() -> {
                targetLabel.setText(String.format(
                        localeService.getMessage("report.dialog.target.user", "Reporting user: %s"),
                        user.getDisplayName()));

                userReasonComboBox.setVisible(true);
                userReasonComboBox.setManaged(true);
                listingReasonComboBox.setVisible(false);
                listingReasonComboBox.setManaged(false);
            });
        }
    }

    /**
     * @param listing
     */
    public void setReportedListing(ListingViewModel listing) {
        this.reportedListing = listing;
        this.isUserReport = false;

        if (listing != null) {
            Platform.runLater(() -> {
                targetLabel.setText(String.format(
                        localeService.getMessage("report.dialog.target.listing", "Reporting listing: \"%s\""),
                        listing.getTitle()));

                userReasonComboBox.setVisible(false);
                userReasonComboBox.setManaged(false);
                listingReasonComboBox.setVisible(true);
                listingReasonComboBox.setManaged(true);
            });
        }
    }

    /**
     * 
     */
    @FXML
    private void handleSubmit() {
        if (!validateReport()) {
            return;
        }

        submitButton.setDisable(true);

        if (isUserReport) {
            submitUserReport();
        } else {
            submitListingReport();
        }
    }

    /**
     * 
     */
    private void submitUserReport() {
        UserViewModel currentUser = ViewModelMapper.getInstance().toViewModel(sessionService.getUser());
        UserReportViewModel report = new UserReportViewModel(
                currentUser,
                reportedUser,
                userReasonComboBox.getValue(),
                descriptionTextArea.getText().trim());

        reportService.createUserReport(report)
                .thenAccept(_ -> Platform.runLater(() -> {
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("report.submit.success.title", "Report Submitted"),
                            localeService.getMessage("report.submit.success.header", "Thank you!"),
                            localeService.getMessage("report.submit.success.message",
                                    "Your report has been submitted and will be reviewed by our team."));
                    closeWindow();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("report.submit.error.title", "Error"),
                                localeService.getMessage("report.submit.error.header", "Failed to submit report"),
                                ex.getMessage());
                        submitButton.setDisable(false);
                    });
                    return null;
                });
    }

    /**
     * 
     */
    private void submitListingReport() {
        UserViewModel currentUser = ViewModelMapper.getInstance().toViewModel(sessionService.getUser());
        ListingReportViewModel report = new ListingReportViewModel(
                currentUser,
                reportedListing,
                listingReasonComboBox.getValue(),
                descriptionTextArea.getText().trim());

        reportService.createListingReport(report)
                .thenAccept(_ -> Platform.runLater(() -> {
                    AlertHelper.showInformationAlert(
                            localeService.getMessage("report.submit.success.title", "Report Submitted"),
                            localeService.getMessage("report.submit.success.header", "Thank you!"),
                            localeService.getMessage("report.submit.success.message",
                                    "Your report has been submitted and will be reviewed by our team."));
                    closeWindow();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("report.submit.error.title", "Error"),
                                localeService.getMessage("report.submit.error.header", "Failed to submit report"),
                                ex.getMessage());
                        submitButton.setDisable(false);
                    });
                    return null;
                });
    }

    /**
     * 
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    /**
     * @return
     */
    private boolean validateReport() {
        if (isUserReport && userReasonComboBox.getValue() == null) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("report.validation.title", "Validation Error"),
                    localeService.getMessage("report.validation.header", "Invalid Input"),
                    localeService.getMessage("report.validation.reason.required",
                            "Please select a reason for the report"));
            return false;
        }

        if (!isUserReport && listingReasonComboBox.getValue() == null) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("report.validation.title", "Validation Error"),
                    localeService.getMessage("report.validation.header", "Invalid Input"),
                    localeService.getMessage("report.validation.reason.required",
                            "Please select a reason for the report"));
            return false;
        }
        if (descriptionTextArea.getText().length() > 1000) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("report.validation.title", "Validation Error"),
                    localeService.getMessage("report.validation.header", "Invalid Input"),
                    localeService.getMessage("report.validation.description.too.long",
                            "Description is too long (max 1000 characters)"));
            return false;
        }

        return true;
    }

    /**
     * 
     */
    private void updateCharacterCount() {
        int length = descriptionTextArea.getText().length();
        int maxLength = 1000;
        characterCountLabel.setText(String.format("%d/%d", length, maxLength));

        if (length > maxLength) {
            characterCountLabel.getStyleClass().add("character-count-exceeded");
        } else {
            characterCountLabel.getStyleClass().remove("character-count-exceeded");
        }
    }

    /**
     * 
     */
    private void updateSubmitButton() {
        boolean hasValidReason = (isUserReport && userReasonComboBox.getValue() != null) ||
                (!isUserReport && listingReasonComboBox.getValue() != null);
        boolean hasValidDescription = descriptionTextArea.getText().length() <= 1000;

        submitButton.setDisable(!hasValidReason || !hasValidDescription);
    }

    /**
     * 
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}