//package com.example.to_do_list;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.Toast;
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import com.android.volley.AuthFailureError;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import org.json.JSONException;
//import org.json.JSONObject;
//import java.util.HashMap;
//import java.util.Map;
//
//public class MainActivity extends AppCompatActivity {
//    private RequestQueue requestQueue;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestQueue = Volley.newRequestQueue(this);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//
//        // Check if already logged in (token exists)
//        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
//        String accessToken = sharedPreferences.getString("access_token", null);
//        Log.d("MainActivity", "Access Token: " + accessToken);
//
//        if (accessToken != null) {
//            // Validate token before proceeding
//            String url = "http://10.0.2.2:5000/auth/login/";
//            StringRequest validationRequest = createAuthenticatedRequest(Request.Method.GET, url,
//                    response -> {
//                        try {
//                            Log.d("MainActivity", "Token Validation Response: " + response);
//                            JSONObject jsonResponse = new JSONObject(response);
//                            String status = jsonResponse.getString("status");
//                            if (status.equals("success")) {
//                                // Token is valid, go to TDList
//                                Intent intent = new Intent(MainActivity.this, TDList.class);
//                                startActivity(intent);
//                                finish();
//                            } else {
//                                // Token is invalid, clear it and show login
//                                clearTokenAndShowLogin();
//                            }
//                        } catch (JSONException e) {
//                            Log.e("MainActivity", "JSON Parsing Error: " + e.toString());
//                            clearTokenAndShowLogin();
//                        }
//                    }, error -> {
//                        Log.e("MainActivity", "Token Validation Error: " + error.toString());
//                        if (error.networkResponse != null) {
//                            Log.e("MainActivity", "Status Code: " + error.networkResponse.statusCode);
//                            Log.e("MainActivity", "Error Response: " + new String(error.networkResponse.data != null ? error.networkResponse.data : new byte[0]));
//                        }
//                        clearTokenAndShowLogin();
//                    }, null);
//
//            if (validationRequest != null) {
//                requestQueue.add(validationRequest);
//            } else {
//                Log.e("MainActivity", "Validation request is null, clearing token");
//                clearTokenAndShowLogin();
//            }
//        } else {
//            Log.d("MainActivity", "No access token found, showing login screen");
//            // No token, stay on login screen
//        }
//        // Apply window insets
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.input1), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//    }
//
//    private StringRequest createAuthenticatedRequest(int method, String url,
//                                                     final Response.Listener<String> listener,
//                                                     final Response.ErrorListener errorListener,
//                                                     final JSONObject jsonParams) {
//        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
//        final String accessToken = sharedPreferences.getString("access_token", null);
//
//        if (accessToken == null) {
//            Log.w("MainActivity", "No access token for authenticated request");
//            return null;
//        }
//
//        return new StringRequest(method, url, listener, errorListener) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + accessToken);
//                headers.put("Content-Type", "application/json");
//                Log.d("MainActivity", "Request Headers: " + headers.toString());
//                return headers;
//            }
//
//            @Override
//            public byte[] getBody() throws AuthFailureError {
//                if (jsonParams != null) {
//                    return jsonParams.toString().getBytes();
//                } else {
//                    return new byte[0];
//                }
//            }
//        };
//    }
//
//    private void clearTokenAndShowLogin() {
//        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove("access_token");
//        editor.apply();
//        Log.d("MainActivity", "Token cleared, showing login screen");
//        // Stay on login screen (no need to setContentView again)
//    }
//
//    public void viewList(View view) {
//        EditText username = findViewById(R.id.input1);
//        EditText password = findViewById(R.id.input2);
//        String un = username.getText().toString().trim();
//        String pass = password.getText().toString().trim();
//
//        if (un.isEmpty() || pass.isEmpty()) {
//            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String postUrl = "http://10.0.2.2:5000/auth/login/";
//        StringRequest postRequest = new StringRequest(Request.Method.POST, postUrl,
//                response -> {
//                    try {
//                        Log.d("MainActivity", "Login Response: " + response);
//                        JSONObject jsonResponse = new JSONObject(response);
//                        String status = jsonResponse.getString("status");
//                        String message = jsonResponse.getString("message");
//
//                        if (status.equals("success")) {
//                            String accessToken = jsonResponse.getString("access_token");
//                            SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            editor.putString("access_token", accessToken);
//                            editor.apply();
//                            Log.d("MainActivity", "Access Token Stored: " + accessToken);
//
//                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//                            Intent intent = new Intent(this, TDList.class);
//                            startActivity(intent);
//                            finish();
//                        } else {
//                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//                        }
//                    } catch (JSONException e) {
//                        Log.e("MainActivity", "JSON Error: " + e.toString());
//                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
//                    }
//                }, error -> {
//            Log.e("MainActivity", "Login Volley Error: " + error.toString());
//            if (error.networkResponse != null && error.networkResponse.data != null) {
//                Log.e("MainActivity", "Error Response: " + new String(error.networkResponse.data));
//            }
//            Toast.makeText(this, "Login Failed: Network error", Toast.LENGTH_SHORT).show();
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("username", un);
//                params.put("password", pass);
//                return params;
//            }
//        };
//
//        requestQueue.add(postRequest);
//    }
//
//    public void toggle(View view) {
//        Intent intent = new Intent(MainActivity.this, Register.class);
//        startActivity(intent);
//    }
//}

package com.example.to_do_list;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.AuthFailureError;
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

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }

        // Check if already logged in (token exists)
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("access_token", null);
        Log.d("MainActivity", "Access Token: " + accessToken);

        if (accessToken != null) {
            // Validate token before proceeding
            String url = "http://10.0.2.2:5000/auth/login/";
            StringRequest validationRequest = createAuthenticatedRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            Log.d("MainActivity", "Token Validation Response: " + response);
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                // Token is valid, go to TDList
                                Intent intent = new Intent(MainActivity.this, TDList.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Token is invalid, clear it and show login
                                clearTokenAndShowLogin();
                            }
                        } catch (JSONException e) {
                            Log.e("MainActivity", "JSON Parsing Error: " + e.toString());
                            clearTokenAndShowLogin();
                        }
                    }, error -> {
                        Log.e("MainActivity", "Token Validation Error: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("MainActivity", "Status Code: " + error.networkResponse.statusCode);
                            Log.e("MainActivity", "Error Response: " + new String(error.networkResponse.data != null ? error.networkResponse.data : new byte[0]));
                        }
                        clearTokenAndShowLogin();
                    }, null);

            if (validationRequest != null) {
                requestQueue.add(validationRequest);
            } else {
                Log.e("MainActivity", "Validation request is null, clearing token");
                clearTokenAndShowLogin();
            }
        } else {
            Log.d("MainActivity", "No access token found, showing login screen");
            // No token, stay on login screen
        }
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.input1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notification permission granted");
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private StringRequest createAuthenticatedRequest(int method, String url,
                                                     final Response.Listener<String> listener,
                                                     final Response.ErrorListener errorListener,
                                                     final JSONObject jsonParams) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        final String accessToken = sharedPreferences.getString("access_token", null);

        if (accessToken == null) {
            Log.w("MainActivity", "No access token for authenticated request");
            return null;
        }

        return new StringRequest(method, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                Log.d("MainActivity", "Request Headers: " + headers.toString());
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                if (jsonParams != null) {
                    return jsonParams.toString().getBytes();
                } else {
                    return new byte[0];
                }
            }
        };
    }

    private void clearTokenAndShowLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("access_token");
        editor.apply();
        Log.d("MainActivity", "Token cleared, showing login screen");
        // Stay on login screen (no need to setContentView again)

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        Log.d("TDList", "Token cleared and notifications canceled");
    }

    public void viewList(View view) {
        EditText username = findViewById(R.id.input1);
        EditText password = findViewById(R.id.input2);
        String un = username.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (un.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        String postUrl = "http://10.0.2.2:5000/auth/login/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, postUrl,
                response -> {
                    try {
                        Log.d("MainActivity", "Login Response: " + response);
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if (status.equals("success")) {
                            String accessToken = jsonResponse.getString("access_token");
                            SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("access_token", accessToken);
                            editor.apply();
                            Log.d("MainActivity", "Access Token Stored: " + accessToken);

                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, TDList.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON Error: " + e.toString());
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            Log.e("MainActivity", "Login Volley Error: " + error.toString());
            if (error.networkResponse != null && error.networkResponse.data != null) {
                Log.e("MainActivity", "Error Response: " + new String(error.networkResponse.data));
            }
            Toast.makeText(this, "Login Failed: Network error", Toast.LENGTH_SHORT).show();
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

    public void toggle(View view) {
        Intent intent = new Intent(MainActivity.this, Register.class);
        startActivity(intent);
    }
}