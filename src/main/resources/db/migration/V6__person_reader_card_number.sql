ALTER TABLE person ADD COLUMN reader_card_number VARCHAR(64);

UPDATE person
SET reader_card_number = 'MIGR-' || LPAD(person_id::text, 8, '0')
WHERE reader_card_number IS NULL;

ALTER TABLE person ALTER COLUMN reader_card_number SET NOT NULL;

CREATE UNIQUE INDEX person_reader_card_number_key ON person (reader_card_number);
