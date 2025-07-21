package com.uninaswap.client.controller;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.LocaleService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListingCardController implements Refreshable {

    @FXML
    private VBox itemCard;
    @FXML
    private ImageView itemImage;
    @FXML
    private Label itemName;
    @FXML
    private Text categoryText;
    @FXML
    private Text sellerName;
    @FXML
    private Text itemPrice;
    @FXML
    private ImageView favoriteIcon;
    @FXML
    private StackPane listingCardContainer;
    @FXML
    private StackPane imageContainer; // Add this field

    private ListingViewModel listing;
    private boolean isFavorite = false;

    private final NavigationService navigationService = NavigationService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final ImageService imageService = ImageService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();

    private Label imageCountLabel; // For showing "1/3" etc.
    private int currentImageIndex = 0;
    private List<String> availableImagePaths = new ArrayList<>();

    public ListingCardController() {
    }

    public ListingCardController(ListingViewModel listing) {
        this.listing = listing;
    }

    @FXML
    public void initialize() {
        Rectangle clip = new Rectangle(240, 360);
        listingCardContainer.setClip(clip);
        if (listing != null) {
            setListing(listing);
            // Initialize favorite status from service
            initializeFavoriteStatus();
        }
        
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("listingcard.debug.initialized", "ListingCard initialized"));
    }

    public void setListing(ListingViewModel listing) {
        this.listing = listing;

        if (listing != null) {
            // Set listing title
            if (itemName != null) {
                itemName.setText(listing.getTitle());
            }

            // Set seller name
            if (sellerName != null && listing.getUser() != null) {
                sellerName.setText(listing.getUser().getUsername());
            }

            // Set price based on listing type
            if (itemPrice != null) {
                String priceText = getPriceText(listing);
                itemPrice.setText(priceText);
            }

            // Set category (from first item if available)
            if (categoryText != null) {
                String category = getListingCategory(listing);
                categoryText.setText(category);
            }

            // Set image from first item with image
            if (itemImage != null) {
                loadListingImages(listing);
            }

            // Initialize favorite status after setting listing
            initializeFavoriteStatus();
            
            System.out.println(localeService.getMessage("listingcard.debug.listing.set", "Listing set for card: {0}").replace("{0}", listing.getTitle()));
        }
    }

    private void loadListingImages(ListingViewModel listing) {
        availableImagePaths = getAllImagePaths(listing);
        currentImageIndex = 0;

        if (!availableImagePaths.isEmpty()) {
            // Load first image immediately
            loadImageFromPath(availableImagePaths.get(0));

            // If multiple images, add the indicator
            if (availableImagePaths.size() > 1) {
                addMultipleImageIndicator(availableImagePaths.size());
            }
        } else {
            setDefaultImage();
        }
    }

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

    private void loadImageFromPath(String imagePath) {
        // Load image from server using ImageService
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
                    System.err.println(localeService.getMessage("listingcard.error.image.load", "Failed to load listing image: {0}").replace("{0}", ex.getMessage()));
                    Platform.runLater(this::setDefaultImage);
                    return null;
                });
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass()
                    .getResourceAsStream("/images/icons/immagine_generica.png"));
            if (defaultImage != null && !defaultImage.isError()) {
                itemImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("listingcard.error.default.image", "Could not load default image: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void initializeFavoriteStatus() {
        if (listing != null) {
            // Check if this listing is already in favorites
            boolean isCurrentlyFavorite = favoritesService.isFavoriteListing(listing.getId());
            setFavorite(isCurrentlyFavorite);
        }
    }

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
                return localeService.getMessage("listingcard.type.sell", "For Sale");

            case "TRADE":
                return localeService.getMessage("listingcard.type.trade", "Trade");

            case "GIFT":
                return localeService.getMessage("listingcard.type.gift", "Gift");

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
                        return currency + " " + startingPrice + " " + localeService.getMessage("listingcard.auction.starting", "(starting)");
                    }
                }
                return localeService.getMessage("listingcard.type.auction", "Auction");

            default:
                return type;
        }
    }

    private String getListingCategory(ListingViewModel listing) {
        if (listing.getItems() != null && !listing.getItems().isEmpty()) {
            // Get category from first item
            String itemCategory = listing.getItems().get(0).getItem().getItemCategory();
            if (itemCategory != null && !itemCategory.isEmpty()) {
                // Try to map existing category strings to proper categories
                Category category = Category.fromString(itemCategory);
                return CategoryService.getInstance().getLocalizedCategoryName(category);
            }
        }

        // Fallback to listing type
        switch (listing.getListingTypeValue().toUpperCase()) {
            case "SELL":
                return localeService.getMessage("listingcard.type.sell", "For Sale");
            case "TRADE":
                return localeService.getMessage("listingcard.type.trade", "Trade");
            case "GIFT":
                return localeService.getMessage("listingcard.type.gift", "Gift");
            case "AUCTION":
                return localeService.getMessage("listingcard.type.auction", "Auction");
            default:
                return listing.getListingTypeValue();
        }
    }

    // Update the setFavorite method to use correct icon paths
    private void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        // Update favorite icon appearance
        if (favoriteIcon != null) {
            // Use the same icon paths as ItemCardController
            String iconPath = favorite ? "/images/icons/favorites_remove.png" : // Filled heart
                    "/images/icons/favorites_add.png"; // Empty heart
            try {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                favoriteIcon.setImage(icon);
                
                System.out.println(localeService.getMessage("listingcard.debug.favorite.updated", "Favorite status updated to: {0}").replace("{0}", String.valueOf(favorite)));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("listingcard.error.favorite.icon", "Could not load favorite icon: {0}").replace("{0}", e.getMessage()));
            }
        }
    }

    @FXML
    private void openListingDetails(MouseEvent event) {
        if (listing != null) {
            System.out.println(localeService.getMessage("listingcard.debug.opening.details", "Opening listing details for: {0}").replace("{0}", listing.getTitle()));

            try {
                // Load the listing details view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingDetailsView.fxml"));
                Parent detailsView = loader.load();

                // Get the controller and set the listing
                ListingDetailsController controller = loader.getController();
                controller.setListing(listing);

                // Navigate to the details view using the new loadView method
                navigationService.loadView(detailsView, localeService.getMessage("listingcard.details.window.title", "Details: {0}").replace("{0}", listing.getTitle()));

            } catch (Exception e) {
                System.err.println(localeService.getMessage("listingcard.error.opening.details", "Error opening listing details: {0}").replace("{0}", e.getMessage()));
                e.printStackTrace();
            }
        } else {
            System.out.println(localeService.getMessage("listingcard.debug.no.listing.details", "No listing available for details view"));
        }
    }

    @FXML
    private void toggleFavorite(MouseEvent event) {
        event.consume(); // Prevent triggering openListingDetails

        if (listing != null) {
            // Optimistic UI update
            boolean newFavoriteState = !isFavorite;
            setFavorite(newFavoriteState);

            // Sync with server
            if (newFavoriteState) {
                favoritesService.addFavoriteToServer(listing.getId())
                        .thenAccept(favoriteViewModel -> Platform.runLater(() -> {
                            // Server sync successful - local tracking is updated via message handler
                            System.out.println(localeService.getMessage("listingcard.debug.favorite.added", "Successfully added listing to favorites: {0}").replace("{0}", listing.getId()));
                        }))
                        .exceptionally(ex -> {
                            // Revert UI on failure
                            setFavorite(false);
                            System.err.println(localeService.getMessage("listingcard.error.favorite.add", "Failed to add to favorites: {0}").replace("{0}", ex.getMessage()));
                            return null;
                        });
            } else {
                favoritesService.removeFavoriteFromServer(listing.getId())
                        .thenAccept(success -> Platform.runLater(() -> {
                            // Server sync successful - local tracking is updated via message handler
                            System.out.println(localeService.getMessage("listingcard.debug.favorite.removed", "Successfully removed listing from favorites: {0}").replace("{0}", listing.getId()));
                        }))
                        .exceptionally(ex -> {
                            // Revert UI on failure
                            setFavorite(true);
                            System.err.println(localeService.getMessage("listingcard.error.favorite.remove", "Failed to remove from favorites: {0}").replace("{0}", ex.getMessage()));
                            return null;
                        });
            }
        } else {
            System.out.println(localeService.getMessage("listingcard.debug.no.listing.favorite", "No listing available for favorite toggle"));
        }
    }

    private void addMultipleImageIndicator(int imageCount) {
        // No longer need to check or wrap - imageContainer is always a StackPane
        if (imageContainer != null) {
            // Create image count indicator
            createImageCountIndicator(imageCount, imageContainer);

            // Add click handler for image cycling
            addImageCyclingHandler();
        }
    }

    private void createImageCountIndicator(int imageCount, StackPane imageContainer) {
        // Create a label showing current image / total images
        imageCountLabel = new Label(localeService.getMessage("listingcard.image.count", "{0}/{1}").replace("{0}", "1").replace("{1}", String.valueOf(imageCount)));
        imageCountLabel.getStyleClass().addAll("image-count-indicator");
        imageCountLabel.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.7); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 2 6 2 6; " +
                        "-fx-background-radius: 10; " +
                        "-fx-font-size: 10px; " +
                        "-fx-font-weight: bold;");

        // Position in bottom-right corner of the image
        StackPane.setAlignment(imageCountLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(imageCountLabel, new Insets(0, 5, 5, 0));

        // Add to the container
        imageContainer.getChildren().add(imageCountLabel);

        // Create navigation dots (optional)
        // createNavigationDots(imageCount, imageContainer);
    }

    private void createNavigationDots(int imageCount, StackPane imageContainer) {
        if (imageCount <= 5) { // Only show dots for reasonable number of images
            HBox dotsContainer = new HBox(3);
            dotsContainer.setAlignment(Pos.CENTER);

            for (int i = 0; i < imageCount; i++) {
                Label dot = new Label("•");
                dot.setStyle(
                        "-fx-text-fill: " + (i == 0 ? "white" : "rgba(255, 255, 255, 0.5)") + "; " +
                                "-fx-font-size: 12px;");
                dot.getStyleClass().add("navigation-dot");
                dotsContainer.getChildren().add(dot);
            }

            // Position at bottom center
            StackPane.setAlignment(dotsContainer, Pos.BOTTOM_CENTER);
            StackPane.setMargin(dotsContainer, new Insets(0, 0, 15, 0));

            imageContainer.getChildren().add(dotsContainer);
        }
    }

    private void addImageCyclingHandler() {
        if (itemImage != null) {
            // Add click handler to cycle through images
            itemImage.setOnMouseClicked(event -> {
                if (availableImagePaths.size() > 1) {
                    // Prevent event from bubbling to card click
                    event.consume();

                    // Cycle to next image
                    cycleToNextImage();
                }
            });

            // Add hover effect to indicate clickability
            itemImage.setOnMouseEntered(event -> {
                if (availableImagePaths.size() > 1) {
                    itemImage.setStyle(
                            "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 0);");
                }
            });

            itemImage.setOnMouseExited(event -> {
                itemImage.setStyle("-fx-cursor: default; -fx-effect: null;");
            });
        }
    }

    private void cycleToNextImage() {
        if (availableImagePaths.isEmpty())
            return;

        // Move to next image
        currentImageIndex = (currentImageIndex + 1) % availableImagePaths.size();

        // Load the new image
        String nextImagePath = availableImagePaths.get(currentImageIndex);
        loadImageFromPath(nextImagePath);

        addImageCyclingHandler();
        // Update the counter label
        updateImageCountIndicator();

        // Update navigation dots
        updateNavigationDots();
        
        System.out.println(localeService.getMessage("listingcard.debug.image.cycled", "Cycled to image {0} of {1}").replace("{0}", String.valueOf(currentImageIndex + 1)).replace("{1}", String.valueOf(availableImagePaths.size())));
    }

    private void updateImageCountIndicator() {
        if (imageCountLabel != null) {
            imageCountLabel.setText(localeService.getMessage("listingcard.image.count", "{0}/{1}")
                    .replace("{0}", String.valueOf(currentImageIndex + 1))
                    .replace("{1}", String.valueOf(availableImagePaths.size())));
        }
    }

    private void updateNavigationDots() {
        // Navigation dots update logic would go here if implemented
        // Currently commented out in the original code
    }

    @Override
    public void refreshUI() {
        // Refresh the listing display with current locale
        if (listing != null) {
            // Update price text with current locale
            if (itemPrice != null) {
                String priceText = getPriceText(listing);
                itemPrice.setText(priceText);
            }

            // Update category text with current locale
            if (categoryText != null) {
                String category = getListingCategory(listing);
                categoryText.setText(category);
            }

            // Update image count indicator if it exists
            if (imageCountLabel != null && availableImagePaths.size() > 1) {
                updateImageCountIndicator();
            }
        }
    }

    // Getter methods for external access
    public ListingViewModel getListing() {
        return listing;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}
