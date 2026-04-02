package com.springdatajpa.library.repositories;

import com.springdatajpa.library.models.CatalogPasswordSetupToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogPasswordSetupTokenRepository extends JpaRepository<CatalogPasswordSetupToken, Long> {

    Optional<CatalogPasswordSetupToken> findByTokenHash(String tokenHash);

    void deleteByUsernameAndUsedAtIsNull(String username);
}
