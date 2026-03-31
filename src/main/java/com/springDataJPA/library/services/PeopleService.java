package com.springDataJPA.library.services;

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
import java.util.Optional;

@Service
@Transactional(readOnly=true)
public class PeopleService {

    /** Срок выдачи: помечаем книгу как просроченную, если с момента {@code takenAt} прошло больше 10 суток. */
    private static final long BOOK_RETURN_OVERDUE_AFTER_MS = 10L * 24 * 60 * 60 * 1000;

    private PeopleRepository peopleRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public List<Person> findAll() {
        return peopleRepository.findAll();
    }

    public Person findById(int id) {
        Optional<Person> person = peopleRepository.findById(id);
        return person.orElse(null);
    }

    @Transactional
    public void save(Person person) {
        peopleRepository.save(person);
    }

    @Transactional
    public void update(int id, Person person) {
        person.setPersonId(id);
        peopleRepository.save(person);
    }

    @Transactional
    public void delete(int id) {
        peopleRepository.deleteById(id);
    }

    public List<Book> getBooksByPersonId(int id) {
        Optional<Person> person = peopleRepository.findById(id);

        if (person.isPresent()) {
            Hibernate.initialize(person.get().getBooks());
            person.get().getBooks().forEach(book -> {
                Date takenAt = book.getTakenAt();
                if (takenAt != null) {
                    long diffInMillis = Math.abs(takenAt.getTime() - new Date().getTime());
                    if (diffInMillis > BOOK_RETURN_OVERDUE_AFTER_MS) {
                        book.setExpired(true);
                    }
                }
            });
            return person.get().getBooks();
        } else {
            return Collections.emptyList();
        }
    }

}