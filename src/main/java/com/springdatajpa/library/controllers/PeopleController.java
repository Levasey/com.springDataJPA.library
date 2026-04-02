package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.services.PeopleService;
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

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
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
        if (bindingResult.hasErrors()) {
            return "people/new";
        }
        try {
            peopleService
                    .save(personForm)
                    .ifPresent(link -> redirectAttributes.addFlashAttribute("readerCatalogSetupLink", link));
        } catch (ConflictException ex) {
            rejectConflict(ex, bindingResult);
            return "people/new";
        }
        return "redirect:/people";
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
        try {
            peopleService.update(id, personForm);
        } catch (ConflictException ex) {
            rejectConflict(ex, bindingResult);
            model.addAttribute("personId", id);
            return "people/edit";
        }
        return "redirect:/people";
    }

    @DeleteMapping("/{personId}")
    public String delete(@PathVariable("personId") int id) {
        peopleService.delete(id);
        return "redirect:/people";
    }

    private static void rejectConflict(ConflictException ex, BindingResult bindingResult) {
        String field = ex.getField();
        if (field != null && !field.isBlank()) {
            bindingResult.rejectValue(field, "conflict", ex.getMessage());
        } else {
            bindingResult.reject("conflict", ex.getMessage());
        }
    }
}
