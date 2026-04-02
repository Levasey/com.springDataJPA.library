package com.springdatajpa.library.services;

import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.models.UserRole;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private LibraryUserRepository libraryUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void register_generatesPasswordOnlyAtCreation_andStoresHash() {
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "HASH-" + inv.getArgument(0));

        String plain = registrationService.register("newreader");

        assertEquals(16, plain.length());
        assertTrue(plain.chars().allMatch(c -> Character.isLetterOrDigit(c)));

        ArgumentCaptor<LibraryUser> captor = ArgumentCaptor.forClass(LibraryUser.class);
        verify(libraryUserRepository).save(captor.capture());
        assertEquals("HASH-" + plain, captor.getValue().getPassword());
        assertEquals("newreader", captor.getValue().getUsername());
        assertEquals(UserRole.LIBRARIAN, captor.getValue().getRole());
    }

    @Test
    void registerCatalogUser_savesEncodedPassword() {
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "H-" + inv.getArgument(0));
        when(libraryUserRepository.existsByUsername("a@b.c")).thenReturn(false);

        registrationService.registerCatalogUser("a@b.c", "user-secret");

        ArgumentCaptor<LibraryUser> captor = ArgumentCaptor.forClass(LibraryUser.class);
        verify(libraryUserRepository).save(captor.capture());
        assertEquals("H-user-secret", captor.getValue().getPassword());
        assertEquals("a@b.c", captor.getValue().getUsername());
    }

    @Test
    void registerCatalogUser_throwsWhenUsernameTaken() {
        when(libraryUserRepository.existsByUsername("x@y.z")).thenReturn(true);

        assertThrows(ConflictException.class, () -> registrationService.registerCatalogUser("x@y.z", "password12"));

        verify(libraryUserRepository, never()).save(any());
    }

    @Test
    void registerCatalogUserWithInvitationPassword_savesRandomEncodedPassword() {
        when(passwordEncoder.encode(any())).thenAnswer(inv -> "HASH-" + inv.getArgument(0));
        when(libraryUserRepository.existsByUsername("inv@test")).thenReturn(false);

        registrationService.registerCatalogUserWithInvitationPassword("inv@test");

        ArgumentCaptor<String> rawPass = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(rawPass.capture());
        assertEquals(16, rawPass.getValue().length());

        ArgumentCaptor<LibraryUser> captor = ArgumentCaptor.forClass(LibraryUser.class);
        verify(libraryUserRepository).save(captor.capture());
        assertEquals("HASH-" + rawPass.getValue(), captor.getValue().getPassword());
        assertEquals("inv@test", captor.getValue().getUsername());
    }

    @Test
    void catalogUsernameFromEmail_normalizes() {
        assertEquals("a@b.co", RegistrationService.catalogUsernameFromEmail("  A@B.Co "));
    }
}
