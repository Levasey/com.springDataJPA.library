package com.springdatajpa.library.services;

import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.models.UserRole;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class RegistrationService {

    private static final int INITIAL_PASSWORD_LENGTH = 16;
    /** Без похожих символов 0/O, 1/l/I для удобства передачи читателю */
    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final LibraryUserRepository libraryUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegistrationService(LibraryUserRepository libraryUserRepository, PasswordEncoder passwordEncoder) {
        this.libraryUserRepository = libraryUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return libraryUserRepository.existsByUsername(catalogUsernameFromEmail(username));
    }

    /** Логин в каталоге для читателя — нормализованный email. */
    public static String catalogUsernameFromEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Учётная запись каталога со случайным паролем (читатель задаёт свой через ссылку из письма).
     */
    @Transactional
    public void registerCatalogUserWithInvitationPassword(String username) {
        registerCatalogUser(username, generateInitialPassword());
    }

    /**
     * Учётная запись каталога с заданным паролем (например административная регистрация {@code /register}).
     */
    @Transactional
    public void registerCatalogUser(String username, String rawPassword) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя каталога не задано.");
        }
        if (libraryUserRepository.existsByUsername(username)) {
            throw new ConflictException("Учётная запись каталога с таким логином уже существует.");
        }
        LibraryUser user = new LibraryUser(username, passwordEncoder.encode(rawPassword), true);
        libraryUserRepository.save(user);
    }

    /**
     * Создаёт учётную запись библиотекаря с начальным паролем (форма {@code /register}).
     *
     * @return открытый пароль (однократно для передачи пользователю; в БД сохраняется только хеш)
     */
    @Transactional
    public String register(String username) {
        String normalized = catalogUsernameFromEmail(username);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не задано.");
        }
        if (libraryUserRepository.existsByUsername(normalized)) {
            throw new ConflictException("Учётная запись с таким логином уже существует.");
        }
        String rawPassword = generateInitialPassword();
        LibraryUser user =
                new LibraryUser(normalized, passwordEncoder.encode(rawPassword), true, UserRole.LIBRARIAN);
        libraryUserRepository.save(user);
        return rawPassword;
    }

    private String generateInitialPassword() {
        StringBuilder sb = new StringBuilder(INITIAL_PASSWORD_LENGTH);
        for (int i = 0; i < INITIAL_PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_ALPHABET.charAt(secureRandom.nextInt(PASSWORD_ALPHABET.length())));
        }
        return sb.toString();
    }
}
