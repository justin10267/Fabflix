import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/placeOrder")
public class PlaceOrderServlet extends HttpServlet {
    private static final String INSERT_QUERY =
            "INSERT INTO moviedb.sales (customerId, movieId, saleDate)\n" +
            "VALUES (?, ?, ?);";
    private static final String CREDIT_CARD_QUERY =
            "SELECT *\n" +
            "FROM creditcards\n" +
            "WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?;";
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("cardNumber");
        String expDate = request.getParameter("expDate");
        JsonObject responseJsonObject = new JsonObject();
        PrintWriter out = response.getWriter();
        if (isValidCreditCard(firstName, lastName, cardNumber, expDate)) {
            System.out.println("debug 1");
            HttpSession session = request.getSession();
            User loggedInUser = (User) session.getAttribute("user");
            String userId = loggedInUser.getUsername();
            String cartKey = "cart_" + userId;
            HashMap<String, ShoppingCartServlet.MovieItem> cart = (HashMap<String, ShoppingCartServlet.MovieItem>)
                    session.getAttribute(cartKey);
            LocalDate purchaseDate = LocalDate.now();
            System.out.println(purchaseDate);
            for (String movie: cart.keySet()) {
                System.out.println(cart.get(movie));
                recordTransaction(cart.get(movie), userId, purchaseDate, responseJsonObject);
            }
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("message", "success");
            cart.clear();
            session.setAttribute("cartKey", cart);
        }
        else {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "transaction failed");
        }
        out.write(responseJsonObject.toString());
    }

    private boolean isValidCreditCard(String firstName, String lastName, String cardNumber, String expDate) {
        if (expDate == null || !expDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        java.sql.Date expirationDate;
        try {
            expirationDate = java.sql.Date.valueOf(expDate);
        } catch (IllegalArgumentException e) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String formattedExpirationDate = sdf.format(expirationDate);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CREDIT_CARD_QUERY)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, cardNumber);
            stmt.setString(4, formattedExpirationDate);
            System.out.println(stmt);
            ResultSet rs = stmt.executeQuery();
            boolean valid = rs.next();
            System.out.println(valid);
            rs.close();
            return valid;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void recordTransaction(ShoppingCartServlet.MovieItem movieItem, String userId, LocalDate purchaseDate,
                                   JsonObject responseJsonObject) {
        try (Connection conn = dataSource.getConnection()) {
            for (int i=0; i<movieItem.getQuantity(); i++) {
                System.out.println("debug 2");
                PreparedStatement preparedStatement = conn.prepareStatement(INSERT_QUERY);
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, movieItem.getId());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedPurchaseDate = purchaseDate.format(formatter);
                preparedStatement.setString(3, formattedPurchaseDate);
                System.out.println(preparedStatement);
                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected <= 0) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "transaction failed");
                    return;
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "transaction failed");
        }
    }
}