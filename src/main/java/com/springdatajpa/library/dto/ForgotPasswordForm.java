package com.springdatajpa.library.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordForm {

    @NotBlank(message = "Укажите email.")
    @Email(message = "Укажите корректный email.")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
