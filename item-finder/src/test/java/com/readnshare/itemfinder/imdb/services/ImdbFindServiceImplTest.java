package com.readnshare.itemfinder.imdb.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(classes = {ImdbProperties.class, ImdbFindServiceImpl.class})
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        MongoReactiveDataAutoConfiguration.class,
        MongoReactiveAutoConfiguration.class
})
class ImdbFindServiceImplTest {

    @Autowired
    private ImdbFindServiceImpl imdbFindService;


    @Test
    void searchWithEmptyExpression_shouldThrowException() {
        StepVerifier.create(imdbFindService.search(""))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void searchWithNullExpression_shouldThrowException() {
        StepVerifier.create(imdbFindService.search(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void searchValidExpression_shouldReturnSomeResult() {
        final String expression = "inception 2010";

        StepVerifier.create(imdbFindService.search(expression))
                .consumeNextWith(searchData -> {
                    assertEquals("", searchData.getErrorMessage());
                    assertNotEquals(0, searchData.getResults().size());
                })
                .verifyComplete();
    }

    @Test
    void getMovieInfoWithEmptyExpression_shouldThrowException() {
        StepVerifier.create(imdbFindService.getMovieInfo(""))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getMovieInfoWithNullExpression_shouldThrowException() {
        StepVerifier.create(imdbFindService.getMovieInfo(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getMovieInfoWithInvalidExpression_shouldThrowException() {
        StepVerifier.create(imdbFindService.getMovieInfo("invalid_because_not_started_with_tt"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void getMovieInfoWithValidExpression_shouldReturnMovieData() {
        final String imdbId = "tt1375666";

        StepVerifier.create(imdbFindService.getMovieInfo(imdbId))
                .consumeNextWith(result -> assertEquals("", result.getErrorMessage()))
                .verifyComplete();
    }

}