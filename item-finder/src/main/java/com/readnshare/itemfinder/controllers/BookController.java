package com.readnshare.itemfinder.controllers;

import com.readnshare.itemfinder.dto.BookDto;
import com.readnshare.itemfinder.dto.BookSearchDataDto;
import com.readnshare.itemfinder.googlebooks.services.GoogleBookFindService;
import com.readnshare.itemfinder.mappers.BookMapper;
import com.readnshare.itemfinder.mappers.BookSearchDataMapper;
import com.readnshare.itemfinder.services.BookService;
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
@RequestMapping(path = "/api/v1/book")
@RequiredArgsConstructor
@Tags(@Tag(name = "Books", description = "Book REST Controller"))
public class BookController {

    private final BookService bookService;

    @Operation(method = "searchBooksByExpression",
            summary = "Search books by expression",
            operationId = "searchBooksByExpression",
            description = "Search books by given expression that encounters in titles")
    @GetMapping(path = "search/{expression}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BookSearchDataDto>> searchBooksByExpression(@PathVariable String expression,
                                                                           @RequestParam(name = "maxResults", defaultValue = "25") int maxResults,
                                                                           @RequestParam(name = "startIndex", defaultValue = "0") int startIndex,
                                                                           @RequestParam(name = "orderBy", defaultValue = "relevance") String orderBy) {
        return bookService.searchBookByExpression(expression, GoogleBookFindService.BookSearchOrder.valueOf(orderBy.toUpperCase()), startIndex, maxResults)
                .map(searchData -> ResponseEntity.ok(BookSearchDataMapper.toDto(searchData)));
    }

    @Operation(method = "getBooksByGoogleBookId",
            summary = "Search books by its Google Book id",
            operationId = "getBooksByGoogleBookId",
            description = "Search books by given Google Book id")
    @GetMapping(path = "{googleBookId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<BookDto>> getBooksByGoogleBookId(@PathVariable String googleBookId) {
        return bookService.getBookByGoogleId(googleBookId)
                .map(searchData -> ResponseEntity.ok(BookMapper.toDto(searchData)));
    }

    @Operation(method = "verifyGoogleBooksIds",
            summary = "Verify Google Book Ids and get books",
            operationId = "verifyGoogleBooksIds",
            description = "Verify Google Book Ids and get books if all ids are valid")
    @GetMapping("/verify")
    private Mono<ResponseEntity<List<BookDto>>> verifyGoogleBooksIds(@RequestParam List<String> googleBooksIds) {
        return bookService.getBookByGoogleIds(googleBooksIds)
                .map(BookMapper::toDto)
                .collectList()
                .map(ResponseEntity::ok);
    }


}
