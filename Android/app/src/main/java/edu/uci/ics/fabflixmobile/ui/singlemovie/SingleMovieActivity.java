package edu.uci.ics.fabflixmobile.ui.singlemovie;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListViewAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity.parseGenres;
import static edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity.parseStars;

public class SingleMovieActivity extends AppCompatActivity {
    private TextView movieTitle;
    private TextView movieYear;
    private TextView movieDirector;
    private TextView movieRating;
    private TextView movieGenres;
    private TextView movieStars;
    private final String host = "10.0.2.2";
    private final String port = "8080";
    private final String domain = "Fabflix_war";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlemovie);
        movieTitle = findViewById(R.id.movieTitleTextView);
        movieYear = findViewById(R.id.movieYearTextView);
        movieDirector = findViewById(R.id.movieDirectorTextView);
        movieRating = findViewById(R.id.movieRatingTextView);
        movieGenres = findViewById(R.id.movieGenresTextView);
        movieStars = findViewById(R.id.movieStarsTextView);
        Intent intent = getIntent();
        String movieId = intent.getStringExtra("movieId");
        String encodedId = Uri.encode(movieId);
        String url = baseURL + "/api/single-movie?id=" + encodedId;;
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        StringRequest movieRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject movieJson = jsonArray.getJSONObject(0);
                        System.out.println(jsonArray);
                        String movieTitleData = movieJson.getString("movie_title");
                        int movieYearData = movieJson.getInt("movie_year");
                        String movieDirectorData = movieJson.getString("movie_director");
                        float moviePriceData = Float.parseFloat(movieJson.getString("movie_price"));
                        ArrayList<String> movieGenresData = parseGenres(movieJson.getString("movie_genres"));
                        ArrayList<String> movieStarsData = parseStars(movieJson.getString("movie_stars"));
                        float movieRatingData = movieJson.isNull("movie_rating") ? -1 : Float.parseFloat(movieJson.getString("movie_rating"));
                        Movie movie = new Movie(movieId, movieTitleData, movieYearData, movieDirectorData, moviePriceData, movieGenresData, movieStarsData, movieRatingData);
                        movieTitle.setText(movie.getTitle());
                        movieYear.setText(movie.getYear());
                        movieDirector.setText(movie.getDirector());
                        movieRating.setText(movie.getRating());
                        movieGenres.setText(movie.getGenres(true));
                        movieStars.setText(movie.getStars(true));
                    } catch (JSONException e) {
                        Log.e("search.error", "Error parsing JSON response", e);
                    }
                },
                error -> {
                    Log.e("search.error", "Error with the request: " + error.toString());
                }
        );
        queue.add(movieRequest);
    }
}