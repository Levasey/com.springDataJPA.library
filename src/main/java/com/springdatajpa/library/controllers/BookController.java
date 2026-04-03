package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.BookForm;
import com.springdatajpa.library.exception.ResourceNotFoundException;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Genre;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.models.UserRole;
import com.springdatajpa.library.services.BookService;
import com.springdatajpa.library.services.PeopleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@Validated
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;
    private final PeopleService peopleService;

    public BookController(BookService bookService, PeopleService peopleService) {
        this.bookService = bookService;
        this.peopleService = peopleService;
    }

    @GetMapping()
    public String index(Model model, @RequestParam(value = "page", required = false) Integer page,
                        @RequestParam(value = "books_per_page", required = false) Integer booksPerPage,
                        @RequestParam(value = "sort_by_year", required = false) boolean sortByYear,
                        @RequestParam(value = "sort_by_genre", required = false) boolean sortByGenre,
                        @RequestParam(value = "sort_by_title", required = false) boolean sortByTitle,
                        @RequestParam(value = "sort_by_author", required = false) boolean sortByAuthor,
                        @RequestParam(value = "availability_preset", required = false) String availabilityPresetParam,
                        @RequestParam(value = "sort_by_availability", required = false, defaultValue = "false") boolean sortByAvailabilityLegacy,
                        @RequestParam(value = "availability_issued_first", required = false, defaultValue = "false") boolean availabilityIssuedFirstLegacy) {
        boolean sortByAvailability;
        boolean availabilityIssuedFirst;
        if (StringUtils.hasText(availabilityPresetParam)) {
            sortByAvailability = switch (availabilityPresetParam) {
                case "free", "issued" -> true;
                default -> false;
            };
            availabilityIssuedFirst = "issued".equals(availabilityPresetParam);
        } else {
            sortByAvailability = sortByAvailabilityLegacy;
            availabilityIssuedFirst = availabilityIssuedFirstLegacy;
        }

        Page<Book> booksPage = bookService.findForIndexPage(
                page, booksPerPage, sortByYear, sortByGenre, sortByTitle, sortByAuthor,
                sortByAvailability, availabilityIssuedFirst);
        model.addAttribute("booksPage", booksPage);
        model.addAttribute("sortByYear", sortByYear);
        model.addAttribute("sortByGenre", sortByGenre);
        model.addAttribute("sortByTitle", sortByTitle);
        model.addAttribute("sortByAuthor", sortByAuthor);
        model.addAttribute("sortByAvailability", sortByAvailability);
        model.addAttribute("availabilityIssuedFirst", availabilityIssuedFirst);
        model.addAttribute("availabilityPreset", sortByAvailability
                ? (availabilityIssuedFirst ? "issued" : "free")
                : "all");
        currentCatalogReader().ifPresent(p ->
                model.addAttribute("readBookIds", peopleService.getReadBookIdsForPerson(p.getPersonId())));
        return "books/index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        if (StringUtils.hasText(q)) {
            String trimmed = q.trim();
            model.addAttribute("books", bookService.searchBooks(trimmed));
            model.addAttribute("q", trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed);
        }
        currentCatalogReader(SecurityContextHolder.getContext().getAuthentication()).ifPresent(p ->
                model.addAttribute("readBookIds", peopleService.getReadBookIdsForPerson(p.getPersonId())));
        return "books/search";
    }

    @GetMapping("/new")
    public String newBook(@ModelAttribute("bookForm") BookForm bookForm, Model model) {
        model.addAttribute("genres", Genre.values());
        return "books/new";
    }

    @PostMapping()
    public String create(@ModelAttribute("bookForm") @Valid BookForm bookForm,
                         BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", Genre.values());
            return "books/new";
        }

        bookService.createBook(bookForm);
        return "redirect:/books";
    }

    @GetMapping("/{bookId}")
    public String show(@PathVariable("bookId") int id, Model model) {
        Book book = bookService.findOne(id);
        model.addAttribute("book", book);
        Person owner = book.getOwner();
        if (owner != null) {
            model.addAttribute("owner", owner);
        } else if (isLibrarian()) {
            model.addAttribute("people", peopleService.findAll());
        }
        currentCatalogReader(SecurityContextHolder.getContext().getAuthentication()).ifPresent(p ->
                model.addAttribute("readerMarkedRead", peopleService.hasPersonReadBook(p.getPersonId(), id)));
        return "books/show";
    }

    private static boolean isLibrarian() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority a : authentication.getAuthorities()) {
            if (UserRole.LIBRARIAN.getSpringAuthority().equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/{bookId}/edit")
    public String edit(Model model, @PathVariable("bookId") int id) {
        model.addAttribute("bookForm", BookForm.from(bookService.findOne(id)));
        model.addAttribute("bookId", id);
        model.addAttribute("genres", Genre.values());
        return "books/edit";
    }

    @PatchMapping("/{bookId}")
    public String update(@ModelAttribute("bookForm") @Valid BookForm bookForm, BindingResult bindingResult,
                         @PathVariable("bookId") int id, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", id);
            model.addAttribute("genres", Genre.values());
            return "books/edit";
        }

        bookService.update(id, bookForm);
        return "redirect:/books";
    }

    @DeleteMapping("/{bookId}")
    public String delete(@PathVariable("bookId") int id) {
        bookService.delete(id);
        return "redirect:/books";
    }

    @PatchMapping("/{bookId}/release")
    public String release(@PathVariable("bookId") int id) {
        bookService.release(id);
        return "redirect:/books/" + id;
    }

    @PatchMapping("/{bookId}/assign")
    public String assign(@PathVariable("bookId") int id, @RequestParam("personId") @Min(1) int personId) {
        bookService.assign(id, personId);
        return "redirect:/books/" + id;
    }

    @PatchMapping("/{bookId}/read")
    public String setReadStatus(
            @PathVariable("bookId") int id,
            @RequestParam(value = "read", defaultValue = "true") boolean read) {
        Person reader = resolveCatalogReader(SecurityContextHolder.getContext().getAuthentication());
        peopleService.setReaderBookReadStatus(reader.getPersonId(), id, read);
        return "redirect:/books/" + id;
    }

    private Optional<Person> currentCatalogReader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return currentCatalogReader(authentication);
    }

    private Optional<Person> currentCatalogReader(Authentication authentication) {
        if (authentication == null || !hasRole(authentication, UserRole.USER)) {
            return Optional.empty();
        }
        Optional<Person> found = peopleService.findByCatalogLogin(authentication.getName());
        return found != null ? found : Optional.empty();
    }

    private Person resolveCatalogReader(Authentication authentication) {
        return currentCatalogReader(authentication)
                .orElseThrow(() -> new ResourceNotFoundException("Читатель не найден."));
    }

    private static boolean hasRole(Authentication authentication, UserRole role) {
        if (authentication == null) {
            return false;
        }
        String authority = role.getSpringAuthority();
        for (GrantedAuthority a : authentication.getAuthorities()) {
            if (authority.equals(a.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
