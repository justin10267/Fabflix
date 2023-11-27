package edu.uci.ics.fabflixmobile.ui.login;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.main.MainActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView loginMessage;
//    private final String host = "10.0.2.2";
//    private final String port = "8080";
//    private final String domain = "Fabflix_war";
//    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private final String host = "18.218.212.43";
    private final String port = "8443";
    private final String domain = "Fabflix";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        username = binding.username;
        password = binding.password;
        loginMessage = binding.loginMessage;
        final Button loginButton = binding.login;
        loginButton.setOnClickListener(view -> login());
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        loginMessage.setText("Trying to login");
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/androidlogin",
                response -> {
                    try {
                        JSONObject responseObject = new JSONObject(response);
                        String status = responseObject.get("status").toString();
                        String responseMessage = responseObject.get("message").toString();
                        Log.d("login.success", response);
                        if (status.equals("success")) {
                            finish();
                            Intent MainPage = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(MainPage);
                        }
                        else {
                            loginMessage.setText(responseMessage);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                return params;
            }
        };
        queue.add(loginRequest);
    }
}