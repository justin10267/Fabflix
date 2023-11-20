public class QueryStrings {
    public static String SEARCH_QUERY =
            "SELECT \n" +
                    "    m.id,\n" +
                    "    m.title,\n" +
                    "    m.year,\n" +
                    "    m.director,\n" +
                    "    m.price,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(g.name\n" +
                    "                    ORDER BY g.name DESC)\n" +
                    "        FROM\n" +
                    "            genres_in_movies gm\n" +
                    "                INNER JOIN\n" +
                    "            genres g ON gm.genreId = g.id\n" +
                    "        WHERE\n" +
                    "            gm.movieId = m.id), ',', 3) AS genres,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(CONCAT(s.id, ':', s.name)\n" +
                    "                    ORDER BY s.name DESC , s.id)\n" +
                    "        FROM\n" +
                    "            stars_in_movies sm\n" +
                    "                INNER JOIN\n" +
                    "            stars s ON sm.starId = s.id\n" +
                    "        WHERE\n" +
                    "            sm.movieId = m.id), ',', 3) AS stars,\n" +
                    "    r.rating\n" +
                    "FROM\n" +
                    "    movies m\n" +
                    "        LEFT JOIN\n" +
                    "    ratings r ON m.id = r.movieId\n" +
                    "WHERE \n" +
                    "    MATCH (title) AGAINST (? IN BOOLEAN MODE)\n" +
                    "    AND year LIKE ?\n" +
                    "    AND UPPER(director) LIKE ?\n" +
                    "GROUP BY m.id , m.title , m.year , m.director , r.rating\n" +
                    "HAVING UPPER(stars) LIKE ?\n" +
                    "ORDER BY %s\n" +
                    "LIMIT ?\n" +
                    "OFFSET ?;";
    public static String GENRE_QUERY =
            "WITH\n" +
                    "\tgenredFilteredMovies AS \n" +
                    "\t(\n" +
                    "\t\tSELECT m.id, m.title, m.year, m.director, m.price, g.name as genre\n" +
                    "\t\tFROM movies as m JOIN genres_in_movies as gm ON m.id = gm.movieId JOIN genres as g ON gm.genreId = g.id\n" +
                    "\t\tWHERE g.name = ?\n" +
                    "    )\n" +
                    "SELECT \n" +
                    "    gfm.id,\n" +
                    "    gfm.title,\n" +
                    "    gfm.year,\n" +
                    "    gfm.director,\n" +
                    "    gfm.price,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(g.name\n" +
                    "                    ORDER BY g.name DESC)\n" +
                    "        FROM\n" +
                    "            genres_in_movies gm\n" +
                    "                INNER JOIN\n" +
                    "            genres g ON gm.genreId = g.id\n" +
                    "        WHERE\n" +
                    "            gm.movieId = gfm.id), ',', 3) AS genres,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(CONCAT(s.id, ':', s.name)\n" +
                    "                    ORDER BY s.name DESC , s.id)\n" +
                    "        FROM\n" +
                    "            stars_in_movies sm\n" +
                    "                INNER JOIN\n" +
                    "            stars s ON sm.starId = s.id\n" +
                    "        WHERE\n" +
                    "            sm.movieId = gfm.id), ',', 3) AS stars,\n" +
                    "    r.rating\n" +
                    "FROM\n" +
                    "    genredFilteredMovies gfm\n" +
                    "        LEFT JOIN\n" +
                    "    ratings r ON gfm.id = r.movieId\n" +
                    "GROUP BY gfm.id , gfm.title , gfm.year , gfm.director , r.rating\n" +
                    "ORDER BY %s\n" +
                    "LIMIT ?\n" +
                    "OFFSET ?;";
    public static String TITLE_QUERY =
            "SELECT \n" +
                    "    m.id,\n" +
                    "    m.title,\n" +
                    "    m.year,\n" +
                    "    m.director,\n" +
                    "    m.price,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(g.name\n" +
                    "                    ORDER BY g.name DESC)\n" +
                    "        FROM\n" +
                    "            genres_in_movies gm\n" +
                    "                INNER JOIN\n" +
                    "            genres g ON gm.genreId = g.id\n" +
                    "        WHERE\n" +
                    "            gm.movieId = m.id), ',', 3) AS genres,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(CONCAT(s.id, ':', s.name)\n" +
                    "                    ORDER BY s.name DESC , s.id)\n" +
                    "        FROM\n" +
                    "            stars_in_movies sm\n" +
                    "                INNER JOIN\n" +
                    "            stars s ON sm.starId = s.id\n" +
                    "        WHERE\n" +
                    "            sm.movieId = m.id), ',', 3) AS stars,\n" +
                    "    r.rating\n" +
                    "FROM\n" +
                    "    movies m\n" +
                    "        LEFT JOIN\n" +
                    "    ratings r ON m.id = r.movieId\n" +
                    "WHERE UPPER(m.title) LIKE ?\n" +
                    "GROUP BY m.id , m.title , m.year , m.director , r.rating\n" +
                    "ORDER BY %s\n" +
                    "LIMIT ?\n" +
                    "OFFSET ?;";
    public static String SPECIAL_TITLE_QUERY =
            "SELECT \n" +
                    "    m.id,\n" +
                    "    m.title,\n" +
                    "    m.year,\n" +
                    "    m.director,\n" +
                    "    m.price,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(g.name\n" +
                    "                    ORDER BY g.name DESC)\n" +
                    "        FROM\n" +
                    "            genres_in_movies gm\n" +
                    "                INNER JOIN\n" +
                    "            genres g ON gm.genreId = g.id\n" +
                    "        WHERE\n" +
                    "            gm.movieId = m.id), ',', 3) AS genres,\n" +
                    "    SUBSTRING_INDEX((SELECT \n" +
                    "            GROUP_CONCAT(CONCAT(s.id, ':', s.name)\n" +
                    "                    ORDER BY s.name DESC , s.id)\n" +
                    "        FROM\n" +
                    "            stars_in_movies sm\n" +
                    "                INNER JOIN\n" +
                    "            stars s ON sm.starId = s.id\n" +
                    "        WHERE\n" +
                    "            sm.movieId = m.id), ',', 3) AS stars,\n" +
                    "    r.rating\n" +
                    "FROM\n" +
                    "    movies m\n" +
                    "        LEFT JOIN\n" +
                    "    ratings r ON m.id = r.movieId\n" +
                    "WHERE m.title REGEXP '^[^a-zA-Z0-9]'\n" +
                    "GROUP BY m.id , m.title , m.year , m.director , r.rating\n" +
                    "ORDER BY %s\n" +
                    "LIMIT ?\n" +
                    "OFFSET ?;";
}
