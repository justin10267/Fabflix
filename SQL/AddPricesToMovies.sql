USE moviedb;
ALTER TABLE movies
ADD COLUMN price DECIMAL(5, 2);

UPDATE movies
SET price = FLOOR(1 + RAND() * 10);