package com.uninaswap.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.uninaswap.common.dto.ItemDTO;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.LocaleService;

public class ItemCardController implements Refreshable {

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

    private ItemDTO item;
    private boolean isFavorite = false;

    private final NavigationService navigationService = NavigationService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();

    public ItemCardController() {
    }

    public ItemCardController(ItemDTO item) {
        this.item = item;
    }

    @FXML
    public void initialize() {
        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("itemcard.debug.initialized", "ItemCard initialized"));
    }

    public void setItem(ItemDTO item) {
        this.item = item;
        
        if (item != null) {
            // Set item name
            if (itemName != null) {
                itemName.setText(item.getName());
            }
            
            // Set category
            if (categoryText != null) {
                String category = item.getCategory();
                if (category != null && !category.isEmpty()) {
                    categoryText.setText(category);
                } else {
                    categoryText.setText(localeService.getMessage("itemcard.category.unknown", "Unknown"));
                }
            }
            
            // Set seller name (if available)
            if (sellerName != null) {
                // TODO: Implement when seller information is available in ItemDTO
                sellerName.setText(localeService.getMessage("itemcard.seller.placeholder", "Seller"));
            }
            
            // Set price (if available)
            if (itemPrice != null) {
                // TODO: Implement when price information is available in ItemDTO
                itemPrice.setText(localeService.getMessage("itemcard.price.placeholder", "Price TBD"));
            }
            
            // Load item image if available
            loadItemImage();
            
            // Set favorite status
            setFavorite(favoritesService.isFavorite(item.getId()));
            
            System.out.println(localeService.getMessage("itemcard.debug.item.set", "Item set for card: {0}").replace("{0}", item.getName()));
        } else {
            clearItemData();
        }
    }

    private void loadItemImage() {
        if (item != null && itemImage != null) {
            // TODO: Implement image loading when image path is available in ItemDTO
            // For now, use default image
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/immagine_generica.png"));
            if (itemImage != null) {
                itemImage.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("itemcard.error.default.image", "Failed to load default item image: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void clearItemData() {
        if (itemName != null) {
            itemName.setText(localeService.getMessage("itemcard.name.placeholder", "Item Name"));
        }
        if (categoryText != null) {
            categoryText.setText(localeService.getMessage("itemcard.category.placeholder", "Category"));
        }
        if (sellerName != null) {
            sellerName.setText(localeService.getMessage("itemcard.seller.placeholder", "Seller"));
        }
        if (itemPrice != null) {
            itemPrice.setText(localeService.getMessage("itemcard.price.placeholder", "Price TBD"));
        }
        if (itemImage != null) {
            setDefaultImage();
        }
        setFavorite(false);
    }

    private void setFavorite(boolean favorite) {
        isFavorite = favorite;
        
        if (favoriteIcon != null) {
            try {
                String iconPath = favorite
                        ? "/images/icons/elenco_preferiti.png"
                        : "/images/icons/preferiti.png";
                Image favoriteImage = new Image(getClass().getResourceAsStream(iconPath));
                favoriteIcon.setImage(favoriteImage);
                
                System.out.println(localeService.getMessage("itemcard.debug.favorite.updated", "Favorite status updated to: {0}").replace("{0}", String.valueOf(favorite)));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("itemcard.error.favorite.icon", "Failed to load favorite icon: {0}").replace("{0}", e.getMessage()));
            }
        }
    }

    @FXML
    private void openItemDetails(MouseEvent event) {
        if (item != null) {
            try {
                navigationService.navigateToItemDetails(item.getId());
                System.out.println(localeService.getMessage("itemcard.debug.details.opened", "Opening item details for: {0}").replace("{0}", item.getName()));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("itemcard.error.navigation", "Failed to navigate to item details: {0}").replace("{0}", e.getMessage()));
            }
        } else {
            System.out.println(localeService.getMessage("itemcard.debug.no.item.details", "No item available for details view"));
        }
    }

    @FXML
    private void toggleFavorite(MouseEvent event) {
        if (item == null) {
            System.out.println(localeService.getMessage("itemcard.debug.no.item.favorite", "No item available for favorite toggle"));
            return;
        }
        
        try {
            if (isFavorite) {
                favoritesService.removeFavorite(item.getId());
                setFavorite(false);
                System.out.println(localeService.getMessage("itemcard.debug.favorite.removed", "Removed item from favorites: {0}").replace("{0}", item.getName()));
            } else {
                favoritesService.addFavorite(item.getId());
                setFavorite(true);
                System.out.println(localeService.getMessage("itemcard.debug.favorite.added", "Added item to favorites: {0}").replace("{0}", item.getName()));
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("itemcard.error.favorite.toggle", "Failed to toggle favorite status: {0}").replace("{0}", e.getMessage()));
        }
        
        // Prevent event bubbling
        event.consume();
    }

    @Override
    public void refreshUI() {
        // Update tooltip for favorite icon
        updateTooltips();
        
        // Refresh item data display if item is set
        if (item != null) {
            // Re-set the item to refresh all localized content
            setItem(item);
        } else {
            // Clear and set placeholder text with current language
            clearItemData();
        }
    }

    private void updateTooltips() {
        if (favoriteIcon != null) {
            String tooltipText = localeService.getMessage("itemcard.tooltip.favorite", "Add/remove from favorites");
            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(favoriteIcon, tooltip);
        }
    }

    // Getter methods for external access
    public ItemDTO getItem() {
        return item;
    }

    public boolean isFavorite() {
        return isFavorite;
    }
}
