package com.readnshare.itemreviewer.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.readnshare.itemreviewer.dto.UserDto;
import com.readnshare.itemreviewer.dto.UserVerifyingResponse;
import com.readnshare.itemreviewer.exceptions.UserNotVerifiedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class VerifyUserServiceImpl implements VerifyUserService {

    private static final String SERVICE_NAME = "VERIFY_USER_SERVICE";

    private final WebClient webClient;

    public VerifyUserServiceImpl(@Value("${user-service.base-url}") String baseUrl) {
        if (!StringUtils.hasText(baseUrl))
            throw new IllegalArgumentException("user-service.base-url property is required");
        webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public Mono<UserDto> verify(String token) {
        if (!StringUtils.hasText(token))
            throw new UserNotVerifiedException("token is null or blank");
        return webClient.get()
                .uri("/api/v1/auth/verify?token={token}", token)
                .retrieve()
                .onStatus(this::isNotVerifiedStatusCode, this::createException)
                .bodyToMono(UserVerifyingResponse.class)
                .map(UserVerifyingResponse::getUser)
                .doOnSuccess(userDto -> log.debug("[{}] successfully verified token <{}>: {}", SERVICE_NAME, token, userDto))
                .doOnError(error -> log.debug("[{}] error occurred during token verification:", SERVICE_NAME, error));
    }


    private Mono<UserNotVerifiedException> createException(ClientResponse response) {
        return response.bodyToMono(String.class)
                .handle((responseBody, sink) -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        JsonNode actualObj = mapper.readTree(responseBody);
                        JsonNode message = actualObj.get("message");
                        sink.error(new UserNotVerifiedException(message != null ? message.asText() : ""));
                    } catch (JsonProcessingException e) {
                        sink.error(new UserNotVerifiedException("unknown error occurred"));
                    }
                });
    }

    private boolean isNotVerifiedStatusCode(HttpStatusCode httpStatusCode) {
        return httpStatusCode.value() == 422;
    }
}
