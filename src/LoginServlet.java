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

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    private static String verifyCredentials(String email, String password, Connection connection) {
        String query = "SELECT * from customers where email=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                if (success) {
                    String userId = rs.getString("id");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    rs.close();
                    preparedStatement.close();

                    return userId + "," + firstName + "," + lastName;
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
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String bypassRecaptcha = request.getParameter("bypass");
        JsonObject responseJsonObject = new JsonObject();
        if (!bypassRecaptcha.equals("true")) {
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                System.out.println("Recaptcha Failed");
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Recaptcha Failed");
                responseJsonObject.addProperty("message", "Recaptcha Verification Error: Please do Recaptcha");
                response.setHeader("loggedIn", "CaptchaFailed");
                System.out.println(responseJsonObject);
                out.write(responseJsonObject.toString());
                out.close();
            }
        }
        try (Connection conn = dataSource.getConnection()) {
            String result = verifyCredentials(username, password, conn);
            if (result.equals("Incorrect Password") || result.equals("Incorrect Username") || result.equals("Error")) {
                System.out.println(result);
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", result);
                response.setHeader("loggedIn", "False");
            }
            else {
                String[] userInformation = result.split(",");
                System.out.println(userInformation[1] + " " + userInformation[2]);
                request.getSession().setAttribute("user", new User(userInformation[0]));
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                response.setHeader("loggedIn", "True");
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
