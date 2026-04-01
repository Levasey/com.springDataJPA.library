package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {

    @EntityGraph(attributePaths = "owner")
    List<Book> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = "owner")
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String titlePart, String authorPart);

    boolean existsByOwnerPersonId(int personId);

    @EntityGraph(attributePaths = "owner")
    @Query("SELECT b FROM Book b WHERE b.owner.personId = :personId")
    List<Book> findBorrowedBooksWithOwnerByPersonId(@Param("personId") int personId);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.owner WHERE b.bookId = :id")
    Optional<Book> findWithOwnerById(@Param("id") int id);

    List<Book> findByOwnerIsNull(Sort sort);

    List<Book> findByOwnerIsNotNull(Sort sort);

    Page<Book> findByOwnerIsNull(Pageable pageable);

    Page<Book> findByOwnerIsNotNull(Pageable pageable);
}
