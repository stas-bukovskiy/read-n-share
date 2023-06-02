package com.readnshare.itemshelfer.mappers;

import com.google.protobuf.Timestamp;
import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import v1.*;

import java.util.HashSet;

public final class WishlistMapper {

    private WishlistMapper() {
    }

    public static Wishlist of(CreateWishlistRequest request) {
        return Wishlist.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .itemIds(new HashSet<>(request.getItemIdsList()))
                .wishlistType(Wishlist.WishlistType.valueOf(request.getWishlistType().name()))
                .itemType(Wishlist.ItemType.valueOf(request.getType().name()))
                .build();
    }

    public static v1.Wishlist toGRPC(Wishlist wishlist, AccessRight right) {
        return v1.Wishlist.newBuilder()
                .setId(wishlist.getId())
                .setTitle(wishlist.getTitle())
                .setDescription(wishlist.getDescription())
                .addAllItemIds(wishlist.getItemIds())
                .setItemType(ItemType.valueOf(wishlist.getItemType().name()))
                .setWishlistType(WishlistType.valueOf(wishlist.getWishlistType().name()))
                .setPermission(Permission.valueOf(right.getPermission().name()))
                .setCreatedAt(Timestamp.newBuilder().setSeconds(wishlist.getCreatedAt().getTime()).build())
                .setUpdatedAt(Timestamp.newBuilder().setSeconds(wishlist.getUpdatedAt().getTime()).build())
                .build();
    }

    public static Wishlist of(UpdateWishlistRequest request) {
        return Wishlist.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .itemIds(new HashSet<>(request.getItemIdsList()))
                .wishlistType(Wishlist.WishlistType.valueOf(request.getWishlistType().name()))
                .build();
    }
}
