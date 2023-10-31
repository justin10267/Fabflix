import java.sql.PreparedStatement;
import java.util.HashMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    private final String username;
    private String limit;
    private String sort;
    private String previousQueryType;
    private HashMap<String, String> previousSearchParameters;
    private String previousGenre;
    private String previousPrefix;

    private static HashMap<String, String> sortMap = new HashMap<String, String>();
    static {
        sortMap.put("1", "title asc, rating asc");
        sortMap.put("2", "title asc, rating desc");
        sortMap.put("3", "title desc, rating asc");
        sortMap.put("4", "title desc, rating desc");
        sortMap.put("5", "rating asc, title asc");
        sortMap.put("6", "rating asc, title desc");
        sortMap.put("7", "rating desc, title asc");
        sortMap.put("8", "rating desc, title desc");
    }

    public User(String username) {
        this.username = username;
        this.limit = "25";
        this.sort = "1";
        this.previousSearchParameters = new HashMap<String, String>();
    }

    public String getUsername() {
        return username;
    }

    public String getLimit() {
        return limit;
    }

    public String getSort() {
        return sort;
    }

    public String getPreviousQueryType() { return previousQueryType; }

    public String getPreviousTitle() { return previousSearchParameters.get("title"); }

    public String getPreviousYear() { return previousSearchParameters.get("year"); }

    public String getPreviousDirector() { return previousSearchParameters.get("director"); }

    public String getPreviousStars() { return previousSearchParameters.get("stars"); }

    public String getPreviousGenre() { return previousGenre; }

    public String getPreviousPrefix() { return previousPrefix; }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public void setPreviousQueryType(String queryType) { this.previousQueryType = queryType; };

    public void setPreviousSearchParameters(String title, String year, String director, String stars) {
        previousSearchParameters.clear();
        previousSearchParameters.put("title", title);
        previousSearchParameters.put("year", year);
        previousSearchParameters.put("director", director);
        previousSearchParameters.put("stars", stars);
    }

    public void setPreviousGenre(String genre) { previousGenre = genre; }

    public void setPreviousPrefix(String prefix) { previousPrefix = prefix; }
    public static String getSortQuery(String sortNumber) { return sortMap.get(sortNumber); }
}
