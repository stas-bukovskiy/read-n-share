package com.readnshare.itemfinder.delivery.http;

import com.readnshare.itemfinder.dto.MovieDto;
import com.readnshare.itemfinder.dto.SearchDataDto;
import com.readnshare.itemfinder.mappers.MovieMapper;
import com.readnshare.itemfinder.mappers.SearchDataMapper;
import com.readnshare.itemfinder.services.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/movie")
@RequiredArgsConstructor
@Tags(@Tag(name = "Movies", description = "Movie REST Controller"))
public class MovieController {

    private final MovieService movieService;

    @Operation(method = "searchMoviesByExpression",
            summary = "Search movies and series by expression",
            operationId = "searchMoviesByExpression",
            description = "Search movies and series by given expression that encounters in titles")
    @GetMapping(path = "search/{expression}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SearchDataDto>> searchMoviesByExpression(@PathVariable String expression) {
        return movieService.searchMovieByExpression(expression)
                .map(searchData -> ResponseEntity.ok(SearchDataMapper.toDto(searchData)));
    }

    @Operation(method = "getMovieByImdbId",
            summary = "Search movie or series by its IMDb id",
            operationId = "getMovieByImdbId",
            description = "Search movie or series by given IMDb id")
    @GetMapping(path = "{imdbId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MovieDto>> getMovieByImdbId(@PathVariable String imdbId) {
        return movieService.getMovieByImdbId(imdbId)
                .map(searchData -> ResponseEntity.ok(MovieMapper.toDto(searchData)));
    }


}
