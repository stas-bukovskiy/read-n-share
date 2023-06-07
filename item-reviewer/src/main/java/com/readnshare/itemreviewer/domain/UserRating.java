package com.readnshare.itemreviewer.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@Document(collection = "ratings")
public class UserRating {

    @Id
    private String id;

    @Field(name = "user_id")
    private String userId;

    @Field(name = "item_id")
    private String itemId;

    private int rating;

}
