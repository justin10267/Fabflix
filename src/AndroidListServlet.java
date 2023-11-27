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


@WebServlet(name = "AndroidListServlet", urlPatterns = "/api/androidlist")
public class AndroidListServlet extends HttpServlet {
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
            String page = request.getParameter("page");
            System.out.println("title: " + title);
            System.out.println("page: " + page);
            if (page == null) {
                page = "1";
            }
            String offset = Integer.toString((Integer.parseInt(page) - 1 ) * 10);
            PreparedStatement statement = createSearchStatement(conn, title, offset);
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = processResultSet(rs, out, response);
            boolean isLastPage = jsonArray.size() < 11;
            if (jsonArray.size() == 11) {
                jsonArray.remove(jsonArray.size() - 1);
            }
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("isLastPage", isLastPage);
            jsonResponse.add("data", jsonArray);
            rs.close();
            statement.close();
            out.write(jsonResponse.toString());
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

    private JsonArray processResultSet(ResultSet rs, PrintWriter out, HttpServletResponse response) throws
            Exception {
        JsonArray jsonArray = new JsonArray();
        int count = 0;
        while (rs.next() && count < 11) {
            String movie_id = rs.getString("id");
            String movie_title = rs.getString("title");
            String movie_year = rs.getString("year");
            String movie_director = rs.getString("director");
            String movie_price = rs.getString("price");
            String movie_genres = rs.getString("genres");
            String movie_stars = rs.getString("stars");
            String movie_rating = rs.getString("rating");

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
            count++;
        }
        return jsonArray;
    }

    private PreparedStatement createSearchStatement(Connection conn, String title, String offset)
            throws Exception {
        PreparedStatement statement = conn.prepareStatement(QueryStrings.ANDROID_SEARCH_QUERY);
        String searchString = null;
        if (title != null) {
            String[] tokenizedTitle = title.split(" ");
            StringBuilder searchStringBuilder = new StringBuilder();
            for (String token: tokenizedTitle)
                searchStringBuilder.append("+").append(token).append("* ");
            searchString = searchStringBuilder.toString().trim();
            System.out.println(searchString);
        }
        statement.setString(1, searchString);
        statement.setInt(2, Integer.parseInt(offset));
        return statement;
    }
}