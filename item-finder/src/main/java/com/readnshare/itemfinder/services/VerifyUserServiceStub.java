package com.readnshare.itemfinder.services;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class VerifyUserServiceStub implements VerifyUserService {
    @Override
    public Mono<UserDetails> verify(String token) {
        return Mono.just(User.builder()
                .username("username")
                .password("password")
                .authorities("USER_ROLE")
                .build());
    }
}
