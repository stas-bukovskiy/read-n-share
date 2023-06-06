package com.readnshare.itemshelfer.repositories;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface WishlistRepository extends ReactiveMongoRepository<Wishlist, String> {


    Flux<Wishlist> findAllByRightsContainsAndWishlistType(AccessRight accessRight, Wishlist.WishlistType wishlistType);

    Flux<Wishlist> findAllByRightsContains(AccessRight accessRight);

    Flux<Wishlist> findAllByRightsContainsAndItemType(AccessRight accessRight, Wishlist.ItemType itemType);
}
