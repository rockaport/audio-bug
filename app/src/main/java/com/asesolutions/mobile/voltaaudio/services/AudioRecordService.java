package com.asesolutions.mobile.voltaaudio.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.asesolutions.mobile.voltaaudio.MainApplication;
import com.asesolutions.mobile.voltaaudio.R;
import com.asesolutions.mobile.voltaaudio.models.events.AudioRecordServiceEvent;
import com.asesolutions.mobile.voltaaudio.ui.MainActivity;
import com.squareup.otto.Bus;

public class AudioRecordService extends Service {
    private static int notificationId = 1001;
    private Bus bus;

    public AudioRecordService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Gen an instance of the bus and register this class
        bus = MainApplication.getBus();
        bus.register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Update the application recording state
        MainApplication.getState().setIsRecording(true);

        // Display a foreground notification for the user
        startForeground(notificationId, buildNotification());

        // Send an audio service event
        bus.post(new AudioRecordServiceEvent());

        return super.onStartCommand(intent, flags, startId);
    }

    private Notification buildNotification() {
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_mic_white_24dp)
                .setContentTitle("Recording")
                .setContentText("0:0s");

        // Add a pending intent that will reopen this application
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Cancel the foreground notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);

        // Update the application state
        MainApplication.getState().setIsRecording(false);

        // Send a final audio service event
        bus.post(new AudioRecordServiceEvent());

        // Unregister this object from the bus
        bus.unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public enum STATE {
        LISTENING,
        RECORDING,
    }

}
