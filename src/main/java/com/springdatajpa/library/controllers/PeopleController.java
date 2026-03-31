package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.services.PeopleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
    public String create(@ModelAttribute("personForm") @Valid PersonForm personForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "people/new";
        }
        peopleService.save(personForm);
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
        peopleService.update(id, personForm);
        return "redirect:/people";
    }

    @DeleteMapping("/{personId}")
    public String delete(@PathVariable("personId") int id) {
        peopleService.delete(id);
        return "redirect:/people";
    }
}
