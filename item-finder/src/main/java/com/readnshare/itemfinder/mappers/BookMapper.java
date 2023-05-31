package com.readnshare.itemfinder.mappers;

import com.readnshare.itemfinder.domain.Book;
import com.readnshare.itemfinder.dto.BookDto;
import com.readnshare.itemfinder.googlebooks.domain.BookData;
import v1.GetBookByGoogleBooksIdResponse;

public final class BookMapper {
    private BookMapper() {
    }

    public static Book of(BookData bookData) {
        return Book.builder()
                .googleBookId(bookData.getId())
                .title(bookData.getTitle())
                .subtitle(bookData.getSubtitle())
                .authors(bookData.getAuthors())
                .publishedDate(bookData.getPublishedDate())
                .categories(bookData.getCategories())
                .description(bookData.getDescription())
                .imageLinks(bookData.getImageLinks())
                .pageCount(bookData.getPageCount())
                .averageRating(bookData.getAverageRating())
                .ratingsCount(bookData.getRatingsCount())
                .language(bookData.getLanguage())
                .build();
    }

    public static BookDto toDto(Book book) {
        return BookDto.builder()
                .id(book.getId())
                .googleBookId(book.getGoogleBookId())
                .title(book.getTitle())
                .subtitle(book.getSubtitle())
                .authors(book.getAuthors())
                .publishedDate(book.getPublishedDate())
                .categories(book.getCategories())
                .description(book.getDescription())
                .imageLinks(book.getImageLinks())
                .pageCount(book.getPageCount())
                .averageRating(book.getAverageRating())
                .ratingsCount(book.getRatingsCount())
                .language(book.getLanguage())
                .build();
    }

    public static GetBookByGoogleBooksIdResponse toGRPC(Book book) {
        return GetBookByGoogleBooksIdResponse.newBuilder()
                .setBook(v1.BookData.newBuilder()
                        .setId(book.getId())
                        .setGoogleBookId(book.getGoogleBookId())
                        .setTitle(book.getTitle())
                        .setSubtitle(book.getSubtitle())
                        .addAllAuthors(book.getAuthors())
                        .setPublishedDate(book.getPublishedDate())
                        .addAllCategories(book.getCategories())
                        .setDescription(book.getDescription())
                        .putAllImageLinks(book.getImageLinks())
                        .setPageCount(book.getPageCount())
                        .setAverageRating(book.getAverageRating())
                        .setRatingsCount(book.getRatingsCount())
                        .setLanguage(book.getLanguage())
                        .build()
                ).build();

    }
}

