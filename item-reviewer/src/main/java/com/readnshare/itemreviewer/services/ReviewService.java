package com.readnshare.itemreviewer.services;

import com.readnshare.itemreviewer.domain.Review;
import com.readnshare.itemreviewer.dto.CreateReviewRequest;
import com.readnshare.itemreviewer.dto.UpdateReviewRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {
    Flux<Review> getAllByItemId(String itemId);

    Flux<Review> getAllOfCurrentUserByItemId(String itemId);

    Mono<Review> createReview(CreateReviewRequest createReviewRequest);

    Mono<Review> updateReview(String id, UpdateReviewRequest updateReviewRequest);

    Mono<Review> deleteReview(String id);
}
