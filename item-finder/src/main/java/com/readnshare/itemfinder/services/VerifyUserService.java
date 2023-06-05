package com.readnshare.itemfinder.services;


import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface VerifyUserService {
    Mono<UserDetails> verify(String token);
}
