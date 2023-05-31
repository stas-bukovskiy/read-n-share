package com.readnshare.itemfinder.mappers;

import com.readnshare.itemfinder.dto.MovieSearchDataDto;
import com.readnshare.itemfinder.dto.MovieSearchResultDto;
import com.readnshare.itemfinder.imdb.domain.MovieSearchData;
import v1.MovieSearchResponse;
import v1.MovieSearchResult;

public final class MovieSearchDataMapper {

    private MovieSearchDataMapper() {
    }

    public static MovieSearchResponse toGRPC(MovieSearchData movieSearchData) {
        return MovieSearchResponse.newBuilder()
                .setExpression(movieSearchData.getExpression())
                .addAllResults(movieSearchData.getResults().stream()
                        .map(result -> MovieSearchResult.newBuilder()
                                .setId(result.getId())
                                .setTitle(result.getTitle())
                                .setDescription(result.getTitle())
                                .setImage(result.getImage())
                                .build())
                        .toList())
                .build();
    }


    public static MovieSearchDataDto toDto(MovieSearchData movieSearchData) {
        return MovieSearchDataDto.builder()
                .expression(movieSearchData.getExpression())
                .results(movieSearchData.getResults().stream()
                        .map(result -> MovieSearchResultDto.builder()
                                .imdbId(result.getId())
                                .title(result.getTitle())
                                .description(result.getDescription())
                                .image(result.getImage())
                                .build())
                        .toList())
                .build();
    }

}
