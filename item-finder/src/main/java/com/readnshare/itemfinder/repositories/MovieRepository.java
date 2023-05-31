package com.readnshare.itemfinder.repositories;

import com.readnshare.itemfinder.domain.Movie;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface MovieRepository extends ReactiveMongoRepository<Movie, String> {
    Mono<Movie> findByImdbId(String imdbId);
}
