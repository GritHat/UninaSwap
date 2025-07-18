package com.uninaswap.client.controller;

import com.uninaswap.client.mapper.ViewModelMapper;
import com.uninaswap.client.service.*;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.AuctionListingViewModel;
import com.uninaswap.client.viewmodel.GiftListingViewModel;
import com.uninaswap.client.viewmodel.ListingItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.SellListingViewModel;
import com.uninaswap.client.viewmodel.TradeListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.common.enums.Currency;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class ListingDetailsController {

    // Header elements
    @FXML
    private Button backButton;
    @FXML
    private Button favoriteButton;
    @FXML
    private ImageView favoriteIcon;

    // Image gallery elements
    @FXML
    private ImageView mainImage;
    @FXML
    private HBox imageNavigation;
    @FXML
    private Button prevImageButton;
    @FXML
    private Button nextImageButton;
    @FXML
    private Label imageCounter;
    @FXML
    private ScrollPane thumbnailScrollPane;
    @FXML
    private HBox thumbnailContainer;

    // Listing details elements
    @FXML
    private Text listingTitle;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private VBox itemsSection;
    @FXML
    private VBox itemsList;

    // Seller information
    @FXML
    private ImageView sellerAvatar;
    @FXML
    private Text sellerName;
    @FXML
    private Text sellerRating;

    // Price/Action section
    @FXML
    private Text priceLabel;
    @FXML
    private Text priceValue;
    @FXML
    private Text priceDetails;
    @FXML
    private VBox actionButtonsSection;

    // Sell listing actions
    @FXML
    private VBox sellActions;
    @FXML
    private Button buyNowButton;
    @FXML
    private Button makeOfferButton;

    // Trade listing actions
    @FXML
    private VBox tradeActions;
    @FXML
    private Button proposeTradeButton;
    @FXML
    private VBox tradeOptionsSection;
    @FXML
    private CheckBox includeMoneyCheckBox;
    @FXML
    private HBox moneyOfferSection;
    @FXML
    private TextField moneyAmountField;
    @FXML
    private ComboBox<Currency> currencyComboBox;

    // Gift listing actions
    @FXML
    private VBox giftActions;
    @FXML
    private Button requestGiftButton;
    @FXML
    private VBox thankYouSection;
    @FXML
    private CheckBox offerThankYouCheckBox;
    @FXML
    private TextArea thankYouMessageArea;

    // Auction listing actions
    @FXML
    private VBox auctionActions;
    @FXML
    private Text currentBidValue;
    @FXML
    private Text timeRemainingLabel;
    @FXML
    private TextField bidAmountField;
    @FXML
    private ComboBox<Currency> bidCurrencyComboBox;
    @FXML
    private Text minimumBidLabel;
    @FXML
    private Button placeBidButton;

    // Common actions
    @FXML
    private Button contactSellerButton;
    @FXML
    private Button reportListingButton;

    // Services
    private final NavigationService navigationService = NavigationService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final ListingService listingService = ListingService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final UserSessionService sessionService = UserSessionService.getInstance();

    // State
    private ListingViewModel currentListing;
    private List<String> imageUrls = new ArrayList<>();
    private int currentImageIndex = 0;
    private boolean isFavorite = false;

    @FXML
    public void initialize() {
        setupCurrencyComboBoxes();
        setupEventHandlers();
    }

    private void setupCurrencyComboBoxes() {
        // Setup currency options
        List<Currency> currencies = List.of(Currency.EUR, Currency.USD, Currency.GBP);
        currencyComboBox.setItems(FXCollections.observableArrayList(currencies));
        bidCurrencyComboBox.setItems(FXCollections.observableArrayList(currencies));

        // Set default currency
        currencyComboBox.setValue(Currency.EUR);
        bidCurrencyComboBox.setValue(Currency.EUR);
    }

    private void setupEventHandlers() {
        // Money offer checkbox handler
        includeMoneyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            moneyOfferSection.setVisible(newVal);
        });

        // Thank you offer checkbox handler
        offerThankYouCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            thankYouMessageArea.setVisible(newVal);
        });
    }

    public void setListing(ListingViewModel listing) {
        this.currentListing = listing;

        if (listing != null) {
            Platform.runLater(() -> {
                populateListingDetails();
                setupImageGallery();
                setupActionButtons();
                initializeFavoriteStatus();
            });
        }
    }

    private void populateListingDetails() {
        // Basic listing information
        listingTitle.setText(currentListing.getTitle());
        descriptionArea.setText(currentListing.getDescription());

        // Category and status
        categoryLabel.setText(getListingCategory());
        statusLabel.setText(currentListing.getStatus().toString());

        // Seller information
        if (currentListing.getUser() != null) {
            sellerName.setText(currentListing.getUser().getUsername());
            // TODO: Load seller rating and avatar
            sellerRating.setText("⭐ 4.8 (127 recensioni)"); // Placeholder
        }

        // Price information
        setupPriceSection();

        // Items list
        populateItemsList();
    }

    private String getListingCategory() {
        if (currentListing.getItems() != null && !currentListing.getItems().isEmpty()) {
            String itemCategory = currentListing.getItems().get(0).getItem().getItemCategory();
            if (itemCategory != null && !itemCategory.isEmpty()) {
                return itemCategory;
            }
        }
        return currentListing.getListingTypeValue();
    }

    private void setupPriceSection() {
        String listingType = currentListing.getListingTypeValue();

        switch (listingType.toUpperCase()) {
            case "SELL":
                setupSellPricing((SellListingViewModel) currentListing);
                break;
            case "TRADE":
                setupTradePricing((TradeListingViewModel) currentListing);
                break;
            case "GIFT":
                setupGiftPricing((GiftListingViewModel) currentListing);
                break;
            case "AUCTION":
                setupAuctionPricing((AuctionListingViewModel) currentListing);
                break;
        }
    }

    private void setupSellPricing(SellListingViewModel sellListing) {
        priceLabel.setText("Prezzo");
        String currency = sellListing.getCurrency() != null ? sellListing.getCurrency().getSymbol() : "€";
        priceValue.setText(currency + " " + sellListing.getPrice());
        priceDetails.setText("Prezzo fisso");
    }

    private void setupTradePricing(TradeListingViewModel tradeListing) {
        priceLabel.setText("Tipo di scambio");
        priceValue.setText("Scambio");

        StringBuilder details = new StringBuilder();
        if (tradeListing.isAcceptMoneyOffers()) {
            if (tradeListing.getReferencePrice() != null) {
                String currency = tradeListing.getCurrency() != null ? tradeListing.getCurrency().getSymbol() : "€";
                details.append("Ref: ").append(currency).append(" ").append(tradeListing.getReferencePrice());
            }
        }
        if (tradeListing.isAcceptMixedOffers()) {
            details.append(details.length() > 0 ? " | " : "").append("Offerte miste accettate");
        }
        priceDetails.setText(details.toString());
    }

    private void setupGiftPricing(GiftListingViewModel giftListing) {
        priceLabel.setText("Tipo");
        priceValue.setText("Regalo");

        StringBuilder details = new StringBuilder("Gratuito");
        if (giftListing.isPickupOnly()) {
            details.append(" - Solo ritiro");
        }
        if (giftListing.isAllowThankYouOffers()) {
            details.append(" - Ringraziamenti accettati");
        }
        priceDetails.setText(details.toString());
    }

    private void setupAuctionPricing(AuctionListingViewModel auctionListing) {
        priceLabel.setText("Asta");

        BigDecimal currentBid = auctionListing.getHighestBid();
        String currency = auctionListing.getCurrency() != null ? auctionListing.getCurrency().getSymbol() : "€";

        if (currentBid != null && currentBid.compareTo(BigDecimal.ZERO) > 0) {
            priceValue.setText(currency + " " + currentBid);
            priceDetails.setText("Offerta attuale");
        } else {
            priceValue.setText(currency + " " + auctionListing.getStartingPrice());
            priceDetails.setText("Prezzo di partenza");
        }

        // Update auction-specific elements
        currentBidValue.setText(currency + " " +
                (currentBid != null ? currentBid : auctionListing.getStartingPrice()));

        // Calculate minimum bid
        BigDecimal minimumBid = auctionListing.getMinimumNextBid();
        minimumBidLabel.setText("Offerta minima: " + currency + " " + minimumBid);

        // Calculate time remaining
        updateTimeRemaining(auctionListing);
    }

    private void updateTimeRemaining(AuctionListingViewModel auction) {
        if (auction.getAuctionEndTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = auction.getAuctionEndTime();

            if (now.isBefore(endTime)) {
                long days = ChronoUnit.DAYS.between(now, endTime);
                long hours = ChronoUnit.HOURS.between(now, endTime) % 24;
                long minutes = ChronoUnit.MINUTES.between(now, endTime) % 60;

                String timeText = String.format("Tempo rimanente: %d giorni %d ore %d minuti",
                        days, hours, minutes);
                timeRemainingLabel.setText(timeText);
            } else {
                timeRemainingLabel.setText("Asta terminata");
                placeBidButton.setDisable(true);
            }
        }
    }

    private void populateItemsList() {
        itemsList.getChildren().clear();

        if (currentListing.getItems() != null) {
            for (ListingItemViewModel item : currentListing.getItems()) {
                HBox itemRow = createItemRow(item);
                itemsList.getChildren().add(itemRow);
            }
        }
    }

    private HBox createItemRow(ListingItemViewModel item) {
        HBox itemRow = new HBox(10);
        itemRow.getStyleClass().add("item-row");

        // Item image thumbnail
        ImageView itemImage = new ImageView();
        itemImage.setFitHeight(40);
        itemImage.setFitWidth(40);
        itemImage.setPreserveRatio(true);
        itemImage.getStyleClass().add("item-thumbnail");

        // Load item image
        if (item.getItem().getImagePath() != null && !item.getItem().getImagePath().isEmpty()) {
            imageService.fetchImage(item.getItem().getImagePath())
                    .thenAccept(image -> Platform.runLater(() -> {
                        if (image != null && !image.isError()) {
                            itemImage.setImage(image);
                        } else {
                            setDefaultItemImage(itemImage);
                        }
                    }));
        } else {
            setDefaultItemImage(itemImage);
        }

        // Item details
        VBox itemDetails = new VBox(2);
        Text itemName = new Text(item.getName());
        itemName.getStyleClass().add("item-name");
        Text itemQuantity = new Text("Quantità: " + item.getQuantity());
        itemQuantity.getStyleClass().add("item-quantity");

        itemDetails.getChildren().addAll(itemName, itemQuantity);

        itemRow.getChildren().addAll(itemImage, itemDetails);
        return itemRow;
    }

    private void setDefaultItemImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default item image: " + e.getMessage());
        }
    }

    private void setupImageGallery() {
        // Collect all image URLs from listing items
        imageUrls.clear();
        if (currentListing.getItems() != null) {
            for (ListingItemViewModel item : currentListing.getItems()) {
                String imagePath = item.getItem().getImagePath();
                if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default")) {
                    imageUrls.add(imagePath);
                }
            }
        }

        if (!imageUrls.isEmpty()) {
            currentImageIndex = 0;
            loadMainImage(imageUrls.get(0));

            if (imageUrls.size() > 1) {
                setupImageNavigation();
                createThumbnails();
            }
        } else {
            setDefaultMainImage();
        }

        updateImageNavigation();
    }

    private void loadMainImage(String imagePath) {
        imageService.fetchImage(imagePath)
                .thenAccept(image -> Platform.runLater(() -> {
                    if (image != null && !image.isError()) {
                        mainImage.setImage(image);
                    } else {
                        setDefaultMainImage();
                    }
                }));
    }

    private void setDefaultMainImage() {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            mainImage.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default main image: " + e.getMessage());
        }
    }

    private void setupImageNavigation() {
        imageNavigation.setVisible(imageUrls.size() > 1);
        updateImageCounter();
    }

    private void updateImageNavigation() {
        if (imageUrls.size() > 1) {
            prevImageButton.setDisable(currentImageIndex == 0);
            nextImageButton.setDisable(currentImageIndex == imageUrls.size() - 1);
            updateImageCounter();
        }
    }

    private void updateImageCounter() {
        if (!imageUrls.isEmpty()) {
            imageCounter.setText((currentImageIndex + 1) + " / " + imageUrls.size());
        }
    }

    private void createThumbnails() {
        thumbnailContainer.getChildren().clear();

        for (int i = 0; i < imageUrls.size(); i++) {
            final int index = i;
            ImageView thumbnail = new ImageView();
            thumbnail.setFitHeight(60);
            thumbnail.setFitWidth(60);
            thumbnail.setPreserveRatio(true);
            thumbnail.getStyleClass().add("thumbnail");

            // Load thumbnail image
            imageService.fetchImage(imageUrls.get(i))
                    .thenAccept(image -> Platform.runLater(() -> {
                        if (image != null && !image.isError()) {
                            thumbnail.setImage(image);
                        }
                    }));

            // Add click handler
            thumbnail.setOnMouseClicked(e -> {
                currentImageIndex = index;
                loadMainImage(imageUrls.get(index));
                updateImageNavigation();
                updateThumbnailSelection();
            });

            thumbnailContainer.getChildren().add(thumbnail);
        }

        thumbnailScrollPane.setVisible(true);
        updateThumbnailSelection();
    }

    private void updateThumbnailSelection() {
        for (int i = 0; i < thumbnailContainer.getChildren().size(); i++) {
            ImageView thumbnail = (ImageView) thumbnailContainer.getChildren().get(i);
            if (i == currentImageIndex) {
                thumbnail.getStyleClass().add("selected-thumbnail");
            } else {
                thumbnail.getStyleClass().remove("selected-thumbnail");
            }
        }
    }

    private void setupActionButtons() {
        // Hide all action sections first
        sellActions.setVisible(false);
        tradeActions.setVisible(false);
        giftActions.setVisible(false);
        auctionActions.setVisible(false);

        // Show appropriate actions based on listing type
        String listingType = currentListing.getListingTypeValue();
        switch (listingType.toUpperCase()) {
            case "SELL":
                sellActions.setVisible(true);
                break;
            case "TRADE":
                tradeActions.setVisible(true);
                break;
            case "GIFT":
                giftActions.setVisible(true);
                break;
            case "AUCTION":
                auctionActions.setVisible(true);
                break;
        }

        // Disable actions if user is the owner
        boolean isOwner = currentListing.getUser() != null &&
                sessionService.getUser() != null &&
                currentListing.getUser().getId().equals(sessionService.getUser().getId());

        if (isOwner) {
            disableAllActionButtons();
        }
    }

    private void disableAllActionButtons() {
        buyNowButton.setDisable(true);
        makeOfferButton.setDisable(true);
        proposeTradeButton.setDisable(true);
        requestGiftButton.setDisable(true);
        placeBidButton.setDisable(true);

        buyNowButton.setText("La tua inserzione");
        makeOfferButton.setText("La tua inserzione");
        proposeTradeButton.setText("La tua inserzione");
        requestGiftButton.setText("La tua inserzione");
        placeBidButton.setText("La tua inserzione");
    }

    private void initializeFavoriteStatus() {
        if (currentListing != null) {
            isFavorite = favoritesService.isFavoriteListing(currentListing.getId());
            updateFavoriteIcon();
        }
    }

    private void updateFavoriteIcon() {
        String iconPath = isFavorite ? "/images/icons/favorites_add.png" : "/images/icons/favorites_remove.png";
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            favoriteIcon.setImage(icon);
        } catch (Exception e) {
            System.err.println("Could not load favorite icon: " + e.getMessage());
        }
    }

    // Event Handlers
    @FXML
    private void handleBack() {
        navigationService.goBack();
    }

    @FXML
    private void toggleFavorite() {
        if (currentListing != null) {
            // Optimistic UI update
            boolean newFavoriteState = !isFavorite;
            isFavorite = newFavoriteState;
            updateFavoriteIcon();

            // Sync with server
            if (newFavoriteState) {
                favoritesService.addFavoriteToServer(currentListing.getId())
                        .thenAccept(favoriteViewModel -> Platform.runLater(() -> {
                            // Server sync successful - observable lists updated via message handler
                            System.out.println("Successfully added listing to favorites: " + currentListing.getId());
                        }))
                        .exceptionally(ex -> {
                            // Revert UI on failure
                            isFavorite = false;
                            updateFavoriteIcon();
                            System.err.println("Failed to add to favorites: " + ex.getMessage());
                            return null;
                        });
            } else {
                favoritesService.removeFavoriteFromServer(currentListing.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            // Server sync successful - observable lists updated via message handler
                            System.out
                                    .println("Successfully removed listing from favorites: " + currentListing.getId());
                        }))
                        .exceptionally(ex -> {
                            // Revert UI on failure
                            isFavorite = true;
                            updateFavoriteIcon();
                            System.err.println("Failed to remove from favorites: " + ex.getMessage());
                            return null;
                        });
            }
        }
    }

    @FXML
    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            loadMainImage(imageUrls.get(currentImageIndex));
            updateImageNavigation();
            updateThumbnailSelection();
        }
    }

    @FXML
    private void showNextImage() {
        if (currentImageIndex < imageUrls.size() - 1) {
            currentImageIndex++;
            loadMainImage(imageUrls.get(currentImageIndex));
            updateImageNavigation();
            updateThumbnailSelection();
        }
    }

    // Action button handlers
    @FXML
    private void handleBuyNow() {
        if (currentListing instanceof SellListingViewModel) {
            SellListingViewModel sellListing = (SellListingViewModel) currentListing;

            String message = String.format("Confermi l'acquisto di '%s' per %s %s?",
                    currentListing.getTitle(),
                    sellListing.getCurrency().getSymbol(),
                    sellListing.getPrice());

            boolean confirmed = AlertHelper.showConfirmationAlert(
                    "Conferma acquisto",
                    "Acquisto diretto",
                    message);

            if (confirmed) {
                // TODO: Implement purchase logic
                AlertHelper.showInformationAlert(
                        "Acquisto confermato",
                        "Il tuo acquisto è stato registrato",
                        "Riceverai presto i dettagli per il pagamento e la consegna.");
            }
        }
    }

    @FXML
    private void handleMakeOffer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OfferDialogView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            DialogPane dialogPane = loader.load();

            ButtonType confirmButtonType = new ButtonType(
                    localeService.getMessage("offer.dialog.button.send"),
                    ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(
                    localeService.getMessage("offer.dialog.button.cancel"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

            OfferDialogController controller = loader.getController();
            controller.setListing(currentListing);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(localeService.getMessage("offer.dialog.title"));
            dialog.setHeaderText(localeService.getMessage("offer.dialog.header", currentListing.getTitle()));
            dialog.setDialogPane(dialogPane);

            // Enable/disable confirm button based on offer validity
            Button confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            confirmButton.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(
                            () -> !controller.isValidOffer(),
                            controller.getIncludeMoneyCheckBox().selectedProperty(),
                            controller.getMoneyAmountField().textProperty(),
                            controller.getSelectedItems()));

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
                // Now using the ViewModel-based service
                controller.createOffer()
                        .thenAccept(success -> Platform.runLater(() -> {
                            if (success) {
                                AlertHelper.showInformationAlert(
                                        localeService.getMessage("offer.success.title"),
                                        localeService.getMessage("offer.success.header"),
                                        localeService.getMessage("offer.success.message"));
                            } else {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("offer.error.title"),
                                        localeService.getMessage("offer.error.header"),
                                        localeService.getMessage("offer.error.connection"));
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> AlertHelper.showErrorAlert(
                                    localeService.getMessage("offer.error.title"),
                                    localeService.getMessage("offer.error.header"),
                                    ex.getMessage()));
                            return null;
                        });
            }

            // Clean up temporary reservations
            controller.cleanup();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("offer.error.title"),
                    localeService.getMessage("offer.error.header"),
                    e.getMessage());
        }
    }

    @FXML
    private void handleProposeTrade() {
        // Only allow for trade listings
        if (!(currentListing instanceof TradeListingViewModel)) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("trade.propose.error.title"),
                    localeService.getMessage("trade.propose.error.header"),
                    localeService.getMessage("trade.propose.error.notrade"));
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OfferDialogView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            DialogPane dialogPane = loader.load();

            ButtonType confirmButtonType = new ButtonType(
                    localeService.getMessage("trade.dialog.button.send", "Invia proposta"),
                    ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(
                    localeService.getMessage("trade.dialog.button.cancel", "Annulla"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

            OfferDialogController controller = loader.getController();
            controller.setListing(currentListing);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(localeService.getMessage("trade.dialog.title", "Proponi scambio"));
            dialog.setHeaderText(localeService.getMessage("trade.dialog.header",
                    "Proponi uno scambio per: " + currentListing.getTitle()));
            dialog.setDialogPane(dialogPane);

            // Enable/disable confirm button based on offer validity
            Button confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            confirmButton.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(
                            () -> !controller.isValidOffer(),
                            controller.getIncludeMoneyCheckBox().selectedProperty(),
                            controller.getMoneyAmountField().textProperty(),
                            controller.getSelectedItems()));

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
                // Now using the ViewModel-based service - same pattern as handleMakeOffer
                controller.createOffer()
                        .thenAccept(success -> Platform.runLater(() -> {
                            if (success) {
                                AlertHelper.showInformationAlert(
                                        localeService.getMessage("trade.success.title", "Proposta inviata"),
                                        localeService.getMessage("trade.success.header",
                                                "La tua proposta di scambio è stata inviata"),
                                        localeService.getMessage("trade.success.message",
                                                "Il proprietario riceverà la tua proposta e ti risponderà presto."));
                            } else {
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("trade.error.title", "Errore"),
                                        localeService.getMessage("trade.error.header",
                                                "Impossibile inviare la proposta"),
                                        localeService.getMessage("trade.error.connection",
                                                "Errore di connessione durante l'invio della proposta"));
                            }
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> AlertHelper.showErrorAlert(
                                    localeService.getMessage("trade.error.title", "Errore"),
                                    localeService.getMessage("trade.error.header",
                                            "Impossibile inviare la proposta"),
                                    ex.getMessage()));
                            return null;
                        });
            }

            // Clean up temporary reservations
            controller.cleanup();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("trade.error.title", "Errore"),
                    localeService.getMessage("trade.error.header", "Impossibile aprire la finestra di proposta"),
                    e.getMessage());
        }
    }

    @FXML
    private void handleRequestGift() {
        if (currentListing instanceof GiftListingViewModel) {
            boolean offerThankYou = offerThankYouCheckBox.isSelected();
            String thankYouMessage = offerThankYou ? thankYouMessageArea.getText() : "";

            String message = "Confermi la richiesta per il regalo '" + currentListing.getTitle() + "'?";
            if (offerThankYou) {
                message += "\n\nMessaggio di ringraziamento incluso.";
            }

            boolean confirmed = AlertHelper.showConfirmationAlert(
                    "Conferma richiesta regalo",
                    "Richiesta regalo",
                    message);

            if (confirmed) {
                // TODO: Implement gift request logic
                AlertHelper.showInformationAlert(
                        "Richiesta inviata",
                        "La tua richiesta è stata inviata",
                        "Il donatore riceverà la tua richiesta e ti risponderà presto.");
            }
        }
    }

    @FXML
    private void handlePlaceBid() {
        if (currentListing instanceof AuctionListingViewModel) {
            AuctionListingViewModel auction = (AuctionListingViewModel) currentListing;

            try {
                BigDecimal bidAmount = new BigDecimal(bidAmountField.getText().trim());
                BigDecimal minimumBid = auction.getMinimumNextBid();

                if (bidAmount.compareTo(minimumBid) < 0) {
                    AlertHelper.showWarningAlert(
                            "Offerta non valida",
                            "Importo troppo basso",
                            "L'offerta deve essere almeno " +
                                    auction.getCurrency().getSymbol() + " " + minimumBid);
                    return;
                }

                boolean confirmed = AlertHelper.showConfirmationAlert(
                        "Conferma offerta",
                        "Offerta all'asta",
                        String.format("Confermi l'offerta di %s %s per '%s'?",
                                bidCurrencyComboBox.getValue().getSymbol(),
                                bidAmount,
                                currentListing.getTitle()));

                if (confirmed) {
                    // TODO: Implement bid placement logic
                    AlertHelper.showInformationAlert(
                            "Offerta piazzata",
                            "La tua offerta è stata registrata",
                            "Ti aggiorneremo se qualcuno supera la tua offerta.");

                    // Clear bid field
                    bidAmountField.clear();
                }

            } catch (NumberFormatException e) {
                AlertHelper.showWarningAlert(
                        "Offerta non valida",
                        "Formato importo errato",
                        "Inserisci un importo valido.");
            }
        }
    }

    @FXML
    private void handleContactSeller() {
        // TODO: Open chat/message dialog
        AlertHelper.showInformationAlert(
                "Contatta venditore",
                "Funzionalità in sviluppo",
                "La messaggistica diretta sarà disponibile presto.");
    }

    @FXML
    private void handleReportListing() {
        if (currentListing != null) {
            navigationService.openReportDialog(currentListing, (Stage) backButton.getScene().getWindow());
        }
    }

    @FXML
    private void handleReportUser() {
        if (currentListing != null && currentListing.getUser() != null) {
            UserViewModel userViewModel = currentListing.getUser();
            navigationService.openReportDialog(userViewModel, (Stage) backButton.getScene().getWindow());
        }
    }
}