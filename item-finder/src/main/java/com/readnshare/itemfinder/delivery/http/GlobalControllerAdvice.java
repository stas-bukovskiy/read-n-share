package com.readnshare.itemfinder.delivery.http;


import com.readnshare.itemfinder.dto.ErrorHttpResponseDto;
import com.readnshare.itemfinder.googlebooks.exceptions.GoogleBookException;
import com.readnshare.itemfinder.googlebooks.exceptions.GoogleBookNotFoundException;
import com.readnshare.itemfinder.imdb.exceptions.ImdbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Order(2)
@RestControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    private static final String SERVICE_NAME = "GLOBAL_CONTROLLER_ADVICE";

    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleRuntimeException(RuntimeException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("[{}] RuntimeException: ", SERVICE_NAME, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("[{}] IllegalArgumentException: ", SERVICE_NAME, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {ImdbException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleImdbException(ImdbException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("[{}] ImdbException: ", SERVICE_NAME, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {GoogleBookNotFoundException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleGoogleBookNotFoundException(GoogleBookNotFoundException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.NOT_FOUND.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("[{}] GoogleBookNotFoundException: ", SERVICE_NAME, ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

    @ExceptionHandler(value = {GoogleBookException.class})
    public ResponseEntity<ErrorHttpResponseDto> handleGoogleBookException(GoogleBookException ex) {
        var errorHttpResponseDto = new ErrorHttpResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getLocalizedMessage(), LocalDateTime.now());
        log.error("[{}] GoogleBookException: ", SERVICE_NAME, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorHttpResponseDto);
    }

}