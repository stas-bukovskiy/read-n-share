package com.readnshare.itemfinder.services;

import com.readnshare.itemfinder.domain.Movie;
import com.readnshare.itemfinder.imdb.domain.MovieSearchData;
import reactor.core.publisher.Mono;

public interface MovieService {

    Mono<MovieSearchData> searchMovieByExpression(String expression);

    Mono<Movie> getMovieByImdbId(String imdbId);

}
