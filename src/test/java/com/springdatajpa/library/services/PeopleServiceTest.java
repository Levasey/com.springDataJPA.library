package com.springdatajpa.library.services;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.exception.ResourceNotFoundException;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.repositories.BookRepository;
import com.springdatajpa.library.repositories.PeopleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private ReaderWelcomeMailService readerWelcomeMailService;

    @Mock
    private CatalogPasswordSetupService catalogPasswordSetupService;

    @InjectMocks
    private PeopleService peopleService;

    @Test
    void findAll_delegatesToRepository() {
        List<Person> list = List.of(new Person());
        when(peopleRepository.findAll()).thenReturn(list);
        assertSame(list, peopleService.findAll());
    }

    @Test
    void searchPeople_blankDoesNotHitRepository() {
        assertTrue(peopleService.searchPeople("").isEmpty());
        assertTrue(peopleService.searchPeople("   ").isEmpty());
        verifyNoInteractions(peopleRepository);
    }

    @Test
    void searchPeople_delegatesToRepository() {
        List<Person> list = List.of(new Person());
        when(peopleRepository
                .findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrPatronymicContainingIgnoreCaseOrEmailContainingIgnoreCaseOrReaderCardNumberContainingIgnoreCase(
                        "x", "x", "x", "x", "x")).thenReturn(list);
        assertSame(list, peopleService.searchPeople("x"));
    }

    @Test
    void findById_returnsPersonWhenPresent() {
        Person p = new Person();
        when(peopleRepository.findById(1)).thenReturn(Optional.of(p));
        assertSame(p, peopleService.findById(1));
    }

    @Test
    void findById_throwsWhenMissing() {
        when(peopleRepository.findById(2)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> peopleService.findById(2));
    }

    @Test
    void save_registersInvitationAndMailsWhenPrimed() {
        when(peopleRepository.findByEmail("n@s.com")).thenReturn(Optional.empty());
        when(peopleRepository.findByReaderCardNumber("CARD-N")).thenReturn(Optional.empty());
        when(catalogPasswordSetupService.createTokenForUsername("n@s.com")).thenReturn("raw-token");
        when(readerWelcomeMailService.willSendWelcomeEmail()).thenReturn(true);
        PersonForm form = new PersonForm();
        form.setName("N");
        form.setSurname("S");
        form.setEmail("  N@s.com ");
        form.setReaderCardNumber("CARD-N");
        form.setAddress("USA, Boston, 111111");
        assertTrue(peopleService.save(form).isEmpty());
        verify(peopleRepository).save(any(Person.class));
        verify(registrationService).registerCatalogUserWithInvitationPassword("n@s.com");
        verify(catalogPasswordSetupService).createTokenForUsername("n@s.com");
        verify(readerWelcomeMailService)
                .sendWelcomeIfConfigured("n@s.com", "n@s.com", "raw-token", "CARD-N");
    }

    @Test
    void save_returnsHandoffLinkWhenMailNotPrimed() {
        when(peopleRepository.findByEmail("n@s.com")).thenReturn(Optional.empty());
        when(peopleRepository.findByReaderCardNumber("CARD-N")).thenReturn(Optional.empty());
        when(catalogPasswordSetupService.createTokenForUsername("n@s.com")).thenReturn("raw-token");
        when(readerWelcomeMailService.willSendWelcomeEmail()).thenReturn(false);
        when(readerWelcomeMailService.buildSetupLinkForHandoff("raw-token")).thenReturn("/catalog/setup-password?token=raw-token");
        PersonForm form = new PersonForm();
        form.setName("N");
        form.setSurname("S");
        form.setEmail("n@s.com");
        form.setReaderCardNumber("CARD-N");
        form.setAddress("USA, Boston, 111111");
        assertEquals(
                Optional.of("/catalog/setup-password?token=raw-token"), peopleService.save(form));
        verify(readerWelcomeMailService).sendWelcomeIfConfigured("n@s.com", "n@s.com", "raw-token", "CARD-N");
    }

    @Test
    void save_throwsWhenEmailTaken() {
        when(peopleRepository.findByEmail("x@y.z")).thenReturn(Optional.of(new Person()));
        PersonForm form = new PersonForm();
        form.setName("N");
        form.setSurname("S");
        form.setEmail("x@y.z");
        form.setReaderCardNumber("C1");
        assertThrows(ConflictException.class, () -> peopleService.save(form));
        verify(peopleRepository, never()).save(any());
        verifyNoInteractions(registrationService);
        verifyNoInteractions(readerWelcomeMailService);
        verifyNoInteractions(catalogPasswordSetupService);
    }

    @Test
    void save_throwsWhenReaderCardTaken() {
        when(peopleRepository.findByEmail("a@b.c")).thenReturn(Optional.empty());
        when(peopleRepository.findByReaderCardNumber("TAKEN")).thenReturn(Optional.of(new Person()));
        PersonForm form = new PersonForm();
        form.setName("N");
        form.setSurname("S");
        form.setEmail("a@b.c");
        form.setReaderCardNumber("TAKEN");
        assertThrows(ConflictException.class, () -> peopleService.save(form));
        verify(peopleRepository, never()).save(any());
        verifyNoInteractions(registrationService);
        verifyNoInteractions(readerWelcomeMailService);
        verifyNoInteractions(catalogPasswordSetupService);
    }

    @Test
    void update_appliesFormToManagedPerson() {
        Person existing = new Person();
        existing.setPersonId(3);
        when(peopleRepository.findById(3)).thenReturn(Optional.of(existing));
        when(peopleRepository.findByEmail("e@example.com")).thenReturn(Optional.of(existing));
        when(peopleRepository.findByReaderCardNumber("CARD-UPD")).thenReturn(Optional.of(existing));
        PersonForm form = new PersonForm();
        form.setName("New");
        form.setSurname("Name");
        form.setEmail("e@example.com");
        form.setReaderCardNumber("CARD-UPD");
        form.setAddress("USA, Boston, 123456");

        peopleService.update(3, form);

        assertEquals("New", existing.getName());
        assertEquals("Name", existing.getSurname());
        assertEquals("CARD-UPD", existing.getReaderCardNumber());
        assertEquals("e@example.com", existing.getEmail());
        verify(peopleRepository, never()).save(any());
    }

    @Test
    void update_throwsWhenEmailBelongsToAnotherPerson() {
        Person existing = new Person();
        existing.setPersonId(3);
        Person other = new Person();
        other.setPersonId(7);
        when(peopleRepository.findById(3)).thenReturn(Optional.of(existing));
        when(peopleRepository.findByEmail("taken@x.com")).thenReturn(Optional.of(other));
        PersonForm form = new PersonForm();
        form.setName("N");
        form.setSurname("S");
        form.setEmail("taken@x.com");
        form.setReaderCardNumber("R1");
        assertThrows(ConflictException.class, () -> peopleService.update(3, form));
    }

    @Test
    void update_throwsWhenPersonMissing() {
        when(peopleRepository.findById(3)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> peopleService.update(3, new PersonForm()));
        verify(peopleRepository, never()).save(any());
    }

    @Test
    void delete_delegatesToRepository() {
        when(peopleRepository.existsById(9)).thenReturn(true);
        when(bookRepository.existsByOwnerPersonId(9)).thenReturn(false);
        peopleService.delete(9);
        verify(peopleRepository).deleteById(9);
    }

    @Test
    void delete_throwsConflictWhenPersonHasBooks() {
        when(peopleRepository.existsById(9)).thenReturn(true);
        when(bookRepository.existsByOwnerPersonId(9)).thenReturn(true);
        assertThrows(ConflictException.class, () -> peopleService.delete(9));
        verify(peopleRepository, never()).deleteById(anyInt());
    }

    @Test
    void delete_throwsWhenMissing() {
        when(peopleRepository.existsById(9)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> peopleService.delete(9));
        verify(peopleRepository, never()).deleteById(anyInt());
    }

    @Test
    void getBooksByPersonId_throwsWhenPersonMissing() {
        when(peopleRepository.existsById(1)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> peopleService.getBooksByPersonId(1));
    }

    @Test
    void getBooksByPersonId_marksOverdueBooksExpired() {
        when(peopleRepository.existsById(1)).thenReturn(true);
        Book overdue = new Book("T", "A", 2000);
        overdue.setTakenAt(LocalDateTime.now().minusDays(11));
        Book fresh = new Book("T2", "A2", 2001);
        fresh.setTakenAt(LocalDateTime.now());

        when(bookRepository.findBorrowedBooksWithOwnerByPersonId(1))
                .thenReturn(new ArrayList<>(List.of(overdue, fresh)));

        List<Book> books = peopleService.getBooksByPersonId(1);

        assertEquals(2, books.size());
        assertTrue(overdue.isExpired());
        assertFalse(fresh.isExpired());
    }

    @Test
    void getBooksByPersonId_doesNotMarkExpiredWhenTakenAtInFuture() {
        when(peopleRepository.existsById(1)).thenReturn(true);
        Book book = new Book("T", "A", 2000);
        book.setTakenAt(LocalDateTime.now().plusDays(1));

        when(bookRepository.findBorrowedBooksWithOwnerByPersonId(1)).thenReturn(new ArrayList<>(List.of(book)));

        peopleService.getBooksByPersonId(1);

        assertFalse(book.isExpired());
    }
}
