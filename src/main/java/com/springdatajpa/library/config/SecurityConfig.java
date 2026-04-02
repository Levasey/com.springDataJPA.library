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
                        .requestMatchers(HttpMethod.GET, "/about", "/about/").permitAll()
                        .requestMatchers("/catalog/setup-password").permitAll()
                        .requestMatchers("/", "/login", "/logout", "/forgot-password", "/error",
                                LibraryAccessDeniedHandler.FORBIDDEN_PAGE_PATH).permitAll()
                        .requestMatchers("/register").hasRole("LIBRARIAN")
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .requestMatchers("/me", "/me/**").hasRole("USER")
                        .requestMatchers("/people/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/books/new", "/books/*/edit").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/books", "/books/**").hasAnyRole("USER", "LIBRARIAN")
                        .requestMatchers(HttpMethod.POST, "/books").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PATCH, "/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("LIBRARIAN")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/books", true))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
                .csrf(Customizer.withDefaults());
        return http.build();
    }
}
