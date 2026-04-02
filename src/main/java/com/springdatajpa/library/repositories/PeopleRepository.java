package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.Person;
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
}
