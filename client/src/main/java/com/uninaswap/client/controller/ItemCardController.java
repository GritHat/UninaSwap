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

public class ItemCardController {

    @FXML private VBox itemCard;
    @FXML private ImageView itemImage;
    @FXML private Text itemName;
    @FXML private Text categoryText;
    @FXML private Text sellerName;
    @FXML private Text itemPrice;
    @FXML private ImageView favoriteIcon;

    private ItemDTO item;
    private boolean isFavorite = false;

    private final NavigationService navigationService = NavigationService.getInstance();
    private final FavoritesService favoritesService = FavoritesService.getInstance();

    @FXML
    public void initialize() {
        // Tooltip per il preferito
        Tooltip.install(favoriteIcon, new Tooltip("Aggiungi/rimuovi dai preferiti"));
    }
/*da aggiungere
    public void setItem(ItemDTO item) {
        this.item = item;
        itemName.setText(item.getName());
        categoryText.setText(item.getCategory());
        sellerName.setText(item.getSellerName());
        itemPrice.setText(item.getFormattedPrice());
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            itemImage.setImage(new Image(item.getImageUrl(), true));
        }
        setFavorite(favoritesService.isFavorite(item.getId()));
    }
*/
    private void setFavorite(boolean favorite) {
        isFavorite = favorite;
        String iconPath = favorite
            ? "/images/elenco_preferiti.png"
            : "/images/streamline-ultimate-colors---free--24x24-PNG/Heart-1--Streamline-Ultimate.png";
        favoriteIcon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
    }

    @FXML
    private void openItemDetails(MouseEvent event) {
        if (item != null) {
            navigationService.navigateToItemDetails(item.getId());
        }
    }

    @FXML
    private void toggleFavorite(MouseEvent event) {
        if (item == null) return;
        if (isFavorite) {
            favoritesService.removeFavorite(item.getId());
            setFavorite(false);
        } else {
            favoritesService.addFavorite(item.getId());
            setFavorite(true);
        }
        event.consume();
    }
}
