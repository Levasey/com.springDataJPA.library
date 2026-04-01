package com.springdatajpa.library.services;

import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LibraryUserDetailsService implements UserDetailsService {

    private final LibraryUserRepository libraryUserRepository;

    public LibraryUserDetailsService(LibraryUserRepository libraryUserRepository) {
        this.libraryUserRepository = libraryUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LibraryUser u = libraryUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .disabled(!u.isEnabled())
                .roles(u.getRole().name())
                .build();
    }
}
