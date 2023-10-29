<<<<<<< HEAD
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

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

        HttpSession session = request.getSession();
        ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        try (Connection conn = dataSource.getConnection()) {
            Statement statement = conn.createStatement();

            JsonArray jsonArray = new JsonArray();

            // For a more realistic scenario, cart should be an ArrayList of Movie or MovieId objects,
            // and you'd fetch movie details for each id in the cart.
            for (String movieId : cart) {
                String query = "SELECT ...";  // query to fetch details for movieId

                ResultSet rs = statement.executeQuery(query);

                if (rs.next()) {
                    // Convert result set into JsonObject and add to jsonArray
                }

                rs.close();
            }
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");  // e.g., "add", "remove", etc.
        String movieId = request.getParameter("movieId");

        HttpSession session = request.getSession();
        ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        if ("add".equals(action)) {
            cart.add(movieId);
        } else if ("remove".equals(action)) {
            cart.remove(movieId);
        }

        session.setAttribute("cart", cart);
        response.setStatus(200);
    }
}


=======
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

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

        HttpSession session = request.getSession();
        ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        try (Connection conn = dataSource.getConnection()) {
            Statement statement = conn.createStatement();

            JsonArray jsonArray = new JsonArray();

            // For a more realistic scenario, cart should be an ArrayList of Movie or MovieId objects,
            // and you'd fetch movie details for each id in the cart.
            for (String movieId : cart) {
                String query = "SELECT ...";  // query to fetch details for movieId

                ResultSet rs = statement.executeQuery(query);

                if (rs.next()) {
                    // Convert result set into JsonObject and add to jsonArray
                }

                rs.close();
            }
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");  // e.g., "add", "remove", etc.
        String movieId = request.getParameter("movieId");

        HttpSession session = request.getSession();
        ArrayList<String> cart = (ArrayList<String>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        if ("add".equals(action)) {
            cart.add(movieId);
        } else if ("remove".equals(action)) {
            cart.remove(movieId);
        }

        session.setAttribute("cart", cart);
        response.setStatus(200);
    }
}


>>>>>>> df8357c (Front end modify, project 1)
