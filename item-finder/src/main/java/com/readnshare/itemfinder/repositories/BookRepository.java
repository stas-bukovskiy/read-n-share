package com.readnshare.itemfinder.repositories;

import com.readnshare.itemfinder.domain.Book;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface BookRepository extends ReactiveMongoRepository<Book, String> {
    Mono<Book> findByGoogleBookId(String googleBookId);
}
