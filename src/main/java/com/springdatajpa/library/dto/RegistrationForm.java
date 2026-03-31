package com.springdatajpa.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Укажите имя пользователя")
    @Size(min = 3, max = 64, message = "От 3 до 64 символов")
    private String username;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 8, max = 128, message = "Пароль: от 8 до 128 символов")
    private String password;

    @NotBlank(message = "Подтвердите пароль")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
