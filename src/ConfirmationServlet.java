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
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

    private static final String CONFIRMATION_QUERY =
            "SELECT\n" +
            "\tGROUP_CONCAT(s.id) AS sales_ids,\n" +
            "    s.movieId,\n" +
            "    m.title,\n" +
            "    s.saleDate,\n" +
            "    s.customerId,\n" +
            "    COUNT(*) AS quantity,\n" +
            "    m.price AS price,\n" +
            "    SUM(m.price) AS total_price\n" +
            "FROM\n" +
            "    sales s\n" +
            "JOIN\n" +
            "    movies m ON s.movieId = m.id\n" +
            "WHERE\n" +
            "    s.customerId = ?\n" +
            "    AND s.saleDate = ?\n" +
            "GROUP BY\n" +
            "    s.movieId, s.saleDate, s.customerId, m.title";
    private static final long serialVersionUID = 3L;
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
             PreparedStatement preparedStatement = conn.prepareStatement(CONFIRMATION_QUERY)) {
            LocalDate purchaseDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedPurchaseDate = purchaseDate.format(formatter);
            HttpSession session = request.getSession();
            User loggedInUser = (User) session.getAttribute("user");
            String userId = loggedInUser.getUsername();
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, formattedPurchaseDate);
            ResultSet rs = preparedStatement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String sales_ids = rs.getString("sales_ids");
                String movie_id = rs.getString("movieId");
                String movie_title = rs.getString("title");
                String sale_date = rs.getString("saleDate");
                String customer_id = rs.getString("customerId");
                String price = rs.getString("price");
                String quantity = rs.getString("quantity");
                String total_price = rs.getString("total_price");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("sales_ids", sales_ids);
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("sale_date", sale_date);
                jsonObject.addProperty("customer_id", customer_id);
                jsonObject.addProperty("quantity", quantity);
                jsonObject.addProperty("price", price);
                jsonObject.addProperty("total_price", total_price);

                jsonArray.add(jsonObject);
            }
            rs.close();
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
