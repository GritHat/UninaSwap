package com.uninaswap.common.dto;

/**
 * DTO representing a free gift listing (donation)
 */
/**
 * 
 */
public class GiftListingDTO extends ListingDTO {

    /**
     * 
     */
    private boolean pickupOnly;
    /**
     * 
     */
    private String restrictions;
    /**
     * 
     */
    private boolean allowThankYouOffers = true;
    /**
     * 
     */
    private String pickupLocation;

    
    /**
     * 
     */
    public GiftListingDTO() {
        super();
    }

    /**
     *
     */
    @Override
    public String getListingTypeValue() {
        return "GIFT";
    }

    /**
     *
     */
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

    
    /**
     * @return
     */
    public boolean isPickupOnly() {
        return pickupOnly;
    }

    /**
     * @param pickupOnly
     */
    public void setPickupOnly(boolean pickupOnly) {
        this.pickupOnly = pickupOnly;
    }

    /**
     * @return
     */
    public String getRestrictions() {
        return restrictions;
    }

    /**
     * @param restrictions
     */
    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }

    /**
     * @return
     */
    public boolean isAllowThankYouOffers() {
        return allowThankYouOffers;
    }

    /**
     * @param allowThankYouOffers
     */
    public void setAllowThankYouOffers(boolean allowThankYouOffers) {
        this.allowThankYouOffers = allowThankYouOffers;
    }

    /**
     *
     */
    @Override
    public String getPickupLocation() {
        return pickupLocation;
    }

    /**
     *
     */
    @Override
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}