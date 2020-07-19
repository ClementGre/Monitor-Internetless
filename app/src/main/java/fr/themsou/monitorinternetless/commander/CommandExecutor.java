package fr.themsou.monitorinternetless.commander;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import fr.themsou.monitorinternetless.R;
import fr.themsou.monitorinternetless.ui.commands.Command;
import fr.themsou.monitorinternetless.ui.commands.CommandsFragment;

public class CommandExecutor {

    public String execute(String[] args, Context context) {

        ArrayList<Command> commands = CommandsFragment.getCommands(context);

        for (Command command : commands) {
            if (context.getString(command.getTitle()).equalsIgnoreCase(args[0])) {
                if (command.isEnabled()) {
                    if (command.hasPermission(context)) {

                        switch (context.getString(command.getTitle())) {
                            case "!info":
                                return executeInfoCommand(context);
                            case "!locate":
                                return executeLocateCommand(context);
                        }

                        return "La commande a été exécuté !";

                    } else return context.getString(R.string.command_error_no_permission);
                } else return context.getString(R.string.command_error_not_enabled);
            }
        }
        return null;
    }

    private String executeInfoCommand(Context context){

        // BATTERY
        String batteryLevel = "Unknown";
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            if (bm != null)
                batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) + "%";
        } else {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            double batteryPct = level / (double) scale;
            batteryLevel = (int) (batteryPct * 100) + "%";
        }

        // LASTLOCATION
        String lastLocation = "Unknown";
        final BlockingQueue<String> asyncResult = new SynchronousQueue<>();
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override public void onSuccess(Location location) {
                    try{
                        if(location != null){
                            asyncResult.put("\n" +
                                    "  Lat/long : " + location.getLatitude() + "° " + location.getLongitude() + "°\n" +
                                    "  Accuracy : " + location.getAccuracy() + " radial meters" + "\n" +
                                    "  Date : " + new Date(location.getTime()).toString());
                        }else{
                            asyncResult.put("Unknown");
                        }
                    }catch(InterruptedException e){ e.printStackTrace(); }
                }
            });
        }

        try{
            lastLocation = asyncResult.take();
        }catch(InterruptedException e){ e.printStackTrace(); }

        return "Battery : " + batteryLevel + "\n" +
                "LastLocation : " + lastLocation + "\n" +
                "Eco mode : " + "" + "\n" +
                "Mobile data : " + "" + "\n" +
                "GPS : " + "" + "\n" +
                "Wifi : " + "" + "\n" +
                "Bluetooth : " + "";
    }

    private String executeLocateCommand(Context context){

        String location = "Unknown";
        final BlockingQueue<String> asyncResult = new SynchronousQueue<>();
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    fusedLocationClient.removeLocationUpdates(this);
                    if(locationResult != null && locationResult.getLocations().size() > 0){
                        Location location = locationResult.getLocations().get(locationResult.getLocations().size() -1);
                        try{
                            asyncResult.put(
                                    "Latitude : " + location.getLatitude() + "°\n" +
                                    "Longitude : " + location.getLongitude() + "°\n" +
                                    "Accuracy : " + location.getAccuracy() + " radial meters" + "\n" +
                                    "Bearing : " + location.getBearing() + "°\n" +
                                    "Speed : " + location.getSpeed() + " m/s \n" +
                                    "Date : " + new Date(location.getTime()).toString());
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }

                    }
                }
            }, Looper.getMainLooper());
        }

        try{
            location = asyncResult.take();
        }catch(InterruptedException e){ e.printStackTrace(); }

        if(!location.isEmpty()){
            return "loc : " + location;
        }else{
            return "Unknown location";
        }


    }

}
