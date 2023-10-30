import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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


@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/placeOrder")
public class PlaceOrderServlet extends HttpServlet {

    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            // Obtain our environment naming context
            InitialContext initialContext = new InitialContext();
            Context envContext = (Context) initialContext.lookup("java:/comp/env");
            // Look up our data source by the name we gave it when we created it
            dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("cardNumber");
        String expDate = request.getParameter("expDate");

        try (PrintWriter out = response.getWriter()) { // Using try-with-resources to ensure PrintWriter is closed
            // Validate credit card info against "credit cards" table
            if (isValidCreditCard(firstName, lastName, cardNumber, expDate)) {
                // Record transaction in "sales" table
                recordTransaction();

                // Send success response
                out.write("{\"success\": true}");
            } else {
                // Send error response
                out.write("{\"success\": false, \"message\": \"Invalid payment details. Please try again.\"}");
            }
        } catch (Exception e) {
            // If an error occurs, log it and return a generic error message to the user.
            e.printStackTrace(); // Log the error (You might want to use a logger instead)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"success\": false, \"message\": \"An error occurred. Please try again later.\"}");
            }
        }
    }

    private boolean isValidCreditCard(String firstName, String lastName, String cardNumber, String expDate) {
        // Check if the date string is in the correct format
        if (expDate == null || !expDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }

        // Convert the string to a java.sql.Date object
        java.sql.Date expirationDate;
        try {
            expirationDate = java.sql.Date.valueOf(expDate);
        } catch (IllegalArgumentException e) {
            // The date string format is invalid
            return false;
        }

        // Convert the date format to match your database format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String formattedExpirationDate = sdf.format(expirationDate);

        // Query the database
        String query = "SELECT id FROM creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, cardNumber);
            stmt.setString(4, formattedExpirationDate);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Found a matching record
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void recordTransaction() {
        // Implement logic to record the transaction in the "sales" table
        // Placeholder for now
    }
}