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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "MainServlet", urlPatterns = "/api/main")
public class MainServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;
    private static final String GENRE_AND_PREFIX_QUERY =
            "SELECT result\n" +
            "FROM (\n" +
            "  SELECT CONCAT('Genre:', name) AS result, 1 as `limit`\n" +
            "  FROM genres\n" +
            "  UNION ALL\n" +
            "  SELECT DISTINCT\n" +
            "    CASE\n" +
            "      WHEN title REGEXP '^[A-Za-z]' THEN CONCAT('1_Prefix:', UPPER(LEFT(title, 1)))\n" +
            "      WHEN title REGEXP '^[0-9]' THEN CONCAT('2_Prefix:', LEFT(title, 1))\n" +
            "      ELSE '3_Prefix: *'\n" +
            "    END AS result, -1 as `limit`\n" +
            "  FROM movies\n" +
            ") AS combined_result\n" +
            "ORDER BY `limit`, result ASC;";
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
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery(GENRE_AND_PREFIX_QUERY)) {
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String genre_prefix_item = rs.getString("result");
                String[] parts = genre_prefix_item.split(":");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("type", parts[0]);
                jsonObject.addProperty("value", parts[1]);
                jsonArray.add(jsonObject);
            }
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