package com.readnshare.itemreviewer.mappers;

import com.readnshare.itemreviewer.domain.User;
import com.readnshare.itemreviewer.dto.UserDto;
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
