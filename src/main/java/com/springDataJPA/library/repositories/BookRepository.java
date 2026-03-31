package com.springDataJPA.library.repositories;

import com.springDataJPA.library.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    boolean existsByOwnerPersonId(int personId);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.owner WHERE b.bookId = :id")
    Optional<Book> findWithOwnerById(@Param("id") int id);
}
