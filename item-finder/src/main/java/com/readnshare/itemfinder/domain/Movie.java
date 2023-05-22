package com.readnshare.itemfinder.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Data
@AllArgsConstructor
@Document(collection = "movies")
public class Movie {

    @Id
    private String id;
    private String imdbId;

    private String title;
    private String originalTitle;
    private Integer year;
    private Integer runtimeMins;
    private String plot;
    private String imageURL;

    private Set<String> awards;
    private Set<String> genres;
    private Set<String> directors;
    private Set<String> writers;

    private Double imdbRating;
    private Integer imDbRatingVotes;

}
