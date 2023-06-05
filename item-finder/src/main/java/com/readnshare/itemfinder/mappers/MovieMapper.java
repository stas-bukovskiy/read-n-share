package com.readnshare.itemfinder.mappers;

import com.readnshare.itemfinder.domain.Movie;
import com.readnshare.itemfinder.dto.MovieDto;
import com.readnshare.itemfinder.imdb.domain.MovieData;

import static com.readnshare.itemfinder.utils.ParseUtil.*;

public final class MovieMapper {

    private MovieMapper() {
    }

    public static Movie of(MovieData movieData) {
        return Movie.builder()
                .imdbId(movieData.getId())
                .title(movieData.getTitle())
                .originalTitle(movieData.getOriginalTitle())
                .year(parseInteger(movieData.getYear()))
                .runtimeMins(parseInteger(movieData.getRuntimeMins()))
                .plot(movieData.getPlot())
                .imageURL(movieData.getImage())
                .awards(parseStringSet(movieData.getAwards()))
                .genres(parseStringSet(movieData.getGenres()))
                .directors(parseStringSet(movieData.getDirectors()))
                .writers(parseStringSet(movieData.getWriters()))
                .imdbRating(parseDouble(movieData.getImDbRating()))
                .imdbRatingVotes(parseInteger(movieData.getImDbRatingVotes()))
                .build();
    }

    public static MovieDto toDto(Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .imdbId(movie.getImdbId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .year(movie.getYear())
                .runtimeMins(movie.getRuntimeMins())
                .plot(movie.getPlot())
                .imageURL(movie.getImageURL())
                .awards(movie.getAwards().stream().toList())
                .genres(movie.getGenres().stream().toList())
                .directors(movie.getDirectors().stream().toList())
                .writers(movie.getWriters().stream().toList())
                .imdbRating(movie.getImdbRating())
                .imdbRatingVotes(movie.getImdbRatingVotes())
                .build();
    }
}
