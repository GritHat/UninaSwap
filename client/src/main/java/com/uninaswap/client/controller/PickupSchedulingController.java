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

public class PickupSchedulingController {

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

    private final LocaleService localeService = LocaleService.getInstance();
    private final PickupService pickupService = PickupService.getInstance();
    private String offerId;
    private OfferViewModel offer;
    private List<LocalDate> selectedDates = new ArrayList<>();
    private boolean isReschedulingMode = false;

    @FXML
    public void initialize() {
        setupTimeSpinners();
        setupDatePickers();
        setupLabels();
        updateSelectedDatesDisplay();
    }

    private void setupTimeSpinners() {
        startHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 9));
        startMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        endHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18));
        endMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15));
        startHourSpinner.setEditable(true);
        startMinuteSpinner.setEditable(true);
        endHourSpinner.setEditable(true);
        endMinuteSpinner.setEditable(true);
        startHourSpinner.valueProperty().addListener((_, _, _) -> validateTimeRange());
        startMinuteSpinner.valueProperty().addListener((_, _, _) -> validateTimeRange());
        endHourSpinner.valueProperty().addListener((_, _, _) -> validateTimeRange());
        endMinuteSpinner.valueProperty().addListener((_, _, _) -> validateTimeRange());
    }

    private void setupDatePickers() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today.plusDays(7));
        startDatePicker.setDayCellFactory(_ -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        endDatePicker.setDayCellFactory(_ -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate startDate = startDatePicker.getValue();
                setDisable(empty || date.isBefore(today) ||
                        (startDate != null && date.isBefore(startDate)));
            }
        });
        startDatePicker.valueProperty().addListener((_, _, newDate) -> {
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
            if (reschedulingMode) {
                titleLabel.setText(localeService.getMessage("pickup.reschedule.title", "Reschedule Pickup"));
                instructionsLabel.setText(localeService.getMessage("pickup.reschedule.instructions",
                        "Propose new available dates and time range for pickup"));
                confirmButton.setText(localeService.getMessage("pickup.reschedule.confirm", "Propose New Schedule"));
            } else {
                titleLabel.setText(localeService.getMessage("pickup.scheduling.title", "Schedule Pickup"));
                instructionsLabel.setText(localeService.getMessage("pickup.scheduling.instructions",
                        "Select your available dates and time range for pickup"));
                confirmButton.setText(localeService.getMessage("pickup.scheduling.confirm", "Schedule Pickup"));
            }
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
        AlertHelper.showInformationAlert(
                localeService.getMessage("pickup.dates.added.title", "Dates Added"),
                localeService.getMessage("pickup.dates.added.header", "Success"),
                String.format(
                        localeService.getMessage("pickup.dates.added.message", "Added %d dates to your availability"),
                        datesToAdd.size()));
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
                offer.getListing().getPickupLocation(),
                detailsArea.getText().trim(),
                null);

        confirmButton.setDisable(true);

        if (isReschedulingMode) {
            pickupService.createPickup(pickupDTO)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("pickup.reschedule.success.title", "Rescheduling Proposed"),
                                    localeService.getMessage("pickup.reschedule.success.header", "Success"),
                                    localeService.getMessage("pickup.reschedule.success.message",
                                            "Your new pickup schedule has been proposed. The other party can now review your availability."));
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
                        });
                        return null;
                    });
        } else {
            pickupService.createPickup(pickupDTO)
                    .thenAccept(success -> Platform.runLater(() -> {
                        if (success) {
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("pickup.created.title", "Pickup Scheduled"),
                                    localeService.getMessage("pickup.created.header", "Success"),
                                    localeService.getMessage("pickup.created.message",
                                            "Pickup has been scheduled successfully. The other party can now select a convenient time."));
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
                        });
                        return null;
                    });
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void validateTimeRange() {
        LocalTime startTime = LocalTime.of(startHourSpinner.getValue(), startMinuteSpinner.getValue());
        LocalTime endTime = LocalTime.of(endHourSpinner.getValue(), endMinuteSpinner.getValue());

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            confirmButton.setDisable(true);
        } else {
            confirmButton.setDisable(false);
        }
    }

    private boolean validateForm() {
        if (locationField.getText().trim().isEmpty()) {
            AlertHelper.showWarningAlert(
                    localeService.getMessage("pickup.validation.title", "Validation Error"),
                    localeService.getMessage("pickup.validation.header", "Invalid Input"),
                    localeService.getMessage("pickup.validation.location.required", "Please enter a pickup location"));
            locationField.requestFocus();
            return false;
        }
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

        selectedDatesCountLabel.setText(String.format(
                localeService.getMessage("pickup.dates.selected.count", "Selected dates: %d"),
                selectedDates.size()));

        for (LocalDate date : selectedDates) {
            Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateLabel.getStyleClass().add("date-chip");

            Button removeButton = new Button("×");
            removeButton.getStyleClass().add("date-chip-remove");
            removeButton.setOnAction(_ -> {
                selectedDates.remove(date);
                updateSelectedDatesDisplay();
            });

            dateLabel.setGraphic(removeButton);
            selectedDatesPane.getChildren().add(dateLabel);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
