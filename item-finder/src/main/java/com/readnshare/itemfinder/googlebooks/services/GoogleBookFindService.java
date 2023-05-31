package com.readnshare.itemfinder.googlebooks.services;


import com.readnshare.itemfinder.googlebooks.domain.BookData;
import com.readnshare.itemfinder.googlebooks.domain.BookSearchData;
import reactor.core.publisher.Mono;

public interface GoogleBookFindService {
    Mono<BookSearchData> search(String expression, BookSearchOrder searchOrder, int startIndex, int maxResults);

    Mono<BookData> getBookInfo(String bookId);


    enum BookSearchOrder {
        RELEVANCE, NEWEST
    }

}
