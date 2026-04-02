package com.springdatajpa.library.services;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.exception.ResourceNotFoundException;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.LibraryUser;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.models.UserRole;
import com.springdatajpa.library.repositories.BookRepository;
import com.springdatajpa.library.repositories.LibraryUserRepository;
import com.springdatajpa.library.repositories.PeopleRepository;
import org.springframework.stereotype.Service;
import com.springdatajpa.library.support.TransactionCallbacks;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PeopleService {

    private static final Duration BOOK_RETURN_OVERDUE_AFTER = Duration.ofDays(10);

    private final PeopleRepository peopleRepository;
    private final BookRepository bookRepository;
    private final LibraryUserRepository libraryUserRepository;
    private final RegistrationService registrationService;
    private final CatalogPasswordSetupService catalogPasswordSetupService;
    private final ReaderWelcomeMailService readerWelcomeMailService;

    public PeopleService(
            PeopleRepository peopleRepository,
            BookRepository bookRepository,
            LibraryUserRepository libraryUserRepository,
            RegistrationService registrationService,
            CatalogPasswordSetupService catalogPasswordSetupService,
            ReaderWelcomeMailService readerWelcomeMailService) {
        this.peopleRepository = peopleRepository;
        this.bookRepository = bookRepository;
        this.libraryUserRepository = libraryUserRepository;
        this.registrationService = registrationService;
        this.catalogPasswordSetupService = catalogPasswordSetupService;
        this.readerWelcomeMailService = readerWelcomeMailService;
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public List<Person> searchPeople(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }
        String q = query.trim();
        if (q.length() > 200) {
            q = q.substring(0, 200);
        }
        return peopleRepository.searchByText(q);
    }

    public Person findById(int id) {
        return peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Читатель не найден (id=" + id + ")."));
    }

    /**
     * Читатель в каталоге: логин совпадает с нормализованным email в карточке.
     */
    public Optional<Person> findByCatalogLogin(String catalogLogin) {
        if (!StringUtils.hasText(catalogLogin)) {
            return Optional.empty();
        }
        return peopleRepository.findByEmail(RegistrationService.catalogUsernameFromEmail(catalogLogin));
    }

    /**
     * @param excludePersonId при редактировании — id текущего читателя; при создании — {@code null}
     */
    public boolean isEmailTakenBySomeoneElse(String normalizedEmail, Integer excludePersonId) {
        return conflictsWithAnotherPerson(peopleRepository.findByEmail(normalizedEmail), excludePersonId);
    }

    /**
     * @param excludePersonId при редактировании — id текущего читателя; при создании — {@code null}
     */
    public boolean isReaderCardTakenBySomeoneElse(String normalizedReaderCard, Integer excludePersonId) {
        return conflictsWithAnotherPerson(
                peopleRepository.findByReaderCardNumber(normalizedReaderCard), excludePersonId);
    }

    private static boolean conflictsWithAnotherPerson(Optional<Person> owner, Integer excludePersonId) {
        return owner.map(p -> excludePersonId == null || p.getPersonId() != excludePersonId).orElse(false);
    }

    /**
     * Новый нормализованный email уже занят другой учёткой каталога — нельзя присвоить читателю при редактировании.
     *
     * @param excludePersonId id читателя в карточке
     * @param newNormalizedEmail целевой email после нормализации
     */
    public boolean isCatalogLoginTakenBySomeoneElse(int excludePersonId, String newNormalizedEmail) {
        if (!StringUtils.hasText(newNormalizedEmail)) {
            return false;
        }
        Person person = peopleRepository.findById(excludePersonId)
                .orElseThrow(() -> new ResourceNotFoundException("Читатель не найден (id=" + excludePersonId + ")."));
        String previousLogin = RegistrationService.catalogUsernameFromEmail(person.getEmail());
        if (newNormalizedEmail.equals(previousLogin)) {
            return false;
        }
        return libraryUserRepository.existsByUsername(newNormalizedEmail);
    }

    /**
     * @return ссылку для установки пароля, если приветственное письмо не уйдёт (нет SMTP / выключено / нет public URL) —
     *         её нужно передать читателю вручную
     */
    @Transactional
    public Optional<String> save(PersonForm form) {
        String email = RegistrationService.catalogUsernameFromEmail(form.getEmail());
        String readerCard = normalizeReaderCard(form.getReaderCardNumber());
        assertUniqueEmailAndReaderCard(email, readerCard, null);
        Person person = form.toNewPerson();
        person.setEmail(email);
        person.setReaderCardNumber(readerCard);
        peopleRepository.save(person);
        registrationService.registerCatalogUserWithInvitationPassword(email);
        String rawToken = catalogPasswordSetupService.createTokenForUsername(email);
        TransactionCallbacks.runAfterCommit(
                () -> readerWelcomeMailService.sendWelcomeIfConfigured(email, email, rawToken, readerCard));
        if (!readerWelcomeMailService.willSendWelcomeEmail()) {
            return Optional.of(readerWelcomeMailService.buildSetupLinkForHandoff(rawToken));
        }
        return Optional.empty();
    }

    @Transactional
    public void update(int id, PersonForm form) {
        Person person = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Читатель не найден (id=" + id + ")."));
        String previousCatalogLogin = RegistrationService.catalogUsernameFromEmail(person.getEmail());
        String email = RegistrationService.catalogUsernameFromEmail(form.getEmail());
        String readerCard = normalizeReaderCard(form.getReaderCardNumber());
        assertUniqueEmailAndReaderCard(email, readerCard, id);
        syncReaderCatalogUsernameIfEmailChanged(previousCatalogLogin, email);
        form.applyTo(person);
        person.setEmail(email);
        person.setReaderCardNumber(readerCard);
    }

    private void syncReaderCatalogUsernameIfEmailChanged(String previousLogin, String newLogin) {
        if (!StringUtils.hasText(newLogin) || newLogin.equals(previousLogin)) {
            return;
        }
        LibraryUser readerAccount = libraryUserRepository.findByUsername(previousLogin).orElse(null);
        if (readerAccount == null || readerAccount.getRole() != UserRole.USER) {
            return;
        }
        if (libraryUserRepository.existsByUsername(newLogin)) {
            throw new ConflictException("Этот email уже используется для входа в каталог.");
        }
        readerAccount.setUsername(newLogin);
    }

    private static String normalizeReaderCard(String readerCard) {
        return readerCard == null ? "" : readerCard.trim();
    }

    private void assertUniqueEmailAndReaderCard(String email, String readerCard, Integer excludePersonId) {
        if (isEmailTakenBySomeoneElse(email, excludePersonId)) {
            throw new ConflictException("Этот email уже указан у другого читателя.");
        }
        if (isReaderCardTakenBySomeoneElse(readerCard, excludePersonId)) {
            throw new ConflictException("Этот номер читательского билета уже используется.");
        }
    }

    @Transactional
    public void delete(int id) {
        Person person = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Читатель не найден (id=" + id + ")."));
        if (bookRepository.existsByOwnerPersonId(id)) {
            throw new ConflictException("Нельзя удалить читателя, у которого есть несданные книги.");
        }
        String catalogLogin = RegistrationService.catalogUsernameFromEmail(person.getEmail());
        if (!catalogLogin.isBlank()) {
            libraryUserRepository.findByUsername(catalogLogin).ifPresent(user -> {
                if (user.getRole() == UserRole.USER) {
                    libraryUserRepository.delete(user);
                }
            });
        }
        peopleRepository.deleteById(id);
    }

    public List<Book> getBooksByPersonId(int id) {
        if (!peopleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Читатель не найден (id=" + id + ").");
        }
        List<Book> books = bookRepository.findBorrowedBooksWithOwnerByPersonId(id);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime overdueThreshold = now.minus(BOOK_RETURN_OVERDUE_AFTER);
        books.forEach(book -> {
            LocalDateTime takenAt = book.getTakenAt();
            if (takenAt != null && takenAt.isBefore(overdueThreshold)) {
                book.setExpired(true);
            }
        });
        return books;
    }
}
