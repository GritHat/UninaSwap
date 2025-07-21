package com.uninaswap.server.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.uninaswap.common.enums.OfferStatus;

/**
 * Represents a free gift listing (donation)
 */
@Entity
@Table(name = "gift_listings")
@DiscriminatorValue("GIFT")
public class GiftListingEntity extends ListingEntity {

    @Column(nullable = false)
    private boolean pickupOnly;

    private String restrictions;

    // Thank-you offer settings
    @Column(nullable = false)
    private boolean allowThankYouOffers;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OfferEntity> thankYouOffers = new ArrayList<>();

    private String pickupLocation;

    // Default constructor
    public GiftListingEntity() {
        super();
    }

    // Constructor with required fields
    public GiftListingEntity(String title, String description, UserEntity creator, String ImagePath,
            boolean pickupOnly, String restrictions, boolean allowThankYouOffers) {
        super();
        this.setTitle(title);
        this.setDescription(description);
        this.setCreator(creator);
        this.setImagePath(ImagePath);
        this.pickupOnly = pickupOnly;
        this.restrictions = restrictions;
        this.allowThankYouOffers = allowThankYouOffers;
    }

    /**
     * Add an item to this listing with quantity
     * 
     * @param item     The item to add
     * @param quantity Number of this item to include
     */
    public void addItem(ItemEntity item, int quantity) {
        ListingItemEntity listingItem = new ListingItemEntity(this, item, quantity);
        this.getListingItems().add(listingItem);
    }

    /**
     * Record a new thank-you offer for this gift
     * 
     * @param offer The thank-you offer to add
     */
    public void addThankYouOffer(OfferEntity offer) {
        if (!allowThankYouOffers) {
            throw new IllegalStateException("Thank-you offers are not allowed for this gift");
        }

        offer.setListing(this);
        this.thankYouOffers.add(offer);
    }

    /**
     * Accept a thank-you offer
     * 
     * @param offerId The ID of the offer to accept
     * @return true if successfully accepted, false otherwise
     */
    public boolean acceptThankYouOffer(String offerId) {
        for (OfferEntity offer : thankYouOffers) {
            if (offer.getId().equals(offerId)) {
                offer.setStatus(OfferStatus.ACCEPTED);
                return true;
            }
        }
        return false;
    }

    /**
     * Decline a thank-you offer
     * 
     * @param offerId The ID of the offer to decline
     * @return true if successfully declined, false otherwise
     */
    public boolean declineThankYouOffer(String offerId) {
        for (OfferEntity offer : thankYouOffers) {
            if (offer.getId().equals(offerId)) {
                offer.setStatus(OfferStatus.REJECTED);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getListingType() {
        return "Gift";
    }

    @Override
    public String getPriceInfo() {
        StringBuilder sb = new StringBuilder("Free");

        if (pickupOnly) {
            sb.append(" (Pickup only)");
        }

        if (allowThankYouOffers) {
            sb.append(" (Thank-you offers welcome)");
        }

        return sb.toString();
    }

    // Getters and Setters
    public boolean isPickupOnly() {
        return pickupOnly;
    }

    public void setPickupOnly(boolean pickupOnly) {
        this.pickupOnly = pickupOnly;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }

    public boolean isAllowThankYouOffers() {
        return allowThankYouOffers;
    }

    public void setAllowThankYouOffers(boolean allowThankYouOffers) {
        this.allowThankYouOffers = allowThankYouOffers;
    }

    public List<OfferEntity> getThankYouOffers() {
        return thankYouOffers;
    }

    @Override
    public String getPickupLocation() {
        return pickupLocation;
    }

    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}