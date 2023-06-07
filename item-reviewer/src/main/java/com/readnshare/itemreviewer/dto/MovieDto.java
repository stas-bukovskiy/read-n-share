package com.readnshare.itemreviewer.dto;

import lombok.Builder;

@Builder
public record MovieDto(
        String imdbId
) {
}