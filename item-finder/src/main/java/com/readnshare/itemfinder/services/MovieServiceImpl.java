package com.readnshare.itemfinder.services;

import com.readnshare.itemfinder.domain.Movie;
import com.readnshare.itemfinder.imdb.domain.SearchData;
import com.readnshare.itemfinder.imdb.services.ImdbFindService;
import com.readnshare.itemfinder.mappers.MovieMapper;
import com.readnshare.itemfinder.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private static final String SERVICE_NAME = "MOVIE_SERVICE";

    private final ImdbFindService imdbFindService;
    private final MovieRepository movieRepository;

    @Override
    @Transactional
    public Mono<SearchData> searchMovieByExpression(String expression) {
        return imdbFindService.search(expression)
                .doOnSuccess(searchData -> log.debug("{} search result: {}", SERVICE_NAME, searchData))
                .doOnError(error -> log.error("{} error occurred during movie searching", SERVICE_NAME, error));
    }

    @Override
    @Transactional
    public Mono<Movie> getMovieByImdbId(String imdbId) {
        return movieRepository.findByImdbId(imdbId)
                .switchIfEmpty(imdbFindService.getMovieInfo(imdbId)
                        .map(MovieMapper::of)
                        .flatMap(movieRepository::save)
                        .doOnSuccess(searchData -> log.debug("{} movie saved: {}", SERVICE_NAME, searchData))
                        .doOnError(error -> log.error("{} error occurred during movie saving", SERVICE_NAME, error)))
                .doOnSuccess(searchData -> log.debug("{} movie found by id <{}>: {}", SERVICE_NAME, imdbId, searchData))
                .doOnError(error -> log.error("{} error occurred during movie finding by id <{}>", SERVICE_NAME, imdbId, error));
    }
}
