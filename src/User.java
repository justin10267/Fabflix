import java.util.HashMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    /*
    member variable, sort, maps a number to the type of sort
    1: title asc, rating asc
    2: title asc, rating desc
    3: title desc, rating asc
    4: title desc, rating desc
    5: rating asc, title asc
    6: rating asc, title desc
    7: rating desc, title asc
    8: rating desc, title desc
     */

    /*
    member variable, limit, represents the user imposed limit of number of results on a page
    limit can be the values
    {10, 25, 50, 100}
     */

    private final String username;
    private String limit;
    private String sort;

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

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public static String getSortQuery(String sortNumber) { return sortMap.get(sortNumber); }
}
