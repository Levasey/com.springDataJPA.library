package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.BookForm;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.services.BookService;
import com.springdatajpa.library.services.PeopleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
                        @RequestParam(value = "sort_by_year", required = false) boolean sortByYear) {
        Page<Book> booksPage = bookService.findForIndexPage(page, booksPerPage, sortByYear);
        model.addAttribute("booksPage", booksPage);
        model.addAttribute("sortByYear", sortByYear);
        return "books/index";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q, Model model) {
        if (StringUtils.hasText(q)) {
            String trimmed = q.trim();
            model.addAttribute("books", bookService.searchByTitle(trimmed));
            model.addAttribute("q", trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed);
        }
        return "books/search";
    }

    @GetMapping("/new")
    public String newBook(@ModelAttribute("bookForm") BookForm bookForm) {
        return "books/new";
    }

    @PostMapping()
    public String create(@ModelAttribute("bookForm") @Valid BookForm bookForm,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
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
        } else {
            model.addAttribute("people", peopleService.findAll());
        }
        return "books/show";
    }

    @GetMapping("/{bookId}/edit")
    public String edit(Model model, @PathVariable("bookId") int id) {
        model.addAttribute("bookForm", BookForm.from(bookService.findOne(id)));
        model.addAttribute("bookId", id);
        return "books/edit";
    }

    @PatchMapping("/{bookId}")
    public String update(@ModelAttribute("bookForm") @Valid BookForm bookForm, BindingResult bindingResult,
                         @PathVariable("bookId") int id, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", id);
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
}
