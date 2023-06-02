package com.readnshare.itemshelfer.services;

import com.readnshare.itemshelfer.domain.Wishlist;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ItemVerifyingService {
    Mono<Set<String>> verify(Set<String> itemIds, Wishlist.ItemType itemType);
}
