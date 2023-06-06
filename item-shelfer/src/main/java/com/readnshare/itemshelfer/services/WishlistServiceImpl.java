package com.readnshare.itemshelfer.services;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import com.readnshare.itemshelfer.exceptions.NotEnoughAccessRight;
import com.readnshare.itemshelfer.repositories.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.LinkedHashSet;
import java.util.List;

import static com.readnshare.itemshelfer.utils.TimestampUtil.getCurrentTimestamp;


@Slf4j
@RequiredArgsConstructor
@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ItemVerifyingService itemVerifyingService;
    private final UserService userService;
    private final AccessRightService accessRightService;


    @Override
    public Flux<Tuple2<Wishlist, AccessRight>> getAllOfCurrentUser() {
        return userService.getCurrentUserId()
                .flatMapMany(userId -> wishlistRepository.findAllByRightsContains(
                        AccessRight.builder().userId(userId).permission(AccessRight.Permission.OWNER).build()
                ))
                .flatMap(wishlist -> Mono.just(wishlist)
                        .zipWhen(accessRightService::getAccessRightOfCurrentUser))
                .doOnComplete(() -> log.debug("successfully get all wishlists of current user"))
                .doOnTerminate(() -> log.error("termination occurred during getting all wishlists of current user:"));
    }

    @Override
    public Flux<Tuple2<Wishlist, AccessRight>> getAllByItemType(Wishlist.ItemType itemType) {
        return userService.getCurrentUserId()
                .flatMapMany(userId -> wishlistRepository.findAllByRightsContainsAndItemType(
                        AccessRight.builder().userId(userId).permission(AccessRight.Permission.OWNER).build(), itemType)
                )
                .flatMap(wishlist -> Mono.just(wishlist)
                        .zipWhen(accessRightService::getAccessRightOfCurrentUser))
                .doOnComplete(() -> log.debug("successfully get all wishlists by itemType"))
                .doOnTerminate(() -> log.error("termination occurred during getting all wishlists by itemType:"));
    }

    @Override
    public Flux<Tuple2<Wishlist, AccessRight>> getAllByUserId(String userId) {
        return wishlistRepository.findAllByRightsContainsAndWishlistType(
                        AccessRight.builder().userId(userId).permission(AccessRight.Permission.OWNER).build(),
                        Wishlist.WishlistType.PUBLIC
                )
                .flatMap(wishlist -> Mono.just(wishlist)
                        .zipWith(verifyAccessRight(wishlist, AccessRight.Permission.READ, "read")))
                .concatWith(getSharedWithCurrentUser()
                        .filter(wishlistAccessRightTuple2 -> wishlistAccessRightTuple2.getT1().getRights().stream()
                                .anyMatch(accessRight -> accessRight.getUserId().equals(userId))))
                .distinct()
                .doOnComplete(() -> log.debug("successfully get all wishlists by user <{}> that can see current user", userId))
                .doOnTerminate(() -> log.error("termination occurred during getting all wishlists that can see current user:"));
    }

    @Override
    public Flux<Tuple2<Wishlist, AccessRight>> getAllByUserIdItemType(String userId, Wishlist.ItemType itemType) {
        return getAllByUserId(userId)
                .filter(wishlistAccessRightTuple2 -> wishlistAccessRightTuple2.getT1().getItemType().equals(itemType));
    }

    @Override
    public Mono<Tuple2<Wishlist, AccessRight>> getById(String wishlistId) {
        return getWishlistById(wishlistId)
                .zipWhen(wishlist -> verifyAccessRight(wishlist, AccessRight.Permission.READ, "read"))
                .doOnSuccess(wishlistAccessRightTuple2 -> log.debug("successfully find wishlist: {}", wishlistAccessRightTuple2.getT1()))
                .doOnError(error -> log.error("error occurred during whitelist finding by id <{}>:", wishlistId, error));
    }

    @Override
    public Mono<Tuple2<Wishlist, AccessRight>> create(Wishlist wishlistDataToCreate) {
        return verifyItemIds(wishlistDataToCreate)
                .zipWith(userService.getCurrentUserId())
                .map(wishlistAndUserId -> {
                    Wishlist wishlistToCreate = wishlistAndUserId.getT1();
                    AccessRight ownerAccessRight = AccessRight.builder()
                            .userId(wishlistAndUserId.getT2())
                            .permission(AccessRight.Permission.OWNER)
                            .build();
                    wishlistToCreate.setId(null);
                    wishlistToCreate.setCreatedAt(getCurrentTimestamp());
                    wishlistToCreate.setUpdatedAt(getCurrentTimestamp());
                    wishlistToCreate.setRights(new LinkedHashSet<>(List.of(ownerAccessRight)));
                    return Tuples.of(wishlistToCreate, ownerAccessRight);
                })
                .flatMap(wishlistAccessRightTuple2 -> wishlistRepository.save(wishlistAccessRightTuple2.getT1())
                        .zipWith(Mono.just(wishlistAccessRightTuple2.getT2())))
                .doOnSuccess(wishlistAccessRightTuple2 -> log.debug("successfully create wishlist: {}", wishlistAccessRightTuple2.getT1()))
                .doOnError(error -> log.error("error occurred during whitelist creating:", error));

    }

    @Override
    public Mono<Tuple2<Wishlist, AccessRight>> update(String id, Wishlist wishlistDataToUpdate) {
        return getWishlistById(id)
                .zipWhen(wishlist -> verifyAccessRight(wishlist, AccessRight.Permission.MODIFY, "update"))
                .zipWhen((accessRightWishlistTuple2) -> {
                    wishlistDataToUpdate.setItemType(accessRightWishlistTuple2.getT1().getItemType());
                    return verifyItemIds(wishlistDataToUpdate);
                }, (accessRightWishlistTuple2, verifiedWishlistDataToUpdate) -> Tuples.of(
                        accessRightWishlistTuple2.getT1(), verifiedWishlistDataToUpdate, accessRightWishlistTuple2.getT2()))
                .map(wishlistWishlistAccessRightTuple3 -> {
                    Wishlist wishlistToUpdate = wishlistWishlistAccessRightTuple3.getT1();
                    Wishlist verifiedWishlistDataToUpdate = wishlistWishlistAccessRightTuple3.getT2();
                    wishlistToUpdate.setTitle(verifiedWishlistDataToUpdate.getTitle());
                    wishlistToUpdate.setDescription(verifiedWishlistDataToUpdate.getDescription());
                    wishlistToUpdate.setItemIds(verifiedWishlistDataToUpdate.getItemIds());
                    wishlistToUpdate.setWishlistType(verifiedWishlistDataToUpdate.getWishlistType());
                    wishlistToUpdate.setUpdatedAt(getCurrentTimestamp());
                    return Tuples.of(wishlistToUpdate, wishlistWishlistAccessRightTuple3.getT3());
                })
                .flatMap(wishlistAccessRightTuple2 -> wishlistRepository.save(wishlistAccessRightTuple2.getT1())
                        .zipWith(Mono.just(wishlistAccessRightTuple2.getT2())))
                .doOnSuccess(wishlistAccessRightTuple2 -> log.debug("successfully update wishlist: {}", wishlistAccessRightTuple2.getT1()))
                .doOnError(error -> log.error("error occurred during whitelist updating:", error));
    }

    @Override
    public Mono<Tuple2<Wishlist, AccessRight>> delete(String id) {
        return getWishlistById(id)
                .zipWhen(wishlistToDelete -> verifyAccessRight(wishlistToDelete, AccessRight.Permission.OWNER, "delete"))
                .flatMap(wishlistAccessRightTuple2 -> wishlistRepository.deleteById(wishlistAccessRightTuple2.getT1().getId())
                        .then(Mono.fromCallable(wishlistAccessRightTuple2::getT1))
                        .zipWith(Mono.just(wishlistAccessRightTuple2.getT2())))
                .doOnSuccess(wishlistAccessRightTuple2 -> log.debug("successfully delete wishlist: {}", wishlistAccessRightTuple2.getT1()))
                .doOnError(error -> log.error("error occurred during whitelist deleting:", error));
    }


    @Override
    public Flux<Tuple2<Wishlist, AccessRight>> getSharedWithCurrentUser() {
        return userService.getCurrentUserId()
                .flatMapMany(userId -> wishlistRepository.findByRightsUserIdAndRightsPermissionIsNot(userId, AccessRight.Permission.OWNER))
                .flatMap(wishlist -> Mono.just(wishlist)
                        .zipWhen(accessRightService::getAccessRightOfCurrentUser))
                .doOnComplete(() -> log.debug("successfully get all wishlists shared with current user"))
                .doOnTerminate(() -> log.error("termination occurred during getting all wishlists shared with current user:"));
    }


    private Mono<Wishlist> getWishlistById(String wishlistId) {
        return wishlistRepository.findById(wishlistId)
                .switchIfEmpty(Mono.error(new NumberFormatException("Not found wishlist with id = " + wishlistId)));
    }

    private Mono<Wishlist> verifyItemIds(Wishlist wishlistToVerify) {
        return itemVerifyingService.verify(wishlistToVerify.getItemIds(), wishlistToVerify.getItemType())
                .map(verifiedItemIds -> {
                    wishlistToVerify.setItemIds(verifiedItemIds);
                    return wishlistToVerify;
                });
    }

    private Mono<AccessRight> verifyAccessRight(Wishlist wishlist, AccessRight.Permission permission, String action) {
        return accessRightService.getAccessRightOfCurrentUser(wishlist)
                .switchIfEmpty(Mono.error(new NotEnoughAccessRight(action + " wishlist with id: " + wishlist.getId())))
                .handle((accessRight, sink) -> {
                    boolean isNotEnoughAccess = true;
                    switch (permission) {
                        case READ -> {
                            if (accessRight != null || wishlist.getWishlistType().equals(Wishlist.WishlistType.PUBLIC)) {
                                sink.next(accessRight);
                                isNotEnoughAccess = false;
                            }
                        }
                        case MODIFY -> {
                            if (!accessRight.getPermission().equals(AccessRight.Permission.READ)) {
                                sink.next(accessRight);
                                isNotEnoughAccess = false;
                            }
                        }
                        case OWNER -> {
                            if (accessRight.getPermission().equals(AccessRight.Permission.OWNER)) {
                                sink.next(accessRight);
                                isNotEnoughAccess = false;
                            }
                        }
                    }
                    if (isNotEnoughAccess) {
                        log.debug("user doesn't have permission to " + action + " with this wishlist <{}>", wishlist.getId());
                        sink.error(new NotEnoughAccessRight(action + " wishlist with id: " + wishlist.getId()));
                    }
                })
                .cast(AccessRight.class)
                .doOnSuccess(accessRight -> log.debug("successfully verify access right <{}> to execute action <{}> on wishlist <{}>", accessRight, action, wishlist))
                .doOnError(error -> log.error("error occurred during verifying access right to execute action <{}> on wishlist", action, error));

    }

}
