package com.springdatajpa.library.controllers;

import com.springdatajpa.library.exception.GlobalExceptionHandler;
import com.springdatajpa.library.services.CatalogPasswordSetupService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CatalogPasswordSetupControllerTest {

    @Mock
    private CatalogPasswordSetupService catalogPasswordSetupService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new CatalogPasswordSetupController(catalogPasswordSetupService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getForm_withToken() throws Exception {
        mockMvc.perform(get("/catalog/setup-password").param("token", "abc"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/setup-password"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    void post_valid_callsServiceAndRedirects() throws Exception {
        mockMvc.perform(post("/catalog/setup-password")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("token", "abc")
                        .param("password", "secretpass")
                        .param("confirmPassword", "secretpass"))
                .andExpect(redirectedUrl("/login?passwordSet"));
        verify(catalogPasswordSetupService).setPasswordFromToken("abc", "secretpass");
    }

    @Test
    void post_mismatch_returnsForm() throws Exception {
        mockMvc.perform(post("/catalog/setup-password")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("token", "abc")
                        .param("password", "secretpass")
                        .param("confirmPassword", "other"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/setup-password"));
        verifyNoInteractions(catalogPasswordSetupService);
    }

    @Test
    void post_shortPassword_returnsForm() throws Exception {
        mockMvc.perform(post("/catalog/setup-password")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("token", "abc")
                        .param("password", "ab")
                        .param("confirmPassword", "ab"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalog/setup-password"));
        verifyNoInteractions(catalogPasswordSetupService);
    }
}
