package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.BookForm;
import com.springdatajpa.library.exception.GlobalExceptionHandler;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.services.BookService;
import com.springdatajpa.library.services.PeopleService;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private PeopleService peopleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new BookController(bookService, peopleService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void index_allBooks() throws Exception {
        when(bookService.findForIndexPage(null, null, false, false, false, false, false, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/index"));
        verify(bookService).findForIndexPage(null, null, false, false, false, false, false, false);
    }

    @Test
    void index_withPagination_delegatesToService() throws Exception {
        when(bookService.findForIndexPage(1, 10, true, false, false, false, false, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("page", "1").param("books_per_page", "10").param("sort_by_year", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(1, 10, true, false, false, false, false, false);
    }

    @Test
    void index_sortByGenre_delegatesToService() throws Exception {
        when(bookService.findForIndexPage(null, null, false, true, false, false, false, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("sort_by_genre", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(null, null, false, true, false, false, false, false);
    }

    @Test
    void index_sortByTitle_delegatesToService() throws Exception {
        when(bookService.findForIndexPage(null, null, false, false, true, false, false, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("sort_by_title", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(null, null, false, false, true, false, false, false);
    }

    @Test
    void index_multipleSortFlags_delegatesToService() throws Exception {
        when(bookService.findForIndexPage(null, null, true, false, false, true, false, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books")
                        .param("sort_by_year", "true")
                        .param("sort_by_author", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(null, null, true, false, false, true, false, false);
    }

    @Test
    void index_availabilityPresetFree_delegatesToService() throws Exception {
        when(bookService.findForIndexPage(null, null, false, false, false, false, true, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("availability_preset", "free"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(null, null, false, false, false, false, true, false);
    }

    @Test
    void index_availabilityPresetOverridesLegacyParams() throws Exception {
        when(bookService.findForIndexPage(null, null, false, false, false, false, false, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books")
                        .param("availability_preset", "all")
                        .param("sort_by_availability", "true")
                        .param("availability_issued_first", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(null, null, false, false, false, false, false, false);
    }

    @Test
    void index_sortByAvailabilityIssuedFirst_delegatesToService() throws Exception {
        when(bookService.findForIndexPage(null, null, false, false, false, false, true, true))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("sort_by_availability", "true").param("availability_issued_first", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(null, null, false, false, false, false, true, true);
    }

    @Test
    void index_withPagination_pageZeroClampedToFirstPage() throws Exception {
        when(bookService.findForIndexPage(0, 10, true, false, false, false, false, false))
                .thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("page", "0").param("books_per_page", "10").param("sort_by_year", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(0, 10, true, false, false, false, false, false);
    }

    @Test
    void searchPage() throws Exception {
        mockMvc.perform(get("/books/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/search"));
    }

    @Test
    void search_withQuery() throws Exception {
        when(bookService.searchBooks("abc")).thenReturn(List.of());
        mockMvc.perform(get("/books/search").param("q", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/search"));
        verify(bookService).searchBooks("abc");
    }

    @Test
    void create_validBook_redirects() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Title")
                        .param("author", "Author")
                        .param("yearPublished", "2000")
                        .param("genre", "FICTION"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        verify(bookService).createBook(any(BookForm.class));
    }

    @Test
    void create_invalidBook_returnsForm() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "x")
                        .param("author", "A")
                        .param("yearPublished", "1499")
                        .param("genre", "OTHER"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/new"));
    }

    @Test
    void show_withOwner() throws Exception {
        Book book = new Book("T", "A", 2000);
        Person owner = new Person();
        book.setOwner(owner);
        when(bookService.findOne(1)).thenReturn(book);
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/show"));
    }

    @Test
    void show_withoutOwner_withoutLibrarianRole_doesNotLoadPeople() throws Exception {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "u", null, AuthorityUtils.createAuthorityList("ROLE_USER")));
        Book book = new Book("T", "A", 2000);
        when(bookService.findOne(2)).thenReturn(book);
        mockMvc.perform(get("/books/2"))
                .andExpect(status().isOk());
        verify(peopleService, never()).findAll();
    }

    @Test
    void show_withoutOwner_librarianLoadsPeople() throws Exception {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(
                        "lib", null, AuthorityUtils.createAuthorityList("ROLE_LIBRARIAN")));
        Book book = new Book("T", "A", 2000);
        when(bookService.findOne(2)).thenReturn(book);
        when(peopleService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/books/2"))
                .andExpect(status().isOk());
        verify(peopleService).findAll();
    }

    @Test
    void update_valid_redirects() throws Exception {
        mockMvc.perform(patch("/books/3")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "T2")
                        .param("author", "A2")
                        .param("yearPublished", "2001")
                        .param("genre", "DETECTIVE"))
                .andExpect(redirectedUrl("/books"));
        verify(bookService).update(eq(3), any(BookForm.class));
    }

    @Test
    void delete_redirects() throws Exception {
        mockMvc.perform(delete("/books/4"))
                .andExpect(redirectedUrl("/books"));
        verify(bookService).delete(4);
    }

    @Test
    void release_redirects() throws Exception {
        mockMvc.perform(patch("/books/5/release"))
                .andExpect(redirectedUrl("/books/5"));
        verify(bookService).release(5);
    }

    @Test
    void assign_redirects() throws Exception {
        mockMvc.perform(patch("/books/6/assign")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("personId", "9"))
                .andExpect(redirectedUrl("/books/6"));
        verify(bookService).assign(6, 9);
    }
}
