package edu.uci.ics.fabflixmobile.data.model;
import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String title;
    private final int year;
    private final String director;
    private final float price;
    private final ArrayList<String> genres;
    private final ArrayList<String> stars;
    private final float rating;

    public Movie(String id, String title, int year, String director, float price, ArrayList<String> genres, ArrayList<String> stars,
                 float rating) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.price = price;
        this.genres = genres;
        this.stars = stars;
        this.rating = rating;
    }

    public String getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getYear() { return "Year: " + year; }

    public String getDirector() { return "Director: " + director; }

    public String getPrice() { return "Price: " + price; }

    public String getGenres(boolean singleMoviePage) {
        if (singleMoviePage) {
            return String.join("\n", genres);
        }
        return "Genres: " + String.join(", ", genres);
    }

    public String getStars(boolean singleMoviePage) {
        if (singleMoviePage) {
            if (String.join(", ", stars).isEmpty()) {
                return "N/A";
            }
            return String.join("\n", stars);
        }
        if (String.join(", ", stars).isEmpty()) {
            return "Stars: N/A";
        }
        return "Stars: " + String.join(", ", stars);
    }

    public String getRating() {
        return rating == -1 ? "Rating: N/A" : "Rating: " + rating;
    }
}