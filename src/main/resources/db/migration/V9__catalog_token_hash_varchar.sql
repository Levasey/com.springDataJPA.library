-- Hibernate валидирует String как VARCHAR; Flyway V8 задал CHAR(64) → bpchar.
ALTER TABLE catalog_password_setup_token
    ALTER COLUMN token_hash TYPE VARCHAR(64);
