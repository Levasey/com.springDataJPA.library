package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.exception.GlobalExceptionHandler;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.services.PeopleService;
import com.springdatajpa.library.services.RegistrationService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PeopleControllerTest {

    @Mock
    private PeopleService peopleService;

    @Mock
    private RegistrationService registrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new PeopleController(peopleService, registrationService))
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
        when(registrationService.usernameExists("j@example.com")).thenReturn(false);
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
        when(peopleService.isEmailTakenBySomeoneElse("j@example.com", null)).thenReturn(true);
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-1001")
                        .param("email", "j@example.com")
                        .param("address", "USA, Boston, 123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/new"));
        verifyNoInteractions(registrationService);
        verify(peopleService, never()).save(any(PersonForm.class));
    }

    @Test
    void create_duplicateReaderCard_returnsForm() throws Exception {
        when(peopleService.isReaderCardTakenBySomeoneElse("J-1001", null)).thenReturn(true);
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-1001")
                        .param("email", "j@example.com")
                        .param("address", "USA, Boston, 123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/new"));
        verifyNoInteractions(registrationService);
        verify(peopleService, never()).save(any(PersonForm.class));
    }

    @Test
    void create_duplicateCatalogEmail_returnsForm() throws Exception {
        when(peopleService.isEmailTakenBySomeoneElse("j@example.com", null)).thenReturn(false);
        when(peopleService.isReaderCardTakenBySomeoneElse("J-1001", null)).thenReturn(false);
        when(registrationService.usernameExists("j@example.com")).thenReturn(true);
        mockMvc.perform(post("/people")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "John")
                        .param("surname", "Doe")
                        .param("readerCardNumber", "J-1001")
                        .param("email", "j@example.com")
                        .param("address", "USA, Boston, 123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("people/new"));
        verify(peopleService, never()).save(any(PersonForm.class));
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
        when(peopleService.isEmailTakenBySomeoneElse("new@example.com", 2)).thenReturn(false);
        when(peopleService.isReaderCardTakenBySomeoneElse("J-2002", 2)).thenReturn(false);
        when(peopleService.isCatalogLoginTakenBySomeoneElse(2, "new@example.com")).thenReturn(true);
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
        verify(peopleService, never()).update(anyInt(), any(PersonForm.class));
    }

    @Test
    void update_duplicateReaderCard_returnsForm() throws Exception {
        when(peopleService.isReaderCardTakenBySomeoneElse("J-2002", 2)).thenReturn(true);
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
        verify(peopleService, never()).update(anyInt(), any(PersonForm.class));
    }

    @Test
    void delete_redirects() throws Exception {
        mockMvc.perform(delete("/people/3"))
                .andExpect(redirectedUrl("/people"));
        verify(peopleService).delete(3);
    }
}
