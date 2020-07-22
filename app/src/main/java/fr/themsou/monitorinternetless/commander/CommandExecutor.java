package fr.themsou.monitorinternetless.commander;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Pattern;

import fr.themsou.monitorinternetless.R;
import fr.themsou.monitorinternetless.ui.commands.Command;
import fr.themsou.monitorinternetless.ui.commands.CommandsFragment;

public class CommandExecutor {

    private static final String TAG = "CommandExecutor";

    public String execute(String[] args, Context context) {

        ArrayList<Command> commands = CommandsFragment.getCommands(context);

        for(Command command : commands){
            if(context.getString(command.getTitle()).split(Pattern.quote(" "))[0].equalsIgnoreCase(args[0])){
                if(command.isEnabled()) {
                    if(command.hasPermission(context)){

                        switch (context.getString(command.getTitle()).split(Pattern.quote(" "))[0]){
                            case "!info":
                                return executeInfoCommand(context);
                            case "!locate":
                                return executeLocateCommand(context);
                            case "!mobile":
                                if(args.length == 2) return executeMobileCommand(context, args[1]);
                                else return context.getString(command.getTitle());
                            case "!gps":
                                if(args.length == 2) return executeGpsCommand(context, args[1]);
                                else return context.getString(command.getTitle());
                            case "!wifi":
                                if(args.length == 2) return executeWifiCommand(context, args[1]);
                                else return context.getString(command.getTitle());
                        }

                    }else return context.getString(R.string.command_error_no_permission);
                }else return context.getString(R.string.command_error_not_enabled);
            }
        }
        return null;
    }

    @SuppressLint("MissingPermission")
    private String executeInfoCommand(Context context){

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

        return "Battery : " + batteryLevel + "\n" +
                "LastLocation : " + lastLocation + "\n" +
                "Eco mode : " + powerSaver + "\n" +
                "Mobile data : " + mobileData + "\n" +
                "GPS : " + gps + "\n" +
                "Wifi : " + wifi;
    }

    public static BlockingQueue<String> locateAsyncResult;
    @SuppressLint("MissingPermission")
    private String executeLocateCommand(Context context){

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return "Error : GPS isn't enable.";
        }

        locateAsyncResult = new SynchronousQueue<>();
        final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        locationRequest.setExpirationDuration(60000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        Intent intent = new Intent(context, LocateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        fusedLocationClient.requestLocationUpdates(locationRequest, pendingIntent);

        String location = "";
        try{
            location = locateAsyncResult.take();
            fusedLocationClient.removeLocationUpdates(pendingIntent);
        }catch(InterruptedException e){ e.printStackTrace(); }

        CommandExecutor.locateAsyncResult = null;

        if(!location.isEmpty()){
            return location;
        }else{
            return "Unknown location";
        }
    }
    private String executeWifiCommand(Context context, String arg){
        boolean activate = parse(arg);

        if(activate){
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);
            return "WiFi is now enable";
        }else{
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);
            return "WiFi is now disable";
        }
    }

    private String executeGpsCommand(Context context, String arg){
        boolean activate = parse(arg);

        if(activate){

            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);


            return "GPS is now enable";
        }else{

            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            context.sendBroadcast(poke);

            return "GPS is now disable";
        }

    }

    private String executeMobileCommand(Context context, String arg){
        boolean activate = parse(arg);

        if(activate){

            try{
                TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
                setMobileDataEnabledMethod.invoke(telephonyService, true);

                return "Mobile data is now enable";
            }catch(Exception e){
                e.printStackTrace();
                return "Unable to enable mobile data";
            }
        }else{

            try{
                TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
                setMobileDataEnabledMethod.invoke(telephonyService, false);
                return "Mobile data is now disable";
            }catch(Exception e){
                e.printStackTrace();
                return "Unable to disable mobile data";
            }
        }

    }
    private boolean parse(String arg){
        switch(arg.toLowerCase()){
            case "on":
            case "yes":
            case "true":
            case "enable":
                return true;
            default:
                return false;
        }
    }
}
