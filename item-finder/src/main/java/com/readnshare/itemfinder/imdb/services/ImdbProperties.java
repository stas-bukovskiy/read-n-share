package com.readnshare.itemfinder.imdb.services;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "read-n-share.item-finder")
@Getter @Setter
public class ImdbProperties {
    @NotEmpty
    @NotBlank
    private String imdbToken;
    private String baseUrl = "https://imdb-api.com/API";

}