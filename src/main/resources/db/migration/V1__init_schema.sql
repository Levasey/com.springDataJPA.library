CREATE TABLE person (
    person_id SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    surname      VARCHAR(255) NOT NULL,
    age          INTEGER      NOT NULL,
    email        VARCHAR(255) NOT NULL,
    address      VARCHAR(255),
    date_of_birth DATE
);

CREATE TABLE book (
    book_id         SERIAL PRIMARY KEY,
    book_name       VARCHAR(255) NOT NULL,
    author          VARCHAR(255) NOT NULL,
    year_published  INTEGER      NOT NULL,
    taken_at        TIMESTAMP,
    person_id       INTEGER REFERENCES person (person_id)
);

CREATE INDEX idx_book_person_id ON book (person_id);
