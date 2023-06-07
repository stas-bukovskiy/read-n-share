package com.readnshare.itemreviewer.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRatingRequest {

    @NotBlank(message = "itemId can not be blank")
    private String itemId;

    @Min(value = 0, message = "rating must be greater than or equal to 0")
    @Max(value = 10, message = "rating must be smaller than or equal to 10")
    private int rating;

    @NotBlank(message = "itemType can not be black and mes be one oh the following values: 'BOOK', 'MOVIE'")
    private String itemType;

}
