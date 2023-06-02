package com.readnshare.itemshelfer.repositories;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface WishlistRepository extends ReactiveMongoRepository<Wishlist, String> {

    @Query("{$and: [{'rights.userId': ?0}, {'itemType': ?1}]}")
    Flux<Wishlist> findAllByUserIdAndItemType(String userId, Wishlist.ItemType itemType);

    Flux<Wishlist> findByRightsUserIdAndRightsPermissionAndItemType(String userId, AccessRight.Permission permission, Wishlist.ItemType itemType);

    Flux<Wishlist> findByRightsUserIdAndRightsPermissionIsNot(String userId, AccessRight.Permission permission);
}
