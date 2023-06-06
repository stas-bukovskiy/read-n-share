package com.readnshare.itemshelfer.services;

import reactor.core.publisher.Mono;

public interface UserService {
    Mono<String> getCurrentUserId();

    Mono<String> getCurrentUserToken();
}
