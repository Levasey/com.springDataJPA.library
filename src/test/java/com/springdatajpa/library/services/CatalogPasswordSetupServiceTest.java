package com.springdatajpa.library.services;

import com.springdatajpa.library.exception.BadRequestException;
import com.springdatajpa.library.models.CatalogPasswordSetupToken;
import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.repositories.CatalogPasswordSetupTokenRepository;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogPasswordSetupServiceTest {

    @Mock
    private CatalogPasswordSetupTokenRepository tokenRepository;

    @Mock
    private LibraryUserRepository libraryUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CatalogPasswordSetupService catalogPasswordSetupService;

    @BeforeEach
    void setUp() {
        catalogPasswordSetupService =
                new CatalogPasswordSetupService(tokenRepository, libraryUserRepository, passwordEncoder, 168);
    }

    @Test
    void createToken_deletesOldUnused_andStoresHash() throws Exception {
        String username = "u@x.y";

        String token = catalogPasswordSetupService.createTokenForUsername(username);

        assertFalse(token.isBlank());
        verify(tokenRepository).deleteByUsernameAndUsedAtIsNull(username);
        ArgumentCaptor<CatalogPasswordSetupToken> cap = ArgumentCaptor.forClass(CatalogPasswordSetupToken.class);
        verify(tokenRepository).save(cap.capture());
        assertEquals(username, cap.getValue().getUsername());
        assertNull(cap.getValue().getUsedAt());
        assertTrue(cap.getValue().getExpiresAt().isAfter(LocalDateTime.now()));

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String expectedHex = HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
        assertEquals(expectedHex, cap.getValue().getTokenHash());
    }

    @Test
    void setPasswordFromToken_updatesUserAndMarksUsed() throws Exception {
        String raw = "test-token-raw";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String hash = HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));

        CatalogPasswordSetupToken row = new CatalogPasswordSetupToken();
        row.setTokenHash(hash);
        row.setUsername("r@z.w");
        row.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(row));

        LibraryUser user = new LibraryUser("r@z.w", "old", true);
        when(libraryUserRepository.findByUsername("r@z.w")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("ENC-newpass");

        catalogPasswordSetupService.setPasswordFromToken(raw, "newpass");

        assertEquals("ENC-newpass", user.getPassword());
        assertNotNull(row.getUsedAt());
    }

    @Test
    void setPasswordFromToken_throwsWhenUsed() throws Exception {
        String raw = "x";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String hash = HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        CatalogPasswordSetupToken row = new CatalogPasswordSetupToken();
        row.setTokenHash(hash);
        row.setUsername("a@b.c");
        row.setExpiresAt(LocalDateTime.now().plusDays(1));
        row.setUsedAt(LocalDateTime.now());
        when(tokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(row));

        assertThrows(BadRequestException.class, () -> catalogPasswordSetupService.setPasswordFromToken(raw, "p"));

        verify(libraryUserRepository, never()).findByUsername(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
