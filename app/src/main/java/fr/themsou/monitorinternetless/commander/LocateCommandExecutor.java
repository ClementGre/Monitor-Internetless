package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;

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

        Intent serviceIntent = new Intent(context, LocationService.class);
        serviceIntent.putExtra("number", commandExecutor.fromNumber);
        ContextCompat.startForegroundService(context, serviceIntent);

        commandExecutor.replyAndTerminate(context.getString(R.string.info_localizing));
    }
}
