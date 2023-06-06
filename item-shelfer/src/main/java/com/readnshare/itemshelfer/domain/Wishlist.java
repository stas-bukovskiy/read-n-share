package com.readnshare.itemshelfer.domain;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@Document(collection = "wishlists")
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
    private Date createdAt;
    @Field("updated_at")
    private Date updatedAt;
    private Set<AccessRight> rights;


    public enum ItemType {
        BOOK, MOVIE
    }

    public enum WishlistType {
        PRIVATE, PUBLIC
    }
}
