package com.readnshare.itemfinder.conifg;

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
                return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info().title("Item-Finder Service")
                                .description("Service for searching movies and books in a database or third-party API such as IMDb and Google Books API.")
                                .version("v1.0.0"))
                        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                        .components(new Components().addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
        }

}