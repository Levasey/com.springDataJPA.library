ALTER TABLE library_user
    ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'USER';

UPDATE library_user
SET role = 'LIBRARIAN'
WHERE username = 'admin';
