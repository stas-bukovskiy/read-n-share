package com.readnshare.itemreviewer.mappers;

import com.readnshare.itemreviewer.domain.ItemRating;
import com.readnshare.itemreviewer.domain.UserRating;
import com.readnshare.itemreviewer.dto.ItemRatingDto;
import com.readnshare.itemreviewer.dto.UserRatingDto;

public final class RatingMapper {
    private RatingMapper() {
    }

    public static UserRatingDto toDto(UserRating userRating) {
        return UserRatingDto.builder()
                .id(userRating.getId())
                .userId(userRating.getUserId())
                .itemId(userRating.getItemId())
                .rating(userRating.getRating())
                .build();
    }

    public static ItemRatingDto toDto(ItemRating itemRating) {
        return ItemRatingDto.builder()
                .itemId(itemRating.itemId())
                .votesCount(itemRating.votesCount())
                .rating(itemRating.rating())
                .build();
    }
}
