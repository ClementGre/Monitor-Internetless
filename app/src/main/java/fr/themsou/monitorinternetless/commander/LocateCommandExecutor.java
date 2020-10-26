package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import fr.themsou.monitorinternetless.R;

public class LocateCommandExecutor{

    public static BlockingQueue<String> locateAsyncResult;

    private final Context context;
    private final CommandExecutor commandExecutor;
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

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setNumUpdates(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Intent intent = new Intent(context, LocateReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            fusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent);

            String location = "";
            try{
                location = locateAsyncResult.take();
                fusedLocationClient.removeLocationUpdates(pendingIntent);
            }catch(InterruptedException e){ e.printStackTrace(); }

            locateAsyncResult = null;

            if(!location.isEmpty()){
                commandExecutor.replyAndTerminate(location);
            }else{
                commandExecutor.replyAndTerminate(context.getString(R.string.info_location_unknown));
            }
        }
    }
}
