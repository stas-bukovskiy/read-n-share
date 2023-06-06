package com.readnshare.itemshelfer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WishlistDto {
    List<String> itemIds;
    String itemType;
    String wishlistType;
    String permission;
    long createdAt;
    long updatedAt;
    private String id;
    private String title;
    private String description;
}
