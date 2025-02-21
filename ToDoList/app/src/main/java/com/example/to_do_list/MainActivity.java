package com.example.to_do_list;

import static com.example.to_do_list.R.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Login Activity
public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handling Get Request
        // Check if already logged in (token exists)
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", null);

        if (accessToken != null) {
            String url = "http://10.0.2.2:5000/auth/login/"; // Your token validation endpoint

            StringRequest validationRequest = createAuthenticatedRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            //boolean isValid = jsonResponse.getBoolean("valid"); // Check the "valid" field
                            if (status.equals("success")) {
                                // Token is valid, go to TDList (Intent activated)
                                Intent intent = new Intent(this, TDList.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Token is invalid, clear it and show login
                                clearTokenAndShowLogin();
                            }
                        } catch (JSONException e) {
                            Log.e("JSON Parsing Error", e.toString());
                            clearTokenAndShowLogin(); // Handle JSON errors as invalid token
                        }
                    }, error -> {
                        Log.e("Token Validation Error", error.toString());
                        clearTokenAndShowLogin(); // Handle network errors as invalid token
                    }, null);

            if (validationRequest != null) {
                requestQueue.add(validationRequest);
            }
            // User is already logged in, go to TDList directly
            Intent intent = new Intent(MainActivity.this, TDList.class);
            startActivity(intent);
            finish(); // Finish MainActivity so the user can't go back
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.title), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private StringRequest createAuthenticatedRequest(int method, String url,
                                                     final Response.Listener<String> listener,
                                                     final Response.ErrorListener errorListener,
                                                     final JSONObject jsonParams) {

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);

        if (accessToken == null) {
            return null; // Important: Return null if no token
        }

        return new StringRequest(method, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json"); // Absolutely crucial
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                if (jsonParams != null) {
                    return jsonParams.toString().getBytes();
                } else {
                    return new byte[0]; // Handle null for GET requests
                }
            }
        };
    }

    private void clearTokenAndShowLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("access_token");
        editor.apply();
        // The activity will continue to show the login layout because there is no token.
    }

    public void viewList(View view) {// Handling Post Request
        EditText username = findViewById(R.id.input1);
        EditText password = findViewById(R.id.input2);
        String un = username.getText().toString();
        String pass = password.getText().toString();

        String postUrl = "http://10.0.2.2:5000/auth/login/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, postUrl,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");
                        String accessToken = jsonResponse.getString("access_token");

                        Log.d("AccessToken (Received): ", accessToken); // Log the received token

                        if (status.equals("success")) {
                            SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("access_token", accessToken);
                            editor.apply();

                            Log.d("AccessToken (Stored): ", sharedPreferences.getString("access_token", null)); // Log after storing

                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, TDList.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("Login Error", response);
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show(); // Use 'this'
                        }

                    } catch (JSONException e) {
                        Log.e("JSON Error", e.toString());
                        Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show(); // Use 'this'
                    }
                }, error -> {
                    Log.e("Volley Error", error.toString());
                    Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show(); // Use 'this'
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorResponse = new String(error.networkResponse.data);
                        Log.e("Server Error Response", errorResponse);
                    }
            }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", un);
                params.put("password", pass);
                return params;
            }
        };

        requestQueue.add(postRequest);
    }

    // If user hasn't registered yet
    public void toggle(View view) {
        Intent intent = new Intent(MainActivity.this, Register.class);
        startActivity(intent);
    }

}