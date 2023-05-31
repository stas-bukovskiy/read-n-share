package com.readnshare.itemfinder.imdb.services;

import com.readnshare.itemfinder.imdb.domain.MovieData;
import com.readnshare.itemfinder.imdb.domain.MovieSearchData;
import reactor.core.publisher.Mono;

public interface ImdbFindService {
    Mono<MovieSearchData> search(String expression);

    Mono<MovieData> getMovieInfo(String imdbId);
}
