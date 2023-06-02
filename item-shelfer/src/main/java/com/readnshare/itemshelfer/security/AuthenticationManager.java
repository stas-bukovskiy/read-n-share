package com.readnshare.itemshelfer.security;

import com.readnshare.itemshelfer.mappers.UserMapper;
import com.readnshare.itemshelfer.services.VerifyUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final VerifyUserService verifyUserService;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        return verifyUserService.verify(authToken)
                .map(verifyUserResponse -> UserMapper.of(verifyUserResponse.getUser()))
                .map(user -> {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user,
                            user.getPassword(),
                            user.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    return auth;
                });
    }
}