package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PeopleRepositoryTest {

    @Autowired
    private PeopleRepository peopleRepository;

    @Test
    void findByName_returnsPerson() {
        Person p = new Person();
        p.setName("Alice");
        p.setSurname("Smith");
        p.setEmail("a@example.com");
        p.setAddress("USA, Boston, 111111");
        peopleRepository.save(p);
        peopleRepository.flush();

        Optional<Person> found = peopleRepository.findByName("Alice");

        assertTrue(found.isPresent());
        assertEquals("Smith", found.get().getSurname());
    }

    @Test
    void search_matchesNameOrSurnameOrEmail() {
        Person p = new Person();
        p.setName("Иван");
        p.setSurname("Петров");
        p.setEmail("ivan@example.com");
        p.setAddress("USA, Boston, 111111");
        peopleRepository.save(p);
        peopleRepository.flush();

        List<Person> byName = peopleRepository
                .findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase("иван", "иван", "иван");
        List<Person> byEmail = peopleRepository
                .findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrEmailContainingIgnoreCase("example", "example", "example");

        assertFalse(byName.isEmpty());
        assertFalse(byEmail.isEmpty());
    }
}
