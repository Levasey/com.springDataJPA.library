package com.springdatajpa.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "{validation.registration.username.notblank}")
    @Size(min = 3, max = 64, message = "{validation.registration.username.size}")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
