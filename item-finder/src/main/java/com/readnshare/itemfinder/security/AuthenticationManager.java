package com.readnshare.itemfinder.security;

import com.readnshare.itemfinder.services.VerifyUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import v1.VerifyUserResponse;

import java.util.List;

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
                .map(VerifyUserResponse::getUser)
                .map(user -> {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword(),
                            List.of(new SimpleGrantedAuthority(user.getRole()))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    return auth;
                });
    }
}