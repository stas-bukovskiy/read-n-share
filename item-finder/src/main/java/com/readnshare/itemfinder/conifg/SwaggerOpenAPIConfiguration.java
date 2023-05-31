package com.readnshare.itemfinder.conifg;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(info = @Info(
        title = "Item-Finder Service",
        description = "Service for searching movies and books in a database or third-party API " +
                "such as IMDb and Google Books API",
        contact = @Contact(
                name = "Stanislav Bukovskyi",
                email = "stas.bukovskyi@gmail.com",
                url = "https://github.com/stas-bukovskiy"
        ),
        version = "1.0.0"
))
@Configuration
public class SwaggerOpenAPIConfiguration {
}