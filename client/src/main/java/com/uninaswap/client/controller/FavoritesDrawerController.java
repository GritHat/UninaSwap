package com.uninaswap.client.controller;

import com.uninaswap.client.service.FavoritesService;
import com.uninaswap.client.service.ImageService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.UserSessionService;
import com.uninaswap.client.util.AlertHelper;
import com.uninaswap.client.viewmodel.FavoriteViewModel;
import com.uninaswap.client.viewmodel.ListingItemViewModel;
import com.uninaswap.client.viewmodel.ListingViewModel;
import com.uninaswap.client.viewmodel.UserViewModel;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;

public class FavoritesDrawerController implements Refreshable {

    @FXML
    private HBox externalDrawerContainer;

    @FXML
    private VBox drawerContainer;

    // Favorites section
    @FXML
    private TitledPane favoritesPane;

    @FXML
    private VBox favoritesContainer;

    @FXML
    private Label favoritesCountLabel;

    // Following section
    @FXML
    private TitledPane followingPane;

    @FXML
    private VBox followingContainer;

    @FXML
    private Label followingCountLabel;

    // Services
    private final FavoritesService favoritesService = FavoritesService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();
    private final NavigationService navigationService = NavigationService.getInstance();
    private final UserSessionService userSessionService = UserSessionService.getInstance();

    // State
    private final BooleanProperty drawerVisible = new SimpleBooleanProperty(false);
    private MainController mainController;

    // Add reference to observable lists
    private ObservableList<FavoriteViewModel> userFavorites;

    // Animation fields
    private Timeline showAnimation;
    private Timeline hideAnimation;

    @FXML
    public void initialize() {
        setupDrawerToggle();
        setupExpandablePanes();
        setupFavoritesObservableList();
        loadFollowing();
        refreshUI();

        // Set initial state - drawer hidden
        drawerContainer.setVisible(false);
        drawerContainer.setManaged(false);
        drawerContainer.getStyleClass().add("drawer-hidden");
        
        // IMPORTANT: Initially disable mouse events since drawer starts hidden
        externalDrawerContainer.setMouseTransparent(true);
        
        // Add content animation class
        drawerContainer.getStyleClass().add("drawer-content-animated");
    }

    private void setupDrawerToggle() {
        drawerVisible.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                showDrawerAnimated();
            } else {
                hideDrawerAnimated();
            }
            
            // Notify main controller of state change
            if (mainController != null) {
                mainController.onFavoritesDrawerStateChanged();
            }
        });
    }

    /**
     * Show drawer with slide-in animation
     */
    private void showDrawerAnimated() {
        // Stop any existing animation
        if (hideAnimation != null) {
            hideAnimation.stop();
        }

        // Make sure the drawer is visible and managed before animation
        drawerContainer.setVisible(true);
        drawerContainer.setManaged(true);
        
        // IMPORTANT: Re-enable mouse events when showing
        externalDrawerContainer.setMouseTransparent(false);
        
        // Update CSS classes
        drawerContainer.getStyleClass().removeAll("drawer-hidden");
        drawerContainer.getStyleClass().add("drawer-visible");

        // Create slide-in animation
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), drawerContainer);
        slideIn.setFromX(280); // Start off-screen
        slideIn.setToX(0);     // End at normal position
        slideIn.setInterpolator(Interpolator.EASE_OUT);

        // Create fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), drawerContainer);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        // Combine animations
        ParallelTransition showAnimationTransition = new ParallelTransition(slideIn, fadeIn);

        showAnimation = new Timeline();
        showAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1), e -> showAnimationTransition.play()));
        showAnimation.play();
    }

    /**
     * Hide drawer with slide-out animation
     */
    private void hideDrawerAnimated() {
        // Stop any existing animation
        if (showAnimation != null) {
            showAnimation.stop();
        }

        // Create slide-out animation
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(250), drawerContainer);
        slideOut.setFromX(0);    // Start at normal position
        slideOut.setToX(280);    // End off-screen
        slideOut.setInterpolator(Interpolator.EASE_IN);

        // Create fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), drawerContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        // Combine animations
        ParallelTransition hideAnimationTransition = new ParallelTransition(slideOut, fadeOut);
        hideAnimationTransition.setOnFinished(e -> {
            // Hide and unmanage after animation completes
            drawerContainer.setVisible(false);
            drawerContainer.setManaged(false);
            
            // IMPORTANT: Disable mouse events when hidden
            externalDrawerContainer.setMouseTransparent(true);
            
            // Update style classes
            drawerContainer.getStyleClass().removeAll("drawer-visible");
            drawerContainer.getStyleClass().add("drawer-hidden");
        });

        hideAnimation = new Timeline();
        hideAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(1), e -> hideAnimationTransition.play()));
        hideAnimation.play();
    }

    private void setupExpandablePanes() {
        // Set favorites pane expanded by default
        favoritesPane.setExpanded(true);
        followingPane.setExpanded(false);

        // Add accordion behavior with smooth transitions
        favoritesPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && followingPane.isExpanded()) {
                // Create smooth collapse animation for following pane
                createExpandCollapseAnimation(followingPane, false).play();
            }
        });

        followingPane.expandedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && favoritesPane.isExpanded()) {
                // Create smooth collapse animation for favorites pane
                createExpandCollapseAnimation(favoritesPane, false).play();
            }
        });
    }

    /**
     * Create smooth expand/collapse animation for titled panes
     */
    private Timeline createExpandCollapseAnimation(TitledPane pane, boolean expand) {
        Timeline animation = new Timeline();
        
        if (expand) {
            animation.getKeyFrames().add(new KeyFrame(Duration.millis(200), e -> pane.setExpanded(true)));
        } else {
            animation.getKeyFrames().add(new KeyFrame(Duration.millis(200), e -> pane.setExpanded(false)));
        }
        
        return animation;
    }

    private void setupFavoritesObservableList() {
        // Get the observable list from FavoritesService
        userFavorites = favoritesService.getUserFavoritesList();

        // Set up listener for automatic updates
        userFavorites.addListener((ListChangeListener<FavoriteViewModel>) change -> {
            Platform.runLater(() -> {
                populateFavoritesList(userFavorites);
                updateFavoritesCount(userFavorites.size());
            });
        });
    }

    private void loadFavorites() {
        // If the observable list is empty, refresh from server
        if (userFavorites.isEmpty()) {
            favoritesService.refreshUserFavorites();
        } else {
            // Use existing data
            populateFavoritesList(userFavorites);
            updateFavoritesCount(userFavorites.size());
        }
    }

    private void loadFollowing() {
        // TODO: Implement when FollowerService is available
        // For now, use empty list
        Platform.runLater(() -> {
            populateFollowingList(List.of());
            updateFollowingCount(0);
        });
    }

    private void populateFavoritesList(ObservableList<FavoriteViewModel> favorites) {
        favoritesContainer.getChildren().clear();

        if (favorites.isEmpty()) {
            Label emptyLabel = new Label(localeService.getMessage("favorites.drawer.empty", "No favorites yet"));
            emptyLabel.getStyleClass().add("drawer-empty-label");
            favoritesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (FavoriteViewModel favorite : favorites) {
            if (favorite.getListing() != null) {
                VBox listingItem = createListingItem(favorite.getListing());
                favoritesContainer.getChildren().add(listingItem);
            }
        }
    }

    private void populateFollowingList(List<UserViewModel> following) {
        followingContainer.getChildren().clear();

        if (following.isEmpty()) {
            Label emptyLabel = new Label(localeService.getMessage("following.drawer.empty", "Not following anyone"));
            emptyLabel.getStyleClass().add("drawer-empty-label");
            followingContainer.getChildren().add(emptyLabel);
            return;
        }

        for (UserViewModel user : following) {
            VBox userItem = createUserItem(user);
            followingContainer.getChildren().add(userItem);
        }
    }

    private VBox createListingItem(ListingViewModel listing) {
        VBox item = new VBox(5);
        item.getStyleClass().add("drawer-item");

        // Listing image and title
        HBox header = new HBox(10);
        header.getStyleClass().add("drawer-item-header");

        // Thumbnail image
        ImageView thumbnail = new ImageView();
        thumbnail.setFitWidth(40);
        thumbnail.setFitHeight(40);
        thumbnail.setPreserveRatio(true);
        thumbnail.getStyleClass().add("drawer-thumbnail");

        // Load listing image using ImageService
        loadThumbnailImage(thumbnail, listing);

        // Title and type container
        VBox textContainer = new VBox(2);
        Label title = new Label(listing.getTitle());
        title.setMaxWidth(170);
        title.setTextOverrun(OverrunStyle.ELLIPSIS);
        title.getStyleClass().add("drawer-item-title");

        Text type = new Text(listing.getListingTypeValue());
        type.getStyleClass().add("drawer-item-subtitle");

        textContainer.getChildren().addAll(title, type);

        header.getChildren().addAll(thumbnail, textContainer);

        // Click handler with visual feedback
        item.setOnMouseClicked(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), item);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), item);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            
            SequentialTransition clickAnimation = new SequentialTransition(scaleDown, scaleUp);
            clickAnimation.setOnFinished(event -> handleListingClick(listing));
            clickAnimation.play();
        });

        item.getChildren().add(header);
        return item;
    }

    private VBox createUserItem(UserViewModel user) {
        VBox item = new VBox(5);
        item.getStyleClass().add("drawer-item");

        HBox header = new HBox(10);
        header.getStyleClass().add("drawer-item-header");

        // User avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setPreserveRatio(true);
        avatar.getStyleClass().add("drawer-avatar");

        // Load user image using ImageService
        loadAvatarImage(avatar, user);

        // User info
        VBox textContainer = new VBox(2);
        Text displayName = new Text(user.getDisplayName());
        displayName.getStyleClass().add("drawer-item-title");

        Text username = new Text(localeService.getMessage("drawer.user.username.format", "@{0}").replace("{0}", user.getUsername()));
        username.getStyleClass().add("drawer-item-subtitle");

        textContainer.getChildren().addAll(displayName, username);

        header.getChildren().addAll(avatar, textContainer);

        // Click handler with visual feedback
        item.setOnMouseClicked(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), item);
            scaleDown.setToX(0.95);
            scaleDown.setToY(0.95);
            
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), item);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            
            SequentialTransition clickAnimation = new SequentialTransition(scaleDown, scaleUp);
            clickAnimation.setOnFinished(event -> handleUserClick(user));
            clickAnimation.play();
        });

        item.getChildren().add(header);
        return item;
    }

    private void loadThumbnailImage(ImageView thumbnail, ListingViewModel listing) {
        // Get the first available image path from the listing
        String imagePath = getFirstImagePath(listing);

        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default")) {
            // Use ImageService to fetch the image
            ImageService.getInstance().fetchImage(imagePath)
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            if (image != null && !image.isError()) {
                                thumbnail.setImage(image);
                            } else {
                                setDefaultThumbnail(thumbnail);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println(localeService.getMessage("drawer.error.thumbnail.load", "Failed to load thumbnail image: {0}").replace("{0}", ex.getMessage()));
                        Platform.runLater(() -> setDefaultThumbnail(thumbnail));
                        return null;
                    });
        } else {
            setDefaultThumbnail(thumbnail);
        }
    }

    private String getFirstImagePath(ListingViewModel listing) {
        if (listing.getItems() != null && !listing.getItems().isEmpty()) {
            for (ListingItemViewModel item : listing.getItems()) {
                String imagePath = item.getItem().getImagePath();
                if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("default")) {
                    return imagePath;
                }
            }
        }
        return null;
    }

    private void loadAvatarImage(ImageView avatar, UserViewModel user) {
        String imagePath = user.getProfileImagePath();
        
        if (imagePath != null && !imagePath.isEmpty()) {
            ImageService.getInstance().fetchImage(imagePath)
                    .thenAccept(image -> {
                        Platform.runLater(() -> {
                            if (image != null && !image.isError()) {
                                avatar.setImage(image);
                            } else {
                                setDefaultAvatar(avatar);
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        System.err.println(localeService.getMessage("drawer.error.avatar.load", "Failed to load avatar image: {0}").replace("{0}", ex.getMessage()));
                        Platform.runLater(() -> setDefaultAvatar(avatar));
                        return null;
                    });
        } else {
            setDefaultAvatar(avatar);
        }
    }

    private void setDefaultThumbnail(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/immagine_generica.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println(localeService.getMessage("drawer.error.default.thumbnail", "Could not load default thumbnail: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void setDefaultAvatar(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/default_profile.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println(localeService.getMessage("drawer.error.default.avatar", "Could not load default avatar: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void handleListingClick(ListingViewModel listing) {
        try {
            Parent listingDetailsView = navigationService.loadListingDetails(listing);
            mainController.setContent(listingDetailsView);
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    localeService.getMessage("drawer.error.listing.navigation", "Failed to open listing details: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void handleUserClick(UserViewModel user) {
        try {
            Parent profileView = navigationService.loadProfileView(userSessionService.getUserViewModel());
            mainController.setContent(profileView);
        } catch (Exception e) {
            AlertHelper.showErrorAlert(
                    localeService.getMessage("error.title", "Error"),
                    localeService.getMessage("error.navigation", "Navigation Error"),
                    localeService.getMessage("drawer.error.profile.navigation", "Failed to open user profile: {0}").replace("{0}", e.getMessage()));
        }
    }

    private void updateFavoritesCount(int count) {
        String countText = localeService.getMessage("favorites.drawer.count", "({0})").replace("{0}", String.valueOf(count));
        favoritesCountLabel.setText(countText);
    }

    private void updateFollowingCount(int count) {
        String countText = localeService.getMessage("following.drawer.count", "({0})").replace("{0}", String.valueOf(count));
        followingCountLabel.setText(countText);
    }

    public void refreshData() {
        // Simply trigger refresh - the observable list will update automatically
        favoritesService.refreshUserFavorites();
        loadFollowing();
    }

    // Add method to instantly show/hide without animation (useful for initialization)
    public void setDrawerVisibleInstantly(boolean visible) {
        drawerContainer.setVisible(visible);
        drawerContainer.setManaged(visible);
        
        // IMPORTANT: Set mouse transparency based on visibility
        externalDrawerContainer.setMouseTransparent(!visible);
        
        if (visible) {
            drawerContainer.getStyleClass().removeAll("drawer-hidden");
            drawerContainer.getStyleClass().add("drawer-visible");
            drawerContainer.setTranslateX(0);
            drawerContainer.setOpacity(1.0);
        } else {
            drawerContainer.getStyleClass().removeAll("drawer-visible");
            drawerContainer.getStyleClass().add("drawer-hidden");
            drawerContainer.setTranslateX(280);
            drawerContainer.setOpacity(0.0);
        }
        
        drawerVisible.set(visible);
    }

    // Keep all existing public methods
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public boolean isDrawerVisible() {
        return drawerVisible.get();
    }

    public BooleanProperty drawerVisibleProperty() {
        return drawerVisible;
    }

    public void showDrawer() {
        drawerVisible.set(true);
    }

    public void hideDrawer() {
        drawerVisible.set(false);
    }

    @Override
    public void refreshUI() {
        // Update section titles in titled panes
        updateTitledPaneLabels();
        
        // Refresh counts with current language
        if (userFavorites != null) {
            updateFavoritesCount(userFavorites.size());
        }
        updateFollowingCount(0); // Following count when implemented
        
        // Refresh the favorite and following lists to update any hardcoded text
        if (userFavorites != null && !userFavorites.isEmpty()) {
            populateFavoritesList(userFavorites);
        }
        
        // Refresh following list
        populateFollowingList(List.of());
    }

    private void updateTitledPaneLabels() {
        // Update Favorites section title
        HBox favoritesGraphic = (HBox) favoritesPane.getGraphic();
        if (favoritesGraphic != null) {
            Label favoritesLabel = (Label) favoritesGraphic.getChildren().get(1);
            if (favoritesLabel != null) {
                favoritesLabel.setText(localeService.getMessage("drawer.section.favorites", "Favorites"));
            }
        }
        
        // Update Following section title
        HBox followingGraphic = (HBox) followingPane.getGraphic();
        if (followingGraphic != null) {
            Label followingLabel = (Label) followingGraphic.getChildren().get(1);
            if (followingLabel != null) {
                followingLabel.setText(localeService.getMessage("drawer.section.following", "Following"));
            }
        }
    }
}