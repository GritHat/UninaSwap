package com.uninaswap.client.controller;

import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.common.dto.ListingItemDTO;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
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

public class ListingCardController {

    @FXML
    private VBox itemCard;
    @FXML
    private ImageView itemImage;
    @FXML
    private Text itemName;
    @FXML
    private Text categoryText;
    @FXML
    private Text sellerName;
    @FXML
    private Text itemPrice;
    @FXML
    private ImageView favoriteIcon;

    private ListingDTO listing;
    private boolean isFavorite = false;

    private final NavigationService navigationService = NavigationService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final ImageService imageService = ImageService.getInstance();

    private Label imageCountLabel; // For showing "1/3" etc.
    private int currentImageIndex = 0;
    private List<String> availableImagePaths = new ArrayList<>();

    public ListingCardController() {
    }

    public ListingCardController(ListingDTO listing) {
        this.listing = listing;
    }

    @FXML
    public void initialize() {
        if (listing != null) {
            setListing(listing);
            // Initialize favorite status from service
            initializeFavoriteStatus();
        }
    }

    public void setListing(ListingDTO listing) {
        this.listing = listing;

        if (listing != null) {
            // Set listing title
            if (itemName != null) {
                itemName.setText(listing.getTitle());
            }

            // Set seller name
            if (sellerName != null && listing.getCreator() != null) {
                sellerName.setText(listing.getCreator().getUsername());
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
        }
    }

    private void loadListingImages(ListingDTO listing) {
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

    private List<String> getAllImagePaths(ListingDTO listing) {
        List<String> imagePaths = new ArrayList<>();

        if (listing.getItems() != null) {
            for (ListingItemDTO item : listing.getItems()) {
                String imagePath = item.getItemImagePath();
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
                    System.err.println("Failed to load listing image: " + ex.getMessage());
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
            System.err.println("Could not load default image: " + e.getMessage());
        }
    }

    private void initializeFavoriteStatus() {
        if (listing != null) {
            // Check if this listing is already in favorites
            boolean isCurrentlyFavorite = favoritesService.isFavoriteListing(listing.getId());
            setFavorite(isCurrentlyFavorite);
        }
    }

    private String getPriceText(ListingDTO listing) {
        String type = listing.getListingTypeValue();

        switch (type.toUpperCase()) {
            case "SELL":
                // Try to get price from SellListingDTO
                if (listing instanceof com.uninaswap.common.dto.SellListingDTO) {
                    com.uninaswap.common.dto.SellListingDTO sellListing = (com.uninaswap.common.dto.SellListingDTO) listing;
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
                // Try to get current bid from AuctionListingDTO
                if (listing instanceof com.uninaswap.common.dto.AuctionListingDTO) {
                    com.uninaswap.common.dto.AuctionListingDTO auctionListing = (com.uninaswap.common.dto.AuctionListingDTO) listing;
                    BigDecimal currentBid = auctionListing.getCurrentHighestBid();
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

    private String getListingCategory(ListingDTO listing) {
        if (listing.getItems() != null && !listing.getItems().isEmpty()) {
            // Get category from first item
            String itemCategory = listing.getItems().get(0).getItemCategory();
            if (itemCategory != null && !itemCategory.isEmpty()) {
                return itemCategory;
            }
        }

        // Fallback to listing type
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

    // Update the setFavorite method to use correct icon paths
    private void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        // Update favorite icon appearance
        if (favoriteIcon != null) {
            // Use the same icon paths as ItemCardController
            String iconPath = favorite ? "/images/elenco_preferiti.png" : // Filled heart
                    "/images/preferiti.png"; // Empty heart
            try {
                Image icon = new Image(getClass().getResourceAsStream(iconPath));
                favoriteIcon.setImage(icon);
            } catch (Exception e) {
                System.err.println("Could not load favorite icon: " + e.getMessage());
            }
        }
    }

    @FXML
    private void openListingDetails(MouseEvent event) {
        if (listing != null) {
            System.out.println("Opening listing details for: " + listing.getTitle());

            try {
                // Load the listing details view
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListingDetailsView.fxml"));
                Parent detailsView = loader.load();

                // Get the controller and set the listing
                ListingDetailsController controller = loader.getController();
                controller.setListing(listing);

                // Navigate to the details view using the new loadView method
                navigationService.loadView(detailsView, "Dettagli: " + listing.getTitle());

            } catch (Exception e) {
                System.err.println("Error opening listing details: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void toggleFavorite(MouseEvent event) {
        event.consume(); // Prevent triggering openListingDetails

        if (listing != null) {
            // Toggle favorite status
            setFavorite(!isFavorite);

            // Update backend
            if (isFavorite) {
                favoritesService.addFavoriteListing(listing.getId());
            } else {
                favoritesService.removeFavoriteListing(listing.getId());
            }
        }
    }

    private void addMultipleImageIndicator(int imageCount) {
        if (itemImage != null && itemImage.getParent() instanceof StackPane) {
            StackPane imageContainer = (StackPane) itemImage.getParent();

            // Create image count indicator
            createImageCountIndicator(imageCount, imageContainer);

            // Add click handler for image cycling
            addImageCyclingHandler();
        } else {
            // If not in a StackPane, wrap the ImageView in one
            wrapImageViewInStackPane(imageCount);
        }
    }

    private void createImageCountIndicator(int imageCount, StackPane imageContainer) {
        // Create a label showing current image / total images
        imageCountLabel = new Label("1/" + imageCount);
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

            // Style the dots container
            // dotsContainer.setStyle(
            // "-fx-background-color: rgba(0, 0, 0, 0.5); " +
            // "-fx-background-radius: 10; " +
            // "-fx-padding: 2 8 2 8;");

            // Position at bottom center
            StackPane.setAlignment(dotsContainer, Pos.BOTTOM_CENTER);
            StackPane.setMargin(dotsContainer, new Insets(0, 0, 15, 0));

            imageContainer.getChildren().add(dotsContainer);
        }
    }

    private void wrapImageViewInStackPane(int imageCount) {
        if (itemImage.getParent() instanceof VBox) {
            VBox parent = (VBox) itemImage.getParent();
            int imageIndex = parent.getChildren().indexOf(itemImage);

            // Remove ImageView from parent
            parent.getChildren().remove(itemImage);

            // Create StackPane wrapper
            StackPane imageWrapper = new StackPane();
            imageWrapper.getChildren().add(itemImage);

            // Add indicator
            createImageCountIndicator(imageCount, imageWrapper);

            // Add wrapper back to parent at same position
            parent.getChildren().add(imageIndex, imageWrapper);
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
    }

    private void updateImageCountIndicator() {
        if (imageCountLabel != null) {
            imageCountLabel.setText((currentImageIndex + 1) + "/" + availableImagePaths.size());
        }
    }

    private void updateNavigationDots() {
        if (itemImage.getParent() instanceof StackPane) {
            StackPane container = (StackPane) itemImage.getParent();

            // Find the dots container
            container.getChildren().stream()
                    .filter(node -> node instanceof HBox)
                    .map(node -> (HBox) node)
                    .filter(hbox -> hbox.getStyleClass().contains("navigation-dots") ||
                            hbox.getChildren().stream()
                                    .anyMatch(child -> child.getStyleClass().contains("navigation-dot")))
                    .findFirst()
                    .ifPresent(dotsContainer -> {
                        // Update dot styles
                        for (int i = 0; i < dotsContainer.getChildren().size(); i++) {
                            Label dot = (Label) dotsContainer.getChildren().get(i);
                            boolean isActive = i == currentImageIndex;
                            dot.setStyle(
                                    "-fx-text-fill: " + (isActive ? "white" : "rgba(255, 255, 255, 0.5)") + "; " +
                                            "-fx-font-size: 12px;");
                        }
                    });
        }
    }
}
