package com.readnshare.itemshelfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateWishlistRequest {

    @NotBlank(message = "title can not be empty")
    private String title;

    @NotBlank(message = "description can not be empty")
    private String description;

    @NotNull(message = "itemIds can not be null")
    @Size(min = 1, message = "size of itemIds must be larger than 0")
    private List<String> itemIds;

    @NotBlank(message = "itemType can not be empty and must be one of the following values: 'BOOK' or 'MOVIE'")
    private String itemType;

    @NotBlank(message = "wishlistType can not be empty and must be one of the following values: 'PUBLIC' or 'PRIVATE'")
    private String wishlistType;
}
