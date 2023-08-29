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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class LocationService extends Service {

    private LocationManager locationManager;

    private final LocationListener locationListener = this::locationReceived;

    private static final BlockingQueue<Location> locationQueue = new LinkedBlockingQueue<>();

    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "LocationService";

    @Override
    public void onCreate() {
        super.onCreate();
        locationQueue.clear();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    private void locationReceived(Location location){
        Log.d(CHANNEL_ID, "Location listener called. lat,long:" + location.getLatitude() + "," + location.getLongitude());
        locationQueue.add(location);

        stopForeground(true);
        stopSelf();
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }else{
            startForeground(NOTIFICATION_ID, createNotification());
        }

        if(isGooglePlayServicesAvailable() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            Log.d(CHANNEL_ID, "Google play services available, using fused location provider");
            requestLocationUsingFusedLocationProviderClient();
        }else{
            Log.d(CHANNEL_ID, "Google play services not available, using location manager");
            requestLocationUsingLocationManager();
        }
        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUsingFusedLocationProviderClient(){
        final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(location -> {
            Log.d(CHANNEL_ID, "Fused location provider returned location");
            locationReceived(location);

        }).addOnFailureListener(e -> {
            Log.d(CHANNEL_ID, "Fused location provider failed to return location, using location manager");
            requestLocationUsingLocationManager();
        });
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUsingLocationManager(){
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
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

    public static BlockingQueue<Location> getLocationQueue() {
        return locationQueue;
    }
}
