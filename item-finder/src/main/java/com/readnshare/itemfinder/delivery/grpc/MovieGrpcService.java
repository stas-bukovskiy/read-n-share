package com.readnshare.itemfinder.delivery.grpc;

import com.readnshare.itemfinder.grpc.*;
import com.readnshare.itemfinder.interceptors.LogGrpcInterceptor;
import com.readnshare.itemfinder.mappers.MovieMapper;
import com.readnshare.itemfinder.mappers.SearchDataMapper;
import com.readnshare.itemfinder.services.MovieService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService(interceptors = {LogGrpcInterceptor.class})
@RequiredArgsConstructor
public class MovieGrpcService extends ReactorMovieServiceGrpc.MovieServiceImplBase {

    private final MovieService service;

    @Override
    public Mono<SearchResponse> searchMovieByExpression(SearchRequest request) {
        return service.searchMovieByExpression(request.getExpression())
                .map(SearchDataMapper::toGRPC);
    }

    @Override
    public Mono<GetMovieByImdbIdResponse> getMovieByImdbId(GetMovieByImdbIdRequest request) {
        return service.getMovieByImdbId(request.getImdbId())
                .map(MovieMapper::toGRPC);
    }

}
