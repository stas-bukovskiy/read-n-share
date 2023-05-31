package com.readnshare.itemfinder.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorHttpResponseDto(
        int status,
        String message,
        LocalDateTime timestamp) {
}