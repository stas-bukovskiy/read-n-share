package com.readnshare.itemreviewer.mappers;

import com.readnshare.itemreviewer.domain.Review;
import com.readnshare.itemreviewer.dto.ReviewDto;

public final class ReviewMapper {
    private ReviewMapper() {
    }

    public static ReviewDto toDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .itemId(review.getItemId())
                .userId(review.getUserId())
                .review(review.getReview())
                .createdAt(review.getCreatedAt().getTime())
                .updatedAt(review.getUpdatedAt().getTime())
                .build();
    }
}
