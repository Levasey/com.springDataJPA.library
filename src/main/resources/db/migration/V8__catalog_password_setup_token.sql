CREATE TABLE catalog_password_setup_token (
    id           BIGSERIAL PRIMARY KEY,
    token_hash   CHAR(64)     NOT NULL UNIQUE,
    username     VARCHAR(255) NOT NULL REFERENCES library_user (username) ON DELETE CASCADE,
    expires_at   TIMESTAMP    NOT NULL,
    used_at      TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_catalog_pwd_setup_token_username ON catalog_password_setup_token (username);
