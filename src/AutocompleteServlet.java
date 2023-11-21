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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;


@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
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
        try (Connection conn = dataSource.getConnection()) {
            String title = request.getParameter("title");
            PreparedStatement statement = conn.prepareStatement(QueryStrings.AUTOCOMPLETE_QUERY);
            String[] tokenizedTitle = title.split(" ");
            StringBuilder searchStringBuilder = new StringBuilder();
            for (String token: tokenizedTitle)
                searchStringBuilder.append("+").append(token).append("* ");
            String searchString = searchStringBuilder.toString().trim();

            System.out.println(title);
            System.out.println(Arrays.toString(tokenizedTitle));
            System.out.println(searchString);

            statement.setString(1, searchString);
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("value", movie_title);
                jsonObject.addProperty("data", movie_id);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();
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