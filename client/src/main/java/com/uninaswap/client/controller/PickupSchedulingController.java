package com.uninaswap.client.controller;

import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.PickupService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.client.viewmodel.PickupViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PickupSchedulingController implements Refreshable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label instructionsLabel;

    @FXML
    private TextField locationField;

    @FXML
    private TextArea detailsArea;

    @FXML
    private Spinner<Integer> startHourSpinner;

    @FXML
    private Spinner<Integer> startMinuteSpinner;

    @FXML
    private Spinner<Integer> endHourSpinner;

    @FXML
    private Spinner<Integer> endMinuteSpinner;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private FlowPane selectedDatesPane;

    @FXML
    private Button addDateRangeButton;

    @FXML
    private Button clearDatesButton;

    @FXML
    private Label selectedDatesCountLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    // Additional labels that need localization
    @FXML
    private Label locationLabel;

    @FXML
    private Label timeRangeLabel;

    @FXML
    private Label fromLabel;

    @FXML
    private Label toLabel;

    @FXML
    private Label timeHelpLabel;

    @FXML
    private Label availableDatesLabel;

    @FXML
    private Label startDateLabel;

    @FXML
    private Label endDateLabel;

    @FXML
    private Label detailsLabel;

    @FXML
    private Label detailsHelpLabel;

    // Services
    private final LocaleService localeService = LocaleService.getInstance();
    private final PickupService pickupService = PickupService.getInstance();

    // Data
    private String offerId;
    private OfferViewModel offer;
    private List<LocalDate> selectedDates = new ArrayList<>();
    private boolean isReschedulingMode = false;

    @FXML
    public void initialize() {
        setupTimeSpinners();
        setupDatePickers();
        updateSelectedDatesDisplay();
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("pickup.scheduling.debug.initialized", "PickupScheduling controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update labels based on current mode
        if (isReschedulingMode) {
            if (titleLabel != null) {
                titleLabel.setText(localeService.getMessage("pickup.reschedule.title", "Reschedule Pickup"));
            }
            if (instructionsLabel != null) {
                instructionsLabel.setText(localeService.getMessage("pickup.reschedule.instructions",
                        "Propose new available dates and time range for pickup"));
            }
            if (confirmButton != null) {
                confirmButton.setText(localeService.getMessage("pickup.reschedule.confirm", "Propose New Schedule"));
            }
        } else {
            if (titleLabel != null) {
                titleLabel.setText(localeService.getMessage("pickup.scheduling.title", "Schedule Pickup"));
            }
            if (instructionsLabel != null) {
                instructionsLabel.setText(localeService.getMessage("pickup.scheduling.instructions",
                        "Select your available dates and time range for pickup"));
            }
            if (confirmButton != null) {
                confirmButton.setText(localeService.getMessage("pickup.scheduling.confirm", "Schedule Pickup"));
            }
        }

        // Update common labels
        if (locationLabel != null) {
            locationLabel.setText(localeService.getMessage("pickup.label.location", "Pickup Location"));
        }
        if (timeRangeLabel != null) {
            timeRangeLabel.setText(localeService.getMessage("pickup.label.time.range", "Available Time Range"));
        }
        if (fromLabel != null) {
            fromLabel.setText(localeService.getMessage("pickup.label.from", "From:"));
        }
        if (toLabel != null) {
            toLabel.setText(localeService.getMessage("pickup.label.to", "To:"));
        }
        if (timeHelpLabel != null) {
            timeHelpLabel.setText(localeService.getMessage("pickup.help.time.range", "This time range will apply to all selected dates"));
        }
        if (availableDatesLabel != null) {
            availableDatesLabel.setText(localeService.getMessage("pickup.label.available.dates", "Available Dates"));
        }
        if (startDateLabel != null) {
            startDateLabel.setText(localeService.getMessage("pickup.label.start.date", "Start Date:"));
        }
        if (endDateLabel != null) {
            endDateLabel.setText(localeService.getMessage("pickup.label.end.date", "End Date:"));
        }
        if (detailsLabel != null) {
            detailsLabel.setText(localeService.getMessage("pickup.label.details", "Additional Details (Optional)"));
        }
        if (detailsHelpLabel != null) {
            detailsHelpLabel.setText(localeService.getMessage("pickup.help.details", "You can add any special instructions or additional information here"));
        }

        // Update button labels
        if (cancelButton != null) {
            cancelButton.setText(localeService.getMessage("pickup.scheduling.cancel", "Cancel"));
        }
        if (addDateRangeButton != null) {
            addDateRangeButton.setText(localeService.getMessage("pickup.add.dates", "Add Date Range"));
        }
        if (clearDatesButton != null) {
            clearDatesButton.setText(localeService.getMessage("pickup.clear.dates", "Clear All"));
        }

        // Update prompt texts
        if (locationField != null) {
            locationField.setPromptText(localeService.getMessage("pickup.location.prompt",
                    "Enter pickup location (e.g., University Campus, Building A)"));
        }
        if (detailsArea != null) {
            detailsArea.setPromptText(localeService.getMessage("pickup.details.prompt",
                    "Add any additional details or instructions (optional)"));
        }

        // Update selected dates count
        updateSelectedDatesDisplay();
    }

    private void setupTimeSpinners() {
        // Start time spinners
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        // End time spinners
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));

        // Make spinners editable
        startHourSpinner.setEditable(true);
        startMinuteSpinner.setEditable(true);
        endHourSpinner.setEditable(true);
        endMinuteSpinner.setEditable(true);

        // Add validation listeners
        startHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateTimeRange());
        startMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateTimeRange());
        endHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateTimeRange());
        endMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateTimeRange());
    }

    private void setupDatePickers() {
        LocalDate today = LocalDate.now();

        // Set minimum dates to today
        startDatePicker.setValue(today);
        endDatePicker.setValue(today.plusDays(7));

        // Disable past dates
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate startDate = startDatePicker.getValue();
                setDisable(empty || date.isBefore(today) ||
                        (startDate != null && date.isBefore(startDate)));
            }
        });

        // Update end date picker when start date changes
        startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && endDatePicker.getValue() != null &&
                    endDatePicker.getValue().isBefore(newDate)) {
                endDatePicker.setValue(newDate);
            }
        });
    }

    private void setupLabels() {
        titleLabel.setText(localeService.getMessage("pickup.scheduling.title", "Schedule Pickup"));
        instructionsLabel.setText(localeService.getMessage("pickup.scheduling.instructions",
                "Set your available dates and time range for item pickup"));

        locationField.setPromptText(localeService.getMessage("pickup.location.prompt",
                "Enter pickup location (e.g., University Campus, Building A)"));
        detailsArea.setPromptText(localeService.getMessage("pickup.details.prompt",
                "Add any special instructions or additional information here"));

        confirmButton.setText(localeService.getMessage("pickup.scheduling.confirm", "Schedule Pickup"));
        cancelButton.setText(localeService.getMessage("button.cancel", "Cancel"));
        addDateRangeButton.setText(localeService.getMessage("pickup.dates.add.button", "Add Range"));
        clearDatesButton.setText(localeService.getMessage("pickup.dates.clear.button", "Clear All"));
        
        // Update selected dates count display
        updateSelectedDatesDisplay();
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public void setOffer(OfferViewModel offer) {
        this.offer = offer;
    }

    public void setReschedulingMode(boolean reschedulingMode) {
        this.isReschedulingMode = reschedulingMode;

        Platform.runLater(() -> {
            // Refresh UI to update labels based on mode
            refreshUI();
        });
    }

    @FXML
    private void handleAddDateRange() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

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
            if (!selectedDates.contains(current)) {
                datesToAdd.add(current);
            }
            current = current.plusDays(1);
        }

        selectedDates.addAll(datesToAdd);
        selectedDates.sort(LocalDate::compareTo);
        updateSelectedDatesDisplay();

        // Show confirmation
        AlertHelper.showInformationAlert(
                localeService.getMessage("pickup.dates.added.title", "Dates Added"),
                localeService.getMessage("pickup.dates.added.header", "Success"),
                localeService.getMessage("pickup.dates.added.message", "Added {0} dates to your availability")
                    .replace("{0}", String.valueOf(datesToAdd.size())));

        System.out.println(localeService.getMessage("pickup.scheduling.debug.dates.added", "Added {0} dates to pickup schedule")
            .replace("{0}", String.valueOf(datesToAdd.size())));
    }

    @FXML
    private void handleClearDates() {
        if (selectedDates.isEmpty()) {
            return;
        }

        Alert confirmation = AlertHelper.createConfirmationDialog(
                localeService.getMessage("pickup.clear.confirm.title", "Clear Dates"),
                localeService.getMessage("pickup.clear.confirm.header", "Clear All Selected Dates"),
                localeService.getMessage("pickup.clear.confirm.message",
                        "Are you sure you want to clear all selected dates?"));

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                selectedDates.clear();
                updateSelectedDatesDisplay();
                System.out.println(localeService.getMessage("pickup.scheduling.debug.dates.cleared", "All pickup dates cleared"));
            }
        });
    }

    @FXML
    private void handleConfirm() {
        if (!validateForm()) {
            return;
        }

        LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());

        PickupViewModel pickupDTO = new PickupViewModel(
                offerId,
                offer,
                new ArrayList<>(selectedDates),
                startTime,
                endTime,
                locationField.getText().trim(),
                detailsArea.getText().trim(),
                null // createdByUserId will be set by the service
        );

        confirmButton.setDisable(true);

        if (isReschedulingMode) {
            // Handle rescheduling - create new pickup proposal
            pickupService.createPickup(pickupDTO)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("pickup.reschedule.success.title", "Rescheduling Proposed"),
                                    localeService.getMessage("pickup.reschedule.success.header", "Success"),
                                    localeService.getMessage("pickup.reschedule.success.message",
                                            "Your new pickup schedule has been proposed. The other party can now review your availability."));
                            System.out.println(localeService.getMessage("pickup.scheduling.debug.reschedule.success", "Pickup rescheduling proposed successfully"));
                            closeWindow();
                        } else {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("pickup.error.title", "Error"),
                                    localeService.getMessage("pickup.reschedule.error.header", "Failed to propose new schedule"),
                                    localeService.getMessage("pickup.reschedule.error.message",
                                            "Could not propose the new pickup schedule. Please try again."));
                            confirmButton.setDisable(false);
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("pickup.error.title", "Error"),
                                    localeService.getMessage("pickup.error.header", "Connection Error"),
                                    ex.getMessage());
                            confirmButton.setDisable(false);
                            System.err.println(localeService.getMessage("pickup.scheduling.error.reschedule.failed", "Failed to reschedule pickup: {0}").replace("{0}", ex.getMessage()));
                        });
                        return null;
                    });
        } else {
            // Handle original scheduling
            pickupService.createPickup(pickupDTO)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("pickup.created.title", "Pickup Scheduled"),
                                    localeService.getMessage("pickup.created.header", "Success"),
                                    localeService.getMessage("pickup.created.message",
                                            "Pickup has been scheduled successfully. The other party can now select a convenient time."));
                            System.out.println(localeService.getMessage("pickup.scheduling.debug.created.success", "Pickup scheduled successfully"));
                            closeWindow();
                        } else {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("pickup.error.title", "Error"),
                                    localeService.getMessage("pickup.error.header", "Failed to schedule pickup"),
                                    localeService.getMessage("pickup.error.message",
                                            "Could not schedule the pickup. Please try again."));
                            confirmButton.setDisable(false);
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("pickup.error.title", "Error"),
                                    localeService.getMessage("pickup.error.header", "Connection Error"),
                                    ex.getMessage());
                            confirmButton.setDisable(false);
                            System.err.println(localeService.getMessage("pickup.scheduling.error.create.failed", "Failed to schedule pickup: {0}").replace("{0}", ex.getMessage()));
                        });
                        return null;
                    });
        }
    }

    @FXML
    private void handleCancel() {
        System.out.println(localeService.getMessage("pickup.scheduling.debug.cancelled", "Pickup scheduling cancelled by user"));
        closeWindow();
    }

    private void validateTimeRange() {
        LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            // Visual feedback - could add styling here
            confirmButton.setDisable(true);
        } else {
            confirmButton.setDisable(false);
        }
    }

    private boolean validateForm() {
        // Check location
        if (locationField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.location.required", "Please enter a pickup location"));
            locationField.requestFocus();
            return false;
        }

        // Check time range
        LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.time.invalid", "End time must be after start time"));
            startHourSpinner.requestFocus();
            return false;
        }

        // Check selected dates
        if (selectedDates.isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.dates.empty",
                            "Please select at least one available date"));
            return false;
        }

        return true;
    }

    private void updateSelectedDatesDisplay() {
        selectedDatesPane.getChildren().clear();

        if (selectedDatesCountLabel != null) {
            selectedDatesCountLabel.setText(localeService.getMessage("pickup.dates.selected.count", "Selected dates: {0}")
                .replace("{0}", String.valueOf(selectedDates.size())));
        }

        for (LocalDate date : selectedDates) {
            Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateLabel.getStyleClass().add("date-chip");

            Button removeButton = new Button(localeService.getMessage("pickup.date.remove", "×"));
            removeButton.getStyleClass().add("date-chip-remove");
            removeButton.setOnAction(e -> {
                selectedDates.remove(date);
                updateSelectedDatesDisplay();
                System.out.println(localeService.getMessage("pickup.scheduling.debug.date.removed", "Removed date from pickup schedule: {0}").replace("{0}", date.toString()));
            });

            dateLabel.setGraphic(removeButton);
            selectedDatesPane.getChildren().add(dateLabel);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}
