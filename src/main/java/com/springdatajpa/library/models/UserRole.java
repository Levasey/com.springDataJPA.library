package com.springdatajpa.library.models;

public enum UserRole {
    USER,
    LIBRARIAN;

    /** Значение для {@link org.springframework.security.core.GrantedAuthority} ({@code ROLE_…}). */
    public String getSpringAuthority() {
        return "ROLE_" + name();
    }
}
