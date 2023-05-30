package com.readnshare.itemfinder.imdb.domain;

import lombok.Data;

@Data
public class MovieData {

    private String id;
    private String title;
    private String originalTitle;
    private String year;
    private String image;
    private String runtimeMins;
    private String plot;
    private String awards;
    private String directors;
    private String writers;
    private String stars;
    private String genres;
    private String countries;
    private String languages;
    private String imDbRating;
    private String imDbRatingVotes;

    private String errorMessage;

}
