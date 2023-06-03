package com.readnshare.itemfinder.services;

import com.google.common.base.Strings;
import com.readnshare.itemfinder.exceptions.ClientVerifyingException;
import com.readnshare.itemfinder.exceptions.ServerVerifyingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import v1.Error;
import v1.ReactorUserServiceGrpc;
import v1.VerifyUserRequest;
import v1.VerifyUserResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyUserServiceImpl implements VerifyUserService {

    private static final String SERVICE_NAME = "VERIFY_USER_SERVICE";
    private static final String SERVER_ERROR_TYPE = "server";
    private static final String CLIENT_ERROR_TYPE = "client";

    @GrpcClient("user-service")
    private ReactorUserServiceGrpc.ReactorUserServiceStub userServiceStub;


    @Override
    public Mono<VerifyUserResponse> verify(String token) {
        if (Strings.isNullOrEmpty(token))
            return Mono.error(new ClientVerifyingException("Bearer token cannot be null or empty"));
        VerifyUserRequest request = VerifyUserRequest.newBuilder().setToken(token).build();
        return userServiceStub.verifyUser(request)
                .handle((verifyUserResponse, sink) -> {
                    Error error = verifyUserResponse.getError();
                    if (error.getType().equalsIgnoreCase(SERVER_ERROR_TYPE)) {
                        log.error("[{}] server error occurred during token verifying, token <{}>: {}", SERVICE_NAME, token, error.getMessage());
                        sink.error(new ServerVerifyingException(error.getMessage()));
                    } else if (error.getType().equalsIgnoreCase(CLIENT_ERROR_TYPE)) {
                        log.debug("[{}] client error occurred during token verifying, token <{}>: {}", SERVICE_NAME, token, error.getMessage());
                        sink.error(new ClientVerifyingException(error.getMessage()));
                    } else {
                        log.debug("[{}] successfully verified token <{}>", SERVICE_NAME, token);
                        sink.next(verifyUserResponse);
                    }
                });
    }


}
