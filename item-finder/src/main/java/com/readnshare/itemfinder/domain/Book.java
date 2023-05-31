package com.readnshare.itemfinder.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Document(collection = "books")
@Builder
public class Book {


    @Id
    private String id;

    @Field(name = "google_book_id")
    private String googleBookId;

    private String title;
    private String subtitle;
    private List<String> authors;

    @Field(name = "published_date")
    private String publishedDate;
    private List<String> categories;
    private Map<String, String> imageLinks;
    private String description;
    private Integer pageCount;

    @Field(name = "average_rating")
    private Double averageRating;

    @Field(name = "ratings_count")
    private Integer ratingsCount;
    private String language;
}