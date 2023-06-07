package com.readnshare.itemreviewer.controllers;


import com.readnshare.itemreviewer.dto.CreateReviewRequest;
import com.readnshare.itemreviewer.dto.ReviewDto;
import com.readnshare.itemreviewer.dto.UpdateReviewRequest;
import com.readnshare.itemreviewer.mappers.ReviewMapper;
import com.readnshare.itemreviewer.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reviews")
@Tags(@Tag(name = "Reviews", description = "Review REST Controller"))
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(method = "getAllReviews",
            summary = "Get all reviews for item by its id",
            operationId = "getAllReviews",
            description = "Get all reviews for item by its id")
    @GetMapping("/{itemId}")
    public Flux<ReviewDto> getAllReviews(@PathVariable String itemId) {
        return reviewService.getAllByItemId(itemId)
                .map(ReviewMapper::toDto);
    }


    @Operation(method = "getAllReviewsOfCurrentUser",
            summary = "Get all reviews for item by its id of current user",
            operationId = "getAllReviewsOfCurrentUser",
            description = "Get all reviews for item by its id of current user (user that makes a request)")
    @GetMapping("/my/{itemId}")
    public Flux<ReviewDto> getAllReviewsOfCurrentUser(@PathVariable String itemId) {
        return reviewService.getAllOfCurrentUserByItemId(itemId)
                .map(ReviewMapper::toDto);
    }

    @Operation(method = "createReview",
            summary = "Create a new review for item by its id",
            operationId = "createReview",
            description = "Create a new review for item by its id")
    @PostMapping
    public Mono<ReviewDto> createReview(@RequestBody @Validated CreateReviewRequest request) {
        return reviewService.createReview(request)
                .map(ReviewMapper::toDto);
    }

    @Operation(method = "updateReview",
            summary = "Update an existing review for item by review id",
            operationId = "updateReview",
            description = "Update an existing review for item by review id")
    @PutMapping("/{id}")
    public Mono<ReviewDto> updateReview(@PathVariable String id, @RequestBody @Validated UpdateReviewRequest request) {
        return reviewService.updateReview(id, request)
                .map(ReviewMapper::toDto);
    }

    @Operation(method = "deleteReview",
            summary = "Delete an existing review for item by review id",
            operationId = "deleteReview",
            description = "Delete an existing review for item by review id")
    @DeleteMapping("/{id}")
    public Mono<ReviewDto> deleteReview(@PathVariable String id) {
        return reviewService.deleteReview(id)
                .map(ReviewMapper::toDto);
    }

}

