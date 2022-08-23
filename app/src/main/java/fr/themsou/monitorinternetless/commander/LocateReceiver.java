package fr.themsou.monitorinternetless.commander;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

public class LocateReceiver extends LocationCallback {

    private static final String TAG = "LocateIntent";

    @Override
    public void onLocationResult(@NonNull LocationResult locationResult){

        Log.d(TAG, "Location received");
        try{
            if(locationResult.getLocations().size() == 0){
                LocateCommandExecutor.locateAsyncResult.put(null);
                return;
            }
            LocateCommandExecutor.locateAsyncResult.put(locationResult.getLocations().get(0));

        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
