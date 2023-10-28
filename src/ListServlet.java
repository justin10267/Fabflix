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
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "ListServlet", urlPatterns = "/api/list")
public class ListServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;
    private static final String GENRE_QUERY =
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
            "    (SELECT \n" +
            "            GROUP_CONCAT(g.name\n" +
            "                    ORDER BY g.name DESC)\n" +
            "        FROM\n" +
            "            genres_in_movies gm\n" +
            "                INNER JOIN\n" +
            "            genres g ON gm.genreId = g.id\n" +
            "        WHERE\n" +
            "            gm.movieId = gfm.id\n" +
            "        LIMIT 3) AS genres,\n" +
            "    (SELECT \n" +
            "            GROUP_CONCAT(CONCAT(s.id, ':', s.name)\n" +
            "                    ORDER BY s.name DESC , s.id)\n" +
            "        FROM\n" +
            "            stars_in_movies sm\n" +
            "                INNER JOIN\n" +
            "            stars s ON sm.starId = s.id\n" +
            "        WHERE\n" +
            "            sm.movieId = gfm.id\n" +
            "        LIMIT 3) AS stars,\n" +
            "    r.rating\n" +
            "FROM\n" +
            "    genredFilteredMovies gfm\n" +
            "        JOIN\n" +
            "    ratings r ON gfm.id = r.movieId\n" +
            "GROUP BY gfm.id , gfm.title , gfm.year , gfm.director , r.rating\n" +
            "ORDER BY ?\n" +
            "LIMIT ?\n" +
            "OFFSET ?;";
    private static final String TITLE_QUERY =
            "SELECT \n" +
            "    m.id,\n" +
            "    m.title,\n" +
            "    m.year,\n" +
            "    m.director,\n" +
            "    (SELECT \n" +
            "            GROUP_CONCAT(g.name\n" +
            "                    ORDER BY g.name DESC)\n" +
            "        FROM\n" +
            "            genres_in_movies gm\n" +
            "                INNER JOIN\n" +
            "            genres g ON gm.genreId = g.id\n" +
            "        WHERE\n" +
            "            gm.movieId = m.id\n" +
            "        LIMIT 3) AS genres,\n" +
            "    (SELECT \n" +
            "            GROUP_CONCAT(CONCAT(s.id, ':', s.name)\n" +
            "                    ORDER BY s.name DESC , s.id)\n" +
            "        FROM\n" +
            "            stars_in_movies sm\n" +
            "                INNER JOIN\n" +
            "            stars s ON sm.starId = s.id\n" +
            "        WHERE\n" +
            "            sm.movieId = m.id\n" +
            "        LIMIT 3) AS stars,\n" +
            "    r.rating\n" +
            "FROM\n" +
            "    movies m\n" +
            "        JOIN\n" +
            "    ratings r ON m.id = r.movieId\n" +
            "WHERE UPPER(m.title) LIKE '?%'\n" +
            "GROUP BY m.id , m.title , m.year , m.director , r.rating\n" +
            "ORDER BY ?\n" +
            "LIMIT ?\n" +
            "OFFSET ?;";
    private static final String SEARCH_QUERY = "";
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

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {

            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");

            String genre = request.getParameter("genre");

            String prefix = request.getParameter("prefix");

            String limit = request.getParameter("limit");
            String sort = request.getParameter("sort");

            String page = request.getParameter("page");


            // if there is no page, assume we are on page 1
            if (page == null) {
                page = "1";
            }

            // if there is no limit, we take from the session
            if (limit == null) {
                limit = ((User)request.getSession().getAttribute("user")).getLimit();
            }
            else {
                ((User)request.getSession().getAttribute("user")).setLimit(limit);
            }

            // if there is no sort, we take from the session
            if (sort == null) {
                sort = ((User)request.getSession().getAttribute("user")).getSort();
            }
            else {
                ((User)request.getSession().getAttribute("user")).setSort(sort);
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

            // TODO: Please write queries for search, browse by genre, and browse by title. Each query should use limit
            //  and offset

            // TODO: Store queries in User object to allow for list.html requests that have parameters page or limit and
            //  sort

            String query;

            if (title != null || year != null || director != null || star != null) {
                query = "";
            }
            else if (genre != null) {
                query = "";
            }
            else {
                query = "";
            }

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
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

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
