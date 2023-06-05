package com.readnshare.itemfinder.security;

import com.readnshare.itemfinder.dto.UserDto;
import com.readnshare.itemfinder.services.VerifyUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor

public class AuthenticationProvider implements ReactiveAuthenticationManager {

    private final VerifyUserService userAuthService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        return userAuthService.verify(token)
                .map(user -> createAuthentication(user, authentication))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid token")));
    }

    private Authentication createAuthentication(UserDto user, Authentication authentication) {
        return new UsernamePasswordAuthenticationToken(
                user,
                authentication.getCredentials(),
                List.of(new SimpleGrantedAuthority(user.getRole())
                ));
    }
}
