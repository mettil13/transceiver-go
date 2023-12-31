package app_mobili.transceiver_go;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class MeasurementService extends Service {

    MeasurementSingleton measurementSingleton;
    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "persistent_notification_channel";
    private Runnable updateTimeRunnable;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable measuringRun = new Runnable() {
        @Override
        public void run() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Use getContext() in a Fragment or this in an Activity

            int measure_interval = sharedPreferences.getInt("measure_interval", 10);

            boolean automatic_measurements = sharedPreferences.getBoolean("automatic_measurements", false);
            boolean network_measurement = sharedPreferences.getBoolean("measure_lte_umps", false);
            boolean wifi_measurement = sharedPreferences.getBoolean("measure_wifi", false);
            boolean noise_measurement = sharedPreferences.getBoolean("measure_noise", false);

            // activity management in services are bad practice, so we don't do it
            // the functions are prepared to handle this situation, the app will reload
            // measurements on resume, no problem
            if(network_measurement) {
                measurementSingleton.takeNetworkMeasurement(null);
            }
            if(wifi_measurement) {
                measurementSingleton.takeWifiMeasurement(null);
            }
            if(noise_measurement) {
                measurementSingleton.takeNoiseMeasurement(null);
            }

            // Reschedule the task to run again in X minutes if needed
            if (automatic_measurements) {
                if (measure_interval == 0) handler.postDelayed(this, 1000 * 10); // 10 seconds
                else {
                    handler.postDelayed(this, measure_interval * 60_000L);
                }
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        measurementSingleton = MeasurementSingleton.create(getApplicationContext(), new CoordinateListener());

        // Set up a recurring task to update the notification
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                updateNotification();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Use getContext() in a Fragment or this in an Activity

                int measure_interval = sharedPreferences.getInt("measure_interval", 10);

                if (measure_interval == 0) handler.postDelayed(this, 1000 * 10); // 10 seconds
                else {
                    handler.postDelayed(this, measure_interval * 60_000L);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the task when the service is started
        handler.post(measuringRun);

        handler.post(updateTimeRunnable);

        startForeground(NOTIFICATION_ID, createNotification());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Remove the callback when the service is destroyed
        handler.removeCallbacks(measuringRun);
        // Remove the recurring task when the service is stopped
        handler.removeCallbacks(updateTimeRunnable);

        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.transceiver_icon_marker_dark)
                .setContentTitle("Automatic measurements taken!")
                .setContentText("At time: " + getCurrentTime())
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true) // Makes the notification non-dismissible
                .setSilent(true)
                .build();
    }

    private void updateNotification() {
        Notification notification = createNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // cannot request permissions from here without some heavy code spaghetti
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // needed only for android API versions > 26
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Persistent Notification Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}