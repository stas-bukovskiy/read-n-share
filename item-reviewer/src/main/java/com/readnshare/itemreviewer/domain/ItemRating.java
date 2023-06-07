package com.readnshare.itemreviewer.domain;

import lombok.Builder;

@Builder
public record ItemRating(
        String itemId,
        int votesCount,
        double rating
) {
}
