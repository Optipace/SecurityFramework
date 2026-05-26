package com.optipace.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthPrincipal {

    private Long userId;
    private String username;
    private String role;
}
