import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();


        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */

        try (Connection conn = dataSource.getConnection()) {
            // Construct a query with parameter represented by "?"
            String query = "SELECT *\n" +
                    "FROM customers\n" +
                    "WHERE ? = email and ? = password";

            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, username);
            statement.setString(2, password);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                System.out.println("ResultSet in empty in Java");
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "Login failed");
            }
            else {
                System.out.println(rs.getString("firstName") + " " + rs.getString("lastName"));
                request.getSession().setAttribute("user", new User(rs.getString("id")));
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }

        } catch (Exception e) {
            // Write error message JSON object to output
            responseJsonObject.addProperty("errorMessage", e.getMessage());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.write(responseJsonObject.toString());
            out.close();
        }
    }
}
