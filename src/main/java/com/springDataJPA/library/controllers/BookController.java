package com.springDataJPA.library.controllers;

import com.springDataJPA.library.models.Book;
import com.springDataJPA.library.models.Person;
import com.springDataJPA.library.services.BookService;
import com.springDataJPA.library.services.PeopleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String searchPage() {
        return "books/search";
    }

    @PostMapping("/search")
    public String makeSearch(Model model, @RequestParam("query") String query) {
        model.addAttribute("books", bookService.searchByTitle(query));
        return "books/search";
    }

    @GetMapping("/new")
    public String newBook(@ModelAttribute("book") Book book) {
        return "books/new";
    }

    @PostMapping()
    public String create(@ModelAttribute("book") @Valid Book book,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "books/new";
        }

        bookService.saveBook(book);
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
        model.addAttribute("book", bookService.findOne(id));
        return "books/edit";
    }

    @PatchMapping("/{bookId}")
    public String update(@ModelAttribute("book") @Valid Book book, BindingResult bindingResult,
                         @PathVariable("bookId") int id) {
        if (bindingResult.hasErrors()) {
            return "books/edit";
        }

        bookService.update(id, book);
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
