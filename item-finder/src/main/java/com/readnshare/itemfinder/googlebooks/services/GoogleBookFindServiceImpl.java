package com.readnshare.itemfinder.googlebooks.services;

import com.google.common.base.Strings;
import com.readnshare.itemfinder.googlebooks.domain.BookData;
import com.readnshare.itemfinder.googlebooks.domain.BookSearchData;
import com.readnshare.itemfinder.googlebooks.exceptions.GoogleBookNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GoogleBookFindServiceImpl implements GoogleBookFindService {

    private static final String API_BASE_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final String SERVICE_NAME = "GOOGLE_BOOK_FIND_SERVICE";


    private final WebClient webClient;

    public GoogleBookFindServiceImpl() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public Mono<BookSearchData> search(String expression, BookSearchOrder searchOrder, int startIndex, int maxResults) {
        if (Strings.isNullOrEmpty(expression))
            return Mono.error(new IllegalArgumentException("expression can not be blank"));

        String url = buildSearchUrl(expression, searchOrder, startIndex, maxResults);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(BookSearchData.class)
                .map(searchData -> {
                    searchData.setExpression(expression);
                    return searchData;
                })
                .doOnSuccess(searchData -> log.debug("[{}] search result: {}", SERVICE_NAME, searchData))
                .doOnError(error -> log.error("[{}] error occurred during book searching", SERVICE_NAME, error));

    }

    @Override
    public Mono<BookData> getBookInfo(String bookId) {
        if (Strings.isNullOrEmpty(bookId))
            return Mono.error(new IllegalArgumentException("bookId can not be blank"));

        String url = UriComponentsBuilder.fromHttpUrl(API_BASE_URL)
                .pathSegment(bookId)
                .build()
                .toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(this::isNotFoundStatus, (response) -> Mono.error(new GoogleBookNotFoundException(bookId)))
                .bodyToMono(BookData.class)
                .doOnSuccess(searchData -> log.debug("[{}] book found by id <{}>: {}", SERVICE_NAME, bookId, searchData))
                .doOnError(error -> log.error("[{}] error occurred during book finding by id <{}>", SERVICE_NAME, bookId, error));
    }

    private String buildSearchUrl(String query, BookSearchOrder searchOrder, int startIndex, int maxResults) {
        return UriComponentsBuilder.fromHttpUrl(API_BASE_URL)
                .queryParam("q", query)
                .queryParam("orderBy", searchOrder.name().toLowerCase())
                .queryParam("startIndex", startIndex)
                .queryParam("maxResults", maxResults)
                .build()
                .toUriString();
    }

    private boolean isNotFoundStatus(HttpStatusCode httpStatusCode) {
        return httpStatusCode.value() == 404 || httpStatusCode.value() == 503;
    }

}