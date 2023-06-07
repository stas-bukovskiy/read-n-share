package com.readnshare.itemreviewer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerOpenAPIConfiguration {

    @Bean
    public OpenAPI myOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info().title("Item-Reviewer Service")
                        .description("Service for adding and managing of reviews and ratings for items (books and movies).")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

}