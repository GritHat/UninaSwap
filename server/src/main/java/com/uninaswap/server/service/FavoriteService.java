package com.uninaswap.server.service;

import com.uninaswap.common.dto.FavoriteDTO;
import com.uninaswap.common.dto.ListingDTO;
import com.uninaswap.server.entity.FavoriteEntity;
import com.uninaswap.server.entity.ListingEntity;
import com.uninaswap.server.entity.UserEntity;
import com.uninaswap.server.mapper.FavoriteMapper;
import com.uninaswap.server.mapper.ListingMapper;
import com.uninaswap.server.repository.FavoriteRepository;
import com.uninaswap.server.repository.ListingRepository;
import com.uninaswap.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ListingMapper listingMapper;

    /**
     * Add a listing to user's favorites
     */
    @Transactional
    public FavoriteDTO addFavorite(Long userId, String listingId) {
        logger.info("Adding favorite: user {} listing {}", userId, listingId);

        
        if (favoriteRepository.existsByUserIdAndListingId(userId, listingId)) {
            throw new IllegalStateException("Listing already in favorites");
        }

        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ListingEntity listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));

        
        FavoriteEntity favorite = new FavoriteEntity(user, listing);
        favorite = favoriteRepository.save(favorite);

        logger.info("Successfully added favorite for user {} listing {}", userId, listingId);
        return favoriteMapper.toDto(favorite);
    }

    /**
     * Remove a listing from user's favorites
     */
    @Transactional
    public void removeFavorite(Long userId, String listingId) {
        logger.info("Removing favorite: user {} listing {}", userId, listingId);

        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);

        logger.info("Successfully removed favorite for user {} listing {}", userId, listingId);
    }

    /**
     * Check if user has favorited a listing
     */
    @Transactional(readOnly = true)
    public boolean isFavorite(Long userId, String listingId) {
        return favoriteRepository.existsByUserIdAndListingId(userId, listingId);
    }

    /**
     * Get all favorite listings for a user
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getUserFavoriteListings(Long userId) {
        logger.info("Getting favorite listings for user: {}", userId);

        List<FavoriteEntity> favorites = favoriteRepository.findByUserIdWithDetails(userId);

        return favorites.stream()
                .map(favorite -> listingMapper.toDto(favorite.getListing()))
                .collect(Collectors.toList());
    }

    /**
     * Get paginated favorite listings for a user
     */
    @Transactional(readOnly = true)
    public Page<ListingDTO> getUserFavoriteListings(Long userId, int page, int size) {
        logger.info("Getting paginated favorite listings for user: {} (page {}, size {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<FavoriteEntity> favoritePage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return favoritePage.map(favorite -> listingMapper.toDto(favorite.getListing()));
    }

    /**
     * Get favorite listing IDs for a user
     */
    @Transactional(readOnly = true)
    public Set<String> getUserFavoriteListingIds(Long userId) {
        logger.info("Getting favorite listing IDs for user: {}", userId);

        List<String> favoriteIds = favoriteRepository.findListingIdsByUserId(userId);
        return favoriteIds.stream().collect(Collectors.toSet());
    }

    /**
     * Get all favorites for a user with full details
     */
    @Transactional(readOnly = true)
    public List<FavoriteDTO> getUserFavorites(Long userId) {
        logger.info("Getting all favorites for user: {}", userId);

        List<FavoriteEntity> favorites = favoriteRepository.findByUserIdWithDetails(userId);

        return favorites.stream()
                .map(favoriteMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Count favorites for a listing
     */
    @Transactional(readOnly = true)
    public long countListingFavorites(String listingId) {
        return favoriteRepository.countByListingId(listingId);
    }

    /**
     * Count total favorites for a user
     */
    @Transactional(readOnly = true)
    public long countUserFavorites(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    /**
     * Get most favorited listings
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMostFavoritedListings(int limit) {
        logger.info("Getting top {} most favorited listings", limit);

        Pageable pageable = PageRequest.of(0, limit);
        return favoriteRepository.findMostFavoritedListings(pageable);
    }

    /**
     * Toggle favorite status
     */
    @Transactional
    public boolean toggleFavorite(Long userId, String listingId) {
        logger.info("Toggling favorite: user {} listing {}", userId, listingId);

        if (favoriteRepository.existsByUserIdAndListingId(userId, listingId)) {
            removeFavorite(userId, listingId);
            return false;
        } else {
            addFavorite(userId, listingId);
            return true;
        }
    }
}