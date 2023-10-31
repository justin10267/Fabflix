import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query =
                    "SELECT \n" +
                    "    m.id,\n" +
                    "    m.title,\n" +
                    "    m.year,\n" +
                    "    m.director,\n" +
                    "    m.price,\n" +
                    "    (SELECT \n" +
                    "            GROUP_CONCAT(g.name ORDER BY g.name ASC)\n" +
                    "        FROM\n" +
                    "            genres_in_movies gm\n" +
                    "                INNER JOIN\n" +
                    "            genres g ON gm.genreId = g.id\n" +
                    "        WHERE\n" +
                    "            gm.movieId = m.id) AS genres,\n" +
                    "    (SELECT \n" +
                    "            GROUP_CONCAT(CONCAT(s.id, ':', s.name) ORDER BY starCount DESC, s.name ASC)\n" +
                    "        FROM\n" +
                    "            stars s\n" +
                    "            JOIN (\n" +
                    "                SELECT \n" +
                    "                    sm.starId,\n" +
                    "                    COUNT(*) AS starCount\n" +
                    "                FROM\n" +
                    "                    stars_in_movies sm\n" +
                    "                GROUP BY sm.starId\n" +
                    "            ) star_counts ON s.id = star_counts.starId\n" +
                    "            JOIN stars_in_movies sim ON s.id = sim.starId\n" +
                    "        WHERE\n" +
                    "            sim.movieId = m.id\n" +
                    "        ) AS stars,\n" +
                    "    r.rating\n" +
                    "FROM\n" +
                    "    movies m\n" +
                    "        JOIN\n" +
                    "    ratings r ON m.id = r.movieId\n" +
                    "WHERE\n" +
                    "    m.id = ?;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_price = rs.getString("price");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
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
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws  IOException {
        HttpSession session = request.getSession();

        // Extract movie details from request
        String movieId = request.getParameter("movieId");
        String movieTitle = request.getParameter("movieTitle");
        double moviePrice = Double.parseDouble(request.getParameter("moviePrice"));

        // Logic to add movie to cart in session
        HashMap<String, MovieItem> cart = (HashMap<String, MovieItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        if (cart.containsKey(movieId)) {
            MovieItem existingItem = cart.get(movieId);
            existingItem.setQuantity(existingItem.getQuantity() + 1);
        } else {
            MovieItem newItem = new MovieItem(movieId, movieTitle, moviePrice, 1);
            cart.put(movieId, newItem);
        }
        session.setAttribute("cart", cart);

        // Send a response back
        response.getWriter().write("Movie added to cart");
    }

    public static class MovieItem {
        private String id;
        private String title;
        private double price;
        private int quantity;

        public MovieItem(String id, String title, double price, int quantity) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters
        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        // Setters
        public void setId(String id) {
            this.id = id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}


