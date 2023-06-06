package com.readnshare.itemshelfer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessRightDto {
    private String userId;
    private String permission;
}
