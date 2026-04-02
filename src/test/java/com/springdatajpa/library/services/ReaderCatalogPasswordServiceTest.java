package com.springdatajpa.library.services;

import com.springdatajpa.library.exception.BadRequestException;
import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReaderCatalogPasswordServiceTest {

    @Mock
    private LibraryUserRepository libraryUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ReaderCatalogPasswordService readerCatalogPasswordService;

    @Test
    void changePassword_updatesWhenCurrentMatches() {
        LibraryUser user = new LibraryUser("u@test", "encoded-old", true);
        when(libraryUserRepository.findByUsername("u@test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new");

        readerCatalogPasswordService.changePassword("u@test", "old", "new-pass");

        assertTrue(user.getPassword().equals("encoded-new"));
        verify(passwordEncoder).encode("new-pass");
    }

    @Test
    void changePassword_throwsWhenCurrentWrong() {
        LibraryUser user = new LibraryUser("u@test", "encoded-old", true);
        when(libraryUserRepository.findByUsername("u@test")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-old")).thenReturn(false);

        assertThrows(
                BadRequestException.class,
                () -> readerCatalogPasswordService.changePassword("u@test", "wrong", "new"));
    }

    @Test
    void changePassword_throwsWhenUserMissing() {
        when(libraryUserRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(
                BadRequestException.class,
                () -> readerCatalogPasswordService.changePassword("ghost", "a", "b"));
    }
}
