package com.readnshare.itemfinder.services;

import com.readnshare.itemfinder.domain.Book;
import com.readnshare.itemfinder.googlebooks.domain.BookSearchData;
import com.readnshare.itemfinder.googlebooks.services.GoogleBookFindService;
import com.readnshare.itemfinder.mappers.BookMapper;
import com.readnshare.itemfinder.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private static final String SERVICE_NAME = "BOOK_SERVICE";

    private final BookRepository bookRepository;
    private final GoogleBookFindService findService;

    @Override
    @Transactional
    public Mono<BookSearchData> searchBookByExpression(String expression, GoogleBookFindService.BookSearchOrder searchOrder, int startIndex, int maxResults) {
        return findService.search(expression, searchOrder, startIndex, maxResults)
                .doOnSuccess(searchData -> log.debug("[{}] search result: {}", SERVICE_NAME, searchData))
                .doOnError(error -> log.error("[{}] error occurred during book searching", SERVICE_NAME, error));

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Mono<Book> getBookByGoogleId(String googleBookId) {
        return bookRepository.findByGoogleBookId(googleBookId)
                .switchIfEmpty(findService.getBookInfo(googleBookId)
                        .map(BookMapper::of)
                        .flatMap(bookRepository::save)
                        .doOnSuccess(searchData -> log.debug("[{}] book saved: {}", SERVICE_NAME, searchData))
                        .doOnError(error -> log.error("[{}] error occurred during book saving", SERVICE_NAME, error)))
                .doOnSuccess(searchData -> log.debug("[{}] book found by id <{}>: {}", SERVICE_NAME, googleBookId, searchData))
                .doOnError(error -> log.error("[{}] error occurred during book finding by id <{}>", SERVICE_NAME, googleBookId, error));

    }

    @Override
    @Transactional
    public Flux<Book> getBookByGoogleIds(List<String> googleBookIds) {
        return Flux.fromIterable(googleBookIds)
                .flatMap(googleBookId -> getBookByGoogleId(googleBookId)
                        .subscribeOn(Schedulers.boundedElastic()));
    }
}
