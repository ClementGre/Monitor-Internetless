package fr.themsou.monitorinternetless.commander;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.text.UnicodeSetIterator;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;
import java.util.List;

import fr.themsou.monitorinternetless.R;

public class LocateReceiver extends BroadcastReceiver {

    private static final String TAG = "LocateIntent";

    @Override
    public void onReceive(Context context, Intent intent) {

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        final LocationResult locationResult = LocationResult.extractResult(intent);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_NO_CREATE);
        if(locationResult != null && LocateCommandExecutor.locateAsyncResult != null){
            List<Location> locations = locationResult.getLocations();
            try{
                if(locations.size() > 0){
                    Location location = locations.get(0);
                    LocateCommandExecutor.locateAsyncResult.put(
                            "Maps : https://www.google.com/maps/place/" + location.getLatitude() + "%20" + location.getLongitude() +"\n" +
                            context.getString(R.string.info_latitude) + " : " + location.getLatitude() + "°\n" +
                            context.getString(R.string.info_longitude) + " : " + location.getLongitude() + "°\n" +
                            context.getString(R.string.info_accuracy) + " : " + location.getAccuracy() + " m" + "\n" +
                            context.getString(R.string.info_bearing) + " : " + location.getBearing() + "°\n" +
                            context.getString(R.string.info_speed) + " : " + location.getSpeed() + " m/s \n" +
                            "Date : " + new Date(location.getTime()).toString());
                }else{
                    LocateCommandExecutor.locateAsyncResult.put("Unable to refresh location");
                }
            }catch(InterruptedException e){ e.printStackTrace(); }

        }
        //client.removeLocationUpdates(pendingIntent);
    }
}
