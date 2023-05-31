package com.readnshare.itemfinder.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MovieDto(
        String id,
        String imdbId,
        String title,
        String originalTitle,
        Integer year,
        Integer runtimeMins,
        String plot,
        String imageURL,
        List<String> awards,
        List<String> genres,
        List<String> directors,
        List<String> writers,
        Double imdbRating,
        Integer imdbRatingVotes
) {
}
