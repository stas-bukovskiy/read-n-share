package com.readnshare.itemfinder.delivery.grpc;

import com.readnshare.itemfinder.imdb.exceptions.ImdbException;
import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;


@GrpcAdvice
@Slf4j
public class GrpcExceptionAdvice {

    private static final String SERVICE_NAME = "GRPC_GLOBAL_CONTROLLER_ADVICE";


    @GrpcExceptionHandler(RuntimeException.class)
    public StatusException handleRuntimeException(RuntimeException ex) {
        var status = Status.INTERNAL.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("[{}] RuntimeException: ", SERVICE_NAME, ex);
        return status.asException();
    }
    
    @GrpcExceptionHandler(ImdbException.class)
    public StatusException handleInvalidAmountException(ImdbException ex) {
        var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("[{}] ImdbException: ", SERVICE_NAME, ex);
        return status.asException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusException handleIllegalArgumentException(IllegalArgumentException ex) {
        var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("[{}] IllegalArgumentException: ", SERVICE_NAME, ex);
        return status.asException();
    }
}