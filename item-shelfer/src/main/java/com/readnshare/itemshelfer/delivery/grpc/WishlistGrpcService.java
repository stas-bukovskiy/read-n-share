package com.readnshare.itemshelfer.delivery.grpc;


import com.google.protobuf.Empty;
import com.readnshare.itemshelfer.interceptors.LogGrpcInterceptor;
import com.readnshare.itemshelfer.mappers.AccessRightMapper;
import com.readnshare.itemshelfer.mappers.WishlistMapper;
import com.readnshare.itemshelfer.services.AccessRightService;
import com.readnshare.itemshelfer.services.WishlistService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;
import v1.*;

@GrpcService(interceptors = {LogGrpcInterceptor.class})
@RequiredArgsConstructor
public class WishlistGrpcService extends ReactorWishlistServiceGrpc.WishlistServiceImplBase {

    private final WishlistService wishlistService;
    private final AccessRightService accessRightService;

    @Override
    public Mono<Wishlist> createWishlist(CreateWishlistRequest request) {
        return wishlistService.create(WishlistMapper.of(request))
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toGRPC(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Override
    public Mono<Wishlist> updateWishlist(UpdateWishlistRequest request) {
        return wishlistService.update(request.getId(), WishlistMapper.of(request))
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toGRPC(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Override
    public Mono<Wishlist> deleteWishlist(DeleteWishlistRequest request) {
        return wishlistService.delete(request.getId())
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toGRPC(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Override
    public Mono<GetWishlistsResponse> getBookWishlists(Empty request) {
        return getWishlistsByItemType(com.readnshare.itemshelfer.domain.Wishlist.ItemType.BOOK);
    }

    @Override
    public Mono<GetWishlistsResponse> getMovieWishlists(Empty request) {
        return getWishlistsByItemType(com.readnshare.itemshelfer.domain.Wishlist.ItemType.MOVIE);
    }

    @Override
    public Mono<GetWishlistsResponse> getSharedWithMeWishlists(Empty request) {
        return wishlistService.getSharedWithCurrentUser()
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toGRPC(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ))
                .collectList()
                .map(wishlists -> GetWishlistsResponse.newBuilder()
                        .addAllWishlists(wishlists)
                        .build());

    }

    @Override
    public Mono<AccessRightsResponse> getAccessRights(GetAccessRightsRequest request) {
        return accessRightService.getAllAccessRights(request.getWishlistId())
                .map(AccessRightMapper::toGRPC)
                .collectList()
                .map(accessRights -> AccessRightsResponse.newBuilder()
                        .addAllRights(accessRights)
                        .setWishlistId(request.getWishlistId())
                        .build());
    }

    @Override
    public Mono<AccessRightsResponse> addAccessRight(AddAccessRightRequest request) {
        return accessRightService.addAccessRight(request.getWishlistId(), AccessRightMapper.of(request.getRightToAdd()))
                .map(AccessRightMapper::toGRPC)
                .collectList()
                .map(accessRights -> AccessRightsResponse.newBuilder()
                        .addAllRights(accessRights)
                        .setWishlistId(request.getWishlistId())
                        .build());
    }

    @Override
    public Mono<AccessRightsResponse> deleteAccessRight(DeleteAccessRightsRequest request) {
        return accessRightService.deleteAccessRight(request.getWishlistId(), AccessRightMapper.of(request.getRightToDelete()))
                .map(AccessRightMapper::toGRPC)
                .collectList()
                .map(accessRights -> AccessRightsResponse.newBuilder()
                        .addAllRights(accessRights)
                        .setWishlistId(request.getWishlistId())
                        .build());
    }

    private Mono<GetWishlistsResponse> getWishlistsByItemType(com.readnshare.itemshelfer.domain.Wishlist.ItemType itemType) {
        return wishlistService.getAllByItemType(itemType)
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toGRPC(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ))
                .collectList()
                .map(wishlists -> GetWishlistsResponse.newBuilder()
                        .addAllWishlists(wishlists)
                        .build());
    }
}


