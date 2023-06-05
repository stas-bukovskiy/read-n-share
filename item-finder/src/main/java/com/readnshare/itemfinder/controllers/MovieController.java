package com.readnshare.itemfinder.controllers;

import com.readnshare.itemfinder.dto.MovieDto;
import com.readnshare.itemfinder.dto.MovieSearchDataDto;
import com.readnshare.itemfinder.mappers.MovieMapper;
import com.readnshare.itemfinder.mappers.MovieSearchDataMapper;
import com.readnshare.itemfinder.services.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

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
    public Mono<ResponseEntity<MovieSearchDataDto>> searchMoviesByExpression(@PathVariable String expression) {
        return movieService.searchMovieByExpression(expression)
                .map(searchData -> ResponseEntity.ok(MovieSearchDataMapper.toDto(searchData)));
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

    @Operation(method = "verifyImdbIds",
            summary = "Verify IMDb Ids and get movies",
            operationId = "verifyImdbIds",
            description = "Verify IMDb Ids and get movies if all ids are valid")
    @GetMapping("/verify")
    private Mono<ResponseEntity<List<MovieDto>>> verifyImdbIds(@RequestParam List<String> imdbIds) {
        return movieService.getMovieByImdbIds(imdbIds)
                .map(MovieMapper::toDto)
                .collectList()
                .map(ResponseEntity::ok);
    }


}
