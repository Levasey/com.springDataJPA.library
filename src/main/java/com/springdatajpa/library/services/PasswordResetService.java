package com.springdatajpa.library.services;

import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.models.UserRole;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import org.springframework.stereotype.Service;
import com.springdatajpa.library.support.TransactionCallbacks;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private final LibraryUserRepository libraryUserRepository;
    private final CatalogPasswordSetupService catalogPasswordSetupService;
    private final PasswordResetMailService passwordResetMailService;

    public PasswordResetService(
            LibraryUserRepository libraryUserRepository,
            CatalogPasswordSetupService catalogPasswordSetupService,
            PasswordResetMailService passwordResetMailService) {
        this.libraryUserRepository = libraryUserRepository;
        this.catalogPasswordSetupService = catalogPasswordSetupService;
        this.passwordResetMailService = passwordResetMailService;
    }

    /**
     * Для читателя каталога (роль USER, логин — нормализованный email) создаёт одноразовую ссылку и ставит
     * отправку письма после коммита. Если учётной записи нет или это не читатель — ничего не делает
     * (ответ пользователю всё равно обезличенный).
     */
    @Transactional
    public void requestPasswordReset(String rawEmail) {
        String catalogKey = RegistrationService.catalogUsernameFromEmail(rawEmail);
        if (catalogKey.isBlank()) {
            return;
        }
        LibraryUser user =
                libraryUserRepository.findByUsername(catalogKey).orElse(null);
        if (user == null || !user.isEnabled() || user.getRole() != UserRole.USER) {
            return;
        }
        String rawToken = catalogPasswordSetupService.createTokenForUsername(catalogKey);
        TransactionCallbacks.runAfterCommit(
                () -> passwordResetMailService.sendPasswordResetIfConfigured(catalogKey, rawToken));
    }
}
