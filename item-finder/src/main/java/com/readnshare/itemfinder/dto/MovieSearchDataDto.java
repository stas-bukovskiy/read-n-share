package com.readnshare.itemfinder.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MovieSearchDataDto(
        String expression,
        List<MovieSearchResultDto> results
) {

}