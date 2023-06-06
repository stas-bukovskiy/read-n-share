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


    @GetMapping("/{id}")
    public Mono<WishlistDto> getWishlist(@PathVariable String id) {
        return wishlistService.getById(id)
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @PostMapping
    public Mono<WishlistDto> createWishlist(@RequestBody @Validated CreateWishlistRequest request) {
        return wishlistService.create(WishlistMapper.of(request))
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @PutMapping("/{id}")
    public Mono<WishlistDto> updateWishlist(@PathVariable String id, @RequestBody @Validated UpdateWishlistRequest request) {
        return wishlistService.update(id, WishlistMapper.of(request))
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @DeleteMapping("/{id}")
    public Mono<WishlistDto> deleteWishlist(@PathVariable String id) {
        return wishlistService.delete(id)
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }

    @GetMapping("/shared-with-me")
    public Flux<WishlistDto> getSharedWithMeWishlists() {
        return wishlistService.getSharedWithCurrentUser()
                .map(wishlistAndAccessRightTuple2 -> WishlistMapper.toDTO(
                        wishlistAndAccessRightTuple2.getT1(),
                        wishlistAndAccessRightTuple2.getT2()
                ));
    }


    @GetMapping("/{id}/access-rights")
    public Flux<AccessRightDto> getAccessRights(@PathVariable String id) {
        return accessRightService.getAllAccessRights(id)
                .map(AccessRightMapper::toDTO);
    }

    @PostMapping("/{id}/access-rights")
    public Flux<AccessRightDto> addAccessRight(@PathVariable String id, @RequestBody @Validated AccessRightDto accessRightDto) {
        return accessRightService.addAccessRight(id, AccessRightMapper.of(accessRightDto))
                .map(AccessRightMapper::toDTO);
    }

    @PutMapping("/{id}/access-rights")
    public Flux<AccessRightDto> updateAccessRight(@PathVariable String id, @RequestBody @Validated AccessRightDto accessRightDto) {
        return accessRightService.updateAccessRight(id, AccessRightMapper.of(accessRightDto))
                .map(AccessRightMapper::toDTO);
    }

    @DeleteMapping("/{id}/access-rights/{userId}")
    public Flux<AccessRightDto> deleteAccessRight(@PathVariable String id, @PathVariable String userId) {
        return accessRightService.deleteAccessRight(id, userId)
                .map(AccessRightMapper::toDTO);
    }


}


