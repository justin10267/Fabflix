CREATE TABLE highestMovieId (id INT);

INSERT INTO highestMovieId (id)
SELECT CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) AS highestMovieId
FROM movies;