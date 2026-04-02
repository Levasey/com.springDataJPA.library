package com.springdatajpa.library.controllers;

import com.springdatajpa.library.exception.GlobalExceptionHandler;
import com.springdatajpa.library.services.PasswordResetService;
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
class ForgotPasswordControllerTest {

    @Mock
    private PasswordResetService passwordResetService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ForgotPasswordController(passwordResetService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getForm() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgotPassword"))
                .andExpect(model().attributeExists("forgotPasswordForm"));
    }

    @Test
    void post_valid_callsServiceAndRedirectsWithFlash() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "reader@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password"));
        verify(passwordResetService).requestPasswordReset("reader@example.com");
    }

    @Test
    void post_invalidEmail_returnsForm() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgotPassword"));
        verifyNoInteractions(passwordResetService);
    }
}
