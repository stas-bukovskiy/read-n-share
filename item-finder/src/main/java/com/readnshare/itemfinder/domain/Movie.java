package com.readnshare.itemfinder.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Set;

@Data
@AllArgsConstructor
@Document(collection = "movies")
@Builder
public class Movie {

    @Id
    private String id;
    @Field(name = "imdb_id")
    private String imdbId;

    private String title;
    @Field(name = "original_title")
    private String originalTitle;
    private Integer year;
    @Field(name = "runtime_mins")
    private Integer runtimeMins;
    private String plot;
    @Field(name = "image_URL")
    private String imageURL;

    private Set<String> awards;
    private Set<String> genres;
    private Set<String> directors;
    private Set<String> writers;

    @Field(name = "imdb_rating")
    private Double imdbRating;
    @Field(name = "imdb_rating_votes")
    private Integer imdbRatingVotes;

}
