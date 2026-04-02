package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.CatalogPasswordSetupForm;
import com.springdatajpa.library.services.CatalogPasswordSetupService;
import jakarta.validation.Valid;
import java.util.Objects;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/catalog/setup-password")
public class CatalogPasswordSetupController {

    private final CatalogPasswordSetupService catalogPasswordSetupService;

    public CatalogPasswordSetupController(CatalogPasswordSetupService catalogPasswordSetupService) {
        this.catalogPasswordSetupService = catalogPasswordSetupService;
    }

    @GetMapping
    public String showForm(
            @RequestParam(value = "token", required = false) String token,
            @ModelAttribute("form") CatalogPasswordSetupForm form) {
        if (token != null && !token.isBlank()) {
            form.setToken(token);
        }
        return "catalog/setup-password";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("form") CatalogPasswordSetupForm form, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "catalog/setup-password";
        }
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "match", "Пароли не совпадают.");
            return "catalog/setup-password";
        }
        catalogPasswordSetupService.setPasswordFromToken(form.getToken(), form.getPassword());
        return "redirect:/login?passwordSet";
    }
}
