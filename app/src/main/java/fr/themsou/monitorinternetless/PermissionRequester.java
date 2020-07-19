package fr.themsou.monitorinternetless;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
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

    public void grant(String permissionCode){
        if(!isGranted(permissionCode))
            ActivityCompat.requestPermissions(activity, new String[]{permissionCode}, 2000);
    }

    public void grant(String permissionCode, Consumer<Boolean> callBack){
        if(!isGranted(permissionCode)){
            int requestCode = 2000 + new Random().nextInt(1000); // 2000 - 2999
            ActivityCompat.requestPermissions(activity, new String[]{permissionCode}, requestCode);
            currentRequests.put(requestCode, callBack);
        }else{
            callBack.accept(true);
        }
    }
    public void grantOnly(String permissionCode, Consumer<Boolean> grantedCallBack){
        if(!isGranted(permissionCode)){
            int requestCode = 3000 + new Random().nextInt(1000); // 3000 - 3999
            ActivityCompat.requestPermissions(activity, new String[]{permissionCode}, requestCode);
            currentRequests.put(requestCode, grantedCallBack);
        }else{
            grantedCallBack.accept(null);
        }
    }

    public void grantSome(String... permissions){
        if(!isGranted(permissions))
            ActivityCompat.requestPermissions(activity, permissions, 2000);
    }
    public void grantSome(String[] permissions, Consumer<Boolean> callBack){
        if(!isGranted(permissions)){
            int requestCode = 2000 + new Random().nextInt(1000); // 2000 - 2999
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            currentRequests.put(requestCode, callBack);
        }else{
            callBack.accept(true);
        }
    }
    public void grantSomeOnly(String[] permissions, Consumer<Boolean> grantedCallBack){
        if(!isGranted(permissions)){
            int requestCode = 3000 + new Random().nextInt(1000); // 3000 - 3999
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
            currentRequests.put(requestCode, grantedCallBack);
        }else{
            grantedCallBack.accept(null);
        }
    }
}
