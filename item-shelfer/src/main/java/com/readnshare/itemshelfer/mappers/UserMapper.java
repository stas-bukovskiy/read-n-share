package com.readnshare.itemshelfer.mappers;

import com.readnshare.itemshelfer.domain.User;
import com.readnshare.itemshelfer.dto.UserDto;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public final class UserMapper {

    private UserMapper() {
    }

    public static User of(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .authorities(List.of(new SimpleGrantedAuthority(userDto.getRole().toUpperCase())))
                .build();
    }

}
