package com.uninaswap.client.mapper;

import com.uninaswap.client.viewmodel.*;
import com.uninaswap.common.dto.*;

import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class ViewModelMapper {

    private static ViewModelMapper instance;

    private ViewModelMapper() {
    }

    public static synchronized ViewModelMapper getInstance() {
        if (instance == null) {
            instance = new ViewModelMapper();
        }
        return instance;
    }

    // Offer mappings
    public OfferViewModel toViewModel(OfferDTO dto) {
        if (dto == null)
            return null;

        OfferViewModel viewModel = new OfferViewModel();
        viewModel.setId(dto.getId());
        viewModel.setListingId(dto.getListingId());
        viewModel.setUser(toViewModel(dto.getOfferingUser()));
        viewModel.setCreatedAt(dto.getCreatedAt());
        viewModel.setUpdatedAt(dto.getUpdatedAt());
        viewModel.setStatus(dto.getStatus());
        viewModel.setAmount(dto.getAmount());
        viewModel.setCurrency(dto.getCurrency());
        viewModel.setMessage(dto.getMessage());
        viewModel.setListing(toViewModel(dto.getListing()));

        if (dto.getOfferItems() != null) {
            List<OfferItemViewModel> itemViewModels = dto.getOfferItems().stream()
                    .map(this::toViewModel)
                    .collect(Collectors.toList());
            viewModel.getOfferItems().setAll(itemViewModels);
        }

        return viewModel;
    }

    public OfferDTO toDTO(OfferViewModel viewModel) {
        if (viewModel == null)
            return null;

        OfferDTO dto = new OfferDTO();
        dto.setId(viewModel.getId());
        dto.setListingId(viewModel.getListingId());
        dto.setOfferingUser(toDTO(viewModel.getOfferingUser()));
        dto.setCreatedAt(viewModel.getCreatedAt());
        dto.setUpdatedAt(viewModel.getUpdatedAt());
        dto.setStatus(viewModel.getStatus());
        dto.setAmount(viewModel.getAmount());
        dto.setCurrency(viewModel.getCurrency());
        dto.setMessage(viewModel.getMessage());
        dto.setListing(toDTO(viewModel.getListing()));

        if (!viewModel.getOfferItems().isEmpty()) {
            List<OfferItemDTO> itemDTOs = viewModel.getOfferItems().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            dto.setOfferItems(itemDTOs);
        }

        return dto;
    }

    // OfferItem mappings
    public OfferItemViewModel toViewModel(OfferItemDTO dto) {
        if (dto == null)
            return null;

        return new OfferItemViewModel(
                dto.getItemId(),
                dto.getItemName(),
                dto.getItemImagePath(),
                dto.getCondition(),
                dto.getQuantity());
    }

    public OfferItemDTO toDTO(OfferItemViewModel viewModel) {
        if (viewModel == null)
            return null;

        return new OfferItemDTO(
                viewModel.getItemId(),
                viewModel.getItemName(),
                viewModel.getItemImagePath(),
                viewModel.getCondition(),
                viewModel.getQuantity());
    }

    // User mappings
    public UserViewModel toViewModel(UserDTO dto) {
        if (dto == null)
            return null;

        UserViewModel viewModel = new UserViewModel();
        viewModel.setId(dto.getId());
        viewModel.setUsername(dto.getUsername());
        viewModel.setEmail(dto.getEmail());
        viewModel.setFirstName(dto.getFirstName());
        viewModel.setLastName(dto.getLastName());
        viewModel.setProfileImagePath(dto.getProfileImagePath());
        viewModel.setCreatedAt(dto.getCreatedAt());
        viewModel.setLastLoginAt(dto.getLastLoginAt());
        viewModel.setActive(dto.isActive());
        viewModel.setPhoneNumber(dto.getPhoneNumber());
        viewModel.setAddress(dto.getAddress());
        viewModel.setCity(dto.getCity());
        viewModel.setCountry(dto.getCountry());
        viewModel.setBio(dto.getBio());
        // Add rating and review count if available in DTO
        // viewModel.setRating(dto.getRating());
        // viewModel.setReviewCount(dto.getReviewCount());

        return viewModel;
    }

    public UserDTO toDTO(UserViewModel viewModel) {
        if (viewModel == null)
            return null;

        UserDTO dto = new UserDTO();
        dto.setId(viewModel.getId());
        dto.setUsername(viewModel.getUsername());
        dto.setEmail(viewModel.getEmail());
        dto.setFirstName(viewModel.getFirstName());
        dto.setLastName(viewModel.getLastName());
        dto.setProfileImagePath(viewModel.getProfileImagePath());
        dto.setCreatedAt(viewModel.getCreatedAt());
        dto.setLastLoginAt(viewModel.getLastLoginAt());
        dto.setActive(viewModel.isActive());
        dto.setPhoneNumber(viewModel.getPhoneNumber());
        dto.setAddress(viewModel.getAddress());
        dto.setCity(viewModel.getCity());
        dto.setCountry(viewModel.getCountry());
        dto.setBio(viewModel.getBio());

        return dto;
    }

    // Item mappings
    public ItemViewModel toViewModel(ItemDTO dto) {
        if (dto == null)
            return null;

        ItemViewModel viewModel = new ItemViewModel();
        viewModel.setId(dto.getId());
        viewModel.setName(dto.getName());
        viewModel.setDescription(dto.getDescription());
        viewModel.setItemCategory(dto.getCategory());
        viewModel.setCondition(dto.getCondition());
        viewModel.setYear(dto.getYearOfProduction());
        viewModel.setTotalQuantity(dto.getStockQuantity());
        viewModel.setAvailableQuantity(dto.getAvailableQuantity());
        viewModel.setImagePath(dto.getImagePath());
        viewModel.setCreatedAt(dto.getCreatedAt());
        viewModel.setUpdatedAt(dto.getUpdatedAt());
        viewModel.setOwner(toViewModel(dto.getOwner()));
        viewModel.setAvailable(dto.isAvailable());
        viewModel.setVisible(dto.isVisible());

        return viewModel;
    }

    public ItemDTO toDTO(ItemViewModel viewModel) {
        if (viewModel == null)
            return null;

        ItemDTO dto = new ItemDTO();
        dto.setId(viewModel.getId());
        dto.setName(viewModel.getName());
        dto.setDescription(viewModel.getDescription());
        dto.setCategory(viewModel.getItemCategory());
        dto.setCondition(viewModel.getCondition());
        dto.setYearOfProduction(viewModel.getYear());
        dto.setStockQuantity(viewModel.getTotalQuantity());
        dto.setAvailableQuantity(viewModel.getAvailableQuantity());
        dto.setImagePath(viewModel.getImagePath());
        dto.setCreatedAt(viewModel.getCreatedAt());
        dto.setUpdatedAt(viewModel.getUpdatedAt());
        dto.setOwnerId(viewModel.getOwner() != null ? viewModel.getOwner().getId() : null);
        dto.setOwner(toDTO(viewModel.getOwner()));
        dto.setAvailable(viewModel.isAvailable());
        dto.setVisible(viewModel.isVisible());

        return dto;
    }

    // Listing mappings
    public ListingViewModel toViewModel(ListingDTO dto) {
        if (dto == null)
            return null;
        ListingViewModel viewModel;
        if (dto instanceof GiftListingDTO) {
            viewModel = new GiftListingViewModel();
            ((GiftListingViewModel) viewModel).setPickupOnly(((GiftListingDTO) dto).isPickupOnly());
            ((GiftListingViewModel) viewModel).setAllowThankYouOffers(((GiftListingDTO) dto).isAllowThankYouOffers());
        } else if (dto instanceof TradeListingDTO) {
            viewModel = new TradeListingViewModel();
            ((TradeListingViewModel) viewModel).setAcceptMoneyOffers(((TradeListingDTO) dto).isAcceptMoneyOffers());
        } else if (dto instanceof SellListingDTO) {
            viewModel = new SellListingViewModel();
        } else if (dto instanceof AuctionListingDTO) {
            viewModel = new AuctionListingViewModel();
        } else {
            throw new IllegalArgumentException("Unknown ListingDTO type: " + dto.getClass().getName());
        }
        viewModel.setId(dto.getId());
        viewModel.setTitle(dto.getTitle());
        viewModel.setDescription(dto.getDescription());
        viewModel.setCreatedAt(dto.getCreatedAt());
        viewModel.setUpdatedAt(dto.getUpdatedAt());
        viewModel.setUser(toViewModel(dto.getCreator()));
        viewModel.setStatus(dto.getStatus());
        viewModel.setFeatured(dto.isFeatured());
        if (dto.getItems() != null) {
            List<ListingItemViewModel> itemViewModels = dto.getItems().stream()
                    .map(this::toViewModel)
                    .collect(Collectors.toList());
            viewModel.getItems().setAll(itemViewModels);
        }
        return viewModel;
    }

    public ListingDTO toDTO(ListingViewModel viewModel) {
        if (viewModel == null)
            return null;
        ListingDTO dto;
        if (viewModel instanceof GiftListingViewModel) {
            dto = new GiftListingDTO();
        } else if (viewModel instanceof TradeListingViewModel) {
            dto = new TradeListingDTO();
        } else if (viewModel instanceof SellListingViewModel) {
            dto = new SellListingDTO();
        } else if (viewModel instanceof AuctionListingViewModel) {
            dto = new AuctionListingDTO();
        } else {
            throw new IllegalArgumentException("Unknown ListingViewModel type: " + viewModel.getClass().getName());
        }
        dto.setId(viewModel.getId());
        dto.setTitle(viewModel.getTitle());
        dto.setDescription(viewModel.getDescription());
        dto.setCreatedAt(viewModel.getCreatedAt());
        dto.setUpdatedAt(viewModel.getUpdatedAt());
        dto.setCreator(toDTO(viewModel.getUser()));
        dto.setStatus(viewModel.getStatus());
        dto.setFeatured(viewModel.isFeatured());
        if (!viewModel.getItems().isEmpty()) {
            List<ListingItemDTO> itemDTOs = viewModel.getItems().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        return dto;
    }

    public ListingItemViewModel toViewModel(ListingItemDTO dto) {
        if (dto == null)
            return null;

        ListingItemViewModel viewModel = new ListingItemViewModel();
        viewModel.setId(dto.getItemId());
        viewModel.setName(dto.getItemName());
        viewModel.setDescription(dto.getItem().getDescription());
        viewModel.setImagePath(dto.getItem().getImagePath());
        viewModel.setQuantity(dto.getQuantity());
        viewModel.setItem(toViewModel(dto.getItem()));

        return viewModel;
    }

    public ListingItemDTO toDTO(ListingItemViewModel viewModel) {
        if (viewModel == null)
            return null;

        ListingItemDTO dto = new ListingItemDTO();
        dto.setItemId(viewModel.getId());
        dto.setItemName(viewModel.getName());
        dto.setItemImagePath(viewModel.getImagePath());
        dto.setQuantity(viewModel.getQuantity());
        dto.setItem(toDTO(viewModel.getItem()));

        return dto;
    }

    // Review mappings
    public ReviewViewModel toViewModel(ReviewDTO dto) {
        if (dto == null)
            return null;

        ReviewViewModel viewModel = new ReviewViewModel();
        viewModel.setId(dto.getId());
        viewModel.setReviewer(toViewModel(dto.getReviewer()));
        viewModel.setReviewedUser(toViewModel(dto.getReviewedUser()));
        viewModel.setOfferId(dto.getOfferId());
        viewModel.setScore(dto.getScore());
        viewModel.setComment(dto.getComment());
        viewModel.setCreatedAt(dto.getCreatedAt());
        viewModel.setUpdatedAt(dto.getUpdatedAt());

        return viewModel;
    }

    public ReviewDTO toDTO(ReviewViewModel viewModel) {
        if (viewModel == null)
            return null;

        ReviewDTO dto = new ReviewDTO();
        dto.setId(viewModel.getId());
        dto.setReviewer(toDTO(viewModel.getReviewer()));
        dto.setReviewedUser(toDTO(viewModel.getReviewedUser()));
        dto.setOfferId(viewModel.getOfferId());
        dto.setScore(viewModel.getScore());
        dto.setComment(viewModel.getComment());
        dto.setCreatedAt(viewModel.getCreatedAt());
        dto.setUpdatedAt(viewModel.getUpdatedAt());

        return dto;
    }

    // User Report mappings
    public UserReportViewModel toViewModel(UserReportDTO dto) {
        if (dto == null)
            return null;

        UserReportViewModel viewModel = new UserReportViewModel();
        viewModel.setId(dto.getId());
        viewModel.setReportingUser(toViewModel(dto.getReportingUser()));
        viewModel.setReportedUser(toViewModel(dto.getReportedUser()));
        viewModel.setReason(dto.getReason());
        viewModel.setDescription(dto.getDescription());
        viewModel.setCreatedAt(dto.getCreatedAt());
        viewModel.setUpdatedAt(dto.getUpdatedAt());
        viewModel.setReviewed(dto.isReviewed());
        viewModel.setAdminNotes(dto.getAdminNotes());

        return viewModel;
    }

    public UserReportDTO toDTO(UserReportViewModel viewModel) {
        if (viewModel == null)
            return null;

        UserReportDTO dto = new UserReportDTO();
        dto.setId(viewModel.getId());
        dto.setReportingUser(toDTO(viewModel.getReportingUser()));
        dto.setReportedUser(toDTO(viewModel.getReportedUser()));
        dto.setReason(viewModel.getReason());
        dto.setDescription(viewModel.getDescription());
        dto.setCreatedAt(viewModel.getCreatedAt());
        dto.setUpdatedAt(viewModel.getUpdatedAt());
        dto.setReviewed(viewModel.isReviewed());
        dto.setAdminNotes(viewModel.getAdminNotes());

        return dto;
    }

    // Listing Report mappings
    public ListingReportViewModel toViewModel(ListingReportDTO dto) {
        if (dto == null)
            return null;

        ListingReportViewModel viewModel = new ListingReportViewModel();
        viewModel.setId(dto.getId());
        viewModel.setReportingUser(toViewModel(dto.getReportingUser()));
        viewModel.setReportedListing(toViewModel(dto.getReportedListing()));
        viewModel.setReason(dto.getReason());
        viewModel.setDescription(dto.getDescription());
        viewModel.setCreatedAt(dto.getCreatedAt());
        viewModel.setUpdatedAt(dto.getUpdatedAt());
        viewModel.setReviewed(dto.isReviewed());
        viewModel.setAdminNotes(dto.getAdminNotes());

        return viewModel;
    }

    public ListingReportDTO toDTO(ListingReportViewModel viewModel) {
        if (viewModel == null)
            return null;

        ListingReportDTO dto = new ListingReportDTO();
        dto.setId(viewModel.getId());
        dto.setReportingUser(toDTO(viewModel.getReportingUser()));
        dto.setReportedListing(toDTO(viewModel.getReportedListing()));
        dto.setReason(viewModel.getReason());
        dto.setDescription(viewModel.getDescription());
        dto.setCreatedAt(viewModel.getCreatedAt());
        dto.setUpdatedAt(viewModel.getUpdatedAt());
        dto.setReviewed(viewModel.isReviewed());
        dto.setAdminNotes(viewModel.getAdminNotes());

        return dto;
    }

    // Favorite mappings
    public FavoriteViewModel toViewModel(FavoriteDTO dto) {
        if (dto == null)
            return null;

        FavoriteViewModel viewModel = new FavoriteViewModel();
        viewModel.setId(dto.getId());
        viewModel.setUserId(dto.getUserId());
        viewModel.setListingId(dto.getListingId());
        viewModel.setUser(toViewModel(dto.getUser()));
        viewModel.setListing(toViewModel(dto.getListing()));
        viewModel.setCreatedAt(dto.getCreatedAt());

        return viewModel;
    }

    public FavoriteDTO toDTO(FavoriteViewModel viewModel) {
        if (viewModel == null)
            return null;

        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(viewModel.getId());
        dto.setUserId(viewModel.getUserId());
        dto.setListingId(viewModel.getListingId());
        dto.setUser(toDTO(viewModel.getUser()));
        dto.setListing(toDTO(viewModel.getListing()));
        dto.setCreatedAt(viewModel.getCreatedAt());

        return dto;
    }

    // Follower mappings
    public FollowerViewModel toViewModel(FollowerDTO dto) {
        if (dto == null)
            return null;

        FollowerViewModel viewModel = new FollowerViewModel();
        viewModel.setId(dto.getId());
        viewModel.setFollowerId(dto.getFollowerId());
        viewModel.setFollowedId(dto.getFollowedId());
        viewModel.setFollower(toViewModel(dto.getFollower()));
        viewModel.setFollowed(toViewModel(dto.getFollowed()));
        viewModel.setCreatedAt(dto.getCreatedAt());

        return viewModel;
    }

    public FollowerDTO toDTO(FollowerViewModel viewModel) {
        if (viewModel == null)
            return null;

        FollowerDTO dto = new FollowerDTO();
        dto.setId(viewModel.getId());
        dto.setFollowerId(viewModel.getFollowerId());
        dto.setFollowedId(viewModel.getFollowedId());
        dto.setFollower(toDTO(viewModel.getFollower()));
        dto.setFollowed(toDTO(viewModel.getFollowed()));
        dto.setCreatedAt(viewModel.getCreatedAt());

        return dto;
    }

    // Convenience methods for collections
    public List<UserViewModel> toUserViewModels(List<UserDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<ItemViewModel> toItemViewModels(List<ItemDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<OfferViewModel> toOfferViewModels(List<OfferDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<ReviewViewModel> toReviewViewModels(List<ReviewDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<UserDTO> toUserDTOs(List<UserViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ItemDTO> toItemDTOs(List<ItemViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<OfferDTO> toOfferDTOs(List<OfferViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ReviewDTO> toReviewDTOs(List<ReviewViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<UserReportViewModel> toUserReportViewModels(List<UserReportDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<UserReportDTO> toUserReportDTOs(List<UserReportViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ListingReportViewModel> toListingReportViewModels(List<ListingReportDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<ListingReportDTO> toListingReportDTOs(List<ListingReportViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<FavoriteViewModel> toFavoriteViewModels(List<FavoriteDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<FavoriteDTO> toFavoriteDTOs(List<FavoriteViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<FollowerViewModel> toFollowerViewModels(List<FollowerDTO> dtos) {
        if (dtos == null)
            return null;
        return dtos.stream().map(this::toViewModel).collect(Collectors.toList());
    }

    public List<FollowerDTO> toFollowerDTOs(List<FollowerViewModel> viewModels) {
        if (viewModels == null)
            return null;
        return viewModels.stream().map(this::toDTO).collect(Collectors.toList());
    }
}