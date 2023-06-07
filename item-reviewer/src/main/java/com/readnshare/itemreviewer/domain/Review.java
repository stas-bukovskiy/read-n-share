package com.readnshare.itemreviewer.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@Builder
@Document(collection = "reviews")
public class Review {

    @Id
    private String id;

    @Field(name = "user_id")
    private String userId;

    @Field(name = "item_id")
    private String itemId;

    private String review;

    @Field(name = "created_at")
    private Date createdAt;
    @Field(name = "updated_at")
    private Date updatedAt;

}
