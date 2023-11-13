USE moviedb;
CREATE TABLE highestStarId (id INT);

INSERT INTO highestStarId (id)
SELECT CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) AS highestStarId
FROM stars;