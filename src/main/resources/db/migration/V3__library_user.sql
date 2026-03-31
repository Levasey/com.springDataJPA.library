CREATE TABLE library_user (
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Default admin (password: admin), BCrypt
INSERT INTO library_user (username, password, enabled)
VALUES ('admin', '$2b$10$DHiJYlJEHCKfvkoaW9FKveEHK7wCJT7K/d798MupD8bry3UkrZpdW', TRUE);
