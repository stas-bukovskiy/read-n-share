package com.readnshare.itemreviewer.controllers;

import com.readnshare.itemreviewer.dto.CreateUserRatingRequest;
import com.readnshare.itemreviewer.dto.ItemRatingDto;
import com.readnshare.itemreviewer.dto.UserRatingDto;
import com.readnshare.itemreviewer.mappers.RatingMapper;
import com.readnshare.itemreviewer.services.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RatingService ratingService;

    @GetMapping("/{itemId}")
    public Mono<ItemRatingDto> getItemRatingOfItem(@PathVariable String itemId) {
        return ratingService.getItemRating(itemId)
                .map(RatingMapper::toDto);
    }

    @GetMapping("/my/{itemId}")
    public Mono<UserRatingDto> getItemRatingOfItemAndRating(@PathVariable String itemId) {
        return ratingService.getItemRatingOfCurrentUser(itemId)
                .map(RatingMapper::toDto);
    }

    @PostMapping
    public Mono<UserRatingDto> createRating(@RequestBody @Validated CreateUserRatingRequest request) {
        return ratingService.createRating(request)
                .map(RatingMapper::toDto);
    }

    @PutMapping("/{itemId}/{rating}")
    public Mono<UserRatingDto> updateRating(@PathVariable String itemId, @PathVariable int rating) {
        return ratingService.updateRating(itemId, rating)
                .map(RatingMapper::toDto);
    }

    @DeleteMapping("/{itemId}")
    public Mono<UserRatingDto> deleteRating(@PathVariable String itemId) {
        return ratingService.deleteRating(itemId)
                .map(RatingMapper::toDto);
    }

}
