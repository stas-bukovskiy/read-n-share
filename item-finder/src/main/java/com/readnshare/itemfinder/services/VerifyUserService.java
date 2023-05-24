package com.readnshare.itemfinder.services;

import reactor.core.publisher.Mono;
import v1.VerifyUserResponse;

public interface VerifyUserService {
    Mono<VerifyUserResponse> verify(String token);
}
