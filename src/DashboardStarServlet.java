import jakarta.servlet.http.HttpServletRequest;
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
import java.sql.*;


@WebServlet(name = "DashboardStarServlet", urlPatterns = "/_dashboard/api/dashboardStar")
public class DashboardStarServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String starName = request.getParameter("starName");
        String birthYear = request.getParameter("birthYear");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if (starName == null) {
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("error", "Star Name is Missing");
            out.write(errorObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        else {
            try (Connection connection = dataSource.getConnection()) {
                String addMovieCall = "{call add_star(?, ?)}";
                try (CallableStatement statement = connection.prepareCall(addMovieCall)) {
                    statement.setString(1, starName);
                    if (birthYear != null && !birthYear.isEmpty()) {
                        statement.setInt(2, Integer.parseInt(birthYear));
                    } else {
                        statement.setNull(2, java.sql.Types.INTEGER);
                    }
                    ResultSet rs = statement.executeQuery();
                    JsonObject messageObject = new JsonObject();
                    rs.next();
                    String message = rs.getString("message");
                    messageObject.addProperty("message", message);
                    out.write(messageObject.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } catch (Exception e) {
                JsonObject errorObject = new JsonObject();
                errorObject.addProperty("error", e.getMessage());
                e.printStackTrace();
                out.write(errorObject.toString());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                out.close();
            }
        }
    }
}