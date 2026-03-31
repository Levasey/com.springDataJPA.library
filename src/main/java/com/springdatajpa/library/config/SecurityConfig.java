package com.springdatajpa.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new LibraryAccessDeniedHandler();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/error",
                                LibraryAccessDeniedHandler.FORBIDDEN_PAGE_PATH).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**", "/people/**").permitAll()
                        .requestMatchers("/books/**", "/people/**").hasRole("USER")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/books", true))
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
                .csrf(Customizer.withDefaults());
        return http.build();
    }
}
