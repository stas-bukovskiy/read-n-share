package com.readnshare.itemfinder.dto;

import lombok.Builder;

@Builder
public record SearchResultDto(
        String id,
        String image,
        String title,
        String description
) {
}
