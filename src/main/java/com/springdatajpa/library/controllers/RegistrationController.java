package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.RegistrationForm;
import com.springdatajpa.library.services.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String registerForm(@ModelAttribute("registrationForm") RegistrationForm form) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/books";
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form, BindingResult bindingResult) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/books";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "match", "Пароли не совпадают");
        }

        String username = form.getUsername() == null ? "" : form.getUsername().trim();
        if (!username.isBlank() && registrationService.usernameExists(username)) {
            bindingResult.rejectValue("username", "duplicate", "Это имя пользователя уже занято");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        registrationService.register(username, form.getPassword());
        return "redirect:/login?registered";
    }
}
