package com.readnshare.itemfinder.googlebooks.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.readnshare.itemfinder.googlebooks.deserializers.BookSearchDataDeserializer;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize(using = BookSearchDataDeserializer.class)
public class BookSearchData {
    private String expression;
    private List<BookData> results;
}
