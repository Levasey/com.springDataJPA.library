package com.springDataJPA.library.services;

import com.springDataJPA.library.exception.ResourceNotFoundException;
import com.springDataJPA.library.models.Book;
import com.springDataJPA.library.models.Person;
import com.springDataJPA.library.repositories.PeopleRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly=true)
public class PeopleService {

    /** Срок выдачи: помечаем книгу как просроченную, если с момента {@code takenAt} прошло больше 10 суток. */
    private static final long BOOK_RETURN_OVERDUE_AFTER_MS = 10L * 24 * 60 * 60 * 1000;

    private final PeopleRepository peopleRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public Person findById(int id) {
        return peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person not found: id=" + id));
    }

    @Transactional
    public void save(Person person) {
        peopleRepository.save(person);
    }

    @Transactional
    public void update(int id, Person person) {
        if (!peopleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found: id=" + id);
        }
        person.setPersonId(id);
        peopleRepository.save(person);
    }

    @Transactional
    public void delete(int id) {
        if (!peopleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Person not found: id=" + id);
        }
        peopleRepository.deleteById(id);
    }

    public List<Book> getBooksByPersonId(int id) {
        Person person = peopleRepository.findById(id).orElse(null);

        if (person == null) {
            return Collections.emptyList();
        }
        Hibernate.initialize(person.getBooks());
        Date now = new Date();
        person.getBooks().forEach(book -> {
            Date takenAt = book.getTakenAt();
            if (takenAt != null && now.getTime() - takenAt.getTime() > BOOK_RETURN_OVERDUE_AFTER_MS) {
                book.setExpired(true);
            }
        });
        return person.getBooks();
    }

}
