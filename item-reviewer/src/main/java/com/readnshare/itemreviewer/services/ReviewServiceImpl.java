package com.readnshare.itemreviewer.services;

import com.readnshare.itemreviewer.domain.ItemType;
import com.readnshare.itemreviewer.domain.Review;
import com.readnshare.itemreviewer.dto.CreateReviewRequest;
import com.readnshare.itemreviewer.dto.UpdateReviewRequest;
import com.readnshare.itemreviewer.exceptions.NotFoundException;
import com.readnshare.itemreviewer.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final VerifyItemService verifyItemService;

    @Override
    public Flux<Review> getAllByItemId(String itemId) {
        return reviewRepository.findAllByItemId(itemId)
                .doOnComplete(() -> log.debug("successfully found reviews for item <{}>", itemId))
                .doOnError(error -> log.error("error occurred during reviews finding for item <{}>:", itemId, error));
    }

    @Override
    public Flux<Review> getAllOfCurrentUserByItemId(String itemId) {
        return userService.getCurrentUserId()
                .flatMapMany(userId -> reviewRepository.findAllByItemIdAndUserId(itemId, userId))
                .doOnComplete(() -> log.debug("successfully found reviews for item <{}> and current user", itemId))
                .doOnError(error -> log.error("error occurred during reviews finding for item <{}> and current user:", itemId, error));
    }

    @Override
    public Mono<Review> createReview(CreateReviewRequest createReviewRequest) {
        return verifyItemService.verify(createReviewRequest.getItemId(), parseItemType(createReviewRequest.getItemType()))
                .flatMap(verified -> userService.getCurrentUserId())
                .map(userId -> Review.builder()
                        .itemId(createReviewRequest.getItemId())
                        .review(createReviewRequest.getReview())
                        .userId(userId)
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .build())
                .flatMap(reviewRepository::save)
                .doOnSuccess(review -> log.debug("successfully added user review: {}", review))
                .doOnError(error -> log.error("error occurred during user review adding: <{}>:", createReviewRequest, error));
    }

    @Override
    public Mono<Review> updateReview(String id, UpdateReviewRequest updateReviewRequest) {
        return getById(id)
                .map(reviewToUpdate -> {
                    reviewToUpdate.setReview(updateReviewRequest.getReview());
                    reviewToUpdate.setUpdatedAt(new Date());
                    return reviewToUpdate;
                })
                .flatMap(reviewRepository::save)
                .doOnSuccess(review -> log.debug("successfully updated user review: {}", review))
                .doOnError(error -> log.error("error occurred during user review updating: <{}>:", updateReviewRequest, error));
    }

    @Override
    public Mono<Review> deleteReview(String id) {
        return getById(id)
                .publishOn(Schedulers.boundedElastic())
                .map(reviewToDelete -> {
                    reviewRepository.delete(reviewToDelete).subscribe();
                    return reviewToDelete;
                })
                .doOnSuccess(userRating -> log.debug("successfully deleted user review: {}", userRating))
                .doOnError(error -> log.error("error occurred during user review deleting with id <{}>:", id, error));
    }

    private Mono<Review> getById(String id) {
        return reviewRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Not found user review with id " + id)));
    }

    private ItemType parseItemType(String s) {
        return ItemType.valueOf(s);
    }
}
