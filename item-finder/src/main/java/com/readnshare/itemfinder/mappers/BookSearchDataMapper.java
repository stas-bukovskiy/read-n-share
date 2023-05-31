package com.readnshare.itemfinder.mappers;

import com.readnshare.itemfinder.dto.BookSearchDataDto;
import com.readnshare.itemfinder.dto.BookSearchResultDto;
import com.readnshare.itemfinder.googlebooks.domain.BookSearchData;
import v1.BookSearchResponse;
import v1.BookSearchResult;

public final class BookSearchDataMapper {

    private BookSearchDataMapper() {
    }

    public static BookSearchDataDto toDto(BookSearchData searchData) {
        return BookSearchDataDto.builder()
                .expression(searchData.getExpression())
                .results(searchData.getResults().stream()
                        .map(searchResult -> BookSearchResultDto.builder()
                                .goggleBooksId(searchResult.getId())
                                .title(searchResult.getTitle())
                                .subtitle(searchResult.getSubtitle())
                                .authors(searchResult.getAuthors())
                                .publishedDate(searchResult.getPublishedDate())
                                .categories(searchResult.getCategories())
                                .description(searchResult.getDescription())
                                .imageLinks(searchResult.getImageLinks())
                                .pageCount(searchResult.getPageCount())
                                .averageRating(searchResult.getAverageRating())
                                .ratingsCount(searchResult.getRatingsCount())
                                .language(searchResult.getLanguage())
                                .build()
                        ).toList())
                .build();
    }


    public static BookSearchResponse toGRPC(BookSearchData search) {
        return BookSearchResponse.newBuilder()
                .setExpression(search.getExpression())
                .addAllResults(search.getResults().stream()
                        .map(result -> BookSearchResult.newBuilder()
                                .setId(result.getId())
                                .setTitle(result.getTitle())
                                .setSubtitle(result.getSubtitle())
                                .addAllAuthors(result.getAuthors())
                                .setPublishedDate(result.getPublishedDate())
                                .addAllCategories(result.getCategories())
                                .setDescription(result.getDescription())
                                .putAllImageLinks(result.getImageLinks())
                                .setPageCount(result.getPageCount())
                                .setAverageRating(result.getAverageRating())
                                .setRatingsCount(result.getRatingsCount())
                                .setLanguage(result.getLanguage())
                                .build())
                        .toList())
                .build();
    }
}
