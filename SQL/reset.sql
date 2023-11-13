DELIMITER //

CREATE PROCEDURE add_genre(IN genre_name VARCHAR(32))
BEGIN
    DECLARE genreCount INT;
    DECLARE genreId INT;
    SELECT COUNT(*) INTO genreCount FROM genres WHERE name = genre_name;
    IF genreCount = 0 THEN
        INSERT INTO genres (name) VALUES (genre_name);
        SELECT id INTO genreId FROM genres WHERE name = genre_name;
        SELECT CONCAT("Success! Genre Id: ", genreId) as message;
	ELSE
		SELECT "Error! Genre Already Exists." as message;
    END IF;
END//

DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_star(IN starName VARCHAR(100), IN starBirthYear INT)
BEGIN
	DECLARE newStarId INT;
    DECLARE formattedStarId VARCHAR(10);
    UPDATE highestStarId SET id = id + 1;
    SELECT id INTO newStarId FROM highestStarId;
    SET formattedStarId = CONCAT('nm', newStarId);
    INSERT INTO stars (id, name, birthYear) VALUES (formattedStarId, starName, starBirthYear);
    SELECT CONCAT("Success! Star Id: ", formattedStarId) as message;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_movie(
    IN movieTitle VARCHAR(100),
    IN movieYear INT,
    IN movieDirector VARCHAR(100),
    IN starName VARCHAR(100),
    IN starBirthYear INT,
    IN genreName VARCHAR(32)
)
BEGIN
    DECLARE movieExists INT;
    DECLARE newMovieId VARCHAR(10);
    DECLARE existingGenreId INT;
    DECLARE existingStarId VARCHAR(10);
    DECLARE existingStar INT;
    
    SELECT COUNT(*) INTO movieExists
    FROM movies
    WHERE title = movieTitle AND year = movieYear AND director = movieDirector;

    IF movieExists = 0 THEN
        SELECT CONCAT('tt', id + 1) INTO newMovieId
        FROM highestMovieId;
        INSERT INTO movies (id, title, year, director, price) VALUES (newMovieId, movieTitle, movieYear, movieDirector, 1 + FLOOR(RAND() * 10));
        
        UPDATE highestMovieId SET id = id + 1;

        CALL add_genre(genreName);
        SELECT id INTO existingGenreId
        FROM genres
        WHERE name = genreName;
        
        SELECT COUNT(*) INTO existingStar
        FROM stars
        WHERE name = starName;
        
        IF existingStar > 0 THEN
            SELECT id INTO existingStarId
            FROM stars
            WHERE name = starName LIMIT 1;
        ELSE
            SELECT CONCAT('nm', id + 1) INTO existingStarId
            FROM highestStarId;
            INSERT INTO stars (id, name, birthYear) VALUES (existingStarId, starName, starBirthYear);
            UPDATE highestStarId SET id = id + 1;
        END IF;

        INSERT INTO stars_in_movies (starId, movieId) VALUES (existingStarId, newMovieId);
        INSERT INTO genres_in_movies (genreId, movieId) VALUES (existingGenreId, newMovieId);
        
		SELECT CONCAT("Success! Movie Id: ", newMovieId, ", Star Id: ", existingStarId, ", Genre Id: ", existingGenreId) as message;
	ELSE
		SELECT "Error! Movie Already Exists." as message;
    END IF;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_movie_parser(
    IN movieTitle VARCHAR(100),
    IN movieYear INT,
    IN movieDirector VARCHAR(100)
)
BEGIN
    DECLARE movieExists INT;
    DECLARE newMovieId VARCHAR(10);
    
    SELECT COUNT(*) INTO movieExists
    FROM movies
    WHERE title = movieTitle AND year = movieYear AND director = movieDirector;

    IF movieExists = 0 THEN
        SELECT CONCAT('tt', id + 1) INTO newMovieId
        FROM highestMovieId;
        INSERT INTO movies (id, title, year, director, price) VALUES (newMovieId, movieTitle, movieYear, movieDirector, 1 + FLOOR(RAND() * 10));
        
        UPDATE highestMovieId SET id = id + 1;
        
		SELECT CONCAT("Success! Movie Id: ", newMovieId) as message;
	ELSE
		SELECT "Error! Movie Already Exists." as message;
    END IF;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE link_genre_to_movie_parser(
    IN genreName VARCHAR(32),
    IN movieTitle VARCHAR(100),
    IN movieYear INT,
    IN movieDirector VARCHAR(100)
)
BEGIN
    DECLARE genreIdVar INT;
    DECLARE movieIdVar VARCHAR(10);
    
    CALL add_genre(genreName);
    SELECT id INTO genreIdVar FROM genres WHERE name = genreName;
    SELECT id INTO movieIdVar FROM movies WHERE title = movieTitle AND year = movieYear AND director = movieDirector;
    INSERT INTO genres_in_movies (genreId, movieId) VALUES (genreIdVar, movieIdVar);
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_star_parser(IN starName VARCHAR(100), IN starBirthYear INT, OUT formattedStarId VARCHAR(10))
BEGIN
    DECLARE newStarId INT;
    DECLARE tempId VARCHAR(10);
    UPDATE highestStarId SET id = id + 1;
    SELECT id INTO newStarId FROM highestStarId;
    SET tempId = CONCAT('nm', newStarId);
    INSERT INTO stars (id, name, birthYear) VALUES (tempId, starName, starBirthYear);
    SET formattedStarId = tempId;
    SELECT CONCAT("Success! Star Id: ", tempId) as message;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE link_star_to_movie_parser(
    IN starName VARCHAR(100),
    IN starBirthYear INT,
    IN movieTitle VARCHAR(100),
    IN movieYear INT,
    IN movieDirector VARCHAR(100)
)
BEGIN
    DECLARE starIdVar VARCHAR(10);
    DECLARE movieIdVar VARCHAR(10);
    DECLARE starExists INT;
    
    CALL add_star_parser(starName, starBirthYear, starIdVar);
    SELECT id INTO movieIdVar FROM movies WHERE title = movieTitle AND year = movieYear AND director = movieDirector;
    
    INSERT INTO stars_in_movies (starId, movieId) VALUES (starIdVar, movieIdVar);
END //

DELIMITER ;

ALTER TABLE movies
ADD COLUMN price DECIMAL(5, 2);

UPDATE movies
SET price = FLOOR(1 + RAND() * 10);

CREATE TABLE highestMovieId (id INT);

INSERT INTO highestMovieId (id)
SELECT CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) AS highestMovieId
FROM movies;

CREATE TABLE highestStarId (id INT);

INSERT INTO highestStarId (id)
SELECT CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) AS highestStarId
FROM stars;

create table employees(
email varchar(50) primary key,
password varchar(20) not null,
fullname varchar(100));

INSERT INTO employees VALUES ('classta@email.edu', 'classta', 'TA CS122B');

create table customers_backup(
id integer auto_increment primary key,
firstName varchar(50) not null,
lastName varchar(50) not null,
ccId varchar(20) not null,
address varchar(200) not null,
email varchar(50) not null,
password varchar(20) not null,
foreign key(ccId) references creditcards(id));
insert into customers_backup select * from customers;