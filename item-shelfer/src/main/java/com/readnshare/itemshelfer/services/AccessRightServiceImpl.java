package com.readnshare.itemshelfer.services;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import com.readnshare.itemshelfer.exceptions.AccessRightConflictException;
import com.readnshare.itemshelfer.exceptions.NotEnoughAccessRight;
import com.readnshare.itemshelfer.exceptions.NotFoundException;
import com.readnshare.itemshelfer.repositories.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessRightServiceImpl implements AccessRightService {

    private final UserService userService;
    private final WishlistRepository wishlistRepository;

    @Override
    public Mono<AccessRight> getAccessRightOfCurrentUser(Wishlist wishlist) {
        return userService.getCurrentUserId()
                .flatMap(currentUserId -> {
                    Optional<AccessRight> currentUserAccessRight = findAccessRightByUserId(wishlist, currentUserId);
                    return currentUserAccessRight.map(Mono::just)
                            .orElse(wishlist.getWishlistType().equals(Wishlist.WishlistType.PUBLIC) ? Mono.just(AccessRight.builder().userId(currentUserId).permission(AccessRight.Permission.READ).build()) : Mono.empty());
                })
                .doOnSuccess(accessRight -> log.debug("successfully get access right <{}> for user and wishlist <{}>", accessRight, wishlist))
                .doOnError(error -> log.debug("error occurred during access right getting:", error));
    }

    @Override
    public Flux<AccessRight> getAllAccessRights(String wishlistId) {
        return getWishlistWithAccessRightCheck(wishlistId)
                .map(Wishlist::getRights)
                .doOnSuccess(rights -> log.debug("successfully get all access rights <{}> for user and wishlist <{}>", rights, wishlistId))
                .doOnError(error -> log.debug("error occurred during all access rights getting:", error))
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<AccessRight> addAccessRight(String wishlistId, AccessRight rightToaAdd) {
        return getWishlistWithAccessRightCheck(wishlistId)
                .handle((wishlist, sink) -> {
                    Optional<AccessRight> existedUserAccessRight = findAccessRightByUserId(wishlist, rightToaAdd.getUserId());
                    if (existedUserAccessRight.isPresent() && existedUserAccessRight.get().getPermission().equals(AccessRight.Permission.OWNER)) {
                        sink.error(new AccessRightConflictException("Owner cannot change his access right"));
                    } else if (rightToaAdd.getPermission().equals(AccessRight.Permission.OWNER)) {
                        sink.error(new AccessRightConflictException("Only one user can have owner permission"));
                    } else {
                        existedUserAccessRight.ifPresent(accessRight -> wishlist.getRights().remove(accessRight));
                        wishlist.getRights().add(rightToaAdd);
                        sink.next(wishlist);
                    }
                }).cast(Wishlist.class)
                .flatMap(wishlistRepository::save)
                .doOnSuccess(wishlist -> log.debug("successfully add access right <{}> to wishlist <{}>", rightToaAdd, wishlist))
                .doOnError(error -> log.debug("error occurred during access right adding:", error))
                .map(Wishlist::getRights)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<AccessRight> updateAccessRight(String wishlistId, AccessRight rightToUpdate) {
        return getWishlistWithAccessRightCheck(wishlistId)
                .handle((wishlist, sink) -> {
                    Optional<AccessRight> userAccessRightToUpdate = findAccessRightByUserId(wishlist, rightToUpdate.getUserId());
                    if (userAccessRightToUpdate.isEmpty()) {
                        sink.error(new AccessRightConflictException("User with id <" + rightToUpdate.getUserId() + "> does not have any permission to wishlist with id <" + wishlistId + ">"));
                    } else if (userAccessRightToUpdate.get().getPermission().equals(AccessRight.Permission.OWNER)) {
                        sink.error(new AccessRightConflictException("Owner can not update their own permission"));
                    } else if (rightToUpdate.getPermission().equals(AccessRight.Permission.OWNER)) {
                        sink.error(new AccessRightConflictException("Only one user can have owner permission"));
                    } else {
                        wishlist.getRights().remove(userAccessRightToUpdate.get());
                        wishlist.getRights().add(rightToUpdate);
                        sink.next(wishlist);
                    }
                }).cast(Wishlist.class)
                .flatMap(wishlistRepository::save)
                .doOnSuccess(wishlist -> log.debug("successfully update access right <{}> for user in wishlist <{}>", rightToUpdate, wishlist))
                .doOnError(error -> log.debug("error occurred during access right updating for user <{}>:", rightToUpdate.getUserId(), error))
                .map(Wishlist::getRights)
                .flatMapMany(Flux::fromIterable);
    }

    @Override
    public Flux<AccessRight> deleteAccessRight(String wishlistId, String userId) {
        return getWishlistWithAccessRightCheck(wishlistId)
                .handle((wishlist, sink) -> {
                    Optional<AccessRight> existedUserAccessRight = findAccessRightByUserId(wishlist, userId);
                    if (existedUserAccessRight.isEmpty()) {
                        sink.error(new AccessRightConflictException("User with id <" + userId + "> does not have any permission to wishlist with id <" + wishlistId + ">"));
                    } else if (existedUserAccessRight.get().getPermission().equals(AccessRight.Permission.OWNER)) {
                        sink.error(new AccessRightConflictException("Owner can not be deleted"));
                    } else {
                        wishlist.getRights().removeIf(accessRight -> accessRight.getUserId().equals(userId));
                        sink.next(wishlist);
                    }
                }).cast(Wishlist.class)
                .flatMap(wishlistRepository::save)
                .doOnSuccess(wishlist -> log.debug("successfully delete access right for user <{}> from wishlist <{}>", userId, wishlist))
                .doOnError(error -> log.debug("error occurred during access right deleting:", error))
                .map(Wishlist::getRights)
                .flatMapMany(Flux::fromIterable);
    }

    private Mono<Wishlist> getWishlistWithAccessRightCheck(String wishlistId) {
        return wishlistRepository.findById(wishlistId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Not found wishlist with id = " + wishlistId)
                ))
                .zipWhen(wishlist -> getAccessRightOfCurrentUser(wishlist)
                        .map(accessRight -> accessRight.getPermission().equals(AccessRight.Permission.OWNER)))
                .handle((wishlistAndIsUserOwner, sink) -> {
                    if (!wishlistAndIsUserOwner.getT2())
                        sink.error(new NotEnoughAccessRight("Not enough access rights to get access right for id: " + wishlistId));
                    sink.next(wishlistAndIsUserOwner.getT1());
                }).cast(Wishlist.class)
                .doOnSuccess(wishlist -> log.debug("successfully get wishlist <{}> by its id with check on ownership", wishlist))
                .doOnError(error -> log.debug("error occurred during wishlist getting and ownership permission check:", error));
    }

    private Optional<AccessRight> findAccessRightByUserId(Wishlist wishlist, String userId) {
        return wishlist.getRights().stream()
                .filter(right -> right.getUserId().equals(userId))
                .findAny();
    }
}
