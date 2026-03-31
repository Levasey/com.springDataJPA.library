package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.LibraryUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryUserRepository extends JpaRepository<LibraryUser, Long> {

    Optional<LibraryUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
