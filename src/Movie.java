import java.util.ArrayList;
import java.util.List;

public class Movie {
    private final String id;
    private final String title;
    private final int year;
    private final String director;
    private final List<String> genres;
    private List<Star> stars;

    public Movie(String id, String title, int year, String director, List<String> genres) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
        this.stars = new ArrayList<>();
    }
    public String getId() { return id; }
    public String getTitle() {
        return title;
    }
    public int getYear() {
        return year;
    }
    public String getDirector() {
        return director;
    }
    public List<String> getGenres() { return genres; }
    public void addStar(Star star) { stars.add(star); }
    public List<Star> getStars() { return stars; }

    public String toString() {
        return  "Id:" + getId() + ", " +
                "Title:" + getTitle() + ", " +
                "Year:" + getYear() + ", " +
                "Director:" + getDirector() + ", " +
                "Genres:" + getGenres() + ", " +
                "Stars: " + getStars() + ".";
    }
}