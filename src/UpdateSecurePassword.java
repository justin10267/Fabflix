import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePassword {
    public static void main(String[] args) throws Exception {
        String loginUser = "root";
        String loginPasswd = "mangobanana109";
//        String loginUser = "mytestuser";
//        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");
        String query = "SELECT id, password from customers";
        ResultSet rs = statement.executeQuery(query);
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        ArrayList<String> updateQueryList = new ArrayList<>();
        System.out.println("encrypting password (this might take a while)");
        while (rs.next()) {
            String id = rs.getString("id");
            String password = rs.getString("password");
            String encryptedPassword = passwordEncryptor.encryptPassword(password);
            String updateQuery = String.format("UPDATE customers SET password='%s' WHERE id=%s;", encryptedPassword,
                    id);
            updateQueryList.add(updateQuery);
        }
        rs.close();
        System.out.println("updating password");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("updating password completed, " + count + " rows affected");
        statement.close();
        connection.close();
        System.out.println("finished");
    }

}
