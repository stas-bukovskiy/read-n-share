package com.readnshare.itemfinder.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.GetMovieByImdbIdResponse;
import service.SearchResponse;

@Service
public class MovieServiceImpl implements MovieService {
    @Override
    public Mono<SearchResponse> searchMovieByExpression(String expression) {
        return Mono.just(SearchResponse.getDefaultInstance());
    }

    @Override
    public Mono<GetMovieByImdbIdResponse> getMovieByImdbId(String imdbId) {
        return Mono.just(GetMovieByImdbIdResponse.getDefaultInstance());
    }
}
