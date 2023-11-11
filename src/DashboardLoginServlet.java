import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "DashboardLoginServlet", urlPatterns = "/_dashboard/api/dashboardlogin")
public class DashboardLoginServlet extends HttpServlet {
    private DataSource dataSource;

    private static String verifyCredentials(String email, String password, Connection connection) {
        String query = "SELECT * from employees where email=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                if (success) {
                    String fullName = rs.getString("fullname");
                    rs.close();
                    preparedStatement.close();
                    return fullName;
                } else {
                    rs.close();
                    preparedStatement.close();
                    return "Incorrect Password";
                }
            } else {
                rs.close();
                preparedStatement.close();
                return "Incorrect Username";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
//        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        JsonObject responseJsonObject = new JsonObject();
//        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
//        try {
//            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//        } catch (Exception e) {
//            System.out.println("Recaptcha Failed");
//            responseJsonObject.addProperty("status", "fail");
//            request.getServletContext().log("Recaptcha Failed");
//            responseJsonObject.addProperty("message", "Recaptcha Verification Error: Please do Recaptcha");
//            System.out.println(responseJsonObject);
//            out.write(responseJsonObject.toString());
//            out.close();
//        }

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try (Connection conn = dataSource.getConnection()) {
            String result = verifyCredentials(username, password, conn);
            if (result.equals("Incorrect Password") || result.equals("Incorrect Username") || result.equals("Error")) {
                System.out.println(result);
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", result);
            }
            else {
                System.out.println("Full Name: " + result);
                request.getSession().setAttribute("admin", new Admin(username));
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }

        } catch (Exception e) {
            responseJsonObject.addProperty("errorMessage", e.getMessage());
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            System.out.println(responseJsonObject);
            out.write(responseJsonObject.toString());
            response.setStatus(200);
            out.close();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}