package com.readnshare.itemfinder.delivery.grpc;

import com.readnshare.itemfinder.googlebooks.services.GoogleBookFindService;
import com.readnshare.itemfinder.interceptors.LogGrpcInterceptor;
import com.readnshare.itemfinder.mappers.BookMapper;
import com.readnshare.itemfinder.mappers.BookSearchDataMapper;
import com.readnshare.itemfinder.services.BookService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;
import v1.*;

@GrpcService(interceptors = {LogGrpcInterceptor.class})
@RequiredArgsConstructor
public class BookGrpcService extends ReactorBookServiceGrpc.BookServiceImplBase {

    private final BookService service;

    @Override
    public Mono<BookSearchResponse> searchBooksByExpression(BookSearchRequest request) {
        return service.searchBookByExpression(request.getExpression(),
                        GoogleBookFindService.BookSearchOrder.valueOf(request.getSearchOrder().name()),
                        request.getStartIndex(),
                        request.getMaxResults() == 0 ? 25 : request.getMaxResults())
                .map(BookSearchDataMapper::toGRPC);
    }

    @Override
    public Mono<GetBookByGoogleBooksIdResponse> getBookByGoogleBooksId(GetBookByGoogleBooksIdRequest request) {
        return service.getBookByGoogleId(request.getGoogleBooksId())
                .map(BookMapper::toGRPC);
    }

}
