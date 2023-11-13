import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;


@WebServlet(name = "ListServlet", urlPatterns = "/api/list")
public class ListServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;
    private static String SEARCH_QUERY =
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
            "(SOUNDEX(UPPER(title)) = SOUNDEX(?) OR UPPER(title) LIKE ?)\n" +
            "AND year LIKE ?\n" +
            "AND (SOUNDEX(UPPER(director)) = SOUNDEX(?) OR UPPER(director) LIKE ?)\n" +
            "GROUP BY m.id , m.title , m.year , m.director , r.rating\n" +
            "HAVING UPPER(stars) LIKE ?" +
            "ORDER BY %s\n" +
            "LIMIT ?\n" +
            "OFFSET ?;";
    private static String GENRE_QUERY =
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
    private static String TITLE_QUERY =
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
    private static String SPECIAL_TITLE_QUERY =
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
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String stars = request.getParameter("stars");
            String genre = request.getParameter("genre");
            String prefix = request.getParameter("prefix");
            String limit = request.getParameter("limit");
            String sort = request.getParameter("sort");
            String page = request.getParameter("page");
            User sessionUser = (User)(request.getSession().getAttribute("user"));
            if (page == null) {
                page = "1";
            }
            if (limit == null || sort == null) {
                limit = sessionUser.getLimit();
                sort = sessionUser.getSort();
            }
            else {
                sessionUser.setLimit(limit);
                sessionUser.setSort(sort);
            }
            sort = User.getSortQuery(sort);
            String offset = Integer.toString((Integer.parseInt(page) - 1 ) * Integer.parseInt(limit));
            PreparedStatement statement;
            PreparedStatement nextPageStatement;
            if (title != null || year != null || director != null || stars != null) {
                statement = conn.prepareStatement(String.format(SEARCH_QUERY, sort));
                nextPageStatement = conn.prepareStatement(String.format(SEARCH_QUERY, sort));
                if (title != null) {
                    title = title.toUpperCase();
                }
                if (director != null) {
                    director = director.toUpperCase();
                }
                if (stars != null) {
                    stars = stars.toUpperCase();
                }
                statement.setString(1, title);
                statement.setString(2, "%" + title + "%");
                statement.setString(3, year + "%");
                statement.setString(4, director);
                statement.setString(5, "%" + director + "%");
                statement.setString(6, "%" + stars + "%");
                statement.setInt(7, Integer.parseInt(limit));
                statement.setInt(8, Integer.parseInt(offset));

                nextPageStatement.setString(1, title);
                nextPageStatement.setString(2, "%" + title + "%");
                nextPageStatement.setString(3, year + "%");
                nextPageStatement.setString(4, director);
                nextPageStatement.setString(5, "%" + director + "%");
                nextPageStatement.setString(6, "%" + stars + "%");
                nextPageStatement.setInt(7, Integer.parseInt(limit));
                nextPageStatement.setInt(8, Integer.parseInt(offset) + 1);

                sessionUser.setPreviousQueryType("search");
                sessionUser.setPreviousSearchParameters(title, year, director, stars);
            }
            else if (genre != null) {
                statement = conn.prepareStatement(String.format(GENRE_QUERY, sort));
                statement.setString(1, genre);
                statement.setInt(2, Integer.parseInt(limit));
                statement.setInt(3, Integer.parseInt(offset));

                nextPageStatement = conn.prepareStatement(String.format(GENRE_QUERY, sort));
                nextPageStatement.setString(1, genre);
                nextPageStatement.setInt(2, Integer.parseInt(limit));
                nextPageStatement.setInt(3, Integer.parseInt(offset) + 1);

                sessionUser.setPreviousQueryType("genre");
                sessionUser.setPreviousGenre(genre);
            }
            else if (prefix != null) {
                if (prefix.equals(" *")) {
                    statement = conn.prepareStatement(String.format(SPECIAL_TITLE_QUERY, sort));
                    nextPageStatement = conn.prepareStatement(String.format(SPECIAL_TITLE_QUERY, sort));
                    statement.setInt(1, Integer.parseInt(limit));
                    statement.setInt(2, Integer.parseInt(offset));

                    nextPageStatement.setInt(1, Integer.parseInt(limit));
                    nextPageStatement.setInt(2, Integer.parseInt(offset) + 1);
                }
                else {
                    statement = conn.prepareStatement(String.format(TITLE_QUERY, sort));
                    nextPageStatement = conn.prepareStatement(String.format(TITLE_QUERY, sort));
                    statement.setString(1, prefix + "%");
                    statement.setInt(2, Integer.parseInt(limit));
                    statement.setInt(3, Integer.parseInt(offset));

                    nextPageStatement.setString(1, prefix + "%");
                    nextPageStatement.setInt(2, Integer.parseInt(limit));
                    nextPageStatement.setInt(3, Integer.parseInt(offset) + 1);
                }

                sessionUser.setPreviousQueryType("prefix");
                sessionUser.setPreviousPrefix(prefix);
            }
            else {
                String queryType = sessionUser.getPreviousQueryType();
                if (queryType.equals("search")) {
                    statement = conn.prepareStatement(String.format(SEARCH_QUERY, sort));
                    String previousTitle = null;
                    String previousDirector = null;
                    String previousStars = null;
                    if (sessionUser.getPreviousTitle() != null) {
                        previousTitle = sessionUser.getPreviousTitle().toUpperCase();
                    }
                    if (sessionUser.getPreviousDirector() != null) {
                        previousDirector = sessionUser.getPreviousDirector().toUpperCase();
                    }
                    if (sessionUser.getPreviousStars() != null) {
                        previousStars = sessionUser.getPreviousStars().toUpperCase();
                    }
                    statement.setString(1, previousTitle);
                    statement.setString(2, "%" + previousTitle + "%");
                    statement.setString(3, sessionUser.getPreviousYear() + "%");
                    statement.setString(4, previousDirector);
                    statement.setString(5, "%" + previousDirector + "%");
                    statement.setString(6, "%" + previousStars + "%");
                    statement.setInt(7, Integer.parseInt(limit));
                    statement.setInt(8, Integer.parseInt(offset));

                    nextPageStatement = conn.prepareStatement(String.format(SEARCH_QUERY, sort));
                    nextPageStatement.setString(1, previousTitle);
                    nextPageStatement.setString(2, "%" + previousTitle + "%");
                    nextPageStatement.setString(3, sessionUser.getPreviousYear() + "%");
                    nextPageStatement.setString(4, previousDirector);
                    nextPageStatement.setString(5, "%" + previousDirector + "%");
                    nextPageStatement.setString(6, "%" + previousStars + "%");
                    nextPageStatement.setInt(7, Integer.parseInt(limit));
                    nextPageStatement.setInt(8, Integer.parseInt(offset) + 1);
                }
                else if (queryType.equals("genre")) {
                    statement = conn.prepareStatement(String.format(GENRE_QUERY, sort));
                    statement.setString(1, sessionUser.getPreviousGenre());
                    statement.setInt(2, Integer.parseInt(limit));
                    statement.setInt(3, Integer.parseInt(offset));

                    nextPageStatement = conn.prepareStatement(String.format(GENRE_QUERY, sort));
                    nextPageStatement.setString(1, sessionUser.getPreviousGenre());
                    nextPageStatement.setInt(2, Integer.parseInt(limit));
                    nextPageStatement.setInt(3, Integer.parseInt(offset) + 1);
                }
                else {
                    if (sessionUser.getPreviousPrefix().equals(" *")) {
                        statement = conn.prepareStatement(String.format(SPECIAL_TITLE_QUERY, sort));
                        nextPageStatement = conn.prepareStatement(String.format(SPECIAL_TITLE_QUERY, sort));
                        statement.setInt(1, Integer.parseInt(limit));
                        statement.setInt(2, Integer.parseInt(offset));

                        nextPageStatement.setInt(1, Integer.parseInt(limit));
                        nextPageStatement.setInt(2, Integer.parseInt(offset) + 1);
                    }
                    else {
                        statement = conn.prepareStatement(String.format(TITLE_QUERY, sort));
                        nextPageStatement = conn.prepareStatement(String.format(TITLE_QUERY, sort));
                        statement.setString(1, sessionUser.getPreviousPrefix() + "%");
                        statement.setInt(2, Integer.parseInt(limit));
                        statement.setInt(3, Integer.parseInt(offset));

                        nextPageStatement.setString(1, sessionUser.getPreviousPrefix() + "%");
                        nextPageStatement.setInt(2, Integer.parseInt(limit));
                        nextPageStatement.setInt(3, Integer.parseInt(offset) + 1);
                    }
                }
            }
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            int size = 0;
            while (rs.next()) {
                size += 1;
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_price = rs.getString("price");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_price", movie_price);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            System.out.println("Size: " + size);

            boolean isLastPage = false;
            if (size < Integer.parseInt(limit)) {
                isLastPage = true;
            }
            else {
                ResultSet nextPageRS = nextPageStatement.executeQuery();
                int nextPageSize = 0;
                while (nextPageRS.next()) {
                    nextPageSize += 1;
                }
                isLastPage = nextPageSize == 0;
                nextPageRS.close();
                System.out.println(nextPageStatement);
                System.out.println("NextPageSize: " + nextPageSize);
                System.out.println("page: " + page);
                System.out.println("limit: " + limit);
                System.out.println("offset: " + offset);
            }

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("isLastPage", isLastPage);
            jsonResponse.addProperty("pageNum", page);
            jsonResponse.add("data", jsonArray);
            rs.close();
            nextPageStatement.close();
            statement.close();
            request.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonResponse.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
