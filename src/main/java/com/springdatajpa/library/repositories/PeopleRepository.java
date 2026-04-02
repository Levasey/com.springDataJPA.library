package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeopleRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByName(String name);

    Optional<Person> findByEmail(String email);

    Optional<Person> findByReaderCardNumber(String readerCardNumber);

    List<Person> findByNameContainingIgnoreCaseOrSurnameContainingIgnoreCaseOrPatronymicContainingIgnoreCaseOrEmailContainingIgnoreCaseOrReaderCardNumberContainingIgnoreCase(
            String namePart,
            String surnamePart,
            String patronymicPart,
            String emailPart,
            String readerCardPart);
}
