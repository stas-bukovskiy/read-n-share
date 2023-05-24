package com.readnshare.itemfinder.mappers;

import com.readnshare.itemfinder.dto.SearchDataDto;
import com.readnshare.itemfinder.dto.SearchResultDto;
import com.readnshare.itemfinder.imdb.domain.SearchData;
import v1.SearchResponse;
import v1.SearchResult;

public final class SearchDataMapper {

    private SearchDataMapper() {
    }

    public static SearchResponse toGRPC(SearchData searchData) {
        return SearchResponse.newBuilder()
                .setExpression(searchData.getExpression())
                .addAllResults(searchData.getResults().stream()
                        .map(result -> SearchResult.newBuilder()
                                .setId(result.getId())
                                .setTitle(result.getTitle())
                                .setDescription(result.getTitle())
                                .setImage(result.getImage())
                                .build())
                        .toList())
                .build();
    }


    public static SearchDataDto toDto(SearchData searchData) {
        return SearchDataDto.builder()
                .expression(searchData.getExpression())
                .results(searchData.getResults().stream()
                        .map(result -> SearchResultDto.builder()
                                .id(result.getId())
                                .title(result.getTitle())
                                .description(result.getDescription())
                                .image(result.getImage())
                                .build())
                        .toList())
                .build();
    }

}
