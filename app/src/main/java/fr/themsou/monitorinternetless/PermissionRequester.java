package fr.themsou.monitorinternetless;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PermissionRequester {

    private HashMap<Integer, Consumer<Boolean>> currentRequests = new HashMap<>();

    MainActivity activity;
    public PermissionRequester(MainActivity activity){
        this.activity = activity;
    }

    public void receiveActivityResult(int requestCode, boolean accepted){
        if(currentRequests.containsKey(requestCode)){
            if(requestCode < 3000){
                currentRequests.get(requestCode).accept(accepted);
            }else{
                if(accepted) currentRequests.get(requestCode).accept(true);
            }
            currentRequests.remove(requestCode);
        }
    }

    public boolean isGranted(String permissionCode){
        return ContextCompat.checkSelfPermission(activity, permissionCode) == PackageManager.PERMISSION_GRANTED;
    }
    public static boolean isGranted(String permissionCode, Context context){
        return ContextCompat.checkSelfPermission(context, permissionCode) == PackageManager.PERMISSION_GRANTED;
    }
    public boolean isGranted(String... permissionsCode){
        boolean grant = true;
        for(String permissionCode : permissionsCode){
            grant = isGranted(permissionCode) && grant;
        }
        return grant;
    }
    public static boolean isGranted(Context context, String... permissionsCode){
        boolean grant = true;
        for(String permissionCode : permissionsCode){
            grant = isGranted(permissionCode, context) && grant;
        }
        return grant;
    }


    public void grantOnly(String permissionCode, Consumer<Boolean> grantedCallBack){ // If already granted, return null
        if(!isGranted(permissionCode)){
            int requestCode = 3000 + new Random().nextInt(1000); // 3000 - 3999
            ActivityCompat.requestPermissions(activity, new String[]{permissionCode}, requestCode);
            currentRequests.put(requestCode, grantedCallBack);
        }else{
            grantedCallBack.accept(null);
        }
    }

    public void grantSome(String[] permissions, Consumer<Boolean> callBack){

        ArrayList<String> finalPermissions = new ArrayList<>();
        for(String permission : permissions){
            if(permission.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                callBack = insertRequestForBackgroundLocation(callBack);
            }else{
                finalPermissions.add(permission);
            }
        }
        permissions = finalPermissions.toArray(new String[0]);

        if(!isGranted(permissions) && permissions.length > 0){
            int requestCode = 2000 + new Random().nextInt(1000); // 2000 - 2999
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            currentRequests.put(requestCode, callBack);
        }else{
            callBack.accept(true);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Consumer<Boolean> insertRequestForBackgroundLocation(final Consumer<Boolean> callBack){

        return new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isGranted) {
                if(isGranted){
                    if(!isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){ // Everything's granted but not the background location.
                        int requestCode = 2000 + new Random().nextInt(1000); // 2000 - 2999
                        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, requestCode);
                        currentRequests.put(requestCode, callBack);
                    }else{
                        callBack.accept(true);
                    }
                }else{
                    callBack.accept(false);
                }
            }
        };


    }
}
