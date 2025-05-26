package com.example.to_do_list;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class TaskReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "TaskReminderChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TaskReminderReceiver", "Alarm triggered, fetching tasks for notification");

        // Create notification channel
        createNotificationChannel(context);

        // Initialize Volley request queue
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://10.0.2.2:5000/To-Do-List/";
        String accessToken = getJwtToken(context);

        if (accessToken == null) {
            Log.w("TaskReminderReceiver", "No access token, skipping notification");
            return;
        }

        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if (status.equals("success")) {
                            JSONArray tasksArray = jsonResponse.getJSONArray("tasks");
                            String notificationContent = buildNotificationContent(tasksArray);
                            sendNotification(context, notificationContent);
                        } else {
                            String message = jsonResponse.getString("message");
                            Log.e("TaskReminderReceiver", "Failed to fetch tasks: " + message);
                            sendNotification(context, "You have pending tasks to complete!");
                        }
                    } catch (JSONException e) {
                        Log.e("TaskReminderReceiver", "JSON parsing error: " + e.toString());
                        sendNotification(context, "You have pending tasks to complete!");
                    }
                },
                error -> {
                    Log.e("TaskReminderReceiver", "Volley error: " + error.toString());
                    sendNotification(context, "You have pending tasks to complete!");
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + accessToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(getRequest);
    }

    private String getJwtToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyApp", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

    private String buildNotificationContent(JSONArray tasksArray) throws JSONException {
        if (tasksArray.length() == 0) {
            return "No pending tasks, add some now!";
        }
        StringBuilder content = new StringBuilder("You have " + tasksArray.length() + " pending task(s):\n");
        for (int i = 0; i < Math.min(tasksArray.length(), 3); i++) {
            content.append("- ").append(tasksArray.getString(i)).append("\n");
        }
        if (tasksArray.length() > 3) {
            content.append("and more...");
        }
        return content.toString();
    }

    private void sendNotification(Context context, String contentText) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create intent to open TDList activity when notification is clicked
        Intent intent = new Intent(context, TDList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Use default notification sound
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bell);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notifications)
                .setContentTitle("To-Do List Reminder")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(soundUri) // Set the sound for the notification
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
        Log.d("TaskReminderReceiver", "Notification sent with content: " + contentText);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Hourly reminders for your to-do list tasks";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Enable sound for the channel
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(soundUri, null);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}