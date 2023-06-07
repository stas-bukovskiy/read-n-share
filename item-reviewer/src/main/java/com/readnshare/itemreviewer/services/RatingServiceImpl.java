package com.readnshare.itemreviewer.services;

import com.readnshare.itemreviewer.domain.ItemRating;
import com.readnshare.itemreviewer.domain.ItemType;
import com.readnshare.itemreviewer.domain.UserRating;
import com.readnshare.itemreviewer.dto.CreateUserRatingRequest;
import com.readnshare.itemreviewer.exceptions.NotFoundException;
import com.readnshare.itemreviewer.exceptions.RatingException;
import com.readnshare.itemreviewer.repositories.UserRatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final UserRatingRepository userRatingRepository;
    private final UserService userService;
    private final VerifyItemService verifyItemService;

    @Override
    public Mono<ItemRating> getItemRating(String itemId) {
        return userRatingRepository.findAllByItemId(itemId)
                .collectList()
                .map(reviews -> {
                    return ItemRating.builder()
                            .itemId(itemId)
                            .votesCount(reviews.size())
                            .rating(reviews.stream()
                                    .mapToDouble(UserRating::getRating)
                                    .average()
                                    .orElse(0.0))
                            .build();
                })
                .doOnSuccess(itemRating -> log.debug("successfully counted item rating for item <{}>: {}", itemId, itemRating))
                .doOnError(error -> log.error("error occurred during item rating counting for item <{}>:", itemId, error));
    }

    @Override
    public Mono<UserRating> getItemRatingOfCurrentUser(String itemId) {
        return userService.getCurrentUserId()
                .flatMap(currentUserId -> userRatingRepository.findByItemIdAndUserId(itemId, currentUserId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Not found user rating with item idL " + itemId))))
                .doOnSuccess(userRating -> log.debug("successfully found user rating for item <{}>: {}", itemId, userRating))
                .doOnError(error -> log.error("error occurred during user rating finding for item <{}>:", itemId, error));
    }

    @Override
    public Mono<UserRating> createRating(CreateUserRatingRequest request) {
        return verifyItemService.verify(request.getItemId(), parseItemType(request.getItemType()))
                .flatMap(verified -> userService.getCurrentUserId())
                .flatMap(userId -> userRatingRepository.existsByItemIdAndUserId(request.getItemId(), userId)
                        .handle((isExist, sink) -> {
                            if (isExist) {
                                sink.error(new RatingException("rating already exists for item <" + request.getItemId() + ">"));
                                return;
                            }
                            UserRating userRating = UserRating.builder()
                                    .itemId(request.getItemId())
                                    .userId(userId)
                                    .rating(request.getRating())
                                    .build();
                            sink.next(userRating);
                        }))
                .cast(UserRating.class)
                .flatMap(userRatingRepository::save)
                .doOnSuccess(userRating -> log.debug("successfully added user rating: {}", request))
                .doOnError(error -> log.error("error occurred during user rating adding: <{}>:", request, error));
    }

    @Override
    public Mono<UserRating> updateRating(String itemId, int rating) {
        return getItemRatingOfCurrentUser(itemId)
                .handle((userRating, sink) -> {
                    if (rating < 0 || rating > 10) {
                        sink.error(new RatingException("rating must be between 0 and 10"));
                        return;
                    }
                    userRating.setRating(rating);
                    sink.next(userRating);
                })
                .cast(UserRating.class)
                .flatMap(userRatingRepository::save)
                .doOnSuccess(userRating -> log.debug("successfully updated user rating for item <{}>: {}", itemId, userRating))
                .doOnError(error -> log.error("error occurred during user rating updating for item <{}>:", itemId, error));
    }

    @Override
    public Mono<UserRating> deleteRating(String itemId) {
        return getItemRatingOfCurrentUser(itemId)
                .publishOn(Schedulers.boundedElastic())
                .map(userRatingToDelete -> {
                    userRatingRepository.delete(userRatingToDelete).subscribe();
                    return userRatingToDelete;
                })
                .doOnSuccess(userRating -> log.debug("successfully deleted user rating for item <{}>: {}", itemId, userRating))
                .doOnError(error -> log.error("error occurred during user rating deleting for item <{}>:", itemId, error));
    }


    private ItemType parseItemType(String s) {
        return ItemType.valueOf(s.toUpperCase());
    }


}
