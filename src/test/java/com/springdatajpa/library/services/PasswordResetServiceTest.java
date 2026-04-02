package com.springdatajpa.library.services;

import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.models.UserRole;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private LibraryUserRepository libraryUserRepository;

    @Mock
    private CatalogPasswordSetupService catalogPasswordSetupService;

    @Mock
    private PasswordResetMailService passwordResetMailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void requestPasswordReset_blank_doesNothing() {
        passwordResetService.requestPasswordReset("   ");

        verify(libraryUserRepository, never()).findByUsername(any());
        verify(catalogPasswordSetupService, never()).createTokenForUsername(any());
    }

    @Test
    void requestPasswordReset_unknownUser_doesNothing() {
        when(libraryUserRepository.findByUsername("a@b.c")).thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset("A@B.C");

        verify(catalogPasswordSetupService, never()).createTokenForUsername(any());
        verify(passwordResetMailService, never()).sendPasswordResetIfConfigured(any(), any());
    }

    @Test
    void requestPasswordReset_librarian_doesNothing() {
        LibraryUser lib = new LibraryUser("a@b.c", "hash", true, UserRole.LIBRARIAN);
        when(libraryUserRepository.findByUsername("a@b.c")).thenReturn(Optional.of(lib));

        passwordResetService.requestPasswordReset("a@b.c");

        verify(catalogPasswordSetupService, never()).createTokenForUsername(any());
        verify(passwordResetMailService, never()).sendPasswordResetIfConfigured(any(), any());
    }

    @Test
    void requestPasswordReset_disabledReader_doesNothing() {
        LibraryUser u = new LibraryUser("a@b.c", "hash", false);
        when(libraryUserRepository.findByUsername("a@b.c")).thenReturn(Optional.of(u));

        passwordResetService.requestPasswordReset("a@b.c");

        verify(catalogPasswordSetupService, never()).createTokenForUsername(any());
        verify(passwordResetMailService, never()).sendPasswordResetIfConfigured(any(), any());
    }

    @Test
    void requestPasswordReset_reader_createsTokenAndMails() {
        LibraryUser u = new LibraryUser("a@b.c", "hash", true);
        when(libraryUserRepository.findByUsername("a@b.c")).thenReturn(Optional.of(u));
        when(catalogPasswordSetupService.createTokenForUsername("a@b.c")).thenReturn("raw-token");

        passwordResetService.requestPasswordReset("a@b.c");

        verify(catalogPasswordSetupService).createTokenForUsername("a@b.c");
        verify(passwordResetMailService).sendPasswordResetIfConfigured(eq("a@b.c"), eq("raw-token"));
    }
}
