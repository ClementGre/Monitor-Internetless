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
import android.location.LocationRequest;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Date;
import java.util.concurrent.Executors;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class LocationService extends Service implements LocationListener {

    private LocationManager locationManager;
    private String number;
    private boolean isDone = false;
    private boolean isGpsAvailable = false;

    private static final int NOTIFICATION_ID = 2;
    private static final String TAG = "LocationService";

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        number = intent.getStringExtra("number");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }else{
            startForeground(NOTIFICATION_ID, createNotification());
        }


        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isGpsAvailable = true;
            startLocationUpdates(LocationManager.GPS_PROVIDER);

            setTimeout(10, () -> {
                if (isDone) return;
                Log.d(TAG, "GPS Slow: fetching network location");
                if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    startLocationUpdates(LocationManager.NETWORK_PROVIDER);
                }
            });
            setTimeout(60, () -> {
                if (isDone) return;
                Log.d(TAG, "GPS Location timeout");
                CommandExecutor.reply(number, getString(R.string.locate_timeout));
                endService();
            });
        }else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            startLocationUpdates(LocationManager.NETWORK_PROVIDER);
            setTimeout(20, () -> {
                if (isDone) return;
                Log.d(TAG, "Network Location timeout");
                CommandExecutor.reply(number, getString(R.string.locate_timeout));
                endService();
            });
        }

        return START_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(String provider){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31, Android 12
            LocationRequest gpsLocationRequest = new LocationRequest.Builder(0)
                    .setMaxUpdates(1)
                    .setQuality(LocationRequest.QUALITY_HIGH_ACCURACY)
                    .build();
            locationManager.requestLocationUpdates(provider, gpsLocationRequest, Executors.newSingleThreadExecutor(), this);

        }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) { // API 30, Android 11
            locationManager.requestLocationUpdates(provider, 600_000, 0, Executors.newSingleThreadExecutor(), this);
        }else{
            locationManager.requestLocationUpdates(provider, 600_000, 0, this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
            CommandExecutor.reply(number, formatLocation(location));
            endService();
        }else if(LocationManager.NETWORK_PROVIDER.equals(location.getProvider())){
            if (isGpsAvailable){
                CommandExecutor.reply(number, getString(R.string.locate_network_alternative) + "\n" + formatLocation(location));
            }else{
                CommandExecutor.reply(number, getString(R.string.locate_network_replacement) + "\n" + formatLocation(location));
                endService();
            }

        }
    }

    private void setTimeout(long seconds, Runnable runnable){
        Handler timeoutHandler = new Handler();
        timeoutHandler.postDelayed(runnable, seconds * 1000);
    }

    private void endService(){
        locationManager.removeUpdates(this);
        isDone = true;
        stopForeground(true);
        stopSelf();
    }

    private String formatLocation(Location location){
        return "Maps : https://www.google.com/maps/place/" + location.getLatitude() + "%20" + location.getLongitude() + "\n" +
                getString(R.string.info_latitude) + " : " + location.getLatitude() + "°\n" +
                getString(R.string.info_longitude) + " : " + location.getLongitude() + "°\n" +
                getString(R.string.info_accuracy) + " : " + location.getAccuracy() + " m" + "\n" +
                getString(R.string.info_bearing) + " : " + location.getBearing() + "°\n" +
                getString(R.string.info_speed) + " : " + location.getSpeed() + " m/s \n" +
                "Date : " + new Date(location.getTime());
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
