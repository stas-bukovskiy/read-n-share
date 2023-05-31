package com.readnshare.itemfinder.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class BookSearchDataDto {
    private String expression;
    private List<BookSearchResultDto> results;
}
