package fr.themsou.monitorinternetless;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Consumer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import fr.themsou.monitorinternetless.commander.LocateReceiver;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUESTER_START = 2000;
    private static final int PERMISSION_REQUESTER_END = 3999;
    private static String TAG = "MainActivity";
    public PermissionRequester permissionRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionRequester = new PermissionRequester(this);

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_numbers, R.id.navigation_logs, R.id.navigation_settings).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        getSupportActionBar().setCustomView(R.layout.top_toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        checkBasePermissions(this);
        checkAdvancedPermissions(this);

    }

    public void checkBasePermissions(final MainActivity activity){
        if(!permissionRequester.isGranted(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)){
            permissionRequester.grantSome(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, new Consumer<Boolean>() {
                @Override public void accept(Boolean accepted) {
                    if(accepted){
                        new AlertDialog.Builder(activity)
                                .setTitle(getString(R.string.restart_title))
                                .setMessage(getString(R.string.restart_dialog))
                                .setPositiveButton(getString(R.string.message_ok), new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        doRestart(activity);
                                    }
                                }).show();
                    }else{
                        new AlertDialog.Builder(activity)
                                .setTitle(getString(R.string.error_no_permission_title))
                                .setMessage(getString(R.string.error_no_permission))
                                .setPositiveButton(getString(R.string.message_retry), new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        checkBasePermissions(activity);
                                    }
                                }).setNegativeButton(getString(R.string.message_ok), new DialogInterface.OnClickListener(){ @Override public void onClick(DialogInterface dialog, int which){ } }).show();
                    }
                }
            });
        }
    }
    public void checkAdvancedPermissions(final MainActivity activity){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!Settings.System.canWrite(this)){
                new AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.error_no_permission_title))
                    .setMessage(getString(R.string.error_open_permission_settings))
                    .setPositiveButton(getString(R.string.message_ok), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + activity.getApplicationContext().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.getApplicationContext().startActivity(intent);
                        }
                    }).show();
            }
        }else{
            new AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.error_no_permission_title))
                    .setMessage(getString(R.string.error_open_permission_settings))
                    .setPositiveButton(getString(R.string.message_ok), new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
    }


    public Toolbar getTopToolBar(){
        return (Toolbar) getSupportActionBar().getCustomView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode >= PERMISSION_REQUESTER_START && requestCode <= PERMISSION_REQUESTER_END){
            boolean grant = true;
            for(int grantResult : grantResults ){
                grant = grant && (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            permissionRequester.receiveActivityResult(requestCode, grant);
        }
    }

    public static MainActivity inst;
    public static boolean active = false;
    @Override
    public void onStart() {
        super.onStart();
        active = true;
        inst = this;
    }
    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e(TAG, "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e(TAG, "Was not able to restart application, PM null");
                }
            } else {
                Log.e(TAG, "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Was not able to restart application");
        }
    }

}