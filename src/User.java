/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    /*
    member variable, sort, maps a number to the type of sort
    1: title, rating asc
    2: title, rating desc
    3: rating, title asc
    4: rating, title desc
     */

    /*
    member variable, limit, represents the user imposed limit of number of results on a page
    limit can be the values
    {10, 25, 50, 100}
     */

    private final String username;
    private String limit;
    private String sort;

    public User(String username) {
        this.username = username;
        this.limit = "100";
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
}
