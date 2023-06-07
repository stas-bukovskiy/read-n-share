package com.readnshare.itemreviewer.repositories;

import com.readnshare.itemreviewer.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ReviewRepository extends ReactiveMongoRepository<Review, String> {
    Flux<Review> findAllByItemId(String itemId);

    Flux<Review> findAllByItemIdAndUserId(String itemId, String userId);
}
