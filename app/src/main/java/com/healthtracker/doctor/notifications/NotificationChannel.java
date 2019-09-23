package com.healthtracker.doctor.notifications;

import android.app.Application;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.healthtracker.doctor.R;

public class NotificationChannel extends Application {
    private static final String TAG = "NotificationChannel";
    public static final String CHANNEL_ID = "Reminders Channel";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        createNotificationChannel();
    }

    public void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel: ");
            String channel_name = getString(R.string.channel_name);
            String channel_description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, channel_name, importance);
            channel.setDescription(channel_description);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
