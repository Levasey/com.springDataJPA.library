package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.Person;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeopleRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByName(String name);

    Optional<Person> findByEmail(String email);

    Optional<Person> findByReaderCardNumber(String readerCardNumber);

    @Query("""
            SELECT p FROM Person p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.surname) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.patronymic) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.email) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.readerCardNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<Person> searchByText(@Param("q") String q);

    @EntityGraph(attributePaths = "readBooks")
    @Query("SELECT p FROM Person p WHERE p.personId = :personId")
    Optional<Person> findWithReadBooksByPersonId(@Param("personId") int personId);

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Person p JOIN p.readBooks b
            WHERE p.personId = :personId AND b.bookId = :bookId
            """)
    boolean personHasReadBook(@Param("personId") int personId, @Param("bookId") int bookId);

    @Query("SELECT b.bookId FROM Person p JOIN p.readBooks b WHERE p.personId = :personId")
    List<Integer> findReadBookIdsByPersonId(@Param("personId") int personId);
}
