package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import fr.themsou.monitorinternetless.R;

public class LocateCommandExecutor{

    public static BlockingQueue<Location> locateAsyncResult;

    private final Context context;
    private final CommandExecutor commandExecutor;
    private static final String TAG = "LocateCommandExecutor";
    public LocateCommandExecutor(Context context, CommandExecutor commandExecutor){
        this.context = context;
        this.commandExecutor = commandExecutor;
    }

    @SuppressLint("MissingPermission")
    public void execute(String[] args){

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            commandExecutor.replyAndTerminate(context.getString(R.string.info_error_gps_disabled));
        }else{
            commandExecutor.reply(context.getString(R.string.info_localizing));

            locateAsyncResult = new SynchronousQueue<>();

            final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(1);
            LocateReceiver locationCallback = new LocateReceiver();
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            Log.d(TAG, "Location requested");

            Location location = null;
            try{
                location = locateAsyncResult.take();
                Log.d(TAG, "Location took");
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }catch(InterruptedException e){ e.printStackTrace(); }

            locateAsyncResult = null;

            if(location != null){

                commandExecutor.replyAndTerminate(
                        "Maps : https://www.google.com/maps/place/" + location.getLatitude() + "%20" + location.getLongitude() +"\n" +
                        context.getString(R.string.info_latitude) + " : " + location.getLatitude() + "°\n" +
                        context.getString(R.string.info_longitude) + " : " + location.getLongitude() + "°\n" +
                        context.getString(R.string.info_accuracy) + " : " + location.getAccuracy() + " m" + "\n" +
                        context.getString(R.string.info_bearing) + " : " + location.getBearing() + "°\n" +
                        context.getString(R.string.info_speed) + " : " + location.getSpeed() + " m/s \n" +
                        "Date : " + new Date(location.getTime()).toString());
            }else{
                commandExecutor.replyAndTerminate(context.getString(R.string.info_location_unknown));
            }
        }
    }
}
