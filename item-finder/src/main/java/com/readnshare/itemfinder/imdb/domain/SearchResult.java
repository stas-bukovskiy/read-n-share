package com.readnshare.itemfinder.imdb.domain;

import lombok.Data;

@Data
public class SearchResult {
    private String id;
    private String image;
    private String title;
    private String description;
}
