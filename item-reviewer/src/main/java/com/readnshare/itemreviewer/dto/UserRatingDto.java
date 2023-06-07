package com.readnshare.itemreviewer.dto;

import lombok.Builder;

@Builder
public record UserRatingDto(
        String id,
        String userId,
        String itemId,
        int rating
) {
}
