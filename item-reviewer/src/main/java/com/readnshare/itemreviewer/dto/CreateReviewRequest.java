package com.readnshare.itemreviewer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateReviewRequest {

    @NotBlank(message = "itemId can not be blank")
    private String itemId;

    @NotBlank(message = "review can not be blank")
    private String review;

    @NotBlank(message = "itemType can not be black and mes be one oh the following values: 'BOOK', 'MOVIE'")
    private String itemType;

}
