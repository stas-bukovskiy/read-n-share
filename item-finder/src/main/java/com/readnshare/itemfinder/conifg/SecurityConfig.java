package com.readnshare.itemfinder.conifg;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.CompositeGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository securityContextRepository;

    private static final String[] AUTH_WHITELIST = {
            // -- Swagger UI v2
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange()
                .pathMatchers(AUTH_WHITELIST).permitAll()
                .pathMatchers("/**").authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((swe, e) -> Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))).accessDeniedHandler((swe, e) -> Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN))).and()
                .build();
    }

    @Bean
    GrpcAuthenticationReader authenticationReader() {
        final List<GrpcAuthenticationReader> readers = new ArrayList<>();
        // The actual token class is dependent on your spring-security library (OAuth2/JWT/...)
        readers.add(new BearerAuthenticationReader(accessToken -> new UsernamePasswordAuthenticationToken(
                accessToken, accessToken
        )));
        return new CompositeGrpcAuthenticationReader(readers);
    }

}