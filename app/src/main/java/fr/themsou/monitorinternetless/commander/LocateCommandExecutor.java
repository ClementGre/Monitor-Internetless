package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import fr.themsou.monitorinternetless.R;

public class LocateCommandExecutor{

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
            return;
        }

        commandExecutor.reply(context.getString(R.string.info_localizing));

        Intent serviceIntent = new Intent(context, LocationService.class);
        ContextCompat.startForegroundService(context, serviceIntent);

        BlockingQueue<Location> locationQueue = LocationService.getLocationQueue();

        try{
            Location location = locationQueue.take();
            commandExecutor.replyAndTerminate(
                    "Maps : https://www.google.com/maps/place/" + location.getLatitude() + "%20" + location.getLongitude() + "\n" +
                            context.getString(R.string.info_latitude) + " : " + location.getLatitude() + "°\n" +
                            context.getString(R.string.info_longitude) + " : " + location.getLongitude() + "°\n" +
                            context.getString(R.string.info_accuracy) + " : " + location.getAccuracy() + " m" + "\n" +
                            context.getString(R.string.info_bearing) + " : " + location.getBearing() + "°\n" +
                            context.getString(R.string.info_speed) + " : " + location.getSpeed() + " m/s \n" +
                            "Date : " + new Date(location.getTime()));
        }catch(InterruptedException e){
            e.printStackTrace();
            commandExecutor.replyAndTerminate(context.getString(R.string.info_location_unknown));
        }
    }
}
