package com.readnshare.itemreviewer.services;

import com.readnshare.itemreviewer.domain.ItemType;
import com.readnshare.itemreviewer.dto.BookDto;
import com.readnshare.itemreviewer.dto.MovieDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class VerifyItemServiceImpl implements VerifyItemService {

    private final WebClient webClient;
    private final UserService userService;

    public VerifyItemServiceImpl(@Value("${item-finder.base-url}") String baseUrl, UserService userService) {
        this.userService = userService;
        webClient = WebClient.builder().baseUrl(baseUrl).build();
    }


    @Override
    public Mono<String> verify(String itemId, ItemType itemType) {
        switch (itemType) {
            case BOOK -> {
                return verifyBookId(itemId);
            }
            case MOVIE -> {
                return verifyMovieId(itemId);
            }
            default -> {
                return Mono.error(new IllegalArgumentException("Invalid item itemType: " + itemType));
            }
        }
    }


    private Mono<String> verifyBookId(String itemId) {
        return userService.getCurrentUserToken().flatMap(token ->
                webClient.get()
                        .uri("/api/v1/books/verify?googleBooksIds={googleBooksIds}", itemId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(BookDto.class)
                        .map(BookDto::googleBookId)
                        .collectList()
                        .map(result -> result.get(0))
                        .doOnSuccess(verifiedItemIds -> log.debug("successfully verify Google Book Ids via book-service: {}", verifiedItemIds))
                        .doOnError(error -> log.debug("error occurred during verifying Google Book Ids via book-service:", error))
        );
    }

    private Mono<String> verifyMovieId(String itemIds) {
        String imdbIdsString = String.join(",", itemIds);
        return userService.getCurrentUserToken().flatMap(token ->
                webClient.get()
                        .uri("/api/v1/movies/verify?imdbIds={imdbIds}", imdbIdsString)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(MovieDto.class)
                        .map(MovieDto::imdbId)
                        .collectList()
                        .map(result -> result.get(0))
                        .doOnSuccess(verifiedItemIds -> log.debug("successfully verify IMDbIds via movie-service: {}", verifiedItemIds))
                        .doOnError(error -> log.debug("error occurred during verifying IMDb Ids via movie-service:", error))
        );
    }

}
