package com.springdatajpa.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReaderChangePasswordForm {

    @NotBlank(message = "{validation.reader.password.current.notblank}")
    private String currentPassword;

    @NotBlank(message = "{validation.reader.password.new.notblank}")
    @Size(min = 10, max = 128, message = "{validation.reader.password.new.size}")
    private String newPassword;

    @NotBlank(message = "{validation.reader.password.confirm.notblank}")
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
