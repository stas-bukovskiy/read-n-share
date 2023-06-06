package com.readnshare.itemshelfer.dto;

import lombok.Builder;

@Builder
public record MovieDto(
        String imdbId
) {
}
