package com.readnshare.itemfinder.googlebooks.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.readnshare.itemfinder.googlebooks.deserializers.BookDataDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonDeserialize(using = BookDataDeserialize.class)
public final class BookData {
    private String id;
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
