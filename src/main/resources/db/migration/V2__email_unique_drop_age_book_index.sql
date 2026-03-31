ALTER TABLE person DROP COLUMN IF EXISTS age;

CREATE UNIQUE INDEX IF NOT EXISTS person_email_key ON person (email);

CREATE INDEX IF NOT EXISTS idx_book_book_name ON book (book_name);
