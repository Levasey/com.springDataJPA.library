CREATE TABLE person_read_book (
    person_id INTEGER NOT NULL REFERENCES person (person_id) ON DELETE CASCADE,
    book_id   INTEGER NOT NULL REFERENCES book (book_id) ON DELETE CASCADE,
    PRIMARY KEY (person_id, book_id)
);

CREATE INDEX idx_person_read_book_book_id ON person_read_book (book_id);
