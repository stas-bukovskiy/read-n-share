package com.readnshare.itemfinder.interceptors;

import com.readnshare.itemfinder.security.AuthenticationManager;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

import static com.google.common.base.Strings.nullToEmpty;

@Order(60)
@Slf4j
@RequiredArgsConstructor
public class Oauth2AuthenticationInterceptor implements ServerInterceptor {

    private final AuthenticationManager authenticationManager;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String authHeader = nullToEmpty(headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER)));
        if (!(authHeader.startsWith("Bearer ") || authHeader.startsWith("bearer "))) {
            return next.startCall(call, headers);
        }

        try {
            String token = authHeader.substring(7);

            log.debug("Bearer Token Authorization header found");

            if (authenticationIsRequired()) {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(token, token));
            }
        } catch (AuthenticationException e) {
            ReactiveSecurityContextHolder.clearContext();

            log.debug("Authentication request failed: {}", e.getMessage());

            throw Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e).asRuntimeException();
        }

        return next.startCall(call, headers);
    }

    private boolean authenticationIsRequired() {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(existingAuth) || !existingAuth.isAuthenticated()) {
            return true;
        }

        return existingAuth instanceof AnonymousAuthenticationToken;
    }
}