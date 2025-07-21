package com.uninaswap.client.controller;

import com.uninaswap.common.dto.UserDTO;
import com.uninaswap.client.service.NavigationService;
import com.uninaswap.client.service.LocaleService;
import com.uninaswap.client.service.EventBusService;
import com.uninaswap.client.constants.EventTypes;
import com.uninaswap.client.util.AlertHelper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.util.List;

public class UserCardController implements Refreshable {

    @FXML
    private VBox itemCard;
    @FXML
    private ImageView itemImage;
    @FXML
    private Text itemName;

    private UserDTO user;
    private List<UserDTO> users;

    private final NavigationService navigationService = NavigationService.getInstance();
    private final LocaleService localeService = LocaleService.getInstance();

    public UserCardController() {
    }

    public UserCardController(UserDTO user) {
        this.user = user;
    }

    @FXML
    public void initialize() {
        // Subscribe to locale changes
        EventBusService.getInstance().subscribe(EventTypes.LOCALE_CHANGED, _ -> {
            Platform.runLater(this::refreshUI);
        });

        // Set user data if available
        if (user != null) {
            setUser(user);
        }

        // Initial UI refresh
        refreshUI();
        
        System.out.println(localeService.getMessage("usercard.debug.initialized", "UserCard controller initialized"));
    }

    @Override
    public void refreshUI() {
        // Update user name display
        if (user != null) {
            updateUserDisplay();
        } else {
            setPlaceholderDisplay();
        }

        // Update tooltips
        updateTooltips();
        
        System.out.println(localeService.getMessage("usercard.debug.ui.refreshed", "UserCard UI refreshed"));
    }

    /**
     * Sets the user data for this card.
     * 
     * @param user The user to display
     */
    public void setUser(UserDTO user) {
        this.user = user;
        updateUserDisplay();
        
        System.out.println(localeService.getMessage("usercard.debug.user.set", "User set for card: {0}")
            .replace("{0}", user != null ? user.getUsername() : "null"));
    }

    /**
     * Updates the display with current user data.
     */
    private void updateUserDisplay() {
        if (user == null) {
            setPlaceholderDisplay();
            return;
        }

        // Set user name
        if (itemName != null) {
            String displayName = getUserDisplayName(user);
            itemName.setText(displayName);
        }

        // Load user profile image
        loadUserImage();
    }

    /**
     * Gets the appropriate display name for a user.
     */
    private String getUserDisplayName(UserDTO user) {
        if (user == null) {
            return localeService.getMessage("usercard.name.placeholder", "User Name");
        }

        // Try to get full name first
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        
        if (firstName != null && !firstName.trim().isEmpty() && 
            lastName != null && !lastName.trim().isEmpty()) {
            return firstName.trim() + " " + lastName.trim();
        }
        
        // Fall back to username
        String username = user.getUsername();
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        
        // Fall back to email
        String email = user.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            return email.split("@")[0]; // Use part before @
        }
        
        // Final fallback
        return localeService.getMessage("usercard.name.unknown", "Unknown User");
    }

    /**
     * Loads the user's profile image.
     */
    private void loadUserImage() {
        if (itemImage == null) {
            return;
        }

        try {
            String imagePath = getUserImagePath();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                // Try to load custom user image
                Image userImage = new Image(getClass().getResourceAsStream(imagePath));
                if (!userImage.isError()) {
                    itemImage.setImage(userImage);
                    System.out.println(localeService.getMessage("usercard.debug.image.loaded", "User image loaded: {0}")
                        .replace("{0}", imagePath));
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println(localeService.getMessage("usercard.debug.image.error", "Error loading user image: {0}")
                .replace("{0}", e.getMessage()));
        }

        // Fall back to default image
        setDefaultUserImage();
    }

    /**
     * Gets the image path for the current user.
     */
    private String getUserImagePath() {
        if (user == null) {
            return null;
        }

        // TODO: Implement when user profile image path is available in UserDTO
        // return user.getProfileImagePath();
        return null;
    }

    /**
     * Sets the default user image.
     */
    private void setDefaultUserImage() {
        if (itemImage != null) {
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/icons/user_circle.png"));
                itemImage.setImage(defaultImage);
                System.out.println(localeService.getMessage("usercard.debug.default.image.set", "Default user image set"));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("usercard.error.default.image", "Failed to load default user image: {0}")
                    .replace("{0}", e.getMessage()));
            }
        }
    }

    /**
     * Sets placeholder display when no user is available.
     */
    private void setPlaceholderDisplay() {
        if (itemName != null) {
            itemName.setText(localeService.getMessage("usercard.name.placeholder", "User Name"));
        }
        setDefaultUserImage();
    }

    /**
     * Updates tooltips with localized text.
     */
    private void updateTooltips() {
        if (itemCard != null) {
            String tooltipText = user != null ? 
                localeService.getMessage("usercard.tooltip.view.profile", "View {0}'s profile")
                    .replace("{0}", getUserDisplayName(user)) :
                localeService.getMessage("usercard.tooltip.no.user", "No user data available");
            
            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(itemCard, tooltip);
        }
    }

    /**
     * Loads user cards into a container.
     * 
     * @param container The container to load cards into
     * @param <T> The type of container
     */
    public <T extends Pane> void loadUserCardsIntoTab(T container) {
        if (container == null) {
            System.err.println(localeService.getMessage("usercard.error.null.container", "Cannot load user cards: container is null"));
            return;
        }

        container.getChildren().clear();
        
        if (users == null || users.isEmpty()) {
            System.out.println(localeService.getMessage("usercard.debug.no.users", "No users available to load"));
            return;
        }

        try {
            for (UserDTO user : users) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserCard.fxml"));
                    loader.setResources(localeService.getResourceBundle());
                    
                    UserCardController controller = new UserCardController(user);
                    loader.setController(controller);
                    
                    Pane userCard = loader.load();
                    container.getChildren().add(userCard);
                    
                    System.out.println(localeService.getMessage("usercard.debug.card.loaded", "User card loaded for: {0}")
                        .replace("{0}", user.getUsername() != null ? user.getUsername() : "unknown"));
                        
                } catch (IOException e) {
                    System.err.println(localeService.getMessage("usercard.error.load.card", "Failed to load user card: {0}")
                        .replace("{0}", e.getMessage()));
                    
                    AlertHelper.showErrorAlert(
                        localeService.getMessage("usercard.load.error.title", "Load Error"),
                        localeService.getMessage("usercard.load.error.header", "Failed to load user card"),
                        localeService.getMessage("usercard.load.error.content", "Could not load user card: {0}")
                            .replace("{0}", e.getMessage())
                    );
                }
            }
            
            System.out.println(localeService.getMessage("usercard.debug.all.cards.loaded", "All user cards loaded successfully. Total: {0}")
                .replace("{0}", String.valueOf(users.size())));
                
        } catch (Exception e) {
            System.err.println(localeService.getMessage("usercard.error.load.cards", "Error loading user cards: {0}")
                .replace("{0}", e.getMessage()));
        }
    }

    /**
     * Handles click events to open user details.
     */
    @FXML
    private void openUserDetails(MouseEvent event) {
        if (user != null) {
            try {
                navigationService.navigateToUserProfile(user.getId());
                System.out.println(localeService.getMessage("usercard.debug.details.opened", "Opening user details for: {0}")
                    .replace("{0}", getUserDisplayName(user)));
            } catch (Exception e) {
                System.err.println(localeService.getMessage("usercard.error.navigation", "Failed to navigate to user details: {0}")
                    .replace("{0}", e.getMessage()));
                    
                AlertHelper.showErrorAlert(
                    localeService.getMessage("usercard.navigation.error.title", "Navigation Error"),
                    localeService.getMessage("usercard.navigation.error.header", "Could not open user profile"),
                    localeService.getMessage("usercard.navigation.error.content", "Please try again later.")
                );
            }
        } else {
            System.out.println(localeService.getMessage("usercard.debug.no.user.details", "No user available for details view"));
            
            AlertHelper.showWarningAlert(
                localeService.getMessage("usercard.no.user.title", "No User Selected"),
                localeService.getMessage("usercard.no.user.header", "No user data available"),
                localeService.getMessage("usercard.no.user.content", "Please select a valid user to view their profile.")
            );
        }
    }

    // Public getter methods for external access
    
    /**
     * Gets the current user.
     * 
     * @return The current user or null if none set
     */
    public UserDTO getUser() {
        return user;
    }

    /**
     * Gets the list of users.
     * 
     * @return The list of users
     */
    public List<UserDTO> getUsers() {
        return users;
    }

    /**
     * Sets the list of users.
     * 
     * @param users The list of users to set
     */
    public void setUsers(List<UserDTO> users) {
        this.users = users;
        System.out.println(localeService.getMessage("usercard.debug.users.set", "Users list set with {0} users")
            .replace("{0}", users != null ? String.valueOf(users.size()) : "0"));
    }

    /**
     * Checks if a user is currently set.
     * 
     * @return true if a user is set, false otherwise
     */
    public boolean hasUser() {
        return user != null;
    }

    /**
     * Gets the display name of the current user.
     * 
     * @return The display name or placeholder text
     */
    public String getCurrentDisplayName() {
        return user != null ? getUserDisplayName(user) : 
            localeService.getMessage("usercard.name.placeholder", "User Name");
    }

    /**
     * Clears the current user and resets the display.
     */
    public void clearUser() {
        this.user = null;
        setPlaceholderDisplay();
        System.out.println(localeService.getMessage("usercard.debug.user.cleared", "User data cleared"));
    }

    /**
     * Refreshes the user display (useful for external updates).
     */
    public void refreshUserDisplay() {
        updateUserDisplay();
        System.out.println(localeService.getMessage("usercard.debug.display.refreshed", "User display refreshed"));
    }
}
