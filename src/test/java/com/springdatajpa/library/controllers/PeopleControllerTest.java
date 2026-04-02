package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.exception.GlobalExceptionHandler;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.services.PeopleService;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PeopleControllerTest {

    @Mock
    private PeopleService peopleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new PeopleController(peopleService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void index() throws Exception {
        when(peopleService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/people"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/index"));
    }

    @Test
    void searchPage_withoutQuery() throws Exception {
        mockMvc.perform(get("/people/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/search"));
        verifyNoInteractions(peopleService);
    }

    @Test
    void search_withQuery() throws Exception {
        when(peopleService.searchPeople("ив")).thenReturn(List.of());
        mockMvc.perform(get("/people/search").param("q", "ив"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/search"));
        verify(peopleService).searchPeople("ив");
    }

    @Test
    void show() throws Exception {
        Person p = new Person();
        when(peopleService.findById(1)).thenReturn(p);
        when(peopleService.getBooksByPersonId(1)).thenReturn(List.of());
        mockMvc.perform(get("/people/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/show"));
    }

    @Test
    void create_valid_redirects() throws Exception {
        when(peopleService.save(any(PersonForm.class))).thenReturn(Optional.empty());
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-1001")
                        .param("email", "j@example.com")
                        .param("address", "USA, Boston, 123456"))
                .andExpect(redirectedUrl("/people"));
        verify(peopleService).save(any(PersonForm.class));
    }

    @Test
    void create_duplicatePersonEmail_returnsForm() throws Exception {
        when(peopleService.save(any(PersonForm.class)))
                .thenThrow(new ConflictException("Этот email уже указан у другого читателя.", "email"));
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-1001")
                        .param("email", "j@example.com")
                        .param("address", "USA, Boston, 123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/new"));
        verify(peopleService).save(any(PersonForm.class));
    }

    @Test
    void create_duplicateReaderCard_returnsForm() throws Exception {
        when(peopleService.save(any(PersonForm.class)))
                .thenThrow(new ConflictException("Этот номер читательского билета уже используется.", "readerCardNumber"));
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-1001")
                        .param("email", "j@example.com")
                        .param("address", "USA, Boston, 123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/new"));
        verify(peopleService).save(any(PersonForm.class));
    }

    @Test
    void create_duplicateCatalogEmail_returnsForm() throws Exception {
        when(peopleService.save(any(PersonForm.class)))
                .thenThrow(new ConflictException("Учётная запись каталога с таким логином уже существует.", "email"));
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-1001")
                        .param("email", "j@example.com")
                        .param("address", "USA, Boston, 123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/new"));
        verify(peopleService).save(any(PersonForm.class));
    }

    @Test
    void create_invalid_returnsForm() throws Exception {
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "J")
                        .param("surname", "D")
                        .param("email", "bad")
                        .param("address", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/new"));
    }

    @Test
    void update_valid_redirects() throws Exception {
        mockMvc.perform(patch("/people/2")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Jane")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-2002")
                        .param("email", "jane@example.com")
                        .param("address", "USA, Boston, 654321")
                        .param("dateOfBirth", "1990-01-15"))
                .andExpect(redirectedUrl("/people"));
        verify(peopleService).update(eq(2), any(PersonForm.class));
    }

    @Test
    void update_duplicateCatalogEmail_returnsForm() throws Exception {
        doThrow(new ConflictException("Этот email уже используется для входа в каталог.", "email"))
                .when(peopleService).update(eq(2), any(PersonForm.class));
        mockMvc.perform(patch("/people/2")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Jane")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-2002")
                        .param("email", "new@example.com")
                        .param("address", "USA, Boston, 654321")
                        .param("dateOfBirth", "1990-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/edit"));
        verify(peopleService).update(eq(2), any(PersonForm.class));
    }

    @Test
    void update_duplicateReaderCard_returnsForm() throws Exception {
        doThrow(new ConflictException("Этот номер читательского билета уже используется.", "readerCardNumber"))
                .when(peopleService).update(eq(2), any(PersonForm.class));
        mockMvc.perform(patch("/people/2")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Jane")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-2002")
                        .param("email", "jane@example.com")
                        .param("address", "USA, Boston, 654321")
                        .param("dateOfBirth", "1990-01-15"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/edit"));
        verify(peopleService).update(eq(2), any(PersonForm.class));
    }

    @Test
    void delete_redirects() throws Exception {
        mockMvc.perform(delete("/people/3"))
                .andExpect(redirectedUrl("/people"));
        verify(peopleService).delete(3);
    }
}
