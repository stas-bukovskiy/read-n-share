package com.readnshare.itemfinder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class BookDto {
    private String id;
    private String googleBookId;
    private String title;
    private String subtitle;
    private List<String> authors;
    private String publishedDate;
    private List<String> categories;
    private Map<String, String> imageLinks;
    private String description;
    private Integer pageCount;
    private Double averageRating;
    private Integer ratingsCount;
    private String language;
}