package com.readnshare.itemshelfer.services;

import com.readnshare.itemshelfer.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public Mono<String> getCurrentUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> {
                    Authentication authentication = securityContext.getAuthentication();
                    User currentUser = (User) authentication.getPrincipal();
                    return currentUser.getId();
                })
                .doOnSuccess(currentUserId -> log.debug("successfully get currently authenticated user id: <{}>", currentUserId))
                .doOnError(error -> log.error("error occurred during currently authenticated user id getting: g:", error));
    }
}
