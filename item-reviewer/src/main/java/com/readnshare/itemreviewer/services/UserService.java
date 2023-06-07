package com.readnshare.itemreviewer.services;

import reactor.core.publisher.Mono;

public interface UserService {
    Mono<String> getCurrentUserId();

    Mono<String> getCurrentUserToken();
}
