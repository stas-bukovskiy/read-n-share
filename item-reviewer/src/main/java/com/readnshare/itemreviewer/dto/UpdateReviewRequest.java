package com.readnshare.itemreviewer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateReviewRequest {
    @NotBlank(message = "review can not be blank")
    private String review;
}
