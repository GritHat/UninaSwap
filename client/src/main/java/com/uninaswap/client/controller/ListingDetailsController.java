package com.uninaswap.client.controller;
import com.uninaswap.client.service.*;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.AuctionListingViewModel;
import com.uninaswap.client.viewmodel.GiftListingViewModel;
import com.uninaswap.client.viewmodel.ItemViewModel;
import com.uninaswap.client.viewmodel.ListingItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.OfferViewModel;
import com.uninaswap.client.viewmodel.SellListingViewModel;
import com.uninaswap.client.viewmodel.TradeListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import com.uninaswap.common.enums.Currency;
import com.uninaswap.common.enums.DeliveryType;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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

/**
 * 
 */
public class ListingDetailsController {
    /**
     * 
     */
    @FXML
    private Button backButton;
    /**
     * 
     */
    @FXML
    private Button favoriteButton;
    /**
     * 
     */
    @FXML
    private ImageView favoriteIcon;
    /**
     * 
     */
    @FXML
    private ImageView mainImage;
    /**
     * 
     */
    @FXML
    private HBox imageNavigation;
    /**
     * 
     */
    @FXML
    private Button prevImageButton;
    /**
     * 
     */
    @FXML
    private Button nextImageButton;
    /**
     * 
     */
    @FXML
    private Label imageCounter;
    /**
     * 
     */
    @FXML
    private ScrollPane thumbnailScrollPane;
    /**
     * 
     */
    @FXML
    private HBox thumbnailContainer;
    /**
     * 
     */
    @FXML
    private Text listingTitle;
    /**
     * 
     */
    @FXML
    private Label categoryLabel;
    /**
     * 
     */
    @FXML
    private Label statusLabel;
    /**
     * 
     */
    @FXML
    private TextArea descriptionArea;
    /**
     * 
     */
    @FXML
    private VBox itemsSection;
    /**
     * 
     */
    @FXML
    private VBox itemsList;
    /**
     * 
     */
    @FXML
    private ImageView sellerAvatar;
    /**
     * 
     */
    @FXML
    private Text sellerName;
    /**
     * 
     */
    @FXML
    private Text sellerRating;
    /**
     * 
     */
    @FXML
    private Text priceValue;
    /**
     * 
     */
    @FXML
    private Text priceDetails;
    /**
     * 
     */
    @FXML
    private VBox actionButtonsSection;
    /**
     * 
     */
    @FXML
    private VBox sellActions;
    /**
     * 
     */
    @FXML
    private Button buyNowButton;
    /**
     * 
     */
    @FXML
    private Button makeOfferButton;
    /**
     * 
     */
    @FXML
    private VBox tradeActions;
    /**
     * 
     */
    @FXML
    private Button proposeTradeButton;
    /**
     * 
     */
    @FXML
    private VBox tradeOptionsSection;
    /**
     * 
     */
    @FXML
    private CheckBox includeMoneyCheckBox;
    /**
     * 
     */
    @FXML
    private HBox moneyOfferSection;
    /**
     * 
     */
    @FXML
    private TextField moneyAmountField;
    /**
     * 
     */
    @FXML
    private ComboBox<Currency> currencyComboBox;
    /**
     * 
     */
    @FXML
    private VBox giftActions;
    /**
     * 
     */
    @FXML
    private Button requestGiftButton;
    /**
     * 
     */
    @FXML
    private VBox thankYouSection;
    /**
     * 
     */
    @FXML
    private CheckBox offerThankYouCheckBox;
    /**
     * 
     */
    @FXML
    private TextArea thankYouMessageArea;
    /**
     * 
     */
    @FXML
    private VBox auctionActions;
    /**
     * 
     */
    @FXML
    private Text currentBidValue;
    /**
     * 
     */
    @FXML
    private Text timeRemainingLabel;
    /**
     * 
     */
    @FXML
    private TextField bidAmountField;
    /**
     * 
     */
    @FXML
    private ComboBox<Currency> bidCurrencyComboBox;
    /**
     * 
     */
    @FXML
    private Text minimumBidLabel;
    /**
     * 
     */
    @FXML
    private Button placeBidButton;
    /**
     * 
     */
    @FXML
    private Button contactSellerButton;
    /**
     * 
     */
    @FXML
    private Button reportListingButton;
    /**
     * 
     */
    @FXML
    private HBox deliveryOptionsSection;
    /**
     * 
     */
    @FXML
    private Label pickupLocationLabel;
    /**
     * 
     */
    @FXML
    private ComboBox<DeliveryType> deliveryMethodComboBox;

    /**
     * 
     */
    private final NavigationService navigationService = NavigationService.getInstance();
    /**
     * 
     */
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    /**
     * 
     */
    private final ImageService imageService = ImageService.getInstance();
    /**
     * 
     */
    private final LocaleService localeService = LocaleService.getInstance();
    /**
     * 
     */
    private final UserSessionService sessionService = UserSessionService.getInstance();
    /**
     * 
     */
    private ListingViewModel currentListing;
    /**
     * 
     */
    private List<String> imageUrls = new ArrayList<>();
    /**
     * 
     */
    private int currentImageIndex = 0;
    /**
     * 
     */
    private boolean isFavorite = false;

    /**
     * 
     */
    @FXML
    public void initialize() {
        setupCurrencyComboBoxes();
        setupDeliveryMethodComboBox();
        setupEventHandlers();
    }

    /**
     * 
     */
    private void setupCurrencyComboBoxes() {
        List<Currency> currencies = List.of(Currency.EUR, Currency.USD, Currency.GBP);
        currencyComboBox.setItems(FXCollections.observableArrayList(currencies));
        bidCurrencyComboBox.setItems(FXCollections.observableArrayList(currencies));
        currencyComboBox.setValue(Currency.EUR);
        bidCurrencyComboBox.setValue(Currency.EUR);
    }

    /**
     * 
     */
    private void setupDeliveryMethodComboBox() {
        deliveryMethodComboBox.setItems(FXCollections.observableArrayList(
            DeliveryType.PICKUP,
            DeliveryType.SHIPPING,
            DeliveryType.BOTH
        ));
        deliveryMethodComboBox.setValue(DeliveryType.PICKUP);
    }

    /**
     * 
     */
    private void setupEventHandlers() {
        includeMoneyCheckBox.selectedProperty().addListener((_, _, newVal) -> {
            setVisibleAndManaged(moneyOfferSection, newVal);
        });
        offerThankYouCheckBox.selectedProperty().addListener((_, _, newVal) -> {
            setVisibleAndManaged(thankYouMessageArea, newVal);
        });
    }

    /**
     * @param listing
     */
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

    /**
     * 
     */
    private void populateListingDetails() {
        listingTitle.setText(currentListing.getTitle());
        descriptionArea.setText(currentListing.getDescription());
        categoryLabel.setText(getListingCategory());
        statusLabel.setText(currentListing.getStatus().toString());
        if (currentListing.getUser() != null) {
            sellerName.setText(currentListing.getUser().getUsername());
            UserViewModel seller = currentListing.getUser();
            if (seller.getReviewCount() > 0) {
                sellerRating.setText(seller.getFormattedRating());
            } else {
                sellerRating.setText("Nuovo venditore");
            }
            loadSellerAvatar(seller);
            setupSellerAvatarClickHandler();
        }
        setupPriceSection();
        populateItemsList();
        setupDeliveryOptions();
    }

    /**
     * 
     */
    private void setupDeliveryOptions() {
        String pickupLocation = getPickupLocationFromListing();
        
        if (pickupLocation != null && !pickupLocation.trim().isEmpty()) {
            setVisibleAndManaged(deliveryOptionsSection, true);
            pickupLocationLabel.setText(pickupLocation);
            setupDeliveryMethodForListing();
        } else {
            setVisibleAndManaged(deliveryOptionsSection, false);
        }
    }

    /**
     * @return
     */
    private String getPickupLocationFromListing() {
        if (currentListing == null) {
            return null;
        }
        String pickupLocation = currentListing.getPickupLocation();
        if (pickupLocation == null || pickupLocation.trim().isEmpty()) {
            String listingType = currentListing.getListingTypeValue();
            switch (listingType.toUpperCase()) {
                case "SELL":
                    if (currentListing instanceof SellListingViewModel sellListing) {
                        pickupLocation = sellListing.getPickupLocation();
                    }
                    break;
                case "TRADE":
                    if (currentListing instanceof TradeListingViewModel tradeListing) {
                        pickupLocation = tradeListing.getPickupLocation();
                    }
                    break;
                case "GIFT":
                    if (currentListing instanceof GiftListingViewModel giftListing) {
                        pickupLocation = giftListing.getPickupLocation();
                    }
                    break;
                case "AUCTION":
                    if (currentListing instanceof AuctionListingViewModel auctionListing) {
                        pickupLocation = auctionListing.getPickupLocation();
                    }
                    break;
            }
        }
        
        return pickupLocation;
    }

    /**
     * 
     */
    @FXML
    private void handleDeliveryMethodChange() {
        DeliveryType selectedMethod = deliveryMethodComboBox.getValue();
        if (selectedMethod != null) {
            System.out.println("Selected delivery method: " + selectedMethod);
        }
    }
    /**
     * 
     */
    private void setupDeliveryMethodForListing() {
        if (currentListing == null) {
            return;
        }
        boolean supportsShipping = doesListingSupportShipping();
        boolean isPickupOnly = isListingPickupOnly();
        if (isPickupOnly) {
            deliveryMethodComboBox.setItems(FXCollections.observableArrayList(
                DeliveryType.PICKUP
            ));
            deliveryMethodComboBox.setValue(DeliveryType.PICKUP);
            deliveryMethodComboBox.setDisable(true);
        } else if (supportsShipping) {
            deliveryMethodComboBox.setItems(FXCollections.observableArrayList(
                DeliveryType.PICKUP,
                DeliveryType.SHIPPING,
                DeliveryType.BOTH
            ));
            deliveryMethodComboBox.setValue(DeliveryType.PICKUP);
            deliveryMethodComboBox.setDisable(false);
        } else {
            deliveryMethodComboBox.setItems(FXCollections.observableArrayList(
                DeliveryType.PICKUP
            ));
            deliveryMethodComboBox.setValue(DeliveryType.PICKUP);
            deliveryMethodComboBox.setDisable(true);
        }
    }

    /**
     * @return
     */
    private boolean doesListingSupportShipping() {
        if (currentListing == null) {
            return false;
        }
        
        String listingType = currentListing.getListingTypeValue();
        
        switch (listingType.toUpperCase()) {
            case "SELL":
                return true;
            case "AUCTION":
                return true;
            case "TRADE":
                return false;
            case "GIFT":
                if (currentListing instanceof GiftListingViewModel giftListing) {
                    return !giftListing.isPickupOnly();
                }
                return false;
            default:
                return false;
        }
    }

    /**
     * @return
     */
    private boolean isListingPickupOnly() {
        if (currentListing == null) {
            return true;
        }
        
        String listingType = currentListing.getListingTypeValue();
        
        if ("GIFT".equals(listingType.toUpperCase()) && currentListing instanceof GiftListingViewModel giftListing) {
            return giftListing.isPickupOnly();
        }
        return false;
    }


    /**
     * @param seller
     */
    private void loadSellerAvatar(UserViewModel seller) {
        String profileImagePath = seller.getProfileImagePath();
        
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            imageService.fetchImage(profileImagePath)
                    .thenAccept(image -> Platform.runLater(() -> {
                        if (image != null && !image.isError()) {
                            sellerAvatar.setImage(image);
                        } else {
                            setDefaultSellerAvatar();
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            setDefaultSellerAvatar();
                            System.err.println("Failed to load seller avatar: " + ex.getMessage());
                        });
                        return null;
                    });
        } else {
            setDefaultSellerAvatar();
        }
    }
 
    /**
     * 
     */
    private void setDefaultSellerAvatar() {
        try {
            Image defaultAvatar = new Image(getClass()
                    .getResourceAsStream("/images/icons/default_profile.png"));
            sellerAvatar.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Could not load default seller avatar: " + e.getMessage());
        }
    }

    /**
     * @return
     */
    private String getListingCategory() {
        if (currentListing.getItems() != null && !currentListing.getItems().isEmpty()) {
            String itemCategory = currentListing.getItems().get(0).getItem().getItemCategory();
            if (itemCategory != null && !itemCategory.isEmpty()) {
                return itemCategory;
            }
        }
        return currentListing.getListingTypeValue();
    }

    /**
     * 
     */
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

    /**
     * @param sellListing
     */
    private void setupSellPricing(SellListingViewModel sellListing) {
        String currency = sellListing.getCurrency() != null ? sellListing.getCurrency().getSymbol() : "€";
        priceValue.setText(currency + " " + sellListing.getPrice());
        priceDetails.setText("Prezzo fisso");
    }

    /**
     * @param tradeListing
     */
    private void setupTradePricing(TradeListingViewModel tradeListing) {
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

    /**
     * @param giftListing
     */
    private void setupGiftPricing(GiftListingViewModel giftListing) {
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

    /**
     * @param auctionListing
     */
    private void setupAuctionPricing(AuctionListingViewModel auctionListing) {
        BigDecimal currentBid = auctionListing.getHighestBid();
        String currency = auctionListing.getCurrency() != null ? auctionListing.getCurrency().getSymbol() : "€";

        if (currentBid != null && currentBid.compareTo(BigDecimal.ZERO) > 0) {
            priceValue.setText(currency + " " + currentBid);
            priceDetails.setText("Offerta attuale");
        } else {
            priceValue.setText(currency + " " + auctionListing.getStartingPrice());
            priceDetails.setText("Prezzo di partenza");
        }
        currentBidValue.setText(currency + " " +
                (currentBid != null ? currentBid : auctionListing.getStartingPrice()));
        BigDecimal minimumBid = auctionListing.getMinimumNextBid();
        minimumBidLabel.setText("Offerta minima: " + currency + " " + minimumBid);
        updateTimeRemaining(auctionListing);
    }

    /**
     * @param auction
     */
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

    /**
     * 
     */
    private void populateItemsList() {
        itemsList.getChildren().clear();

        if (currentListing.getItems() != null) {
            for (ListingItemViewModel item : currentListing.getItems()) {
                VBox itemRow = createExpandableItemRow(item);
                itemsList.getChildren().add(itemRow);
            }
        }
    }

    /**
     * @param item
     * @return
     */
    private VBox createItemDetailsSection(ListingItemViewModel item) {
        ItemViewModel itemData = item.getItem();
        boolean hasDescription = itemData.getDescription() != null && !itemData.getDescription().trim().isEmpty();
        boolean hasBrand = itemData.getBrand() != null && !itemData.getBrand().trim().isEmpty();
        boolean hasModel = itemData.getModel() != null && !itemData.getModel().trim().isEmpty();
        boolean hasYear = itemData.getYear() != null && itemData.getYear() > 0;

        if (!hasDescription && !hasBrand && !hasModel && !hasYear) {
            return null;
        }
        
        VBox detailsContainer = new VBox(5);
        detailsContainer.getStyleClass().add("item-details-section");
        VBox productInfo = new VBox(3);
        productInfo.getStyleClass().add("product-info-grid");
        if (hasBrand || hasModel) {
            HBox brandModelRow = new HBox(15);
            brandModelRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            if (hasBrand) {
                VBox brandBox = new VBox(2);
                Label brandLabel = new Label("Marca:");
                brandLabel.getStyleClass().add("detail-label");
                Text brandValue = new Text(itemData.getBrand());
                brandValue.getStyleClass().add("detail-value");
                brandBox.getChildren().addAll(brandLabel, brandValue);
                brandModelRow.getChildren().add(brandBox);
            }
            
            if (hasModel) {
                VBox modelBox = new VBox(2);
                Label modelLabel = new Label("Modello:");
                modelLabel.getStyleClass().add("detail-label");
                Text modelValue = new Text(itemData.getModel());
                modelValue.getStyleClass().add("detail-value");
                modelBox.getChildren().addAll(modelLabel, modelValue);
                brandModelRow.getChildren().add(modelBox);
            }
            
            productInfo.getChildren().add(brandModelRow);
        }

        if (hasYear) {
            HBox yearRow = new HBox(10);
            yearRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            VBox yearBox = new VBox(2);
            Label yearLabel = new Label("Anno:");
            yearLabel.getStyleClass().add("detail-label");
            Text yearValue = new Text(String.valueOf(itemData.getYear()));
            yearValue.getStyleClass().add("detail-value");
            yearBox.getChildren().addAll(yearLabel, yearValue);
            yearRow.getChildren().add(yearBox);
            
            productInfo.getChildren().add(yearRow);
        }
        
        if (!productInfo.getChildren().isEmpty()) {
            detailsContainer.getChildren().add(productInfo);
        }
        
        if (hasDescription) {
            VBox descriptionBox = new VBox(5);
            descriptionBox.getStyleClass().add("item-description-section");
            
            Label descLabel = new Label("Descrizione:");
            descLabel.getStyleClass().add("detail-label");
            
            Text descriptionText = new Text(itemData.getDescription());
            descriptionText.getStyleClass().add("item-description-text");
            descriptionText.setWrappingWidth(250);
            
            descriptionBox.getChildren().addAll(descLabel, descriptionText);
            detailsContainer.getChildren().add(descriptionBox);
        }
        
        return detailsContainer;
    }

    /**
     * @param imageView
     */
    private void setDefaultItemImage(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default item image: " + e.getMessage());
        }
    }

    /**
     * 
     */
    private void setupImageGallery() {
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
            } else {
                setVisibleAndManaged(imageNavigation, false);
                setVisibleAndManaged(thumbnailScrollPane, false);
            }
        } else {
            setDefaultMainImage();
            setVisibleAndManaged(imageNavigation, false);
            setVisibleAndManaged(thumbnailScrollPane, false);
        }

        updateImageNavigation();
    }

    /**
     * @param imagePath
     */
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

    /**
     * 
     */
    private void setDefaultMainImage() {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            mainImage.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("Could not load default main image: " + e.getMessage());
        }
    }

    /**
     * Helper method to set both visible and managed properties together
     * 
     * @param node The node to modify
     * @param visible Whether the node should be visible and managed
     */
    /**
     * @param node
     * @param visible
     */
    private void setVisibleAndManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Helper method to set multiple nodes visible and managed together
     * 
     * @param visible Whether the nodes should be visible and managed
     * @param nodes The nodes to modify
     */
    /**
     * @param visible
     * @param nodes
     */
    private void setVisibleAndManaged(boolean visible, Node... nodes) {
        for (Node node : nodes) {
            setVisibleAndManaged(node, visible);
        }
    }

    /**
     * 
     */
    private void setupImageNavigation() {
        boolean hasMultipleImages = imageUrls.size() > 1;
        setVisibleAndManaged(imageNavigation, hasMultipleImages);

        if (hasMultipleImages) {
            updateImageCounter();
        }
    }

    /**
     * 
     */
    private void updateImageNavigation() {
        if (imageUrls.size() > 1) {
            prevImageButton.setDisable(currentImageIndex == 0);
            nextImageButton.setDisable(currentImageIndex == imageUrls.size() - 1);
            updateImageCounter();
        }
    }

    /**
     * 
     */
    private void updateImageCounter() {
        if (!imageUrls.isEmpty()) {
            imageCounter.setText((currentImageIndex + 1) + " / " + imageUrls.size());
        }
    }

    /**
     * 
     */
    private void createThumbnails() {
        thumbnailContainer.getChildren().clear();

        boolean hasMultipleImages = imageUrls.size() > 1;
        setVisibleAndManaged(thumbnailScrollPane, hasMultipleImages);

        if (!hasMultipleImages) {
            return;
        }

        for (int i = 0; i < imageUrls.size(); i++) {
            final int index = i;
            ImageView thumbnail = new ImageView();
            thumbnail.setFitHeight(60);
            thumbnail.setFitWidth(60);
            thumbnail.setPreserveRatio(true);
            thumbnail.getStyleClass().add("thumbnail");
            imageService.fetchImage(imageUrls.get(i))
                    .thenAccept(image -> Platform.runLater(() -> {
                        if (image != null && !image.isError()) {
                            thumbnail.setImage(image);
                        }
                    }));
            thumbnail.setOnMouseClicked(_ -> {
                currentImageIndex = index;
                loadMainImage(imageUrls.get(index));
                updateImageNavigation();
                updateThumbnailSelection();
            });

            thumbnailContainer.getChildren().add(thumbnail);
        }

        updateThumbnailSelection();
    }

    /**
     * 
     */
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

    /**
     * 
     */
    private void setupActionButtons() {
        setVisibleAndManaged(false, sellActions, tradeActions, giftActions, auctionActions);
        setVisibleAndManaged(tradeOptionsSection, false);
        setVisibleAndManaged(moneyOfferSection, false);
        setVisibleAndManaged(thankYouMessageArea, false);
        boolean isOwner = currentListing.getUser() != null &&
                sessionService.getUser() != null &&
                currentListing.getUser().getId().equals(sessionService.getUser().getId());
        String listingType = currentListing.getListingTypeValue();
        switch (listingType.toUpperCase()) {
            case "SELL":
                setVisibleAndManaged(sellActions, true);
                if (isOwner) {
                    setVisibleAndManaged(makeOfferButton, false);
                    buyNowButton.setDisable(true);
                    buyNowButton.setText("La tua inserzione");
                } else {
                    setVisibleAndManaged(makeOfferButton, true);
                    buyNowButton.setDisable(false);
                    buyNowButton.setText("Acquista ora");
                }
                break;
            case "TRADE":
                setVisibleAndManaged(tradeActions, true);
                if (isOwner) {
                    disableTradeActions();
                }
                break;
            case "GIFT":
                setVisibleAndManaged(giftActions, true);
                if (isOwner) {
                    disableGiftActions();
                }
                break;
            case "AUCTION":
                setVisibleAndManaged(auctionActions, true);
                if (isOwner) {
                    disableAuctionActions();
                }
                break;
        }
    }

    /**
     * 
     */
    private void disableTradeActions() {
        proposeTradeButton.setDisable(true);
        proposeTradeButton.setText("La tua inserzione");
    }

    /**
     * 
     */
    private void disableGiftActions() {
        requestGiftButton.setDisable(true);
        requestGiftButton.setText("La tua inserzione");
    }

    /**
     * 
     */
    private void disableAuctionActions() {
        placeBidButton.setDisable(true);
        placeBidButton.setText("La tua inserzione");
    }

    /**
     * 
     */
    private void initializeFavoriteStatus() {
        if (currentListing != null) {
            isFavorite = favoritesService.isFavoriteListing(currentListing.getId());
            updateFavoriteIcon();
        }
    }

    /**
     * 
     */
    private void updateFavoriteIcon() {
        String iconPath = isFavorite ? "/images/icons/favorites_add.png" : "/images/icons/favorites_remove.png";
        try {
            Image icon = new Image(getClass().getResourceAsStream(iconPath));
            favoriteIcon.setImage(icon);
        } catch (Exception e) {
            System.err.println("Could not load favorite icon: " + e.getMessage());
        }
    }

    /**
     * 
     */
    private void setupSellerAvatarClickHandler() {
        if (sellerAvatar != null) {
            sellerAvatar.setOnMouseClicked(_ -> {
                if (currentListing != null && currentListing.getUser() != null) {
                    handleViewSellerProfile();
                }
            });
        }
    }

    /**
     * 
     */
    private void handleViewSellerProfile() {
        if (currentListing != null && currentListing.getUser() != null) {
            UserViewModel seller = currentListing.getUser();
            try {
                navigationService.navigateToProfileView(seller);
            } catch (Exception e) {
                System.err.println("Failed to open seller profile: " + e.getMessage());
                AlertHelper.showInformationAlert(
                    "Profilo Venditore",
                    seller.getDisplayName(),
                    "Rating: " + seller.getFormattedRating() + "\n" +
                    "Membro dal: " + (seller.getCreatedAt() != null ? seller.getCreatedAt().toLocalDate() : "N/A")
                );
            }
        }
    }

    /**
     * 
     */
    @FXML
    private void handleBack() {
        navigationService.goBack();
    }

    /**
     * 
     */
    @FXML
    private void toggleFavorite() {
        if (currentListing != null) {
            boolean newFavoriteState = !isFavorite;
            isFavorite = newFavoriteState;
            updateFavoriteIcon();
            if (newFavoriteState) {
                favoritesService.addFavoriteToServer(currentListing.getId())
                        .thenAccept(_ -> Platform.runLater(() -> {
                            System.out.println("Successfully added listing to favorites: " + currentListing.getId());
                        }))
                        .exceptionally(ex -> {
                            isFavorite = false;
                            updateFavoriteIcon();
                            System.err.println("Failed to add to favorites: " + ex.getMessage());
                            return null;
                        });
            } else {
                favoritesService.removeFavoriteFromServer(currentListing.getId())
                        .thenAccept(_ -> Platform.runLater(() -> {
                            System.out.println("Successfully removed listing from favorites: " + currentListing.getId());
                        }))
                        .exceptionally(ex -> {
                            isFavorite = true;
                            updateFavoriteIcon();
                            System.err.println("Failed to remove from favorites: " + ex.getMessage());
                            return null;
                        });
            }
        }
    }

    /**
     * 
     */
    @FXML
    private void showPreviousImage() {
        if (currentImageIndex > 0) {
            currentImageIndex--;
            loadMainImage(imageUrls.get(currentImageIndex));
            updateImageNavigation();
            updateThumbnailSelection();
        }
    }

    /**
     * 
     */
    @FXML
    private void showNextImage() {
        if (currentImageIndex < imageUrls.size() - 1) {
            currentImageIndex++;
            loadMainImage(imageUrls.get(currentImageIndex));
            updateImageNavigation();
            updateThumbnailSelection();
        }
    }

    /**
     * 
     */
    @FXML
    private void handleBuyNow() {
        if (!(currentListing instanceof SellListingViewModel)) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("buy.error.title", "Errore"),
                    localeService.getMessage("buy.error.header", "Tipo di inserzione non valido"),
                    localeService.getMessage("buy.error.message", "Questa funzione è disponibile solo per gli annunci di vendita."));
            return;
        }

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
            createBuyNowOffer(sellListing);
        }
    }

    /**
     * Creates a buy now offer with the full listing price
     * @param sellListing The sell listing to create an offer for
     */
    private void createBuyNowOffer(SellListingViewModel sellListing) {
        try {
            // Create an offer with the full listing price
            OfferViewModel offer = new OfferViewModel();
            offer.setListingId(sellListing.getId());
            offer.setAmount(sellListing.getPrice());
            offer.setCurrency(sellListing.getCurrency());
            offer.setMessage("Buy Now - Full Price Purchase");
            
            // Set delivery type based on listing and user selection
            offer.setDeliveryType(sellListing.getDeliveryType(
                    deliveryMethodComboBox != null ? deliveryMethodComboBox.getValue() : null));
            
            // Create the offer using the existing service
            OfferService.getInstance().createOffer(offer)
                    .thenAccept(model -> Platform.runLater(() -> {
                        if (model != null) {
                            String successMessage;
                            if (offer.getDeliveryType() == com.uninaswap.common.enums.DeliveryType.SHIPPING) {
                                successMessage = localeService.getMessage("buy.success.shipping.message",
                                        "Acquisto confermato! Il venditore preparerà la spedizione. " +
                                        "Riceverai una notifica quando dovrai confermare l'arrivo del prodotto.");
                            } else {
                                successMessage = localeService.getMessage("buy.success.pickup.message",
                                        "Acquisto confermato! Il venditore ti proporrà alcuni orari per il ritiro. " +
                                        "Riceverai una notifica per scegliere l'orario più conveniente.");
                            }
                            
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("buy.success.title", "Acquisto confermato"),
                                    localeService.getMessage("buy.success.header", "Il tuo acquisto è stato registrato"),
                                    successMessage);
                                    
                            // Navigate to offers view to show the created offer
                            try {
                                navigationService.navigateToOffersView();
                            } catch (Exception e) {
                                System.err.println("Failed to navigate to offers view: " + e.getMessage());
                                AlertHelper.showErrorAlert(
                                        localeService.getMessage("buy.error.navigation.title", "Errore di navigazione"),
                                        localeService.getMessage("buy.error.navigation.header", "Navigazione fallita"),
                                        localeService.getMessage("buy.error.navigation.message",
                                                "Si è verificato un errore durante la navigazione alla sezione offerte: " + e.getMessage()));
                            }
                        } else {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("buy.error.title", "Errore"),
                                    localeService.getMessage("buy.error.header", "Acquisto fallito"),
                                    localeService.getMessage("buy.error.accept.message",
                                            "L'offerta è stata creata ma non è stata possibile accettarla automaticamente. " +
                                            "Controlla la sezione offerte per maggiori dettagli."));
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("buy.error.title", "Errore"),
                                    localeService.getMessage("buy.error.header", "Errore durante l'acquisto"),
                                    localeService.getMessage("buy.error.connection.message",
                                            "Si è verificato un errore durante l'elaborazione dell'acquisto: " + ex.getMessage()));
                        });
                        return null;
                    });
                        
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("buy.error.title", "Errore"),
                    localeService.getMessage("buy.error.header", "Errore durante l'acquisto"),
                    localeService.getMessage("buy.error.unexpected.message",
                            "Si è verificato un errore imprevisto: " + e.getMessage()));
        }
    }

    /**
     * 
     */
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
            Button confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            confirmButton.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(
                            () -> !controller.isValidOffer(),
                            controller.getIncludeMoneyCheckBox().selectedProperty(),
                            controller.getMoneyAmountField().textProperty(),
                            controller.getSelectedItems()));

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
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
            controller.cleanup();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("offer.error.title"),
                    localeService.getMessage("offer.error.header"),
                    e.getMessage());
        }
    }

    /**
     * 
     */
    @FXML
    private void handleProposeTrade() {
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
            Button confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            confirmButton.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(
                            () -> !controller.isValidOffer(),
                            controller.getIncludeMoneyCheckBox().selectedProperty(),
                            controller.getMoneyAmountField().textProperty(),
                            controller.getSelectedItems()));

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
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
            controller.cleanup();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("trade.error.title", "Errore"),
                    localeService.getMessage("trade.error.header", "Impossibile aprire la finestra di proposta"),
                    e.getMessage());
        }
    }

    /**
     * 
     */
    @FXML
    private void handleRequestGift() {
        if (!(currentListing instanceof GiftListingViewModel)) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("gift.error.title", "Errore"),
                    localeService.getMessage("gift.error.header", "Tipo di inserzione non valido"),
                    localeService.getMessage("gift.error.message", "Questa funzione è disponibile solo per i regali."));
            return;
        }

        GiftListingViewModel giftListing = (GiftListingViewModel) currentListing;
        boolean offerThankYou = offerThankYouCheckBox.isSelected();

        if (offerThankYou && giftListing.isAllowThankYouOffers()) {
            // Show offer dialog to create thank-you offer
            openThankYouOfferDialog(giftListing);
        } else {
            // Go straight to confirmation and create empty gift request
            showGiftRequestConfirmation(giftListing, null);
        }
    }

    /**
     * Opens the offer dialog for creating a thank-you offer
     * @param giftListing The gift listing to create an offer for
     */
    private void openThankYouOfferDialog(GiftListingViewModel giftListing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OfferDialogView.fxml"));
            loader.setResources(localeService.getResourceBundle());
            DialogPane dialogPane = loader.load();

            ButtonType confirmButtonType = new ButtonType(
                    localeService.getMessage("gift.thankyou.button.send", "Invia ringraziamento"),
                    ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(
                    localeService.getMessage("gift.thankyou.button.cancel", "Annulla"),
                    ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(confirmButtonType, cancelButtonType);

            OfferDialogController controller = loader.getController();
            controller.setListing(currentListing);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(localeService.getMessage("gift.thankyou.dialog.title", "Offri un ringraziamento"));
            dialog.setHeaderText(localeService.getMessage("gift.thankyou.dialog.header",
                    "Cosa vuoi offrire come ringraziamento per: " + currentListing.getTitle()));
            dialog.setDialogPane(dialogPane);

            Button confirmButton = (Button) dialogPane.lookupButton(confirmButtonType);
            confirmButton.disableProperty().bind(
                    javafx.beans.binding.Bindings.createBooleanBinding(
                            () -> !controller.isValidOffer(),
                            controller.getIncludeMoneyCheckBox().selectedProperty(),
                            controller.getMoneyAmountField().textProperty(),
                            controller.getSelectedItems()));

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == confirmButtonType) {
                // Create the thank-you offer data
                OfferViewModel thankYouOffer = createThankYouOfferFromDialog(controller, giftListing);
                showGiftRequestConfirmation(giftListing, thankYouOffer);
            }
            
            controller.cleanup();

        } catch (IOException e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("gift.error.title", "Errore"),
                    localeService.getMessage("gift.error.header", "Impossibile aprire la finestra di ringraziamento"),
                    e.getMessage());
        }
    }

    /**
     * Creates a thank-you offer from the dialog data
     * @param controller The offer dialog controller
     * @param giftListing The gift listing
     * @return The created offer view model
     */
    @SuppressWarnings("unchecked")
    private OfferViewModel createThankYouOfferFromDialog(OfferDialogController controller, GiftListingViewModel giftListing) {
        OfferViewModel offer = new OfferViewModel();
        offer.setListingId(giftListing.getId());
        offer.setMessage("Gift Request - Thank You Offer");
        
        // Set delivery type based on gift listing
        offer.setDeliveryType(giftListing.getDeliveryType(
                deliveryMethodComboBox != null ? deliveryMethodComboBox.getValue() : null));
        
        // Add money component if selected
        if (controller.getIncludeMoneyCheckBox().isSelected() && 
            !controller.getMoneyAmountField().getText().trim().isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(controller.getMoneyAmountField().getText().trim());
                offer.setAmount(amount);
                offer.setCurrency(controller.getMoneyAmountField().getParent() != null ? 
                    ((ComboBox<Currency>) controller.getMoneyAmountField().getParent()
                        .lookup(".currency-combo")).getValue() : Currency.EUR);
            } catch (NumberFormatException e) {
                // Handle error - could log or set default
                System.err.println("Error parsing money amount: " + e.getMessage());
            }
        }
        
        // Add items if selected
        if (!controller.getSelectedItems().isEmpty()) {
            offer.getOfferItems().setAll(controller.getSelectedItems());
        }
        
        return offer;
    }

    /**
     * Shows confirmation dialog and creates the gift request
     * @param giftListing The gift listing
     * @param thankYouOffer Optional thank-you offer data
     */
    private void showGiftRequestConfirmation(GiftListingViewModel giftListing, OfferViewModel thankYouOffer) {
        String message = "Confermi la richiesta per il regalo '" + currentListing.getTitle() + "'?";
        
        if (thankYouOffer != null) {
            message += "\n\nRingraziamento incluso:";
            if (thankYouOffer.getAmount() != null) {
                message += "\n• Denaro: " + thankYouOffer.getCurrency().getSymbol() + " " + thankYouOffer.getAmount();
            }
            if (thankYouOffer.getOfferItems() != null && !thankYouOffer.getOfferItems().isEmpty()) {
                message += "\n• Oggetti: " + thankYouOffer.getOfferItems().size() + " elemento/i";
            }
        }

        boolean confirmed = AlertHelper.showConfirmationAlert(
                "Conferma richiesta regalo",
                "Richiesta regalo",
                message);

        if (confirmed) {
            createGiftRequest(giftListing, thankYouOffer);
        }
    }

    /**
     * Creates the actual gift request offer
     * @param giftListing The gift listing
     * @param thankYouOffer Optional thank-you offer data
     */
    private void createGiftRequest(GiftListingViewModel giftListing, OfferViewModel thankYouOffer) {
        try {
            OfferViewModel giftRequest;
            
            if (thankYouOffer != null) {
                // Use the thank-you offer data
                giftRequest = thankYouOffer;
            } else {
                // Create empty gift request
                giftRequest = new OfferViewModel();
                giftRequest.setListingId(giftListing.getId());
                giftRequest.setMessage("Gift Request - No Thank You Offer");
                giftRequest.setDeliveryType(giftListing.getDeliveryType(
                        deliveryMethodComboBox != null ? deliveryMethodComboBox.getValue() : null));
            }
            
            // Send the gift request to server
            OfferService.getInstance().createOffer(giftRequest)
                    .thenAccept(createdOffer -> Platform.runLater(() -> {
                        if (createdOffer != null) {
                            String successMessage;
                            if (giftRequest.getDeliveryType() == DeliveryType.SHIPPING) {
                                successMessage = localeService.getMessage("gift.success.shipping.message",
                                        "Richiesta regalo inviata! Il donatore ti contatterà per organizzare la spedizione.");
                            } else {
                                successMessage = localeService.getMessage("gift.success.pickup.message",
                                        "Richiesta regalo inviata! Il donatore ti proporrà alcuni orari per il ritiro.");
                            }
                            
                            AlertHelper.showInformationAlert(
                                    localeService.getMessage("gift.success.title", "Richiesta inviata"),
                                    localeService.getMessage("gift.success.header", "La tua richiesta è stata inviata"),
                                    successMessage);
                                    
                            // Navigate to offers view
                            try {
                                navigationService.navigateToOffersView();
                            } catch (Exception e) {
                                System.err.println("Failed to navigate to offers view: " + e.getMessage());
                            }
                        } else {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("gift.error.title", "Errore"),
                                    localeService.getMessage("gift.error.header", "Richiesta fallita"),
                                    localeService.getMessage("gift.error.message",
                                            "Non è stato possibile inviare la richiesta. Riprova più tardi."));
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            AlertHelper.showErrorAlert(
                                    localeService.getMessage("gift.error.title", "Errore"),
                                    localeService.getMessage("gift.error.header", "Errore durante l'invio"),
                                    localeService.getMessage("gift.error.connection.message",
                                            "Si è verificato un errore durante l'invio della richiesta: " + ex.getMessage()));
                        });
                        return null;
                    });
                        
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("gift.error.title", "Errore"),
                    localeService.getMessage("gift.error.header", "Errore imprevisto"),
                    localeService.getMessage("gift.error.unexpected.message",
                            "Si è verificato un errore imprevisto: " + e.getMessage()));
        }
    }

    /**
     * 
     */
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
                    AlertHelper.showInformationAlert(
                            "Offerta piazzata",
                            "La tua offerta è stata registrata",
                            "Ti aggiorneremo se qualcuno supera la tua offerta.");
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

    /**
     * 
     */
    @FXML
    private void handleContactSeller() {
        AlertHelper.showInformationAlert(
                "Contatta venditore",
                "Funzionalità in sviluppo",
                "La messaggistica diretta sarà disponibile presto.");
    }

    /**
     * 
     */
    @FXML
    private void handleReportListing() {
        if (currentListing != null) {
            navigationService.openReportDialog(currentListing, (Stage) backButton.getScene().getWindow());
        }
    }

    /**
     * 
     */
    @FXML
    private void handleReportUser() {
        if (currentListing != null && currentListing.getUser() != null) {
            UserViewModel userViewModel = currentListing.getUser();
            navigationService.openReportDialog(userViewModel, (Stage) backButton.getScene().getWindow());
        }
    }

    /**
     * @param item
     * @return
     */
    private VBox createExpandableItemRow(ListingItemViewModel item) {
        VBox itemContainer = new VBox(0);
        itemContainer.getStyleClass().add("item-row");
        HBox mainRow = createMainItemRow(item);
        VBox detailsSection = createItemDetailsSection(item);
        
        if (detailsSection != null) {
            detailsSection.setVisible(false);
            detailsSection.setManaged(false);
            Button expandButton = new Button("▼");
            expandButton.getStyleClass().add("expand-button");
            expandButton.setOnAction(_ -> {
                boolean isExpanded = detailsSection.isVisible();
                detailsSection.setVisible(!isExpanded);
                detailsSection.setManaged(!isExpanded);
                expandButton.setText(isExpanded ? "▼" : "▲");
            });
            mainRow.getChildren().add(expandButton);
            itemContainer.getChildren().addAll(mainRow, detailsSection);
        } else {
            itemContainer.getChildren().add(mainRow);
        }
        
        return itemContainer;
    }

    /**
     * @param item
     * @return
     */
    private HBox createMainItemRow(ListingItemViewModel item) {
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("item-header-row");
        ImageView itemImage = new ImageView();
        itemImage.setFitHeight(50);
        itemImage.setFitWidth(50);
        itemImage.setPreserveRatio(true);
        itemImage.getStyleClass().add("item-thumbnail");
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
        VBox itemMainInfo = new VBox(3);
        itemMainInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(itemMainInfo, javafx.scene.layout.Priority.ALWAYS);
        HBox nameQuantityRow = new HBox(10);
        nameQuantityRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label itemName = new Label(item.getName());
        itemName.getStyleClass().add("item-name");
        Label quantityBadge = new Label("x" + item.getQuantity());
        quantityBadge.getStyleClass().add("quantity-badge");
        nameQuantityRow.getChildren().addAll(itemName, quantityBadge);
        HBox categoryConditionRow = new HBox(10);
        categoryConditionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        if (item.getItem().getItemCategory() != null && !item.getItem().getItemCategory().isEmpty()) {
            Label categoryLabel = new Label(item.getItem().getItemCategory());
            categoryLabel.getStyleClass().add("item-category");
            categoryConditionRow.getChildren().add(categoryLabel);
        }
        
        if (item.getItem().getCondition() != null) {
            Label conditionLabel = new Label(item.getItem().getConditionDisplayName());
            conditionLabel.getStyleClass().add("item-condition");
            categoryConditionRow.getChildren().add(conditionLabel);
        }
        
        itemMainInfo.getChildren().addAll(nameQuantityRow);
        if (!categoryConditionRow.getChildren().isEmpty()) {
            itemMainInfo.getChildren().add(categoryConditionRow);
        }
        
        headerRow.getChildren().addAll(itemImage, itemMainInfo);
        
        return headerRow;
    }
}