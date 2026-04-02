package com.springdatajpa.library.controllers;

import com.springdatajpa.library.dto.ReaderChangePasswordForm;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.services.PeopleService;
import com.springdatajpa.library.services.ReaderCatalogPasswordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReaderProfileControllerTest {

    @Mock
    private ReaderCatalogPasswordService readerCatalogPasswordService;

    @Mock
    private PeopleService peopleService;

    private ReaderProfileController controller;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        controller = new ReaderProfileController(peopleService, readerCatalogPasswordService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void profile_returnsViewAndModel() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "reader@test",
                        "x",
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        Person p = new Person();
        p.setPersonId(3);
        when(peopleService.findByCatalogLogin("reader@test")).thenReturn(Optional.of(p));
        when(peopleService.getBooksByPersonId(3)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.profile(auth, model);

        assertEquals("reader/profile", view);
        assertEquals(p, model.get("person"));
        assertEquals(List.of(), model.get("books"));
    }

    @Test
    void changePassword_submit_callsServiceAndRedirects() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "reader@test",
                        "x",
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        Person p = new Person();
        p.setPersonId(3);
        when(peopleService.findByCatalogLogin("reader@test")).thenReturn(Optional.of(p));

        ReaderChangePasswordForm form = new ReaderChangePasswordForm();
        form.setCurrentPassword("old");
        form.setNewPassword("new1");
        form.setConfirmPassword("new1");
        BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");

        String outcome = controller.changePasswordSubmit(auth, form, bindingResult);

        assertEquals("redirect:/me?passwordChanged", outcome);
        assertTrue(bindingResult.getErrorCount() == 0);
        verify(readerCatalogPasswordService).changePassword(eq("reader@test"), eq("old"), eq("new1"));
    }
}
