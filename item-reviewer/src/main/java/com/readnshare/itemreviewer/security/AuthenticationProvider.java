package com.readnshare.itemreviewer.security;

import com.readnshare.itemreviewer.domain.User;
import com.readnshare.itemreviewer.mappers.UserMapper;
import com.readnshare.itemreviewer.services.VerifyUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor

public class AuthenticationProvider implements ReactiveAuthenticationManager {

    private final VerifyUserService userAuthService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        return userAuthService.verify(token)
                .map(UserMapper::of)
                .map(user -> createAuthentication(user, authentication))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid token")));
    }

    private Authentication createAuthentication(User user, Authentication authentication) {
        return new UsernamePasswordAuthenticationToken(
                user,
                authentication.getCredentials(),
                user.getAuthorities()
        );
    }
}
