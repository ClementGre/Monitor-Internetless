package fr.themsou.monitorinternetless.commander;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.telephony.SmsManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.List;

public class LocateReceiver extends BroadcastReceiver {

    private static final String TAG = "LocateIntent";

    @Override
    public void onReceive(Context context, Intent intent) {

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        final LocationResult locationResult = LocationResult.extractResult(intent);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_NO_CREATE);
        if(locationResult != null && CommandExecutor.locateAsyncResult != null){
            List<Location> locations = locationResult.getLocations();
            try{
                if(locations.size() > 0){
                    Location location = locations.get(0);
                    CommandExecutor.locateAsyncResult.put(
                            "Latitude : " + location.getLatitude() + "°\n" +
                            "Longitude : " + location.getLongitude() + "°\n" +
                            "Accuracy : " + location.getAccuracy() + " m" + "\n" +
                            "Bearing : " + location.getBearing() + "°\n" +
                            "Speed : " + location.getSpeed() + " m/s \n" +
                            "Date : " + new Date(location.getTime()).toString());
                }else{
                    CommandExecutor.locateAsyncResult.put("Unable to refresh location");
                }
            }catch(InterruptedException e){ e.printStackTrace(); }

        }
        //client.removeLocationUpdates(pendingIntent);
    }
}
