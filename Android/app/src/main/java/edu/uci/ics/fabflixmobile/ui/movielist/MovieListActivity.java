package edu.uci.ics.fabflixmobile.ui.movielist;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.ui.main.MainActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import java.util.ArrayList;


public class MovieListActivity extends AppCompatActivity {
    private final String host = "10.0.2.2";
    private final String port = "8080";
    private final String domain = "Fabflix_war";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;
    private Button prevButton;
    private Button nextButton;
    private TextView pageNumberText;
    private boolean isLastPage;
    private int currentPage;
    private String currentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        pageNumberText = findViewById(R.id.pageNumberText);
        Intent intent = getIntent();
        currentTitle = intent.getStringExtra("movieTitle");
        currentPage = intent.getIntExtra("page", 1);
        prevButton.setEnabled(currentPage > 1);
        fetchMovieData(currentTitle, currentPage);
    }

    private void fetchMovieData(String title, int page) {
        String encodedTitle = Uri.encode(title);
        String url = baseURL + "/api/androidlist?title=" + encodedTitle + "&page=" + page;
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        isLastPage = responseObject.getBoolean("isLastPage");
                        nextButton.setEnabled(!isLastPage);
                        JSONArray responseData = responseObject.getJSONArray("data");
                        Log.d("search.success", response);
                        final ArrayList<Movie> movies = new ArrayList<>();
                        for (int i = 0; i < responseData.length(); i++) {
                            JSONObject movieJson = responseData.getJSONObject(i);
                            String movieId = movieJson.getString("movie_id");
                            String movieTitle = movieJson.getString("movie_title");
                            int movieYear = movieJson.getInt("movie_year");
                            String movieDirector = movieJson.getString("movie_director");
                            float moviePrice = Float.parseFloat(movieJson.getString("movie_price"));
                            ArrayList<String> movieGenres = parseGenres(movieJson.getString("movie_genres"));
                            ArrayList<String> movieStars = parseStars(movieJson.getString("movie_stars"));
                            float movieRating = movieJson.isNull("movie_rating") ? -1 : Float.parseFloat(movieJson.getString("movie_rating"));
                            Movie movie = new Movie(movieId, movieTitle, movieYear, movieDirector, moviePrice, movieGenres, movieStars, movieRating);
                            movies.add(movie);
                        }
                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            Intent singleMovieIntent = new Intent(MovieListActivity.this,
                                    SingleMovieActivity.class);
                            singleMovieIntent.putExtra("movieId", movie.getId());
                            startActivity(singleMovieIntent);
                        });
                    } catch (JSONException e) {
                        Log.e("search.error", "Error parsing JSON response", e);
                    }
                },
                error -> {
                    Log.e("search.error", "Error with the request: " + error.toString());
                }
        );
        queue.add(searchRequest);
        updateButtonStates();
    }

    @SuppressLint("SetTextI18n")
    private void updateButtonStates() {
        pageNumberText.setText("Page " + currentPage);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(!isLastPage);
        prevButton.setOnClickListener(v -> changePage(-1));
        nextButton.setOnClickListener(v -> changePage(1));
    }

    public static ArrayList<String> parseGenres(String genresString) {
        ArrayList<String> genres = new ArrayList<>();
        if (genresString != null && !genresString.isEmpty()) {
            String[] splitGenres = genresString.split(",");
            for (String genre : splitGenres) {
                genres.add(genre.trim());
            }
        }
        return genres;
    }

    public static ArrayList<String> parseStars(String starsString) {
        ArrayList<String> stars = new ArrayList<>();
        if (starsString != null && !starsString.isEmpty()) {
            String[] splitStars = starsString.split(",");
            for (String star : splitStars) {
                int colonIndex = star.lastIndexOf(':');
                if (colonIndex != -1) {
                    String name = star.substring(colonIndex + 1).trim();
                    stars.add(name);
                }
            }
        }
        return stars;
    }

    private void changePage(int delta) {
        currentPage += delta;
        if (currentPage < 1) {
            currentPage = 1;
        }
        fetchMovieData(currentTitle, currentPage);
    }
}

