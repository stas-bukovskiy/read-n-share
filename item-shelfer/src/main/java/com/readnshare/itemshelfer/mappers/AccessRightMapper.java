package com.readnshare.itemshelfer.mappers;

import com.readnshare.itemshelfer.domain.AccessRight;
import com.readnshare.itemshelfer.dto.AccessRightDto;

public final class AccessRightMapper {

    private AccessRightMapper() {
    }

    public static AccessRightDto toDTO(AccessRight accessRight) {
        return AccessRightDto.builder()
                .userId(accessRight.getUserId())
                .permission(accessRight.getPermission().name())
                .build();
    }

    public static AccessRight of(AccessRightDto accessRightDto) {
        return AccessRight.builder()
                .userId(accessRightDto.getUserId())
                .permission(parsePermission(accessRightDto.getPermission()))
                .build();
    }

    public static AccessRight.Permission parsePermission(String permission) {
        return AccessRight.Permission.valueOf(permission);
    }
}
