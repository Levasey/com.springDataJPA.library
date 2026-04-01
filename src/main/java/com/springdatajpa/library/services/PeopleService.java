package com.springdatajpa.library.services;

import com.springdatajpa.library.dto.PersonForm;
import com.springdatajpa.library.exception.ConflictException;
import com.springdatajpa.library.exception.ResourceNotFoundException;
import com.springdatajpa.library.models.Book;
import com.springdatajpa.library.models.Person;
import com.springdatajpa.library.repositories.BookRepository;
import com.springdatajpa.library.repositories.PeopleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PeopleService {

    private static final Duration BOOK_RETURN_OVERDUE_AFTER = Duration.ofDays(10);

    private final PeopleRepository peopleRepository;
    private final BookRepository bookRepository;

    public PeopleService(PeopleRepository peopleRepository, BookRepository bookRepository) {
        this.peopleRepository = peopleRepository;
        this.bookRepository = bookRepository;
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
        return peopleRepository.findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q, q);
    }

    public Person findById(int id) {
        return peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found: id=" + id));
    }

    @Transactional
    public void save(PersonForm form) {
        peopleRepository.save(form.toNewPerson());
    }

    @Transactional
    public void update(int id, PersonForm form) {
        Person person = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found: id=" + id));
        form.applyTo(person);
    }

    @Transactional
    public void delete(int id) {
        if (!peopleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found: id=" + id);
        }
        if (bookRepository.existsByOwnerPersonId(id)) {
            throw new ConflictException("Cannot delete a person who still has books on loan.");
        }
        peopleRepository.deleteById(id);
    }

    public List<Book> getBooksByPersonId(int id) {
        if (!peopleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found: id=" + id);
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
