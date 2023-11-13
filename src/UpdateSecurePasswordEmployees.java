import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePasswordEmployees {
    public static void main(String[] args) throws Exception {
//        String loginUser = "root";
//        String loginPasswd = "mangobanana109";
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering employees table schema completed, " + alterResult + " rows affected");
        String query = "SELECT email, password from employees";
        ResultSet rs = statement.executeQuery(query);
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        ArrayList<String> updateQueryList = new ArrayList<>();
        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            String email = rs.getString("email");
            String password = rs.getString("password");
            String encryptedPassword = passwordEncryptor.encryptPassword(password);
            String updateQuery = String.format("UPDATE employees SET password='%s' WHERE email='%s';", encryptedPassword,
                    email);
            updateQueryList.add(updateQuery);
        }
        rs.close();
        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            System.out.println(updateQuery);
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");
        statement.close();
        connection.close();
        System.out.println("finished");

    }

}