package com.springdatajpa.library.services;

import com.springdatajpa.library.exception.BadRequestException;
import com.springdatajpa.library.models.CatalogPasswordSetupToken;
import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.repositories.CatalogPasswordSetupTokenRepository;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class CatalogPasswordSetupService {

    private final CatalogPasswordSetupTokenRepository tokenRepository;
    private final LibraryUserRepository libraryUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long tokenValidityHours;

    public CatalogPasswordSetupService(
            CatalogPasswordSetupTokenRepository tokenRepository,
            LibraryUserRepository libraryUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${library.catalog-password-setup.token-validity-hours:168}") long tokenValidityHours) {
        this.tokenRepository = tokenRepository;
        this.libraryUserRepository = libraryUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenValidityHours = Math.max(1, tokenValidityHours);
    }

    /**
     * Создаёт одноразовую ссылку (сырой токен возвращается только для вставки в URL письма).
     */
    @Transactional
    public String createTokenForUsername(String catalogUsername) {
        if (catalogUsername == null || catalogUsername.isBlank()) {
            throw new IllegalArgumentException("Логин каталога не задан.");
        }
        tokenRepository.deleteByUsernameAndUsedAtIsNull(catalogUsername);
        byte[] raw = new byte[32];
        secureRandom.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String hash = sha256Hex(token);
        CatalogPasswordSetupToken entity = new CatalogPasswordSetupToken();
        entity.setTokenHash(hash);
        entity.setUsername(catalogUsername);
        entity.setExpiresAt(LocalDateTime.now().plusHours(tokenValidityHours));
        entity.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(entity);
        return token;
    }

    @Transactional
    public void setPasswordFromToken(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Ссылка недействительна или устарела.");
        }
        String hash = sha256Hex(rawToken);
        CatalogPasswordSetupToken row = tokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Ссылка недействительна или устарела."));
        if (row.getUsedAt() != null) {
            throw new BadRequestException("Эта ссылка уже была использована. Войдите с установленным паролем.");
        }
        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Срок действия ссылки истёк. Обратитесь в библиотеку за новой.");
        }
        LibraryUser user = libraryUserRepository
                .findByUsername(row.getUsername())
                .orElseThrow(() -> new BadRequestException("Учётная запись не найдена."));
        user.setPassword(passwordEncoder.encode(newPassword));
        row.setUsedAt(LocalDateTime.now());
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
