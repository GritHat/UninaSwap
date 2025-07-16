package com.uninaswap.common.dto;

/**
 * DTO representing a free gift listing (donation)
 */
public class GiftListingDTO extends ListingDTO {

    private boolean pickupOnly;
    private String restrictions;
    private boolean allowThankYouOffers = true;

    // Default constructor
    public GiftListingDTO() {
        super();
    }

    @Override
    public String getListingTypeValue() {
        return "GIFT";
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

    // Getters and setters
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
}