package com.springdatajpa.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReaderChangePasswordForm {

    @NotBlank(message = "Укажите текущий пароль.")
    private String currentPassword;

    @NotBlank(message = "Укажите новый пароль.")
    @Size(min = 4, max = 128, message = "Пароль: от 4 до 128 символов.")
    private String newPassword;

    @NotBlank(message = "Повторите новый пароль.")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
