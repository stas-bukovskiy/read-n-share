package com.readnshare.itemfinder.services;

import com.readnshare.itemfinder.domain.Book;
import com.readnshare.itemfinder.googlebooks.domain.BookSearchData;
import com.readnshare.itemfinder.googlebooks.services.GoogleBookFindService;
import reactor.core.publisher.Mono;

public interface BookService {

    Mono<BookSearchData> searchBookByExpression(String expression, GoogleBookFindService.BookSearchOrder searchOrder, int startIndex, int maxResults);

    Mono<Book> getBookByGoogleId(String googleBookId);

}
