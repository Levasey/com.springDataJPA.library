package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.RegistrationForm;
import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.services.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String registerForm(@ModelAttribute("registrationForm") RegistrationForm form) {
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        String username = form.getUsername() == null ? "" : form.getUsername().trim();

        if (bindingResult.hasErrors()) {
            return "register";
        }

        String initialPassword;
        try {
            initialPassword = registrationService.register(username);
        } catch (ConflictException e) {
            String field = e.getField() != null ? e.getField() : "username";
            bindingResult.rejectValue(field, "duplicate", e.getMessage());
            return "register";
        }
        redirectAttributes.addFlashAttribute(
                "registeredUsername", RegistrationService.catalogUsernameFromEmail(username));
        redirectAttributes.addFlashAttribute("registeredInitialPassword", initialPassword);
        return "redirect:/register";
    }
}
