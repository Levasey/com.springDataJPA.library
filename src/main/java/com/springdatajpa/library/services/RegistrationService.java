package com.springdatajpa.library.services;

import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final LibraryUserRepository libraryUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(LibraryUserRepository libraryUserRepository, PasswordEncoder passwordEncoder) {
        this.libraryUserRepository = libraryUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return libraryUserRepository.existsByUsername(username);
    }

    @Transactional
    public void register(String username, String rawPassword) {
        LibraryUser user = new LibraryUser(username, passwordEncoder.encode(rawPassword), true);
        libraryUserRepository.save(user);
    }
}
