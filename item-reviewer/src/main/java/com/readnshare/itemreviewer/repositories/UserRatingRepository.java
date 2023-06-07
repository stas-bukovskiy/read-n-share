package com.readnshare.itemreviewer.repositories;

import com.readnshare.itemreviewer.domain.UserRating;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRatingRepository extends ReactiveMongoRepository<UserRating, String> {
    Flux<UserRating> findAllByItemId(String itemId);

    Mono<UserRating> findByItemIdAndUserId(String itemId, String currentUserId);

    Mono<Boolean> existsByItemIdAndUserId(String itemId, String t2);
}
