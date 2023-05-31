package com.readnshare.itemfinder.dto;

import lombok.Builder;

@Builder
public record MovieSearchResultDto(
        String imdbId,
        String image,
        String title,
        String description
) {
}
