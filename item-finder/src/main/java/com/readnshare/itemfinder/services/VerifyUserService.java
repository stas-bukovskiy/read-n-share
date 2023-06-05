package com.readnshare.itemfinder.services;


import com.readnshare.itemfinder.dto.UserDto;
import reactor.core.publisher.Mono;

public interface VerifyUserService {
    Mono<UserDto> verify(String token);
}
