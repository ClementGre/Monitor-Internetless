package fr.themsou.monitorinternetless.commander;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static BlockingQueue<Location> locationQueue = new LinkedBlockingQueue<>();

    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "LocationServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(CHANNEL_ID, "created()");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = location -> {
            Log.d(CHANNEL_ID, "Location listener received, filling locationQueue...");
            Log.d("Locations", location.getLatitude() + "," + location.getLongitude());
            locationQueue.add(location);
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(CHANNEL_ID, "onStartCommand()");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }else {
            startForeground(NOTIFICATION_ID, createNotification());
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(CHANNEL_ID, "No permission for location!");
        }
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());

        return START_STICKY;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, "ring")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Localisation en cours")
                .setContentText("Une commande \"!locate\" entraîne une localisation de l'appareil...")
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

    public static BlockingQueue<Location> getLocationQueue() {
        return locationQueue;
    }
}
