package com.readnshare.itemfinder.delivery.grpc;

import com.readnshare.itemfinder.interceptors.LogGrpcInterceptor;
import com.readnshare.itemfinder.mappers.MovieMapper;
import com.readnshare.itemfinder.mappers.MovieSearchDataMapper;
import com.readnshare.itemfinder.services.MovieService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;
import v1.*;

@GrpcService(interceptors = {LogGrpcInterceptor.class})
@RequiredArgsConstructor
public class MovieGrpcService extends ReactorMovieServiceGrpc.MovieServiceImplBase {

    private final MovieService service;

    @Override
    public Mono<MovieSearchResponse> searchMoviesByExpression(MovieSearchRequest request) {
        return service.searchMovieByExpression(request.getExpression())
                .map(MovieSearchDataMapper::toGRPC);
    }

    @Override
    public Mono<GetMovieByImdbIdResponse> getMovieByImdbId(GetMovieByImdbIdRequest request) {
        return service.getMovieByImdbId(request.getImdbId())
                .map(MovieMapper::toGRPC);
    }

    @Override
    public Mono<VerifyImdbIdsResponse> verifyImdbIds(VerifyImdbIdsRequest request) {
        return service.getMovieByImdbIds(request.getImdbIdsList())
                .map(MovieMapper::toGRPC)
                .map(GetMovieByImdbIdResponse::getMovie)
                .collectList()
                .map(verifiedMovies -> VerifyImdbIdsResponse.newBuilder()
                        .addAllVerifiedMovies(verifiedMovies)
                        .build());
    }
}
