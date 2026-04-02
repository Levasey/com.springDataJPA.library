package com.springdatajpa.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CatalogPasswordSetupForm {

    @NotBlank(message = "{validation.catalog.token.notblank}")
    private String token;

    @NotBlank(message = "{validation.catalog.password.notblank}")
    @Size(min = 10, max = 128, message = "{validation.catalog.password.size}")
    private String password;

    @NotBlank(message = "{validation.catalog.password.confirm.notblank}")
    private String confirmPassword;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
