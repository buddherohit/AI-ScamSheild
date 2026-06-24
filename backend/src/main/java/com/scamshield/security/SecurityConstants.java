package com.scamshield.security;

public final class SecurityConstants {

    private SecurityConstants() {
        // Restrict instantiation
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ROLE_CLAIM = "roles";
}
