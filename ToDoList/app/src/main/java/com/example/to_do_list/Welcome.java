package com.example.to_do_list;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Welcome extends AppCompatActivity {
    private RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.first_page);

        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", null);
        Log.d("Welcome", "Access Token: " + accessToken);

        String url = "http://10.0.2.2:5000/welcome/"; // flask server IP
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response ->  {
                    Log.d("get request received", response);
                },
                error -> {
                    Log.d("Error getting request", error.toString());
                });

        requestQueue.add(stringRequest);
        if(accessToken == null){
            // Navigate to MainActivity after 3 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(Welcome.this, MainActivity.class);
                startActivity(intent);
                finish();
            }, 3000); // 3000 milliseconds = 3 seconds
        }
        else{
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(Welcome.this, TDList.class);
                startActivity(intent);
                finish();
            }, 3000);
        }

        // Apply window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.gif_image), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}