package com.springdatajpa.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CatalogPasswordSetupForm {

    @NotBlank(message = "Откройте страницу по ссылке из письма.")
    private String token;

    @NotBlank(message = "Укажите пароль.")
    @Size(min = 10, max = 128, message = "Пароль: от 10 до 128 символов.")
    private String password;

    @NotBlank(message = "Повторите пароль.")
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
