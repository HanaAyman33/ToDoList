package com.example.to_do_list;

import static android.widget.Toast.LENGTH_SHORT;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//To-Do-List Activity
public class TDList extends AppCompatActivity {
    private RequestQueue tasksRequestQueue;
    private LinearLayout tasksContainer;
    private EditText addTaskEditText;
    private NestedScrollView scrollView;
    private final int[] tealColors = {
            0xFF00b9ae,
            0xFF037171,
            0xFF02c3bd,
            0xFF009f93
    };

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.layout3);

        tasksRequestQueue = Volley.newRequestQueue(this);
        tasksContainer = findViewById(R.id.taskTextView);
        addTaskEditText = findViewById(R.id.addTask);
        scrollView = findViewById(R.id.scrollView);

        if (tasksContainer == null) {
            Log.e("UI Error", "tasksContainer is NULL! Check if layout3.xml contains @+id/taskTextView.");
        }
        // Fetch user's tasks
        fetchTasks();

        // Schedule hourly notifications
        scheduleHourlyNotifications();
    }

    private void scheduleHourlyNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long intervalMillis = AlarmManager.INTERVAL_HOUR; // 1 hour
        long triggerAtMillis = System.currentTimeMillis() + intervalMillis;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
        } else {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pendingIntent);
        }
        Log.d("TDList", "Hourly notifications scheduled");
    }

    private void fetchTasks() {// Handling Get Request
        String url = "http://10.0.2.2:5000/To-Do-List/";

        String accessToken = getJwtToken(); // Retrieve the token
        Log.d("Access Token (Sending to To-Do):", accessToken);

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("API Response: ", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        // Check if tasksContainer is null
                        if (tasksContainer == null) {
                            Log.e("UI Error", "tasksContainer is NULL! Check layout ID.");
                            return;
                        }
                        if (status.equals("success")) {
                            JSONArray tasksArray = jsonResponse.getJSONArray("tasks");
                            tasksContainer.removeAllViews();
                            for (int i = 0; i < tasksArray.length(); i++) {
                                addTaskToView(tasksArray.getString(i));
                            }
                        } else {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(this, message, LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("GET JSON Error", e.toString());
                        Toast.makeText(this, "JSON parsing error", LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("GET Volley Error", error.toString());
                    handleVolleyError(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                return getAuthHeaders();
            }
        };
        tasksRequestQueue.add(getRequest);
    }

    private void addTaskToView(String task) {
        RelativeLayout taskView = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.task_component, null);
        TextView taskTextView = taskView.findViewById(R.id.taskTextView);
        taskTextView.setText(task);
        tasksContainer.addView(taskView);

        // Set random pastel color for background and text
        Random random = new Random();
        int randomColor = tealColors[random.nextInt(tealColors.length)];
        taskTextView.setBackgroundColor(randomColor);
        taskTextView.setTextColor(0xFFF5F5F5);

        Button doneButton = taskView.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> done(taskView, taskTextView.getText().toString()));//handle deleting/finishing the task

        // Drag and Drop handling
        taskView.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        });

        // Make each task a drop target
        taskView.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(0xFFE0E0E0); // Light gray highlight
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(0x00000000); // Reset background
                    return true;
                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    ViewGroup parent = (ViewGroup) draggedView.getParent();
                    parent.removeView(draggedView);

                    // Find the index of the drop target
                    int dropIndex = tasksContainer.indexOfChild(v);

                    // Insert draggedView at the drop position
                    tasksContainer.addView(draggedView, dropIndex);
                    draggedView.setVisibility(View.VISIBLE);

                    v.setBackgroundColor(0x00000000); // Reset background
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        View draggedView1 = (View) event.getLocalState();
                        draggedView1.setVisibility(View.VISIBLE);
                    }
                    return true;
                default:
                    return false;
            }
        });
    }

    private StringRequest createAuthenticatedRequest(int method, String url,
                                                     final Response.Listener<String> listener,
                                                     final Response.ErrorListener errorListener,
                                                     final JSONObject jsonParams) {

        final String accessToken = getJwtToken();

        if (accessToken == null) {
            Log.e("Auth Request", "No access token found. Redirecting to login.");
            Toast.makeText(this, "Authentication required. Please login.", LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return null; // Important: Return null if no token
        }

        return new StringRequest(method, url, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                Log.d("Request Headers", headers.toString()); // Log the headers here
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

    private Map<String, String> getAuthHeaders() {
        Map<String, String> headers = new HashMap<>();
        String accessToken = getJwtToken();
        if (accessToken != null) {
            headers.put("Authorization", "Bearer " + accessToken);
        } else {
            Log.w("Auth", "Token is null!");
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private void clearToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("access_token");
        editor.apply();
    }

    private String getJwtToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyApp", MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);
        Log.d("JWT Token Check", "Retrieved Token: " + token);
        return token;
    }

    private void handleVolleyError(VolleyError error) {
        Log.e("Volley Error Handler", error.toString());
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            String errorResponse = new String(error.networkResponse.data != null ? error.networkResponse.data : new byte[0]);
            Log.e("Error Response", "Status Code: " + statusCode + ", Response: " + errorResponse);

            try {
                JSONObject jsonError = new JSONObject(errorResponse);
                String message = jsonError.getString("message");
                Toast.makeText(TDList.this, message, Toast.LENGTH_LONG).show();

            } catch (JSONException e) {
                // If not JSON, show generic error
                if (statusCode == 401) {
                    clearToken();
                    Toast.makeText(TDList.this, "Your session has ended", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(TDList.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(TDList.this, "Network error", LENGTH_SHORT).show();
        }
    }

    public void addTask(View view) {
        String task = addTaskEditText.getText().toString().trim();
        if (task.isEmpty()) {
            Toast.makeText(this, "Please enter a task", LENGTH_SHORT).show();
            return; // Don't proceed if task is empty
        }
        try {
            String postUrl = "http://10.0.2.2:5000/To-Do-List/"; // Correct URL for the protected route
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("task", task); // The task you want to add

            StringRequest postRequest = createAuthenticatedRequest(Request.Method.POST, postUrl,
                    response -> {
                        Log.d("POST Response", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if (status.equals("success")) {
                                addTaskToView(task);
                                addTaskEditText.getText().clear();
                                Toast.makeText(this, message, LENGTH_SHORT).show();
                                if (scrollView != null) { // Check for null before using scrollView
                                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                                }
                            } else {
                                Toast.makeText(this, message, LENGTH_SHORT).show(); // Use 'this'
                            }

                        } catch (JSONException e) {
                            Log.e("POST JSON Error", e.toString());
                            Toast.makeText(this, "JSON parsing error", LENGTH_SHORT).show(); // Use 'this'
                        }
                    },
                    error -> {
                        Log.e("POST Volley Error", error.toString());
                        handleVolleyError(error);
                    }, jsonParams); // Pass jsonParams here

            if (postRequest != null) {
                tasksRequestQueue.add(postRequest); // Add the request to the queue
            } else {
                Log.e("Auth Request", "Request is null. Check token retrieval.");
                Toast.makeText(this, "Authentication error", LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class); // Or wherever your login is
                startActivity(intent);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void done(RelativeLayout taskView, String taskToDelete) {// Handle Delete Request
        String url = "http://10.0.2.2:5000/To-Do-List/";
        try {
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("task", taskToDelete); // Use "task" here (as originally intended
            Log.d("DELETE Request JSON", jsonParams.toString());

            StringRequest deleteRequest = new StringRequest(Request.Method.PUT, url,
                    response -> {
                        Log.d("DELETE Response", response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if (status.equals("success")) {
                                tasksContainer.removeView(taskView);
                                Toast.makeText(this, "Done!", LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, message, LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("DELETE JSON Error", e.toString());
                            Toast.makeText(this, "JSON parsing error", LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("DELETE Volley Error", error.toString());
                        handleVolleyError(error);
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return getAuthHeaders();
                }
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return jsonParams.toString().getBytes(StandardCharsets.UTF_8);
                }
            };

            tasksRequestQueue.add(deleteRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}