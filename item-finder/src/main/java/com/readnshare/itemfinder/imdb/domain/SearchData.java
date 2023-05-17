package com.readnshare.itemfinder.imdb.domain;

import lombok.Data;

import java.util.List;

@Data
public class SearchData {
    private String searchType;
    public String expression;
    private List<SearchResult> results;
    private String errorMessage;
}