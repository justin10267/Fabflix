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
            "        JOIN\n" +
            "    ratings r ON m.id = r.movieId\n" +
            "WHERE \n" +
            "(SOUNDEX(title) = SOUNDEX(?) OR title LIKE ?)\n" +
            "AND (SOUNDEX(year) = SOUNDEX(?) OR year LIKE ?)\n" +
            "AND (SOUNDEX(director) = SOUNDEX(?) OR director LIKE ?)\n" +
            "GROUP BY m.id , m.title , m.year , m.director , r.rating\n" +
            "HAVING stars LIKE ?" +
            "ORDER BY %s\n" +
            "LIMIT ?\n" +
            "OFFSET ?;";
    private static String GENRE_QUERY =
            "WITH\n" +
            "\tgenredFilteredMovies AS \n" +
            "\t(\n" +
            "\t\tSELECT m.id, m.title, m.year, m.director, g.name as genre\n" +
            "\t\tFROM movies as m JOIN genres_in_movies as gm ON m.id = gm.movieId JOIN genres as g ON gm.genreId = g.id\n" +
            "\t\tWHERE g.name = ?\n" +
            "    )\n" +
            "SELECT \n" +
            "    gfm.id,\n" +
            "    gfm.title,\n" +
            "    gfm.year,\n" +
            "    gfm.director,\n" +
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
            "        JOIN\n" +
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
            "        JOIN\n" +
            "    ratings r ON m.id = r.movieId\n" +
            "WHERE UPPER(m.title) LIKE ?\n" +
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
            System.out.println("debug 1");
            System.out.println(prefix);

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

            // convert sort number into a sort query string
            sort = User.getSortQuery(sort);
            // formula for offset used in query for pages
            // assume limit is 50
            // offset = (page - 1) * limit

            String offset = Integer.toString((Integer.parseInt(page) - 1 ) * Integer.parseInt(limit));

            // five kinds of queries we need
                // the first is a search query which will include the parameters title, year, director, star
                // the second is browsing query for genre which will include a genre parameter
                // the third is a browsing query for title which will include a title parameter
                // the fourth is a query that reloads the previous user query with an updated limit and sort
                // the fifth is a query that reloads the previous user query with an updated page

            // TODO: Store queries in User object to allow for list.html requests that have parameters page or limit and
            //  sort
            PreparedStatement statement;
            if (title != null || year != null || director != null || stars != null) {
                statement = conn.prepareStatement(String.format(SEARCH_QUERY, sort));
                statement.setString(1, title);
                statement.setString(2, title + "%");
                statement.setString(3, year);
                statement.setString(4, year + "%");
                statement.setString(5, director);
                statement.setString(6, director + "%");
                statement.setString(7, "%" + stars + "%");
                statement.setInt(8, Integer.parseInt(limit));
                statement.setInt(9, Integer.parseInt(offset));

                sessionUser.setPreviousQueryType("search");
                sessionUser.setPreviousSearchParameters(title, year, director, stars);
            }
            else if (genre != null) {
                statement = conn.prepareStatement(String.format(GENRE_QUERY, sort));
                statement.setString(1, genre);
                statement.setInt(2, Integer.parseInt(limit));
                statement.setInt(3, Integer.parseInt(offset));

                sessionUser.setPreviousQueryType("genre");
                sessionUser.setPreviousGenre(genre);
            }
            else if (prefix != null) {
                statement = conn.prepareStatement(String.format(TITLE_QUERY, sort));
                statement.setString(1, prefix + "%");
                statement.setInt(2, Integer.parseInt(limit));
                statement.setInt(3, Integer.parseInt(offset));

                sessionUser.setPreviousQueryType("prefix");
                sessionUser.setPreviousPrefix(prefix);
            }
            else {
                System.out.println("Debug 1");
                String queryType = sessionUser.getPreviousQueryType();
                if (queryType.equals("search")) {
                    statement = conn.prepareStatement(String.format(SEARCH_QUERY, sort));
                    statement.setString(1, sessionUser.getPreviousTitle());
                    statement.setString(2, sessionUser.getPreviousTitle() + "%");
                    statement.setString(3, sessionUser.getPreviousYear());
                    statement.setString(4, sessionUser.getPreviousYear() + "%");
                    statement.setString(5, sessionUser.getPreviousDirector());
                    statement.setString(6, sessionUser.getPreviousDirector() + "%");
                    statement.setString(7, "%" + sessionUser.getPreviousStars() + "%");
                    statement.setInt(8, Integer.parseInt(limit));
                    statement.setInt(9, Integer.parseInt(offset));
                }
                else if (queryType.equals("genre")) {
                    System.out.println("Debug 2");
                    statement = conn.prepareStatement(String.format(GENRE_QUERY, sort));
                    statement.setString(1, sessionUser.getPreviousGenre());
                    statement.setInt(2, Integer.parseInt(limit));
                    statement.setInt(3, Integer.parseInt(offset));
                }
                else {
                    statement = conn.prepareStatement(String.format(TITLE_QUERY, sort));
                    statement.setString(1, sessionUser.getPreviousPrefix() + "%");
                    statement.setInt(2, Integer.parseInt(limit));
                    statement.setInt(3, Integer.parseInt(offset));
                }
            }
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
            request.getServletContext().log("getting " + jsonArray.size() + " results");
            out.write(jsonArray.toString());
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
