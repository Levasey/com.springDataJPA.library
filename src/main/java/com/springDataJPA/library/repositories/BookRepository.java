package com.springDataJPA.library.repositories;

import com.springDataJPA.library.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByTitleStartingWithIgnoreCase(String title);

    boolean existsByOwnerPersonId(int personId);
}
