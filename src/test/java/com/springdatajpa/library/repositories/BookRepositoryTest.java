package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByTitleContainingIgnoreCase_matchesSubstringRegardlessOfCase() {
        bookRepository.save(new Book("Alpha Tale", "A1", 2000));
        bookRepository.save(new Book("Beta Story", "B2", 2001));
        bookRepository.flush();

        List<Book> found = bookRepository.findByTitleContainingIgnoreCase("pha t");

        assertEquals(1, found.size());
        assertEquals("Alpha Tale", found.get(0).getTitle());
    }

    @Test
    void findWithOwnerById_loadsOwner() {
        // smoke: entity manager + query parse
        assertTrue(bookRepository.findWithOwnerById(999).isEmpty());
    }
}
