package com.readnshare.itemfinder.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SearchDataDto(
        String expression,
        List<SearchResultDto> results
) {

}