package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.ReaderChangePasswordForm;
import com.springdatajpa.library.exception.ResourceNotFoundException;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.services.PeopleService;
import com.springdatajpa.library.services.ReaderCatalogPasswordService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;

@Controller
@RequestMapping("/me")
public class ReaderProfileController {

    private final PeopleService peopleService;
    private final ReaderCatalogPasswordService readerCatalogPasswordService;

    public ReaderProfileController(
            PeopleService peopleService,
            ReaderCatalogPasswordService readerCatalogPasswordService) {
        this.peopleService = peopleService;
        this.readerCatalogPasswordService = readerCatalogPasswordService;
    }

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        Person person = resolveReaderOrNotFound(authentication);
        model.addAttribute("person", person);
        model.addAttribute("books", peopleService.getBooksByPersonId(person.getPersonId()));
        model.addAttribute("readBooks", peopleService.getReadBooksByPersonId(person.getPersonId()));
        return "reader/profile";
    }

    @GetMapping("/password")
    public String changePasswordForm(@ModelAttribute("form") ReaderChangePasswordForm form) {
        return "reader/change-password";
    }

    @PostMapping("/password")
    public String changePasswordSubmit(
            Authentication authentication,
            @Valid @ModelAttribute("form") ReaderChangePasswordForm form,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "reader/change-password";
        }
        if (!Objects.equals(form.getNewPassword(), form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "match", "Пароли не совпадают.");
            return "reader/change-password";
        }
        resolveReaderOrNotFound(authentication);
        readerCatalogPasswordService.changePassword(
                authentication.getName(),
                form.getCurrentPassword(),
                form.getNewPassword());
        return "redirect:/me?passwordChanged";
    }

    private Person resolveReaderOrNotFound(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Читатель не найден.");
        }
        return peopleService
                .findByCatalogLogin(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Читатель не найден."));
    }
}
