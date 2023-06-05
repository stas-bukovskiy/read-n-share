package com.readnshare.itemfinder.mappers;

import com.readnshare.itemfinder.domain.Book;
import com.readnshare.itemfinder.dto.BookDto;
import com.readnshare.itemfinder.googlebooks.domain.BookData;

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
}

