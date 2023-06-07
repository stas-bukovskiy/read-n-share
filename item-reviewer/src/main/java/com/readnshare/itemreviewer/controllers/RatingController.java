package com.readnshare.itemreviewer.controllers;

import com.readnshare.itemreviewer.dto.CreateUserRatingRequest;
import com.readnshare.itemreviewer.dto.ItemRatingDto;
import com.readnshare.itemreviewer.dto.UserRatingDto;
import com.readnshare.itemreviewer.mappers.RatingMapper;
import com.readnshare.itemreviewer.services.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ratings")
@Tags(@Tag(name = "Ratings", description = "Rating REST Controller"))
public class RatingController {

    private final RatingService ratingService;

    @Operation(method = "getItemRatingOfItem",
            summary = "Get item rating by its id",
            operationId = "getItemRatingOfItem",
            description = "Get counted item rating by its id")
    @GetMapping("/{itemId}")
    public Mono<ItemRatingDto> getItemRatingOfItem(@PathVariable String itemId) {
        return ratingService.getItemRating(itemId)
                .map(RatingMapper::toDto);
    }

    @Operation(method = "getItemRatingOfItemAndRating",
            summary = "Get item rating by item id of current user",
            operationId = "getItemRatingOfItemAndRating",
            description = "Get item rating by item id of current user (user that makes a request)")
    @GetMapping("/my/{itemId}")
    public Mono<UserRatingDto> getItemRatingOfItemAndRating(@PathVariable String itemId) {
        return ratingService.getItemRatingOfCurrentUser(itemId)
                .map(RatingMapper::toDto);
    }

    @Operation(method = "createRating",
            summary = "Add a new rating for item by its id",
            operationId = "createRating",
            description = "Add a new rating for item by its id, that can be only one")
    @PostMapping
    public Mono<UserRatingDto> createRating(@RequestBody @Validated CreateUserRatingRequest request) {
        return ratingService.createRating(request)
                .map(RatingMapper::toDto);
    }

    @Operation(method = "updateRating",
            summary = "Update an existing rating for item by its id",
            operationId = "updateRating",
            description = "Update an existing rating for item by its id")
    @PutMapping("/{itemId}/{rating}")
    public Mono<UserRatingDto> updateRating(@PathVariable String itemId, @PathVariable int rating) {
        return ratingService.updateRating(itemId, rating)
                .map(RatingMapper::toDto);
    }

    @Operation(method = "deleteRating",
            summary = "Delete an existing rating for item by its id",
            operationId = "deleteRating",
            description = "Delete an existing rating for item by its id")
    @DeleteMapping("/{itemId}")
    public Mono<UserRatingDto> deleteRating(@PathVariable String itemId) {
        return ratingService.deleteRating(itemId)
                .map(RatingMapper::toDto);
    }

}
