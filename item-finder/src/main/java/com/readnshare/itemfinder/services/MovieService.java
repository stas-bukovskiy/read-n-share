package com.readnshare.itemfinder.services;

import com.readnshare.itemfinder.domain.Movie;
import com.readnshare.itemfinder.imdb.domain.MovieSearchData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MovieService {

    Mono<MovieSearchData> searchMovieByExpression(String expression);

    Mono<Movie> getMovieByImdbId(String imdbId);

    Flux<Movie> getMovieByImdbIds(List<String> imdbIds);


}
