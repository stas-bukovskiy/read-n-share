package com.readnshare.itemreviewer.services;

import com.readnshare.itemreviewer.domain.ItemRating;
import com.readnshare.itemreviewer.domain.UserRating;
import com.readnshare.itemreviewer.dto.CreateUserRatingRequest;
import reactor.core.publisher.Mono;

public interface RatingService {
    Mono<ItemRating> getItemRating(String itemId);

    Mono<UserRating> getItemRatingOfCurrentUser(String itemId);

    Mono<UserRating> createRating(CreateUserRatingRequest request);

    Mono<UserRating> updateRating(String itemId, int rating);

    Mono<UserRating> deleteRating(String itemId);
}
