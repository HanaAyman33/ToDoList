package com.example.to_do_list;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// Register Activity
public class Register extends AppCompatActivity {
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout2);
        // Handle Get request
        String url = "http://10.0.2.2:5000/register/"; // flask server IP
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response ->  {
                        Log.d("get request received", response);
                },
                error -> {
                Log.d("Error getting request", error.toString());
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    public void register(View view) {
        EditText username = findViewById(R.id.input1);
        EditText password = findViewById(R.id.input2);
        String un = username.getText().toString().trim(); // Trim leading/trailing whitespace
        String pass = password.getText().toString().trim();

        //Post request handling
        String url = "http://10.0.2.2:5000/register/"; // flask server IP
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response ->  {
                        try {
                            JSONObject jsonResponse = new JSONObject(response); // Parse JSON
                            String status = jsonResponse.getString("status"); // Get the "status" field
                            String message = jsonResponse.getString("message"); // Get the "message"

                            if (status.equals("success")) { // Check the status, if successful, activate intent
                                Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Register.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show(); // Show the error message from the server
                            }

                        } catch (JSONException e) {
                            Toast.makeText(Register.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                }, error ->  {
                Log.e("Volley Error", error.toString());
                Toast.makeText(Register.this, "Network error", Toast.LENGTH_SHORT).show();
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    String errorResponse = new String(error.networkResponse.data);
                    Log.e("Error Response", errorResponse); // Log server error response
                }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", un);
                params.put("password", pass);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);

    }
}