package com.readnshare.itemreviewer.services;

import com.readnshare.itemreviewer.domain.ItemType;
import reactor.core.publisher.Mono;

public interface VerifyItemService {
    Mono<String> verify(String itemId, ItemType itemType);
}
