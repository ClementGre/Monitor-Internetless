package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Date;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class LocationService extends Service implements LocationListener {

    private LocationManager locationManager;
    private String number;

    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "LocationService";



    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        number = intent.getStringExtra("number");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }else{
            startForeground(NOTIFICATION_ID, createNotification());
        }

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, Looper.getMainLooper());
        return START_STICKY;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(CHANNEL_ID, "Location listener called. lat,long:" + location.getLatitude() + "," + location.getLongitude());

        CommandExecutor.reply(number,
                "Maps : https://www.google.com/maps/place/" + location.getLatitude() + "%20" + location.getLongitude() + "\n" +
                        getString(R.string.info_latitude) + " : " + location.getLatitude() + "°\n" +
                        getString(R.string.info_longitude) + " : " + location.getLongitude() + "°\n" +
                        getString(R.string.info_accuracy) + " : " + location.getAccuracy() + " m" + "\n" +
                        getString(R.string.info_bearing) + " : " + location.getBearing() + "°\n" +
                        getString(R.string.info_speed) + " : " + location.getSpeed() + " m/s \n" +
                        "Date : " + new Date(location.getTime()));

        stopForeground(true);
        stopSelf();
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "locate")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.locate_notification_title))
                .setContentText(getString(R.string.locate_notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setSilent(true)
                .setOngoing(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
