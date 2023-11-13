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


@WebServlet(name = "DashboardMovieServlet", urlPatterns = "/_dashboard/api/dashboardMovie")
public class DashboardMovieServlet extends HttpServlet {
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
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String birthYear = request.getParameter("birthYear");
        String genre = request.getParameter("genre");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        if (title == null || year == null || director == null || starName == null || genre == null) {
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("error", "One or more parameters are missing");
            out.write(errorObject.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        else {
            try (Connection connection = dataSource.getConnection()) {
                String addMovieCall = "{call add_movie(?, ?, ?, ?, ?, ?)}";
                try (CallableStatement statement = connection.prepareCall(addMovieCall)) {
                    statement.setString(1, title);
                    statement.setInt(2, Integer.parseInt(year));
                    statement.setString(3, director);
                    statement.setString(4, starName);
                    if (birthYear != null && !birthYear.isEmpty()) {
                        statement.setInt(5, Integer.parseInt(birthYear));
                    } else {
                        statement.setNull(5, java.sql.Types.INTEGER);
                    }
                    statement.setString(6, genre);
                    System.out.println(statement.toString());
                    ResultSet rs = statement.executeQuery();
                    JsonObject messageObject = new JsonObject();
                    boolean hasResultSet = statement.getMoreResults();
                    while (hasResultSet) {
                        rs = statement.getResultSet();
                        if (rs.next()) {
                            String message = rs.getString("message");
                            messageObject.addProperty("message", message);
                        }
                        hasResultSet = statement.getMoreResults();
                    }
                    out.write(messageObject.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } catch (Exception e) {
                JsonObject errorObject = new JsonObject();
                errorObject.addProperty("error", e.getMessage());
                out.write(errorObject.toString());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
                out.close();
            }
        }
    }
}