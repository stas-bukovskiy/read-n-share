package com.readnshare.itemfinder.services;

import com.readnshare.itemfinder.domain.Movie;
import com.readnshare.itemfinder.imdb.domain.SearchData;
import reactor.core.publisher.Mono;

public interface MovieService {

    Mono<SearchData> searchMovieByExpression(String expression);
    Mono<Movie> getMovieByImdbId(String imdbId);

}
