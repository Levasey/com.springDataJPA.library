package com.springdatajpa.library.services;

import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.models.UserRole;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import com.springdatajpa.library.repositories.PeopleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryUserDetailsServiceTest {

    @Mock
    private LibraryUserRepository libraryUserRepository;

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private LibraryUserDetailsService libraryUserDetailsService;

    private LibraryUser catalogUser;

    @BeforeEach
    void setUp() {
        catalogUser = new LibraryUser("reader@mail.test", "{bcrypt}hash", true, UserRole.USER);
    }

    @Test
    void loadUserByUsername_findsByNormalizedEmail() {
        when(libraryUserRepository.findByUsername("reader@mail.test")).thenReturn(Optional.of(catalogUser));

        UserDetails details = libraryUserDetailsService.loadUserByUsername("  Reader@Mail.TEST  ");

        assertEquals("reader@mail.test", details.getUsername());
        assertEquals("{bcrypt}hash", details.getPassword());
        verifyNoInteractions(peopleRepository);
    }

    @Test
    void loadUserByUsername_findsByReaderCard_whenEmailLookupMisses() {
        when(libraryUserRepository.findByUsername("j-1001")).thenReturn(Optional.empty());
        Person person = new Person();
        person.setEmail("reader@mail.test");
        person.setReaderCardNumber("J-1001");
        when(peopleRepository.findByReaderCardNumber("J-1001")).thenReturn(Optional.of(person));
        when(libraryUserRepository.findByUsername("reader@mail.test")).thenReturn(Optional.of(catalogUser));

        UserDetails details = libraryUserDetailsService.loadUserByUsername("J-1001");

        assertEquals("reader@mail.test", details.getUsername());
    }

    @Test
    void loadUserByUsername_blank_throws() {
        assertThrows(UsernameNotFoundException.class, () -> libraryUserDetailsService.loadUserByUsername("   "));
    }

    @Test
    void loadUserByUsername_unknown_throws() {
        when(libraryUserRepository.findByUsername("nope@x.y")).thenReturn(Optional.empty());
        when(peopleRepository.findByReaderCardNumber("nope@x.y")).thenReturn(Optional.empty());

        UsernameNotFoundException ex =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> libraryUserDetailsService.loadUserByUsername("nope@x.y"));

        assertTrue(ex.getMessage().contains("nope"));
    }
}
