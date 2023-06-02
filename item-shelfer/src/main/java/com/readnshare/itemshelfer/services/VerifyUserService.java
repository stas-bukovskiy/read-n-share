package com.readnshare.itemshelfer.services;

import reactor.core.publisher.Mono;
import v1.VerifyUserResponse;

public interface VerifyUserService {
    Mono<VerifyUserResponse> verify(String token);
}
