package com.readnshare.itemfinder.imdb.domain;

import lombok.Data;

import java.util.List;

@Data
public class SearchData {
    public String expression;
    private List<SearchResult> results;
    private String errorMessage;
}