package com.readnshare.itemshelfer.services;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface WishlistService {

    Mono<Tuple2<Wishlist, AccessRight>> create(Wishlist wishlistDataToCreate);

    Mono<Tuple2<Wishlist, AccessRight>> update(String id, Wishlist wishlistDataToUpdate);

    Mono<Tuple2<Wishlist, AccessRight>> delete(String id);

    Flux<Tuple2<Wishlist, AccessRight>> getAllByItemType(Wishlist.ItemType itemType);

    Flux<Tuple2<Wishlist, AccessRight>> getSharedWithCurrentUser();

    Flux<AccessRight> getAccessRights(String wishlistId);
}
