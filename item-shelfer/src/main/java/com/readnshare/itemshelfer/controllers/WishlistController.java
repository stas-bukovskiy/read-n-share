package com.readnshare.itemshelfer.controllers;


import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.domain.Wishlist;
import com.readnshare.itemshelfer.dto.AccessRightDto;
import com.readnshare.itemshelfer.dto.CreateWishlistRequest;
import com.readnshare.itemshelfer.dto.UpdateWishlistRequest;
import com.readnshare.itemshelfer.dto.WishlistDto;
import com.readnshare.itemshelfer.mappers.AccessRightMapper;
import com.readnshare.itemshelfer.mappers.WishlistMapper;
import com.readnshare.itemshelfer.services.AccessRightService;
import com.readnshare.itemshelfer.services.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/wishlists")
@RequiredArgsConstructor
@Tags(@Tag(name = "Books", description = "Wishlist REST Controller"))
public class WishlistController {

    private final WishlistService wishlistService;
    private final AccessRightService accessRightService;


    @Operation(method = "getAllWishlists",
            summary = "Get list of all wishlists with opportunity to filter result",
            operationId = "getAllWishlists",
            description = "Get list of all wishlists with opportunity to filter by item type or/and by user id")
    @GetMapping
    public Flux<WishlistDto> getAllWishlists(@RequestParam(name = "itemType", required = false, defaultValue = "")
                                             String itemTypeString,
                                             @RequestParam(name = "userId", required = false, defaultValue = "")
                                             String userId) {
        Flux<Tuple2<Wishlist, AccessRight>> wishlists;
        if (userId.equals("")) {
            if (itemTypeString.equals("")) {
                wishlists = wishlistService.getAllOfCurrentUser();
            } else {
                Wishlist.ItemType itemType = WishlistMapper.parseItemType(itemTypeString);
                wishlists = wishlistService.getAllByItemType(itemType);
            }
        } else {
            if (itemTypeString.equals("")) {
                wishlists = wishlistService.getAllByUserId(userId);
            } else {
                Wishlist.ItemType itemType = WishlistMapper.parseItemType(itemTypeString);
                wishlists = wishlistService.getAllByUserIdItemType(userId, itemType);
            }
        }
        return wishlists.map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                wishlistAndAccessRightTuple2.getT1(),
                wishlistAndAccessRightTuple2.getT2()
        ));
    }


    @Operation(method = "getWishlist",
            summary = "Get list of all wishlists with opportunity to filter result",
            operationId = "getWishlist",
            description = "Get list of all wishlists with opportunity to filter by item type or/and by user id")
    @GetMapping("/{id}")
    public Mono<WishlistDto> getWishlist(@PathVariable String id) {
        return wishlistService.getById(id)
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Operation(method = "createWishlist",
            summary = "Create new wishlist",
            operationId = "createWishlist",
            description = "Crete new wishlist")
    @PostMapping
    public Mono<WishlistDto> createWishlist(@RequestBody @Validated CreateWishlistRequest request) {
        return wishlistService.create(WishlistMapper.of(request))
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Operation(method = "updateWishlist",
            summary = "Update existing wishlist",
            operationId = "updateWishlist",
            description = "Update existing wishlist by its id")
    @PutMapping("/{id}")
    public Mono<WishlistDto> updateWishlist(@PathVariable String id, @RequestBody @Validated UpdateWishlistRequest request) {
        return wishlistService.update(id, WishlistMapper.of(request))
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Operation(method = "deleteWishlist",
            summary = "Delete existing wishlist",
            operationId = "deleteWishlist",
            description = "Delete existing wishlist by its id")
    @DeleteMapping("/{id}")
    public Mono<WishlistDto> deleteWishlist(@PathVariable String id) {
        return wishlistService.delete(id)
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Operation(method = "getSharedWithMeWishlists",
            summary = "Delete existing wishlist",
            operationId = "getSharedWithMeWishlists",
            description = "Delete existing wishlist by its id")
    @GetMapping("/shared-with-me")
    public Flux<WishlistDto> getSharedWithMeWishlists() {
        return wishlistService.getSharedWithCurrentUser()
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @Operation(method = "getAccessRights",
            summary = "Get access rights for a wishlist by its id",
            operationId = "getAccessRights",
            description = "Get access rights for a wishlist by its id")
    @GetMapping("/{id}/access-rights")
    public Flux<AccessRightDto> getAccessRights(@PathVariable String id) {
        return accessRightService.getAllAccessRights(id)
                .map(AccessRightMapper::toDTO);
    }


    @Operation(method = "addAccessRight",
            summary = "Add access right to wishlist for user",
            operationId = "addAccessRight",
            description = "Add access right to wishlist by its id for user")
    @PostMapping("/{id}/access-rights")
    public Flux<AccessRightDto> addAccessRight(@PathVariable String id, @RequestBody @Validated AccessRightDto accessRightDto) {
        return accessRightService.addAccessRight(id, AccessRightMapper.of(accessRightDto))
                .map(AccessRightMapper::toDTO);
    }

    @Operation(method = "updateAccessRight",
            summary = "Update access right to wishlist for user",
            operationId = "updateAccessRight",
            description = "Update access right to wishlist by its id for user")
    @PutMapping("/{id}/access-rights")
    public Flux<AccessRightDto> updateAccessRight(@PathVariable String id, @RequestBody @Validated AccessRightDto accessRightDto) {
        return accessRightService.updateAccessRight(id, AccessRightMapper.of(accessRightDto))
                .map(AccessRightMapper::toDTO);
    }

    @Operation(method = "deleteAccessRight",
            summary = "Delete access right to wishlist for user",
            operationId = "deleteAccessRight",
            description = "Delete access right to wishlist by its id and by user id")
    @DeleteMapping("/{id}/access-rights/{userId}")
    public Flux<AccessRightDto> deleteAccessRight(@PathVariable String id, @PathVariable String userId) {
        return accessRightService.deleteAccessRight(id, userId)
                .map(AccessRightMapper::toDTO);
    }


}


