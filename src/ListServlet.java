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
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "ListServlet", urlPatterns = "/api/list")
public class ListServlet extends HttpServlet {
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
        long startTimeTS = System.nanoTime();
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String stars = request.getParameter("stars");
            String genre = request.getParameter("genre");
            String prefix = request.getParameter("prefix");
            String limit = request.getParameter("limit");
            String sort = request.getParameter("sort");
            String page = request.getParameter("page");
            User sessionUser = (User)(request.getSession().getAttribute("user"));
            if (page == null) {
                page = "1";
            }
            if (limit == null || sort == null) {
                limit = sessionUser.getLimit();
                sort = sessionUser.getSort();
            }
            else {
                sessionUser.setLimit(limit);
                sessionUser.setSort(sort);
            }
            int queryLimit = Integer.parseInt(limit) + 1;
            sort = User.getSortQuery(sort);
            String offset = Integer.toString((Integer.parseInt(page) - 1 ) * Integer.parseInt(limit));
            long startTimeTJ = System.nanoTime();
            PreparedStatement statement = createAppropriateStatement(conn, sessionUser, sort, title, year, director,
                    stars, genre, prefix, queryLimit, offset);
            System.out.println(statement);
            ResultSet rs = statement.executeQuery();
            long endTimeTJ = System.nanoTime();
            String elapsedTimeTJ = Long.toString(endTimeTJ - startTimeTJ);

            JsonArray jsonArray = processResultSet(rs, queryLimit, out, response);
            boolean isLastPage = jsonArray.size() < queryLimit;
            if (jsonArray.size() == queryLimit) {
                jsonArray.remove(jsonArray.size() - 1);
            }
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("isLastPage", isLastPage);
            jsonResponse.addProperty("pageNum", page);
            jsonResponse.add("data", jsonArray);
            rs.close();
            statement.close();
            out.write(jsonResponse.toString());
            response.setStatus(200);
            long endTimeTS = System.nanoTime();
            String elapsedTimeTS = Long.toString(endTimeTS - startTimeTS);
            logElapsedTime(request, String.format("TJ:%s,TS:%s", elapsedTimeTJ, elapsedTimeTS));
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private PreparedStatement createAppropriateStatement(Connection conn, User sessionUser, String sort, String title,
                                                         String year, String director, String stars, String genre,
                                                         String prefix, int queryLimit, String offset) throws Exception
    {
        PreparedStatement statement;
        if (title != null || year != null || director != null || stars != null) {
            statement = createSearchStatement(conn, sort, title, year, director, stars, queryLimit, offset);
            sessionUser.setPreviousQueryType("search");
            sessionUser.setPreviousSearchParameters(title, year, director, stars);
        }
        else if (genre != null) {
            statement = createGenreStatement(conn, sort, genre, queryLimit, offset);
            sessionUser.setPreviousQueryType("genre");
            sessionUser.setPreviousGenre(genre);
        }
        else if (prefix != null) {
            statement = createTitleStatement(conn, sort, prefix, queryLimit, offset);
            sessionUser.setPreviousQueryType("prefix");
            sessionUser.setPreviousPrefix(prefix);
        }
        else {
            String queryType = sessionUser.getPreviousQueryType();
            if (queryType.equals("search")) {
                statement = createSearchStatement(conn, sort, sessionUser.getPreviousTitle(),
                        sessionUser.getPreviousYear(), sessionUser.getPreviousDirector().toUpperCase(),
                        sessionUser.getPreviousStars().toUpperCase(), queryLimit, offset);
            }
            else if (queryType.equals("genre")) {
                statement = createGenreStatement(conn, sort, sessionUser.getPreviousGenre(), queryLimit, offset);
            }
            else {
                statement = createTitleStatement(conn, sort, sessionUser.getPreviousPrefix(), queryLimit, offset);
            }
        }
        return statement;
    }

    private JsonArray processResultSet(ResultSet rs, int limit, PrintWriter out, HttpServletResponse response) throws
            Exception {
        JsonArray jsonArray = new JsonArray();
        int count = 0;
        while (rs.next() && count < limit) {
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

    private PreparedStatement createSearchStatement(Connection conn, String sort, String title, String year,
                                                    String director, String stars, int limit, String offset)
            throws Exception {
        PreparedStatement statement = conn.prepareStatement(String.format(QueryStrings.SEARCH_QUERY, sort));
        String searchString = null;
        if (title != null) {
            String[] tokenizedTitle = title.split(" ");
            StringBuilder searchStringBuilder = new StringBuilder();
            for (String token: tokenizedTitle)
                searchStringBuilder.append("+").append(token).append("* ");
            searchString = searchStringBuilder.toString().trim();
            System.out.println(searchString);
        }
        if (director != null) {
            director = director.toUpperCase();
        }
        if (stars != null) {
            stars = stars.toUpperCase();
        }
        statement.setString(1, searchString);
        statement.setString(2, year + "%");
        statement.setString(3, "%" + director + "%");
        statement.setString(4, "%" + stars + "%");
        statement.setInt(5, limit);
        statement.setInt(6, Integer.parseInt(offset));
        return statement;
    }

    private PreparedStatement createGenreStatement(Connection conn, String sort, String genre, int limit, String offset)
            throws Exception {
        PreparedStatement statement = conn.prepareStatement(String.format(QueryStrings.GENRE_QUERY, sort));
        statement.setString(1, genre);
        statement.setInt(2, limit);
        statement.setInt(3, Integer.parseInt(offset));
        return statement;
    }

    private PreparedStatement createTitleStatement(Connection conn, String sort, String prefix, int limit,
                                                   String offset) throws Exception {
        PreparedStatement statement;
        if (prefix.equals(" *")) {
            statement = conn.prepareStatement(String.format(QueryStrings.SPECIAL_TITLE_QUERY, sort));
            statement.setInt(1, limit);
            statement.setInt(2, Integer.parseInt(offset));
        }
        else {
            statement = conn.prepareStatement(String.format(QueryStrings.TITLE_QUERY, sort));
            statement.setString(1, prefix + "%");
            statement.setInt(2, limit);
            statement.setInt(3, Integer.parseInt(offset));
        }
        return statement;
    }

    private void logElapsedTime(HttpServletRequest request, String logMessage) throws IOException {
        System.out.println("entered function");
        String contextPath = request.getServletContext().getRealPath("/");
        String logFilePath = contextPath + "\\timeLog.txt";
        System.out.println(logFilePath);
        File logFile = new File(logFilePath);
        System.out.println(logFile.getAbsolutePath());
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
