package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.PickupService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.PickupViewModel;
import com.uninaswap.common.dto.PickupDTO;
import com.uninaswap.common.enums.PickupStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PickupSelectionController implements Refreshable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label instructionsLabel;

    @FXML
    private VBox availableDatesSection;

    @FXML
    private FlowPane availableDatesPane;

    @FXML
    private Label timeRangeLabel;

    @FXML
    private VBox selectedDateTimeSection;

    @FXML
    private DatePicker selectedDatePicker;

    @FXML
    private Spinner<Integer> selectedHourSpinner;

    @FXML
    private Spinner<Integer> selectedMinuteSpinner;

    @FXML
    private Label locationLabel;

    @FXML
    private Label detailsLabel;

    @FXML
    private TextArea detailsArea;

    @FXML
    private Button acceptButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button counterProposeButton;

    @FXML
    private Button cancelButton;

    // Counter proposal section (initially hidden)
    @FXML
    private VBox counterProposalSection;

    @FXML
    private TextField counterLocationField;

    @FXML
    private Spinner<Integer> counterStartHourSpinner;

    @FXML
    private Spinner<Integer> counterStartMinuteSpinner;

    @FXML
    private Spinner<Integer> counterEndHourSpinner;

    @FXML
    private Spinner<Integer> counterEndMinuteSpinner;

    @FXML
    private DatePicker counterStartDatePicker;

    @FXML
    private DatePicker counterEndDatePicker;

    @FXML
    private FlowPane counterSelectedDatesPane;

    @FXML
    private Button addCounterDateRangeButton;

    @FXML
    private Button clearCounterDatesButton;

    @FXML
    private Label counterSelectedDatesCountLabel;

    @FXML
    private TextArea counterDetailsArea;

    @FXML
    private Button submitCounterProposalButton;

    @FXML
    private Button cancelCounterProposalButton;

    // Additional UI elements that need localization
    @FXML
    private Label availableDatesHeaderLabel;

    @FXML
    private Label availableDatesHelpLabel;

    @FXML
    private Label timeRangeHeaderLabel;

    @FXML
    private Label selectTimeHeaderLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label pickupDetailsHeaderLabel;

    @FXML
    private Label locationFieldLabel;

    @FXML
    private Label detailsFieldLabel;

    @FXML
    private Label counterProposalHeaderLabel;

    @FXML
    private Label counterLocationHeaderLabel;

    @FXML
    private Label counterTimeRangeHeaderLabel;

    @FXML
    private Label counterFromLabel;

    @FXML
    private Label counterToLabel;

    @FXML
    private Label counterDatesHeaderLabel;

    @FXML
    private Label counterStartDateLabel;

    @FXML
    private Label counterEndDateLabel;

    @FXML
    private Label counterDetailsHeaderLabel;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final PickupService pickupService = PickupService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();

    // Data
    private PickupViewModel currentPickup;
    private List<LocalDate> counterSelectedDates = new ArrayList<>();
    private boolean isCounterProposalMode = false;

    @FXML
    public void initialize() {
        setupTimeSpinners();
        setupCounterProposalSection();
        updateUI();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("pickup.selection.debug.initialized", "PickupSelection controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update main labels based on current mode
        if (isCounterProposalMode) {
            if (titleLabel != null) {
                titleLabel.setText(localeService.getMessage("pickup.counter.title", "Counter Propose Pickup"));
            }
            if (instructionsLabel != null) {
                instructionsLabel.setText(localeService.getMessage("pickup.counter.instructions",
                        "Propose your available dates and time range for pickup"));
            }
        } else {
            if (titleLabel != null) {
                titleLabel.setText(localeService.getMessage("pickup.selection.title", "Select Pickup Time"));
            }
            if (instructionsLabel != null) {
                instructionsLabel.setText(localeService.getMessage("pickup.selection.instructions",
                        "Choose a convenient date and time from the available options, or propose an alternative"));
            }
        }

        // Update section headers
        if (availableDatesHeaderLabel != null) {
            availableDatesHeaderLabel.setText(localeService.getMessage("pickup.section.available.dates", "Available Dates"));
        }
        if (availableDatesHelpLabel != null) {
            availableDatesHelpLabel.setText(localeService.getMessage("pickup.help.click.date", "Click on a date to select it"));
        }
        if (timeRangeHeaderLabel != null) {
            timeRangeHeaderLabel.setText(localeService.getMessage("pickup.section.time.range", "Available Time Range"));
        }
        if (selectTimeHeaderLabel != null) {
            selectTimeHeaderLabel.setText(localeService.getMessage("pickup.section.select.time", "Select Your Preferred Time"));
        }
        if (pickupDetailsHeaderLabel != null) {
            pickupDetailsHeaderLabel.setText(localeService.getMessage("pickup.section.details", "Pickup Details"));
        }

        // Update field labels
        if (dateLabel != null) {
            dateLabel.setText(localeService.getMessage("pickup.label.date", "Date:"));
        }
        if (timeLabel != null) {
            timeLabel.setText(localeService.getMessage("pickup.label.time", "Time:"));
        }
        if (locationFieldLabel != null) {
            locationFieldLabel.setText(localeService.getMessage("pickup.label.location.field", "Location:"));
        }
        if (detailsFieldLabel != null) {
            detailsFieldLabel.setText(localeService.getMessage("pickup.label.details.field", "Additional Details:"));
        }

        // Update counter proposal section
        if (counterProposalHeaderLabel != null) {
            counterProposalHeaderLabel.setText(localeService.getMessage("pickup.counter.section.header", "Counter Proposal - Your Availability"));
        }
        if (counterLocationHeaderLabel != null) {
            counterLocationHeaderLabel.setText(localeService.getMessage("pickup.counter.label.location", "Preferred Location"));
        }
        if (counterTimeRangeHeaderLabel != null) {
            counterTimeRangeHeaderLabel.setText(localeService.getMessage("pickup.counter.label.time.range", "Your Available Time Range"));
        }
        if (counterFromLabel != null) {
            counterFromLabel.setText(localeService.getMessage("pickup.label.from", "From:"));
        }
        if (counterToLabel != null) {
            counterToLabel.setText(localeService.getMessage("pickup.label.to", "To:"));
        }
        if (counterDatesHeaderLabel != null) {
            counterDatesHeaderLabel.setText(localeService.getMessage("pickup.counter.label.dates", "Your Available Dates"));
        }
        if (counterStartDateLabel != null) {
            counterStartDateLabel.setText(localeService.getMessage("pickup.label.start.date", "Start Date:"));
        }
        if (counterEndDateLabel != null) {
            counterEndDateLabel.setText(localeService.getMessage("pickup.label.end.date", "End Date:"));
        }
        if (counterDetailsHeaderLabel != null) {
            counterDetailsHeaderLabel.setText(localeService.getMessage("pickup.counter.label.details", "Additional Details (Optional)"));
        }

        // Update button labels
        if (acceptButton != null) {
            acceptButton.setText(localeService.getMessage("pickup.selection.accept", "Accept Time"));
        }
        if (rejectButton != null) {
            rejectButton.setText(localeService.getMessage("pickup.selection.reject", "Reject"));
        }
        if (counterProposeButton != null) {
            counterProposeButton.setText(localeService.getMessage("pickup.selection.counter", "Counter Propose"));
        }
        if (cancelButton != null) {
            cancelButton.setText(localeService.getMessage("pickup.selection.cancel", "Cancel"));
        }
        if (submitCounterProposalButton != null) {
            submitCounterProposalButton.setText(localeService.getMessage("pickup.counter.submit", "Submit Counter Proposal"));
        }
        if (cancelCounterProposalButton != null) {
            cancelCounterProposalButton.setText(localeService.getMessage("pickup.counter.cancel", "Cancel Counter Proposal"));
        }
        if (addCounterDateRangeButton != null) {
            addCounterDateRangeButton.setText(localeService.getMessage("pickup.add.dates", "Add Date Range"));
        }
        if (clearCounterDatesButton != null) {
            clearCounterDatesButton.setText(localeService.getMessage("pickup.clear.dates", "Clear All"));
        }

        // Update prompt texts
        if (counterLocationField != null) {
            counterLocationField.setPromptText(localeService.getMessage("pickup.counter.location.prompt",
                    "Enter your preferred pickup location"));
        }
        if (counterDetailsArea != null) {
            counterDetailsArea.setPromptText(localeService.getMessage("pickup.counter.details.prompt",
                    "Add any special instructions or information"));
        }

        // Update counter selected dates count
        updateCounterSelectedDatesDisplay();
    }

    private void setupTimeSpinners() {
        // Selected time spinners
        selectedHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12));
        selectedMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        // Counter proposal time spinners
        counterStartHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        counterStartMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        counterEndHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        counterEndMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        // Make spinners editable
        selectedHourSpinner.setEditable(true);
        selectedMinuteSpinner.setEditable(true);
        counterStartHourSpinner.setEditable(true);
        counterStartMinuteSpinner.setEditable(true);
        counterEndHourSpinner.setEditable(true);
        counterEndMinuteSpinner.setEditable(true);

        // Add validation listeners for counter proposal
        counterStartHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateCounterTimeRange());
        counterStartMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateCounterTimeRange());
        counterEndHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateCounterTimeRange());
        counterEndMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateCounterTimeRange());
    }

    private void setupCounterProposalSection() {
        counterProposalSection.setVisible(false);
        counterProposalSection.setManaged(false);

        // Setup date pickers for counter proposal
        LocalDate today = LocalDate.now();
        counterStartDatePicker.setValue(today);
        counterEndDatePicker.setValue(today.plusDays(7));

        // Disable past dates
        counterStartDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        counterEndDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate startDate = counterStartDatePicker.getValue();
                setDisable(empty || date.isBefore(today) ||
                        (startDate != null && date.isBefore(startDate)));
            }
        });

        // Update end date picker when start date changes
        counterStartDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && counterEndDatePicker.getValue() != null &&
                    counterEndDatePicker.getValue().isBefore(newDate)) {
                counterEndDatePicker.setValue(newDate);
            }
        });

        updateCounterSelectedDatesDisplay();
    }

    public void setPickup(PickupViewModel pickup) {
        this.currentPickup = pickup;
        updateUI();
    }

    private void updateUI() {
        if (currentPickup == null)
            return;

        // Update available dates display
        updateAvailableDatesDisplay();

        // Update time range
        if (currentPickup.getStartTime() != null && currentPickup.getEndTime() != null) {
            timeRangeLabel.setText(String.format("%s - %s",
                    currentPickup.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    currentPickup.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
        }

        // Update location and details
        locationLabel.setText(currentPickup.getLocation() != null ? currentPickup.getLocation() : "");
        detailsArea.setText(currentPickup.getDetails() != null ? currentPickup.getDetails() : "");
        detailsArea.setEditable(false);

        // Set up date picker for selection
        if (!currentPickup.getAvailableDates().isEmpty()) {
            selectedDatePicker.setValue(currentPickup.getAvailableDates().get(0));
        }

        // Set default time to middle of available range
        if (currentPickup.getStartTime() != null && currentPickup.getEndTime() != null) {
            LocalTime middleTime = currentPickup.getStartTime().plusMinutes(
                    java.time.Duration.between(currentPickup.getStartTime(), currentPickup.getEndTime()).toMinutes()
                            / 2);
            selectedHourSpinner.getValueFactory().setValue(middleTime.getHour());
            selectedMinuteSpinner.getValueFactory().setValue(middleTime.getMinute());
        }

        // Validate initial selection
        validateSelection();
    }

    private void updateAvailableDatesDisplay() {
        availableDatesPane.getChildren().clear();

        if (currentPickup.getAvailableDates() != null) {
            for (LocalDate date : currentPickup.getAvailableDates()) {
                Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                dateLabel.getStyleClass().add("available-date-chip");

                // Add click handler to select this date
                dateLabel.setOnMouseClicked(e -> {
                    selectedDatePicker.setValue(date);
                    validateSelection();
                });

                availableDatesPane.getChildren().add(dateLabel);
            }
        }
    }

    @FXML
    private void handleAccept() {
        if (!validateSelection()) {
            return;
        }

        LocalDate selectedDate = selectedDatePicker.getValue();
        LocalTime selectedTime = LocalTime.of(selectedHourSpinner.getValue(), selectedMinuteSpinner.getValue());

        acceptButton.setDisable(true);

        pickupService.acceptPickup(currentPickup.getId(), selectedDate, selectedTime)
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        String formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        String formattedTime = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                        
                        AlertHelper.showInformationAlert(
                                localeService.getMessage("pickup.accept.success.title", "Pickup Confirmed"),
                                localeService.getMessage("pickup.accept.success.header", "Success"),
                                localeService.getMessage("pickup.accept.success.message",
                                        "Pickup confirmed for {0} at {1}. The offer status has been updated to 'Confirmed'.")
                                    .replace("{0}", formattedDate)
                                    .replace("{1}", formattedTime));
                        
                        System.out.println(localeService.getMessage("pickup.selection.debug.accept.success", 
                            "Pickup accepted for {0} at {1}")
                                .replace("{0}", formattedDate)
                                .replace("{1}", formattedTime));
                        closeWindow();
                    } else {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("pickup.accept.error.title", "Error"),
                                localeService.getMessage("pickup.accept.error.header", "Failed to confirm pickup"),
                                localeService.getMessage("pickup.accept.error.message",
                                        "Could not confirm the pickup time"));
                        acceptButton.setDisable(false);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("pickup.error.title", "Error"),
                                localeService.getMessage("pickup.error.header", "Connection Error"),
                                ex.getMessage());
                        acceptButton.setDisable(false);
                        System.err.println(localeService.getMessage("pickup.selection.error.accept.failed", 
                            "Failed to accept pickup: {0}").replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    @FXML
    private void handleReject() {
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("pickup.reject.confirm.title", "Reject Pickup"),
                localeService.getMessage("pickup.reject.confirm.header", "Reject Pickup Proposal"),
                localeService.getMessage("pickup.reject.confirm.message",
                        "Are you sure you want to reject this pickup proposal? This will require rescheduling or cancelling the offer."));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                rejectButton.setDisable(true);

                pickupService.updatePickupStatus(currentPickup.getId(), PickupStatus.DECLINED)
                        .thenAccept(success -> Platform.runLater(() -> {
                            if (success) {
                                // Show options for next steps
                                Alert nextStepsDialog = new Alert(Alert.AlertType.CONFIRMATION);
                                nextStepsDialog.setTitle(localeService.getMessage("pickup.reject.nextsteps.title", "Next Steps"));
                                nextStepsDialog.setHeaderText(localeService.getMessage("pickup.reject.nextsteps.header", 
                                        "Pickup proposal rejected"));
                                nextStepsDialog.setContentText(localeService.getMessage("pickup.reject.nextsteps.message",
                                        "What would you like to do next?"));

                                ButtonType rescheduleButton = new ButtonType(
                                        localeService.getMessage("pickup.reschedule.button", "Propose New Times"));
                                ButtonType cancelOfferButton = new ButtonType(
                                        localeService.getMessage("pickup.cancel.offer.button", "Cancel Offer"));
                                ButtonType decideLaterButton = new ButtonType(
                                        localeService.getMessage("pickup.decide.later.button", "Decide Later"));

                                nextStepsDialog.getButtonTypes().setAll(rescheduleButton, cancelOfferButton, decideLaterButton);

                                nextStepsDialog.showAndWait().ifPresent(choice -> {
                                    if (choice == rescheduleButton) {
                                        // Open pickup scheduling dialog for rescheduling
                                        handleReschedulePickup();
                                    } else if (choice == cancelOfferButton) {
                                        // Cancel the entire offer
                                        handleCancelOffer();
                                    }
                                    // If "Decide Later", just close the dialog
                                });

                                System.out.println(localeService.getMessage("pickup.selection.debug.reject.success", 
                                    "Pickup proposal rejected successfully"));
                                closeWindow();
                            } else {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("pickup.reject.error.title", "Error"),
                                        localeService.getMessage("pickup.reject.error.header", "Failed to reject pickup"),
                                        localeService.getMessage("pickup.reject.error.message",
                                                "Could not reject the pickup proposal"));
                                rejectButton.setDisable(false);
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("pickup.error.title", "Error"),
                                        localeService.getMessage("pickup.error.header", "Connection Error"),
                                        ex.getMessage());
                                rejectButton.setDisable(false);
                                System.err.println(localeService.getMessage("pickup.selection.error.reject.failed", 
                                    "Failed to reject pickup: {0}").replace("{0}", ex.getMessage()));
                            });
                            return null;
                        });
            }
        });
    }

    private void handleReschedulePickup() {
        // Open pickup scheduling dialog for rescheduling
        Stage stage = (Stage) acceptButton.getScene().getWindow();
        navigationService.openPickupRescheduling(currentPickup.getOfferId(), stage);
        System.out.println(localeService.getMessage("pickup.selection.debug.reschedule.opened", 
            "Pickup rescheduling dialog opened"));
    }

    private void handleCancelOffer() {
        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("offer.cancel.confirm.title", "Cancel Offer"),
                localeService.getMessage("offer.cancel.confirm.header", "Cancel Entire Offer"),
                localeService.getMessage("offer.cancel.confirm.message",
                        "Are you sure you want to cancel this offer? This action cannot be undone."));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Call service to cancel the pickup arrangement and update offer status
                pickupService.cancelPickupArrangement(currentPickup.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            if (success) {
                                AlertHelper.showInformationAlert(
                                        localeService.getMessage("offer.cancel.success.title", "Offer Cancelled"),
                                        localeService.getMessage("offer.cancel.success.header", "Success"),
                                        localeService.getMessage("offer.cancel.success.message",
                                                "The offer has been cancelled successfully."));
                                System.out.println(localeService.getMessage("pickup.selection.debug.offer.cancelled", 
                                    "Offer cancelled successfully"));
                            } else {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("offer.cancel.error.title", "Error"),
                                        localeService.getMessage("offer.cancel.error.header", "Failed to cancel offer"),
                                        localeService.getMessage("offer.cancel.error.message",
                                                "Could not cancel the offer"));
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("pickup.error.title", "Error"),
                                        localeService.getMessage("pickup.error.header", "Connection Error"),
                                        ex.getMessage());
                                System.err.println(localeService.getMessage("pickup.selection.error.cancel.failed", 
                                    "Failed to cancel offer: {0}").replace("{0}", ex.getMessage()));
                            });
                            return null;
                        });
            }
        });
    }

    @FXML
    private void handleCounterPropose() {
        isCounterProposalMode = true;

        // Hide main selection section
        availableDatesSection.setVisible(false);
        availableDatesSection.setManaged(false);
        selectedDateTimeSection.setVisible(false);
        selectedDateTimeSection.setManaged(false);

        // Show counter proposal section
        counterProposalSection.setVisible(true);
        counterProposalSection.setManaged(true);

        // Hide original buttons
        acceptButton.setVisible(false);
        rejectButton.setVisible(false);
        counterProposeButton.setVisible(false);

        // Prefill with original location if available
        if (currentPickup.getLocation() != null) {
            counterLocationField.setText(currentPickup.getLocation());
        }

        // Refresh UI to update instructions and labels for counter proposal mode
        refreshUI();
        
        System.out.println(localeService.getMessage("pickup.selection.debug.counter.mode.enabled", 
            "Counter proposal mode enabled"));
    }

    @FXML
    private void handleAddCounterDateRange() {
        LocalDate startDate = counterStartDatePicker.getValue();
        LocalDate endDate = counterEndDatePicker.getValue();

        if (startDate == null || endDate == null) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.dates.required",
                            "Please select both start and end dates"));
            return;
        }

        if (startDate.isAfter(endDate)) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.dates.order",
                            "Start date must be before or equal to end date"));
            return;
        }

        // Add all dates in the range
        List<LocalDate> datesToAdd = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!counterSelectedDates.contains(current)) {
                datesToAdd.add(current);
            }
            current = current.plusDays(1);
        }

        counterSelectedDates.addAll(datesToAdd);
        counterSelectedDates.sort(LocalDate::compareTo);
        updateCounterSelectedDatesDisplay();

        AlertHelper.showInformationAlert(
                localeService.getMessage("pickup.dates.added.title", "Dates Added"),
                localeService.getMessage("pickup.dates.added.header", "Success"),
                localeService.getMessage("pickup.dates.added.message", "Added {0} dates to your availability")
                    .replace("{0}", String.valueOf(datesToAdd.size())));
        
        System.out.println(localeService.getMessage("pickup.selection.debug.dates.added", 
            "Added {0} dates to counter proposal")
                .replace("{0}", String.valueOf(datesToAdd.size())));
    }

    @FXML
    private void handleClearCounterDates() {
        if (counterSelectedDates.isEmpty()) {
            return;
        }

        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("pickup.clear.confirm.title", "Clear Dates"),
                localeService.getMessage("pickup.clear.confirm.header", "Clear All Selected Dates"),
                localeService.getMessage("pickup.clear.confirm.message",
                        "Are you sure you want to clear all selected dates?"));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                counterSelectedDates.clear();
                updateCounterSelectedDatesDisplay();
                System.out.println(localeService.getMessage("pickup.selection.debug.dates.cleared", 
                    "Counter proposal dates cleared"));
            }
        });
    }

    @FXML
    private void handleSubmitCounterProposal() {
        if (!validateCounterProposal()) {
            return;
        }

        LocalTime startTime = LocalTime.of(counterStartHourSpinner.getValue(), counterStartMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(counterEndHourSpinner.getValue(), counterEndMinuteSpinner.getValue());

        // Create new pickup with counter proposal
        PickupViewModel counterPickupViewModel = new PickupViewModel(
                currentPickup.getOfferId(),
                currentPickup.getOffer(),
                new ArrayList<>(counterSelectedDates),
                startTime,
                endTime,
                counterLocationField.getText().trim(),
                counterDetailsArea.getText().trim(),
                null // createdByUserId will be set by the service
        );

        submitCounterProposalButton.setDisable(true);

        // First reject the current pickup
        pickupService.updatePickupStatus(currentPickup.getId(), PickupStatus.CANCELLED)
                .thenCompose(rejected -> {
                    if (rejected) {
                        // Then create the counter proposal
                        return pickupService.createPickup(counterPickupViewModel);
                    } else {
                        throw new RuntimeException(localeService.getMessage("pickup.counter.error.reject.failed", 
                            "Failed to reject current pickup"));
                    }
                })
                .thenAccept(success -> Platform.runLater(() -> {
                    if (success) {
                        AlertHelper.showInformationAlert(
                                localeService.getMessage("pickup.counter.success.title", "Counter Proposal Sent"),
                                localeService.getMessage("pickup.counter.success.header", "Success"),
                                localeService.getMessage("pickup.counter.success.message",
                                        "Your counter proposal has been sent. The other party can now review your availability."));
                        System.out.println(localeService.getMessage("pickup.selection.debug.counter.submitted", 
                            "Counter proposal submitted successfully"));
                        closeWindow();
                    } else {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("pickup.counter.error.title", "Error"),
                                localeService.getMessage("pickup.counter.error.header",
                                        "Failed to send counter proposal"),
                                localeService.getMessage("pickup.counter.error.message",
                                        "Could not send the counter proposal"));
                        submitCounterProposalButton.setDisable(false);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        AlertHelper.showErrorAlert(
                                localeService.getMessage("pickup.counter.error.title", "Error"),
                                localeService.getMessage("pickup.counter.error.header", "Connection Error"),
                                ex.getMessage());
                        submitCounterProposalButton.setDisable(false);
                        System.err.println(localeService.getMessage("pickup.selection.error.counter.failed", 
                            "Failed to submit counter proposal: {0}").replace("{0}", ex.getMessage()));
                    });
                    return null;
                });
    }

    @FXML
    private void handleCancelCounterProposal() {
        isCounterProposalMode = false;

        // Show main selection section
        availableDatesSection.setVisible(true);
        availableDatesSection.setManaged(true);
        selectedDateTimeSection.setVisible(true);
        selectedDateTimeSection.setManaged(true);

        // Hide counter proposal section
        counterProposalSection.setVisible(false);
        counterProposalSection.setManaged(false);

        // Show original buttons
        acceptButton.setVisible(true);
        rejectButton.setVisible(true);
        counterProposeButton.setVisible(true);

        // Clear counter proposal data
        counterSelectedDates.clear();
        updateCounterSelectedDatesDisplay();

        // Refresh UI to restore original instructions and labels
        refreshUI();
        
        System.out.println(localeService.getMessage("pickup.selection.debug.counter.cancelled", 
            "Counter proposal cancelled"));
    }

    @FXML
    private void handleCancel() {
        System.out.println(localeService.getMessage("pickup.selection.debug.cancelled", 
            "Pickup selection cancelled by user"));
        closeWindow();
    }

    private boolean validateSelection() {
        if (currentPickup == null)
            return false;

        LocalDate selectedDate = selectedDatePicker.getValue();
        LocalTime selectedTime = LocalTime.of(selectedHourSpinner.getValue(), selectedMinuteSpinner.getValue());

        // Check if selected date is available
        if (!currentPickup.getAvailableDates().contains(selectedDate)) {
            acceptButton.setDisable(true);
            return false;
        }

        // Check if selected time is within range
        if (selectedTime.isBefore(currentPickup.getStartTime()) || selectedTime.isAfter(currentPickup.getEndTime())) {
            acceptButton.setDisable(true);
            return false;
        }

        acceptButton.setDisable(false);
        return true;
    }

    private void validateCounterTimeRange() {
        LocalTime startTime = LocalTime.of(counterStartHourSpinner.getValue(), counterStartMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(counterEndHourSpinner.getValue(), counterEndMinuteSpinner.getValue());

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            submitCounterProposalButton.setDisable(true);
        } else {
            submitCounterProposalButton.setDisable(false);
        }
    }

    private boolean validateCounterProposal() {
        // Check location
        if (counterLocationField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.location.required", "Please enter a pickup location"));
            counterLocationField.requestFocus();
            return false;
        }

        // Check time range
        LocalTime startTime = LocalTime.of(counterStartHourSpinner.getValue(), counterStartMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(counterEndHourSpinner.getValue(), counterEndMinuteSpinner.getValue());

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.time.invalid", "End time must be after start time"));
            counterStartHourSpinner.requestFocus();
            return false;
        }

        // Check selected dates
        if (counterSelectedDates.isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.dates.empty",
                            "Please select at least one available date"));
            return false;
        }

        return true;
    }

    private void updateCounterSelectedDatesDisplay() {
        counterSelectedDatesPane.getChildren().clear();

        if (counterSelectedDatesCountLabel != null) {
            counterSelectedDatesCountLabel.setText(localeService.getMessage("pickup.dates.selected.count", "Selected dates: {0}")
                .replace("{0}", String.valueOf(counterSelectedDates.size())));
        }

        for (LocalDate date : counterSelectedDates) {
            Label dateLabel = new Label(date.toString());
            dateLabel.getStyleClass().add("date-chip");

            Button removeButton = new Button(localeService.getMessage("pickup.date.remove", "×"));
            removeButton.getStyleClass().add("date-chip-remove");
            removeButton.setOnAction(e -> {
                counterSelectedDates.remove(date);
                updateCounterSelectedDatesDisplay();
                System.out.println(localeService.getMessage("pickup.selection.debug.date.removed", 
                    "Removed date from counter proposal: {0}").replace("{0}", date.toString()));
            });

            dateLabel.setGraphic(removeButton);
            counterSelectedDatesPane.getChildren().add(dateLabel);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}