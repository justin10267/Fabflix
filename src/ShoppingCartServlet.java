import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // For GET request (Fetching cart data)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

        if (cart == null) {
            cart = new HashMap<>();
        }

        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();

            for (String movieId : cart.keySet()) {
                String query = "SELECT \n" +
                        "    id as movie_id,\n" +
                        "    title,\n" +
                        "    ROUND(5 + (RAND() * 20), 2) as price   -- This generates a random price between $5 to $25 with 2 decimal points.\n" +
                        "FROM \n" +
                        "    movies\n" +
                        "WHERE \n" +
                        "    id = ?;  -- This is the movieId placeholder\n";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, movieId);

                ResultSet rs = ps.executeQuery();
                float totalPrice = 0.0f;
                while (rs.next()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("movie_id", rs.getString("movie_id"));
                    jsonObject.addProperty("movie_title", rs.getString("title"));
                    float price = rs.getFloat("price");
                    jsonObject.addProperty("price", price);
                    int quantity = cart.get(movieId);
                    jsonObject.addProperty("quantity", quantity);
                    jsonObject.addProperty("total_for_movie", price * quantity);

                    totalPrice += (price * quantity);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                ps.close();

                JsonObject totalObject = new JsonObject();
                totalObject.addProperty("total_price", totalPrice);
                jsonArray.add(totalObject);
            }

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

    // For POST request (Adding/Removing from cart)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        String movieId = request.getParameter("movieId");

        HttpSession session = request.getSession();
        HashMap<String, Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");

        if (cart == null) {
            cart = new HashMap<>();
        }

        if ("add".equals(action)) {
            cart.put(movieId, cart.getOrDefault(movieId, 0) + 1);
        } else if ("remove".equals(action)) {
            int currentQty = cart.getOrDefault(movieId, 0);
            if (currentQty > 1) {
                cart.put(movieId, currentQty - 1);
            } else {
                cart.remove(movieId);
            }
        }

        session.setAttribute("cart", cart);

        // Send a JSON response
        JsonObject jsonResponse = new JsonObject();

        if ("add".equals(action)) {
            jsonResponse.addProperty("message", "Movie added to cart.");
        } else if ("remove".equals(action)) {
            jsonResponse.addProperty("message", "Movie removed from cart.");
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
        response.setStatus(200);
    }
}
