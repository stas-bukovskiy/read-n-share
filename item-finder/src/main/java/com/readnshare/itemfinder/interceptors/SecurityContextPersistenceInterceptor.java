package com.readnshare.itemfinder.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import reactor.util.context.Context;

import java.util.function.Function;

import static org.springframework.security.core.context.ReactiveSecurityContextHolder.clearContext;

@Order(10)
@Slf4j
public class SecurityContextPersistenceInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        try {
            ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
                @Override
                public void onComplete() {
                    try {
                        super.onComplete();
                    } finally {
                        Function<Context, Context> contextContextFunction = clearContext();
                        log.debug("SecurityContextHolder now cleared, as request processing completed: {}", contextContextFunction);
                    }
                }

                @Override
                public void onCancel() {
                    try {
                        super.onCancel();
                    } finally {
                        Function<Context, Context> contextContextFunction = clearContext();
                        log.debug("SecurityContextHolder now cleared, as request processing was canceled: {}", contextContextFunction);
                    }
                }
            };
        } catch (Throwable t) {
            clearContext();
            throw t;
        }
    }
}