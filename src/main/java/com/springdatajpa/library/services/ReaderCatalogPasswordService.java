package com.springdatajpa.library.services;

import com.springdatajpa.library.exception.BadRequestException;
import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReaderCatalogPasswordService {

    private final LibraryUserRepository libraryUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ReaderCatalogPasswordService(
            LibraryUserRepository libraryUserRepository,
            PasswordEncoder passwordEncoder) {
        this.libraryUserRepository = libraryUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Смена пароля вошедшего читателя: {@code catalogUsername} — как в {@link org.springframework.security.core.Authentication#getName()}.
     */
    @Transactional
    public void changePassword(String catalogUsername, String currentRaw, String newRaw) {
        if (catalogUsername == null || catalogUsername.isBlank()) {
            throw new BadRequestException("Не удалось определить учётную запись.");
        }
        LibraryUser user = libraryUserRepository
                .findByUsername(catalogUsername)
                .orElseThrow(() -> new BadRequestException("Учётная запись не найдена."));
        if (!passwordEncoder.matches(currentRaw, user.getPassword())) {
            throw new BadRequestException("Текущий пароль указан неверно.");
        }
        user.setPassword(passwordEncoder.encode(newRaw));
    }
}
