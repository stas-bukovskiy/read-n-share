package com.readnshare.itemshelfer.mappers;

import com.readnshare.itemshelfer.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public final class UserMapper {

    private UserMapper() {
    }

    public static User of(v1.User user) {
        return User.builder()
                .id(user.getId())
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole())))
                .build();
    }
}
