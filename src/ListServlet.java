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


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movies"
@WebServlet(name = "ListServlet", urlPatterns = "/api/list")
public class ListServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String title = request.getParameter("title");
            String year = request.getParameter("title");
            String director = request.getParameter("title");
            String star = request.getParameter("title");
            String genre = request.getParameter("genre");
            String prefix = request.getParameter("prefix");
            String limit = request.getParameter("limit");
            String page = request.getParameter("page");
            String sort = request.getParameter("sort");

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

            // three kinds of queries we need
                // the first is a search query which will include the parameters title, year, director, star
                // the second is browsing query for genre which will include a genre parameter
                // the third is a browsing query for title which will include a title parameter

            // TODO: Please write queries for search, browse by genre, and browse by title. Each query should use limit
            //  and offset

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
