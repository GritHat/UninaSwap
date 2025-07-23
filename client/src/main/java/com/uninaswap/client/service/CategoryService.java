package com.uninaswap.client.service;

import com.uninaswap.common.enums.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.List;

/**
 * 
 */
public class CategoryService {
    /**
     * 
     */
    private static CategoryService instance;
    /**
     * 
     */
    private final ObservableList<Category> categories;
    /**
     * 
     */
    private final LocaleService localeService;

    /**
     * 
     */
    private CategoryService() {
        this.localeService = LocaleService.getInstance();
        this.categories = FXCollections.observableArrayList(Arrays.asList(Category.values()));
    }

    /**
     * @return
     */
    public static CategoryService getInstance() {
        if (instance == null) {
            instance = new CategoryService();
        }
        return instance;
    }

    /**
     * @return
     */
    public ObservableList<Category> getCategories() {
        return categories;
    }

    /**
     * @return
     */
    public List<Category> getSelectableCategories() {
        return categories.stream()
                .filter(cat -> cat != Category.ALL)
                .toList();
    }

    /**
     * @param category
     * @return
     */
    public String getLocalizedCategoryName(Category category) {
        try {
            return localeService.getMessage(category.getMessageKey());
        } catch (Exception e) {
            return category.getDisplayName();
        }
    }

    /**
     * @param displayName
     * @return
     */
    public Category getCategoryByDisplayName(String displayName) {
        return categories.stream()
                .filter(cat -> cat.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElse(Category.OTHER);
    }
}