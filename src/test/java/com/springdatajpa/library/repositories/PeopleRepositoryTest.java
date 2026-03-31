package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
