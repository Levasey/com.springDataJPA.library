package com.springDataJPA.library.repositories;

import com.springDataJPA.library.config.TestJpaConfig;
import com.springDataJPA.library.models.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@Transactional
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByTitleStartingWith_matchesPrefix() {
        bookRepository.save(new Book("Alpha Tale", "A1", 2000));
        bookRepository.save(new Book("Beta Story", "B2", 2001));
        bookRepository.flush();

        List<Book> found = bookRepository.findByTitleStartingWith("Alp");

        assertEquals(1, found.size());
        assertEquals("Alpha Tale", found.get(0).getTitle());
    }
}
