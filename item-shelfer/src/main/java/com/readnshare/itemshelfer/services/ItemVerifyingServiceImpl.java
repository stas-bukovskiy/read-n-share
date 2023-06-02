package com.readnshare.itemshelfer.services;

import com.readnshare.itemshelfer.domain.Wishlist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import v1.*;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemVerifyingServiceImpl implements ItemVerifyingService {

    @GrpcClient("book-service")
    private ReactorBookServiceGrpc.ReactorBookServiceStub bookService;

    @GrpcClient("movie-service")
    private ReactorMovieServiceGrpc.ReactorMovieServiceStub movieService;

    @Override
    public Mono<Set<String>> verify(Set<String> itemIds, Wishlist.ItemType itemType) {
        switch (itemType) {
            case BOOK -> {
                return verifyBookIds(itemIds);
            }
            case MOVIE -> {
                return verifyMovieIds(itemIds);
            }
            default -> {
                return Mono.error(new IllegalAccessException("Invalid item type: " + itemType));
            }
        }
    }


    private Mono<Set<String>> verifyBookIds(Set<String> itemIds) {
        return bookService.verifyGoogleBookIds(VerifyGoogleBookIdsRequest.newBuilder()
                        .addAllGoogleBookIds(itemIds)
                        .build()
                ).map(verifyGoogleBookIdsResponse -> verifyGoogleBookIdsResponse.getVerifiedBooksList().stream()
                        .map(BookData::getGoogleBookId)
                        .collect(Collectors.toSet()))
                .doOnSuccess(verifiedItemIds -> log.debug("successfully verify Google Book Ids via book-service: {}", verifiedItemIds))
                .doOnError(error -> log.debug("error occurred during verifying Google Book Ids via book-service:", error));

    }

    private Mono<Set<String>> verifyMovieIds(Set<String> itemIds) {
        return movieService.verifyImdbIds(
                        VerifyImdbIdsRequest.newBuilder()
                                .addAllImdbIds(itemIds)
                                .build()
                ).map(verifyGoogleBookIdsResponse -> verifyGoogleBookIdsResponse.getVerifiedMoviesList().stream()
                        .map(MovieData::getImdbId)
                        .collect(Collectors.toSet()))
                .doOnSuccess(verifiedItemIds -> log.debug("successfully verify IMDbIds via movie-service: {}", verifiedItemIds))
                .doOnError(error -> log.debug("error occurred during verifying IMDb Ids via movie-service:", error));
    }

}
