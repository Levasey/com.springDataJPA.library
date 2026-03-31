package com.springDataJPA.library.controllers;

import com.springDataJPA.library.exception.GlobalExceptionHandler;
import com.springDataJPA.library.models.Book;
import com.springDataJPA.library.models.Person;
import com.springDataJPA.library.services.BookService;
import com.springDataJPA.library.services.PeopleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
        when(bookService.findForIndexPage(null, null, false)).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/index"));
        verify(bookService).findForIndexPage(null, null, false);
    }

    @Test
    void index_withPagination_delegatesToService() throws Exception {
        when(bookService.findForIndexPage(1, 10, true)).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("page", "1").param("books_per_page", "10").param("sort_by_year", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(1, 10, true);
    }

    @Test
    void index_withPagination_pageZeroClampedToFirstPage() throws Exception {
        when(bookService.findForIndexPage(0, 10, true)).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/books").param("page", "0").param("books_per_page", "10").param("sort_by_year", "true"))
                .andExpect(status().isOk());
        verify(bookService).findForIndexPage(0, 10, true);
    }

    @Test
    void searchPage() throws Exception {
        mockMvc.perform(get("/books/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/search"));
    }

    @Test
    void makeSearch() throws Exception {
        when(bookService.searchByTitle("abc")).thenReturn(List.of());
        mockMvc.perform(post("/books/search").param("query", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/search"));
        verify(bookService).searchByTitle("abc");
    }

    @Test
    void create_validBook_redirects() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Title")
                        .param("author", "Author")
                        .param("yearPublished", "2000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        verify(bookService).saveBook(any(Book.class));
    }

    @Test
    void create_invalidBook_returnsForm() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "x")
                        .param("author", "A")
                        .param("yearPublished", "1499"))
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
    void show_withoutOwner_loadsPeople() throws Exception {
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
                        .param("yearPublished", "2001"))
                .andExpect(redirectedUrl("/books"));
        verify(bookService).update(eq(3), any(Book.class));
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
