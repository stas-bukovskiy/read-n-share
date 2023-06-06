package com.readnshare.itemshelfer.services;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccessRightService {
    Mono<AccessRight> getAccessRightOfCurrentUser(Wishlist wishlist);

    Flux<AccessRight> getAllAccessRights(String wishlistId);

    Flux<AccessRight> addAccessRight(String wishlistId, AccessRight rightToaAdd);

    Flux<AccessRight> updateAccessRight(String wishlistId, AccessRight rightToUpdate);

    Flux<AccessRight> deleteAccessRight(String wishlistId, String userId);
}
