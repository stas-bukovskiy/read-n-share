package com.readnshare.itemreviewer.dto;

import lombok.Builder;

@Builder
public record BookDto(
        String googleBookId
) {
}