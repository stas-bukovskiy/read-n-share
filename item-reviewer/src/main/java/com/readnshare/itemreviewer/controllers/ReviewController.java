package com.readnshare.itemreviewer.controllers;


import com.readnshare.itemreviewer.dto.CreateReviewRequest;
import com.readnshare.itemreviewer.dto.ReviewDto;
import com.readnshare.itemreviewer.dto.UpdateReviewRequest;
import com.readnshare.itemreviewer.mappers.ReviewMapper;
import com.readnshare.itemreviewer.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{itemId}")
    public Flux<ReviewDto> getAllReviews(@PathVariable String itemId) {
        return reviewService.getAllByItemId(itemId)
                .map(ReviewMapper::toDto);
    }


    @GetMapping("/my/{itemId}")
    public Flux<ReviewDto> getAllReviewsOfCurrentUser(@PathVariable String itemId) {
        return reviewService.getAllOfCurrentUserByItemId(itemId)
                .map(ReviewMapper::toDto);
    }

    @PostMapping
    public Mono<ReviewDto> createReview(@RequestBody @Validated CreateReviewRequest request) {
        return reviewService.createReview(request)
                .map(ReviewMapper::toDto);
    }

    @PutMapping("/{id}")
    public Mono<ReviewDto> updateReview(@PathVariable String id, @RequestBody @Validated UpdateReviewRequest request) {
        return reviewService.updateReview(id, request)
                .map(ReviewMapper::toDto);
    }

    @DeleteMapping("/{id}")
    public Mono<ReviewDto> deleteReview(@PathVariable String id) {
        return reviewService.deleteReview(id)
                .map(ReviewMapper::toDto);
    }

}

