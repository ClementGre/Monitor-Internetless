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
        String batteryLevel = "Unknown";
        if(Build.VERSION.SDK_INT >= 21){
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            if(bm != null)
                batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) + "%";
        }else{
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            double batteryPct = level / (double) scale;
            batteryLevel = (int) (batteryPct * 100) + "%";
        }

        // LASTLOCATION
        String lastLocation = "Unknown";
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final BlockingQueue<String> asyncResult = new SynchronousQueue<>();
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override public void onSuccess(Location location) {
                try{
                    if(location != null){
                        asyncResult.put("\n" +
                                "  Lat/long : " + location.getLatitude() + "° " + location.getLongitude() + "°\n" +
                                "  Accuracy : " + location.getAccuracy() + " m" + "\n" +
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

        String powerSaver = "Unknown";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if(powerManager != null)  powerSaver = powerManager.isPowerSaveMode() ? "Enable" : "Disable";
        }

        String mobileData = "Unknown";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try{
                mobileData = (Settings.Global.getInt(context.getContentResolver(), "mobile_data") == 1) ? "Enable" : "Disable";
            }catch(Settings.SettingNotFoundException e){ e.printStackTrace(); }
        }

        String gps = "Unknown";
        if(lm != null) gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ? "Enable" : "Disable";


        String wifi = "Unknown";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(wifiManager != null) wifi = wifiManager.isWifiEnabled() ? "Enable" : "Disable";

        commandExecutor.replyAndTerminate("Battery : " + batteryLevel + "\n" +
                "LastLocation : " + lastLocation + "\n" +
                "Eco mode : " + powerSaver + "\n" +
                "Mobile data : " + mobileData + "\n" +
                "GPS : " + gps + "\n" +
                "Wifi : " + wifi);
    }
}
