package com.readnshare.itemshelfer.mappers;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import com.readnshare.itemshelfer.dto.CreateWishlistRequest;
import com.readnshare.itemshelfer.dto.UpdateWishlistRequest;
import com.readnshare.itemshelfer.dto.WishlistDto;

import java.util.HashSet;

public final class WishlistMapper {

    private WishlistMapper() {
    }

    public static Wishlist of(CreateWishlistRequest createRequest) {
        return Wishlist.builder()
                .title(createRequest.getTitle())
                .description(createRequest.getDescription())
                .itemIds(new HashSet<>(createRequest.getItemIds()))
                .itemType(parseItemType(createRequest.getItemType()))
                .wishlistType(parseWishlistType(createRequest.getWishlistType()))
                .build();
    }

    public static Wishlist.ItemType parseItemType(String itemType) {
        return Wishlist.ItemType.valueOf(itemType);
    }

    public static Wishlist.WishlistType parseWishlistType(String wishlistType) {
        return Wishlist.WishlistType.valueOf(wishlistType);
    }

    public static WishlistDto toDTO(Wishlist wishlist, AccessRight accessRight) {
        return WishlistDto.builder()
                .id(wishlist.getId())
                .title(wishlist.getTitle())
                .description(wishlist.getDescription())
                .itemIds(wishlist.getItemIds().stream().toList())
                .itemType(wishlist.getItemType().name())
                .wishlistType(wishlist.getWishlistType().name())
                .permission(accessRight.getPermission().name())
                .createdAt(wishlist.getCreatedAt().getTime())
                .updatedAt(wishlist.getUpdatedAt().getTime())
                .build();
    }

    public static Wishlist of(UpdateWishlistRequest updateRequest) {
        return Wishlist.builder()
                .title(updateRequest.getTitle())
                .description(updateRequest.getDescription())
                .itemIds(new HashSet<>(updateRequest.getItemIds()))
                .wishlistType(parseWishlistType(updateRequest.getWishlistType()))
                .build();
    }
}
