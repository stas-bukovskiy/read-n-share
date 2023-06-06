package com.readnshare.itemshelfer.controllers;

import com.readnshare.itemshelfer.dto.ErrorHttpResponseDto;
import com.readnshare.itemshelfer.exceptions.AccessRightConflictException;
import com.readnshare.itemshelfer.exceptions.NotEnoughAccessRight;
import com.readnshare.itemshelfer.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Order(2)
@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice {


    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleRuntimeException(RuntimeException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("RuntimeException: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("IllegalArgumentException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {WebExchangeBindException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleIllegalArgumentException(WebExchangeBindException ex) {
        var fieldError = ex.getBindingResult().getFieldError();
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.BAD_REQUEST.value(),
                fieldError == null ? ex.getMessage() : fieldError.getDefaultMessage(),
                LocalDateTime.now());
        log.error("IllegalArgumentException: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleNotFoundException(NotFoundException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.NOT_FOUND.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("NotFoundException: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {NotEnoughAccessRight.class})
    public ResponseEntity<ErrorHttpResponseDto> handleNotEnoughAccessRight(NotEnoughAccessRight ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.FORBIDDEN.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("NotEnoughAccessRight: ", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleResponseStatusException(ResponseStatusException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(ex.getBody().getStatus(), ex.getMessage(), LocalDateTime.now());
        log.error("NotEnoughAccessRight: ", ex);
        return ResponseEntity.status(ex.getBody().getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {AccessRightConflictException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleAccessRightConflictException(AccessRightConflictException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.FORBIDDEN.value(), ex.getMessage(), LocalDateTime.now());
        log.error("NotEnoughAccessRight: ", ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

}