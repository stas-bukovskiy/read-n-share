package com.readnshare.itemshelfer.services;

import com.google.common.net.HttpHeaders;
import com.readnshare.itemshelfer.domain.Wishlist;
import com.readnshare.itemshelfer.dto.BookDto;
import com.readnshare.itemshelfer.dto.MovieDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemVerifyingServiceImpl implements ItemVerifyingService {

    private final WebClient webClient;
    private final UserService userService;

    public ItemVerifyingServiceImpl(@Value("${item-finder.base-url}") String baseUrl, UserService userService) {
        this.userService = userService;
        webClient = WebClient.builder().baseUrl(baseUrl).build();
    }


    @Override
    public Mono<Set<String>> verify(Set<String> itemIds, Wishlist.ItemType itemType) {
        switch (itemType) {
            case BOOK -> {
                return verifyBookIds(itemIds);
            }
            case MOVIE -> {
                return verifyMovieIds(itemIds);
            }
            default -> {
                return Mono.error(new IllegalAccessException("Invalid item itemType: " + itemType));
            }
        }
    }


    private Mono<Set<String>> verifyBookIds(Set<String> itemIds) {
        String itemsIdsString = String.join(",", itemIds);
        return userService.getCurrentUserToken().flatMap(token ->
                webClient.get()
                        .uri("/api/v1/books/verify?googleBooksIds={googleBooksIds}", itemsIdsString)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(BookDto.class)
                        .map(BookDto::getGoogleBookId)
                        .collect(Collectors.toSet())
                        .doOnSuccess(verifiedItemIds -> log.debug("successfully verify Google Book Ids via book-service: {}", verifiedItemIds))
                        .doOnError(error -> log.debug("error occurred during verifying Google Book Ids via book-service:", error))
        );
    }

    private Mono<Set<String>> verifyMovieIds(Set<String> itemIds) {
        String imdbIdsString = String.join(",", itemIds);
        return userService.getCurrentUserToken().flatMap(token ->
                webClient.get()
                        .uri("/api/v1/movies/verify?imdbIds={imdbIds}", imdbIdsString)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToFlux(MovieDto.class)
                        .map(MovieDto::imdbId)
                        .collect(Collectors.toSet())
                        .doOnSuccess(verifiedItemIds -> log.debug("successfully verify IMDbIds via movie-service: {}", verifiedItemIds))
                        .doOnError(error -> log.debug("error occurred during verifying IMDb Ids via movie-service:", error))
        );
    }

}
