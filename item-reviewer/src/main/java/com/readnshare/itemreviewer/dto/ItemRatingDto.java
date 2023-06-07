package com.readnshare.itemreviewer.dto;

import lombok.Builder;

@Builder
public record ItemRatingDto(
        String itemId,
        int votesCount,
        double rating
) {
}
