package com.readnshare.itemfinder.service;

import com.readnshare.itemfinder.interceptor.LogGrpcInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;
import service.*;

@Slf4j
@GrpcService(interceptors = {LogGrpcInterceptor.class})
@RequiredArgsConstructor
public class MovieGrpcService extends ReactorMovieServiceGrpc.MovieServiceImplBase {

    private static final Long TIMEOUT_MILLIS = 5000L;

    private final MovieService service;


    @Override
    public Mono<SearchResponse> searchMovieByExpression(Mono<SearchRequest> request) {
        return request.map(SearchRequest::getExpression)
                .flatMap(service::searchMovieByExpression);
    }

    @Override
    public Mono<GetMovieByImdbIdResponse> getMovieByImdbId(Mono<GetMovieByImdbIdRequest> request) {
        return request.map(GetMovieByImdbIdRequest::getImdbId)
                .flatMap(service::getMovieByImdbId);
    }

    @Override
    public Mono<SearchResponse> searchMovieByExpression(SearchRequest request) {
        return service.searchMovieByExpression(request.getExpression());
    }

    @Override
    public Mono<GetMovieByImdbIdResponse> getMovieByImdbId(GetMovieByImdbIdRequest request) {
        return service.getMovieByImdbId(request.getImdbId());
    }
}
