package com.readnshare.itemfinder.service;

import reactor.core.publisher.Mono;
import service.GetMovieByImdbIdResponse;
import service.SearchResponse;

public interface MovieService {

    Mono<SearchResponse> searchMovieByExpression(String expression);
    Mono<GetMovieByImdbIdResponse> getMovieByImdbId(String imdbId);

}
