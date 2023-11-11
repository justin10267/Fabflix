import java.util.ArrayList;
import java.util.List;

public class Star {
    private final String name;
    private final int birthYear;
    private List<String> moviesStarredIn;

    public Star(String name, int birthYear) {
        this.name = name;
        this.birthYear = birthYear;
        this.moviesStarredIn = new ArrayList<>();
    }
    public String getName() { return name; }
    public int getBirthYear() {
        return birthYear;
    }
    public List<String> getMoviesStarredIn() { return moviesStarredIn; }
    public void addMovieStarredIn(String movie) { moviesStarredIn.add(movie); }

    public String toString() {
        return  "Name:" + getName() + ", " +
                "Birth Year:" + getBirthYear() + "." +
                "Movies Starred In:" + getMoviesStarredIn() + ".";
    }
}