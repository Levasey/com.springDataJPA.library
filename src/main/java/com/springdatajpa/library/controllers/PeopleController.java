package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.services.PeopleService;
import com.springdatajpa.library.services.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;
    private final RegistrationService registrationService;

    public PeopleController(PeopleService peopleService, RegistrationService registrationService) {
        this.peopleService = peopleService;
        this.registrationService = registrationService;
    }

    @GetMapping()
    public String index(Model model) {
        model.addAttribute("people", peopleService.findAll());

        return "people/index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        if (StringUtils.hasText(q)) {
            String trimmed = q.trim();
            model.addAttribute("people", peopleService.searchPeople(trimmed));
            model.addAttribute("q", trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed);
        }
        return "people/search";
    }

    @GetMapping("/{personId}")
    public String show(@PathVariable("personId") int id, Model model) {
        model.addAttribute("person", peopleService.findById(id));
        model.addAttribute("books", peopleService.getBooksByPersonId(id));
        return "people/show";
    }

    @GetMapping("/new")
    public String newPerson(@ModelAttribute("personForm") PersonForm personForm) {
        return "people/new";
    }

    @PostMapping()
    public String create(
            @ModelAttribute("personForm") @Valid PersonForm personForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        String catalogLogin = RegistrationService.catalogUsernameFromEmail(personForm.getEmail());
        String normCard = normalizeReaderCard(personForm.getReaderCardNumber());
        if (!bindingResult.hasErrors()) {
            if (peopleService.isEmailTakenBySomeoneElse(catalogLogin, null)) {
                bindingResult.rejectValue(
                        "email", "duplicate.personEmail", "Этот email уже указан у другого читателя.");
            }
            if (!bindingResult.hasErrors() && peopleService.isReaderCardTakenBySomeoneElse(normCard, null)) {
                bindingResult.rejectValue(
                        "readerCardNumber",
                        "duplicate.readerCard",
                        "Этот номер читательского билета уже используется.");
            }
            if (!bindingResult.hasErrors() && !catalogLogin.isBlank()
                    && registrationService.usernameExists(catalogLogin)) {
                bindingResult.rejectValue(
                        "email", "duplicate.catalog", "Этот email уже используется для входа в каталог.");
            }
        }

        if (bindingResult.hasErrors()) {
            return "people/new";
        }
        peopleService
                .save(personForm)
                .ifPresent(link -> redirectAttributes.addFlashAttribute("readerCatalogSetupLink", link));
        return "redirect:/people";
    }

    private static String normalizeReaderCard(String readerCard) {
        return readerCard == null ? "" : readerCard.trim();
    }

    @GetMapping("/{personId}/edit")
    public String edit(Model model, @PathVariable("personId") int id) {
        model.addAttribute("personForm", PersonForm.from(peopleService.findById(id)));
        model.addAttribute("personId", id);
        return "people/edit";
    }

    @PatchMapping("/{personId}")
    public String update(@ModelAttribute("personForm") @Valid PersonForm personForm, BindingResult bindingResult,
                         @PathVariable("personId") int id, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("personId", id);
            return "people/edit";
        }
        String catalogLogin = RegistrationService.catalogUsernameFromEmail(personForm.getEmail());
        String normCard = normalizeReaderCard(personForm.getReaderCardNumber());
        if (peopleService.isEmailTakenBySomeoneElse(catalogLogin, id)) {
            bindingResult.rejectValue(
                    "email", "duplicate.personEmail", "Этот email уже указан у другого читателя.");
        }
        if (!bindingResult.hasErrors() && peopleService.isReaderCardTakenBySomeoneElse(normCard, id)) {
            bindingResult.rejectValue(
                    "readerCardNumber",
                    "duplicate.readerCard",
                    "Этот номер читательского билета уже используется.");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("personId", id);
            return "people/edit";
        }
        peopleService.update(id, personForm);
        return "redirect:/people";
    }

    @DeleteMapping("/{personId}")
    public String delete(@PathVariable("personId") int id) {
        peopleService.delete(id);
        return "redirect:/people";
    }
}
