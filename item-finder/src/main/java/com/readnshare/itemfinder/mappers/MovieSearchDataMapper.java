package com.readnshare.itemfinder.mappers;

import com.readnshare.itemfinder.dto.MovieSearchDataDto;
import com.readnshare.itemfinder.dto.MovieSearchResultDto;
import com.readnshare.itemfinder.imdb.domain.MovieSearchData;

public final class MovieSearchDataMapper {

    private MovieSearchDataMapper() {
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
