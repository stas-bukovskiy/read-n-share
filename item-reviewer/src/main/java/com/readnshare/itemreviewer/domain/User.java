package com.readnshare.itemreviewer.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
@Builder
public class User {

    private String id;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

}