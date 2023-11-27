package edu.uci.ics.fabflixmobile.ui.main;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import edu.uci.ics.fabflixmobile.databinding.ActivityMainBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;

public class MainActivity extends AppCompatActivity {
    private EditText searchBox;
    private TextView searchMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        searchBox = binding.searchBox;
        searchMessage = binding.searchMessage;
        final Button searchButton = binding.searchButton;
        searchButton.setOnClickListener(view -> search());
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        String title = searchBox.getText().toString();
        if (!title.isEmpty()) {
            Intent movieListIntent = new Intent(MainActivity.this, MovieListActivity.class);
            movieListIntent.putExtra("movieTitle", title);
            movieListIntent.putExtra("page", 1);
            startActivity(movieListIntent);
        } else {
            searchMessage.setText("Please enter a title to search");
        }
    }
}