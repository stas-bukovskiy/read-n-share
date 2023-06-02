package com.readnshare.itemshelfer.mappers;

import com.readnshare.itemshelfer.domain.AccessRight;

public final class AccessRightMapper {

    private AccessRightMapper() {
    }

    public static v1.AccessRight toGRPC(AccessRight accessRight) {
        return v1.AccessRight.newBuilder()
                .setUserId(accessRight.getUserId())
                .setPermission(v1.Permission.valueOf(accessRight.getPermission().name()))
                .build();
    }

    public static AccessRight of(v1.AccessRight accessRight) {
        return AccessRight.builder()
                .userId(accessRight.getUserId())
                .permission(AccessRight.Permission.valueOf(accessRight.getPermission().name()))
                .build();
    }
}
