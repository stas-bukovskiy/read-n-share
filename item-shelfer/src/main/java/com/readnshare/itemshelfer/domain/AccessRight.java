package com.readnshare.itemshelfer.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessRight {

    private String userId;
    private Permission permission;

    public enum Permission {
        READ,
        MODIFY,
        OWNER

    }
}
