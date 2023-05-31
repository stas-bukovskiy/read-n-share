package com.readnshare.itemfinder.imdb.services;

import com.google.common.base.Strings;
import com.readnshare.itemfinder.imdb.domain.MovieData;
import com.readnshare.itemfinder.imdb.domain.MovieSearchData;
import com.readnshare.itemfinder.imdb.exceptions.ImdbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ImdbFindServiceImpl implements ImdbFindService {

    private static final String SERVICE_NAME = "IMDB_FIND_SERVICE";

    private final ImdbProperties properties;
    private final WebClient webClient;

    public ImdbFindServiceImpl(ImdbProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public Mono<MovieSearchData> search(String expression) {
        if (Strings.isNullOrEmpty(expression))
            return Mono.error(new IllegalArgumentException("expression can not be blank"));

        final String searchURL = "/Search/{apiKey}/{expression}";
        return webClient
                .get()
                .uri(searchURL, properties.getImdbToken(), expression)
                .retrieve()
                .bodyToMono(MovieSearchData.class)
                .handle((searchResult, sink) -> {
                    if (StringUtils.hasText(searchResult.getErrorMessage())) {
                        log.error("[{}] error occurred during searching by expression <{}>: {}", SERVICE_NAME, expression, searchResult.getErrorMessage());
                        sink.error(new ImdbException(searchResult.getErrorMessage()));
                    }
                    else {
                        log.debug("[{}] successfully search by expression <{}>: {}", SERVICE_NAME, expression, searchResult);
                        sink.next(searchResult);
                    }
                });
    }

    @Override
    public Mono<MovieData> getMovieInfo(String imdbId) {
        if (Strings.isNullOrEmpty(imdbId))
            return Mono.error(new IllegalArgumentException("imdbId must not be blank"));
        if (!imdbId.startsWith("tt"))
            return Mono.error(new IllegalArgumentException("valid imdbId start with 'tt'"));

        final String getMovieInfoURL = "/Title/{apiKey}/{expression}";
        return webClient
                .get()
                .uri(getMovieInfoURL, properties.getImdbToken(), imdbId)
                .retrieve()
                .bodyToMono(MovieData.class)
                .handle((searchResult, sink) -> {
                    if (StringUtils.hasText(searchResult.getErrorMessage())) {
                        log.error("[{}] error occurred during searching by imdbId <{}>: {}", SERVICE_NAME, imdbId, searchResult.getErrorMessage());
                        sink.error(new ImdbException(searchResult.getErrorMessage()));
                    }
                    else {
                        log.debug("[{}] successfully search by imdbId <{}>: {}", SERVICE_NAME, imdbId, searchResult);
                        sink.next(searchResult);
                    }
                });
    }


}

