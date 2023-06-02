package com.readnshare.itemshelfer.delivery.grpc;

import com.readnshare.itemshelfer.exceptions.AccessRightConflictException;
import com.readnshare.itemshelfer.exceptions.NotEnoughAccessRight;
import com.readnshare.itemshelfer.exceptions.NotFoundException;
import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;


@GrpcAdvice
@Slf4j
public class GrpcExceptionAdvice {


    @GrpcExceptionHandler(RuntimeException.class)
    public StatusException handleRuntimeException(RuntimeException ex) {
        var status = Status.INTERNAL.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("RuntimeException: ", ex);
        return status.asException();
    }

    @GrpcExceptionHandler(AccessRightConflictException.class)
    public StatusException handleAccessRightConflictException(AccessRightConflictException ex) {
        var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("AccessRightConflictException: ", ex);
        return status.asException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusException handleIllegalArgumentException(IllegalArgumentException ex) {
        var status = Status.INVALID_ARGUMENT.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("IllegalArgumentException: ", ex);
        return status.asException();
    }

    @GrpcExceptionHandler(value = {NotEnoughAccessRight.class})
    public StatusException handleGoogleBookNotFoundException(NotEnoughAccessRight ex) {
        var status = Status.UNAVAILABLE.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("NotEnoughAccessRight: ", ex);
        return status.asException();
    }

    @GrpcExceptionHandler(value = {NotFoundException.class})
    public StatusException handleNotFoundException(NotFoundException ex) {
        var status = Status.NOT_FOUND.withDescription(ex.getLocalizedMessage()).withCause(ex);
        log.error("NotFoundException: ", ex);
        return status.asException();

    }
}