package com.readnshare.itemshelfer.domain;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.sql.Timestamp;
import java.util.Set;

@Data
@Builder
@Document(collation = "wishlists")
public class Wishlist {

    @Id
    private String id;

    private String title;
    private String description;
    @Field("item_ids")
    private Set<String> itemIds;
    @Field("item_type")
    private ItemType itemType;
    @Field("wishlist_type")
    private WishlistType wishlistType;
    @Field("created_at")
    private Timestamp createdAt;
    @Field("updated_at")
    private Timestamp updatedAt;
    private Set<AccessRight> rights;


    public enum ItemType {
        BOOK, MOVIE
    }

    public enum WishlistType {
        PRIVATE, PUBLIC
    }
}
