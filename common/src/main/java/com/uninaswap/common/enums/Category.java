package com.uninaswap.common.enums;

/**
 * 
 */
public enum Category {
    /**
     * 
     */
    ALL("category.all", "Tutte le categorie"),
    /**
     * 
     */
    ELECTRONICS("category.electronics", "Elettronica"),
    /**
     * 
     */
    CLOTHING("category.clothing", "Abbigliamento"),
    /**
     * 
     */
    BOOKS("category.books", "Libri"),
    /**
     * 
     */
    HOME_GARDEN("category.home.garden", "Casa e Giardino"),
    /**
     * 
     */
    SPORTS("category.sports", "Sport e Tempo Libero"),
    /**
     * 
     */
    VEHICLES("category.vehicles", "Veicoli"),
    /**
     * 
     */
    COLLECTIBLES("category.collectibles", "Collezionismo"),
    /**
     * 
     */
    MUSIC_INSTRUMENTS("category.music.instruments", "Strumenti Musicali"),
    /**
     * 
     */
    TOYS_GAMES("category.toys.games", "Giochi e Giocattoli"),
    /**
     * 
     */
    BEAUTY_HEALTH("category.beauty.health", "Bellezza e Salute"),
    /**
     * 
     */
    FOOD_BEVERAGES("category.food.beverages", "Cibo e Bevande"),
    /**
     * 
     */
    SERVICES("category.services", "Servizi"),
    /**
     * 
     */
    OTHER("category.other", "Altro");

    /**
     * 
     */
    private final String messageKey;
    /**
     * 
     */
    private final String displayName;

    /**
     * @param messageKey
     * @param displayName
     */
    Category(String messageKey, String displayName) {
        this.messageKey = messageKey;
        this.displayName = displayName;
    }

    /**
     * @return
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * @return
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * @param value
     * @return
     */
    public static Category fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        
        String trimmedValue = value.trim();
        
        for (Category category : values()) {
            
            if (category.name().equalsIgnoreCase(trimmedValue)) {
                return category;
            }
            
            if (category.getDisplayName().equalsIgnoreCase(trimmedValue) ||
                category.getMessageKey().equalsIgnoreCase(trimmedValue)) {
                return category;
            }
        }
        
        return OTHER;
    }
}