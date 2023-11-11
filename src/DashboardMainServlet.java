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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "DashboardMainServlet", urlPatterns = "/_dashboard/api/dashboardMain")
public class DashboardMainServlet extends HttpServlet {
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

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[] {"TABLE"});
            JsonArray jsonArray = new JsonArray();

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (tableName.equals("sys_config")) {
                    continue;
                }
                System.out.println(tableName);
                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("tableName", tableName);

                ResultSet columns = metaData.getColumns(null, null, tableName, null);
                JsonArray columnsArray = new JsonArray();

                while (columns.next()) {
                    JsonObject columnObject = new JsonObject();
                    columnObject.addProperty("columnName", columns.getString("COLUMN_NAME"));
                    columnObject.addProperty("columnType", columns.getString("TYPE_NAME"));
                    columnsArray.add(columnObject);
                }

                tableObject.add("columns", columnsArray);
                jsonArray.add(tableObject);
            }
            System.out.println(jsonArray);
            out.write(jsonArray.toString());
            response.setStatus(HttpServletResponse.SC_OK);
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