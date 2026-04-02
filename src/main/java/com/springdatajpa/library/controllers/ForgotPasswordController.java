package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.ForgotPasswordForm;
import com.springdatajpa.library.services.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(@ModelAttribute("forgotPasswordForm") ForgotPasswordForm form) {
        return "forgotPassword";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(
            @Valid @ModelAttribute("forgotPasswordForm") ForgotPasswordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "forgotPassword";
        }
        passwordResetService.requestPasswordReset(form.getEmail());
        redirectAttributes.addFlashAttribute("passwordResetAcknowledged", true);
        return "redirect:/forgot-password";
    }
}
