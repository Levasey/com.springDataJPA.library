package com.springdatajpa.library.services;

import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import com.springdatajpa.library.repositories.PeopleRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LibraryUserDetailsService implements UserDetailsService {

    private final LibraryUserRepository libraryUserRepository;
    private final PeopleRepository peopleRepository;

    public LibraryUserDetailsService(
            LibraryUserRepository libraryUserRepository,
            PeopleRepository peopleRepository) {
        this.libraryUserRepository = libraryUserRepository;
        this.peopleRepository = peopleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LibraryUser u = resolveLibraryUser(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return User.builder()
                .username(u.getUsername())
                .password(u.getPassword())
                .disabled(!u.isEnabled())
                .roles(u.getRole().name())
                .build();
    }

    /**
     * Логин: нормализованный email (как раньше), произвольное имя библиотекаря после нормализации,
     * либо номер читательского билета читателя (как в карточке).
     */
    private Optional<LibraryUser> resolveLibraryUser(String rawUsername) {
        if (rawUsername == null || rawUsername.isBlank()) {
            return Optional.empty();
        }
        String catalogKey = RegistrationService.catalogUsernameFromEmail(rawUsername);
        Optional<LibraryUser> byKey = libraryUserRepository.findByUsername(catalogKey);
        if (byKey.isPresent()) {
            return byKey;
        }
        String trimmedCard = rawUsername.trim();
        return peopleRepository
                .findByReaderCardNumber(trimmedCard)
                .map(p -> RegistrationService.catalogUsernameFromEmail(p.getEmail()))
                .filter(s -> !s.isBlank())
                .flatMap(libraryUserRepository::findByUsername);
    }
}
