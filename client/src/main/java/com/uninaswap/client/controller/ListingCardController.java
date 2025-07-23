package com.uninaswap.client.controller;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.viewmodel.AuctionListingViewModel;
import com.uninaswap.client.viewmodel.ListingItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.SellListingViewModel;
import com.uninaswap.common.enums.Category;
import com.uninaswap.client.service.CategoryService;
import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ImageService;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ListingCardController {

    /**
     * 
     */
    @FXML
    private VBox itemCard;
    /**
     * 
     */
    @FXML
    private ImageView itemImage;
    /**
     * 
     */
    @FXML
    private Label itemName;
    /**
     * 
     */
    @FXML
    private Text categoryText;
    /**
     * 
     */
    @FXML
    private Text sellerName;
    /**
     * 
     */
    @FXML
    private Text itemPrice;
    /**
     * 
     */
    @FXML
    private ImageView favoriteIcon;
    /**
     * 
     */
    @FXML
    private StackPane listingCardContainer;
    /**
     * 
     */
    @FXML
    private StackPane imageContainer;

    /**
     * 
     */
    private ListingViewModel listing;
    /**
     * 
     */
    private boolean isFavorite = false;
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
    private Label imageCountLabel;
    /**
     * 
     */
    private int currentImageIndex = 0;
    /**
     * 
     */
    private List<String> availableImagePaths = new ArrayList<>();

    /**
     * 
     */
    public ListingCardController() {
    }

    /**
     * @param listing
     */
    public ListingCardController(ListingViewModel listing) {
        this.listing = listing;
    }

    /**
     * 
     */
    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle(240, 360);
        listingCardContainer.setClip(clip);
        if (listing != null) {
            setListing(listing);
            initializeFavoriteStatus();
        }
    }

    /**
     * @param listing
     */
    public void setListing(ListingViewModel listing) {
        this.listing = listing;

        if (listing != null) {
            if (itemName != null) {
                itemName.setText(listing.getTitle());
            }
            if (sellerName != null && listing.getUser() != null) {
                sellerName.setText(listing.getUser().getUsername());
            }
            if (itemPrice != null) {
                String priceText = getPriceText(listing);
                itemPrice.setText(priceText);
            }
            if (categoryText != null) {
                String category = getListingCategory(listing);
                categoryText.setText(category);
            }
            if (itemImage != null) {
                loadListingImages(listing);
            }
            initializeFavoriteStatus();
        }
    }

    /**
     * @param listing
     */
    private void loadListingImages(ListingViewModel listing) {
        availableImagePaths = getAllImagePaths(listing);
        currentImageIndex = 0;
        if (!availableImagePaths.isEmpty()) {
            loadImageFromPath(availableImagePaths.get(0));
            if (availableImagePaths.size() > 1) {
                addMultipleImageIndicator(availableImagePaths.size());
            }
        } else {
            setDefaultImage();
        }
    }

    /**
     * @param listing
     * @return
     */
    private List<String> getAllImagePaths(ListingViewModel listing) {
        List<String> imagePaths = new ArrayList<>();

        if (listing.getItems() != null) {
            for (ListingItemViewModel item : listing.getItems()) {
                String imagePath = item.getItem().getImagePath();
                if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default")) {
                    imagePaths.add(imagePath);
                }
            }
        }

        return imagePaths;
    }

    /**
     * @param imagePath
     */
    private void loadImageFromPath(String imagePath) {
        imageService.fetchImage(imagePath)
                .thenAccept(image -> {
                    Platform.runLater(() -> {
                        if (image != null && !image.isError()) {
                            itemImage.setImage(image);
                        } else {
                            setDefaultImage();
                        }
                    });
                })
                .exceptionally(ex -> {
                    System.err.println("Failed to load listing image: " + ex.getMessage());
                    Platform.runLater(this::setDefaultImage);
                    return null;
                });
    }

    /**
     * 
     */
    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            if (defaultImage != null && !defaultImage.isError()) {
                itemImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Could not load default image: " + e.getMessage());
        }
    }

    /**
     * 
     */
    private void initializeFavoriteStatus() {
        if (listing != null) {
            boolean isCurrentlyFavorite = favoritesService.isFavoriteListing(listing.getId());
            setFavorite(isCurrentlyFavorite);
        }
    }

    /**
     * @param listing
     * @return
     */
    private String getPriceText(ListingViewModel listing) {
        String type = listing.getListingTypeValue();

        switch (type.toUpperCase()) {
            case "SELL":
                if (listing instanceof SellListingViewModel) {
                    SellListingViewModel sellListing = (SellListingViewModel) listing;
                    BigDecimal price = sellListing.getPrice();
                    String currency = sellListing.getCurrency() != null ? sellListing.getCurrency().getSymbol() : "€";
                    return currency + " " + price;
                }
                return "In vendita";

            case "TRADE":
                return "Scambio";

            case "GIFT":
                return "Regalo";

            case "AUCTION":
                if (listing instanceof AuctionListingViewModel) {
                    AuctionListingViewModel auctionListing = (AuctionListingViewModel) listing;
                    BigDecimal currentBid = auctionListing.getHighestBid();
                    if (currentBid != null && currentBid.compareTo(BigDecimal.ZERO) > 0) {
                        String currency = auctionListing.getCurrency() != null
                                ? auctionListing.getCurrency().getSymbol()
                                : "€";
                        return currency + " " + currentBid;
                    } else {
                        BigDecimal startingPrice = auctionListing.getStartingPrice();
                        String currency = auctionListing.getCurrency() != null
                                ? auctionListing.getCurrency().getSymbol()
                                : "€";
                        return currency + " " + startingPrice + " (base)";
                    }
                }
                return "Asta";

            default:
                return type;
        }
    }

    /**
     * @param listing
     * @return
     */
    private String getListingCategory(ListingViewModel listing) {
        if (listing.getItems() != null && !listing.getItems().isEmpty()) {
            String itemCategory = listing.getItems().get(0).getItem().getItemCategory();
            if (itemCategory != null && !itemCategory.isEmpty()) {
                Category category = Category.fromString(itemCategory);
                return CategoryService.getInstance().getLocalizedCategoryName(category);
            }
        }
        switch (listing.getListingTypeValue().toUpperCase()) {
            case "SELL":
                return "In vendita";
            case "TRADE":
                return "Scambio";
            case "GIFT":
                return "Regalo";
            case "AUCTION":
                return "Asta";
            default:
                return listing.getListingTypeValue();
        }
    }

    /**
     * @param favorite
     */
    private void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        if (favoriteIcon != null) {
            String iconPath = favorite ? "/images/icons/favorites_remove.png" :
                    "/images/icons/favorites_add.png";
            try {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                favoriteIcon.setImage(icon);
            } catch (Exception e) {
                System.err.println("Could not load favorite icon: " + e.getMessage());
            }
        }
    }

    /**
     * @param event
     */
    @FXML
    private void openListingDetails(MouseEvent event) {
        if (listing != null) {
            System.out.println("Opening listing details for: " + listing.getTitle());

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingDetailsView.fxml"));
                Parent detailsView = loader.load();
                ListingDetailsController controller = loader.getController();
                controller.setListing(listing);
                navigationService.loadView(detailsView, "Dettagli: " + listing.getTitle());
            } catch (Exception e) {
                System.err.println("Error opening listing details: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * @param event
     */
    @FXML
    private void toggleFavorite(MouseEvent event) {
        if (listing != null) {
            boolean newFavoriteState = !isFavorite;
            setFavorite(newFavoriteState);
            if (newFavoriteState) {
                favoritesService.addFavoriteToServer(listing.getId())
                        .thenAccept(_ -> Platform.runLater(() -> {
                            System.out.println("Successfully added listing to favorites: " + listing.getId());
                        }))
                        .exceptionally(ex -> {
                            setFavorite(false);
                            System.err.println("Failed to add to favorites: " + ex.getMessage());
                            return null;
                        });
            } else {
                favoritesService.removeFavoriteFromServer(listing.getId())
                        .thenAccept(_ -> Platform.runLater(() -> {
                            System.out.println("Successfully removed listing from favorites: " + listing.getId());
                        }))
                        .exceptionally(ex -> {
                            setFavorite(true);
                            System.err.println("Failed to remove from favorites: " + ex.getMessage());
                            return null;
                        });
            }
        }
    }

    /**
     * @param imageCount
     */
    private void addMultipleImageIndicator(int imageCount) {
        if (imageContainer != null) {
            createImageCountIndicator(imageCount, imageContainer);
            addImageCyclingHandler();
        }
    }

    /**
     * @param imageCount
     * @param imageContainer
     */
    private void createImageCountIndicator(int imageCount, StackPane imageContainer) {
        imageCountLabel = new Label("1/" + imageCount);
        imageCountLabel.getStyleClass().addAll("image-count-indicator");
        imageCountLabel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.7); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 2 6 2 6; " +
                        "-fx-background-radius: 10; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-weight: bold;");
        StackPane.setAlignment(imageCountLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(imageCountLabel, new Insets(0, 5, 5, 0));
        imageContainer.getChildren().add(imageCountLabel);
    }

    /**
     * 
     */
    private void addImageCyclingHandler() {
        if (itemImage != null) {
            itemImage.setOnMouseClicked(event -> {
                if (availableImagePaths.size() > 1) {
                    event.consume();
                    cycleToNextImage();
                }
            });
            itemImage.setOnMouseEntered(_ -> {
                if (availableImagePaths.size() > 1) {
                    itemImage.setStyle(
                            "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 0);");
                }
            });

            itemImage.setOnMouseExited(_ -> {
                itemImage.setStyle("-fx-cursor: default; -fx-effect: null;");
            });
        }
    }

    /**
     * 
     */
    private void cycleToNextImage() {
        if (availableImagePaths.isEmpty())
            return;
        currentImageIndex = (currentImageIndex + 1) % availableImagePaths.size();
        String nextImagePath = availableImagePaths.get(currentImageIndex);
        loadImageFromPath(nextImagePath);
        addImageCyclingHandler();
        updateImageCountIndicator();
    }

    /**
     * 
     */
    private void updateImageCountIndicator() {
        if (imageCountLabel != null) {
            imageCountLabel.setText((currentImageIndex + 1) + "/" + availableImagePaths.size());
        }
    }
}
