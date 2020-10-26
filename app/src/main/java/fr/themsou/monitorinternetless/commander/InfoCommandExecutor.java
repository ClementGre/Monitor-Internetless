package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.core.util.Predicate;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import fr.themsou.monitorinternetless.R;

public class InfoCommandExecutor{

    private final Context context;
    private final CommandExecutor commandExecutor;
    public InfoCommandExecutor(Context context, CommandExecutor commandExecutor){
        this.context = context;
        this.commandExecutor = commandExecutor;
    }

    @SuppressLint("MissingPermission")
    public void execute(String[] args) {

        // BATTERY
        String batteryLevel = context.getString(R.string.unknown);
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if(bm != null) batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) + "%";

        // LASTLOCATION
        String lastLocation = context.getString(R.string.unknown);
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            final BlockingQueue<String> asyncResult = new SynchronousQueue<>();
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override public void onSuccess(Location location) {
                    try{
                        if(location != null){
                            asyncResult.put("\n" +
                                    "  Lat/long : " + location.getLatitude() + "° " + location.getLongitude() + "°\n" +
                                    "  " + context.getString(R.string.info_accuracy) + " : " + location.getAccuracy() + " m" + "\n" +
                                    "  Date : " + new Date(location.getTime()).toString());
                        }else{
                            asyncResult.put("Unknown");
                        }
                    }catch(InterruptedException e){ e.printStackTrace(); }
                }
            });
            try{
                lastLocation = asyncResult.take();
            }catch(InterruptedException e){ e.printStackTrace(); }
        }else{
            lastLocation = context.getString(R.string.info_gps_disabled);
        }

        String powerSaver = context.getString(R.string.unknown);
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(powerManager != null)  powerSaver = powerManager.isPowerSaveMode() ? context.getString(R.string.info_enabled) : context.getString(R.string.info_disabled);

        String mobileData = context.getString(R.string.unknown);
        try{
            mobileData = (Settings.Global.getInt(context.getContentResolver(), "mobile_data") == 1) ? context.getString(R.string.info_enabled) : context.getString(R.string.info_disabled);
        }catch(Settings.SettingNotFoundException e){ e.printStackTrace(); }


        String wifi = context.getString(R.string.unknown);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null) wifi = wifiManager.isWifiEnabled() ? context.getString(R.string.info_enabled) : context.getString(R.string.info_disabled);

        commandExecutor.replyAndTerminate(context.getString(R.string.info_battery) + " : " + batteryLevel + "\n" +
                context.getString(R.string.info_power_saver) + " : " + powerSaver + "\n" +
                context.getString(R.string.info_celular) + " : " + mobileData + "\n" +
                "Wifi : " + wifi + "\n" +
                context.getString(R.string.info_last_location) + " : " + lastLocation);
    }
}
