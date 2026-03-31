package com.springDataJPA.library.repositories;

import com.springDataJPA.library.config.TestJpaConfig;
import com.springDataJPA.library.models.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@Transactional
class PeopleRepositoryTest {

    @Autowired
    private PeopleRepository peopleRepository;

    @Test
    void findByName_returnsPerson() {
        Person p = new Person();
        p.setName("Alice");
        p.setSurname("Smith");
        p.setAge(20);
        p.setEmail("a@example.com");
        p.setAddress("USA, Boston, 111111");
        peopleRepository.save(p);
        peopleRepository.flush();

        Optional<Person> found = peopleRepository.findByName("Alice");

        assertTrue(found.isPresent());
        assertEquals("Smith", found.get().getSurname());
    }
}
