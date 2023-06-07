package com.readnshare.itemreviewer.dto;

import lombok.Builder;

@Builder
public record ReviewDto(
        String id,
        String userId,
        String itemId,
        String review,
        long createdAt,
        long updatedAt
) {

}
