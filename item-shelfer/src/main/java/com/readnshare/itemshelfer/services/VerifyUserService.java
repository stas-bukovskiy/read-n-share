package com.readnshare.itemshelfer.services;


import com.readnshare.itemshelfer.dto.UserDto;
import reactor.core.publisher.Mono;

public interface VerifyUserService {
    Mono<UserDto> verify(String token);
}
