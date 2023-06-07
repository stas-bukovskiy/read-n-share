package com.readnshare.itemreviewer.services;


import com.readnshare.itemreviewer.dto.UserDto;
import reactor.core.publisher.Mono;

public interface VerifyUserService {
    Mono<UserDto> verify(String token);
}
