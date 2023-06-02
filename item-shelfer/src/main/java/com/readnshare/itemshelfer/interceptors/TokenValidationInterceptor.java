package com.readnshare.itemshelfer.interceptors;

import com.readnshare.itemshelfer.exceptions.ClientVerifyingException;
import com.readnshare.itemshelfer.services.VerifyUserService;
import io.grpc.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TokenValidationInterceptor implements ServerInterceptor {

    private static final String SERVICE_NAME = "TOKEN_VALIDATION_INTERCEPTOR";
    private static final String TOKEN_PREFIX = "Bearer ";
    private final VerifyUserService verifyUserService;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Extract the token from headers3
        String token = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (token != null && token.startsWith(TOKEN_PREFIX)) {
            token = token.replace(TOKEN_PREFIX, "");
        } else {
            log.warn("[{}] couldn't find bearer string, will ignore the header.", SERVICE_NAME);
        }

        String finalToken = token;
        return Mono.from(verifyUserService.verify(token))
                .flatMap(response -> {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            response.getUser().getEmail(),
                            response.getUser().getPassword(),
                            List.of(new SimpleGrantedAuthority(response.getUser().getRole()))
                    );

                    return ReactiveSecurityContextHolder.getContext()
                            .doOnNext(securityContext -> securityContext.setAuthentication(authentication))
                            .then(Mono.defer(() -> Mono.just(next.startCall(call, headers))))
                            .doOnSuccess(res -> log.debug("[{}] successfully verified token <{}>", SERVICE_NAME, finalToken));
                })
                .onErrorResume(ClientVerifyingException.class, error -> {
                    log.debug("[{}] client error occurred during token verifying, token <{}>: {}", SERVICE_NAME, finalToken, error.getMessage());
                    call.close(Status.UNAUTHENTICATED.withDescription(error.getMessage()), headers);
                    return Mono.just(new ServerCall.Listener<>() {
                    });
                })
                .contextWrite(ctx -> ctx.put(Context.key("metadata"), headers))
                .block();
    }

}