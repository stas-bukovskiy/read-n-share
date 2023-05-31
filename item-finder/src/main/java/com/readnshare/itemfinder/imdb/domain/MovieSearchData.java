package com.readnshare.itemfinder.imdb.domain;

import lombok.Data;

import java.util.List;

@Data
public class MovieSearchData {
    public String expression;
    private List<MovieSearchResult> results;
    private String errorMessage;
}