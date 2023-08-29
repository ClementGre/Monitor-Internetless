package fr.themsou.monitorinternetless;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.util.Consumer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import fr.themsou.monitorinternetless.commander.RingCommandExecutor;
import fr.themsou.monitorinternetless.ui.about.AboutActivity;

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
        // Commands as default selected tab
        navController.navigate(R.id.navigation_logs);

        // About menu
        Toolbar toolBar = (Toolbar) getSupportActionBar().getCustomView();
        toolBar.getMenu().getItem(0).setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fab_slide_in_from_right, R.anim.fab_slide_out_to_left);
            return true;
        });


        initNotificationsChannels();
        checkBasePermissions();
        //checkAdvancedPermissions();
        checkIgnoreBatteryOptimization();


        // Disable ring
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1);
        if(RingCommandExecutor.mediaPlayer != null && RingCommandExecutor.mediaPlayer.isPlaying()){
            RingCommandExecutor.mediaPlayer.stop();
            final AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, RingCommandExecutor.oldVolume, 0);
        }

        // Remove logged-in numbers
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("authorizednumbers");
        editor.commit();
    }

    public void checkBasePermissions(){
        if(!permissionRequester.isGranted(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)){
            permissionRequester.grantSome(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, new Consumer<Boolean>() {
                @Override public void accept(Boolean accepted) {
                    if(accepted){
                        new AlertDialog.Builder(getApplicationContext())
                                .setTitle(getString(R.string.restart_title))
                                .setMessage(getString(R.string.restart_dialog))
                                .setPositiveButton(getString(R.string.message_ok), new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        doRestart();
                                    }
                                }).show();
                    }else{
                        new AlertDialog.Builder(getApplicationContext())
                                .setTitle(getString(R.string.error_no_permission_title))
                                .setMessage(getString(R.string.error_no_permission))
                                .setPositiveButton(getString(R.string.message_retry), new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        checkBasePermissions();
                                    }
                                }).setNegativeButton(getString(R.string.message_ok), new DialogInterface.OnClickListener(){ @Override public void onClick(DialogInterface dialog, int which){ } }).show();
                    }
                }
            });
        }
    }
    // Write settings permission (not used for the current commands)
    // This will only grant WRITE_SETTINGS
    // To grand WRITE_SECURE_SETTINGS, run
    // adb shell pm grant fr.themsou.monitorinternetless android.permission.WRITE_SECURE_SETTINGS
    // MacOS: brew install android-platform-tools | Windows: winget install --id Google.PlatformTools | Debian : sudo apt-get update && sudo apt-get -y install android-tools-adb
    public void checkAdvancedPermissions(){
        if (!Settings.System.canWrite(this)) {
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error_no_permission_title))
                .setMessage(getString(R.string.error_open_permission_settings))
                .setPositiveButton(getString(R.string.message_ok), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }).show();
        }
    }

    // The app must execute commands at the time they are received.
    // Disabling the battery optimization is not mandatory,
    // but the user will be notified of this being able to fix eventual issues.
    private void checkIgnoreBatteryOptimization() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(sharedPref.getBoolean("ignoreBatteryOptimization", false)) return;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm == null || pm.isIgnoringBatteryOptimizations(getPackageName())) return;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_battery_optimization_title))
                .setMessage(getString(R.string.dialog_battery_optimization_message))
                .setPositiveButton(getString(R.string.message_ok), (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.message_ignore), (dialog, which) -> {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("ignoreBatteryOptimization", true);
                    editor.commit();
                })
                .show();
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

    public void doRestart(){
        finish();
        startActivity(getIntent());
        // Animation
        overridePendingTransition(0, 0);
    }

    private void initNotificationsChannels(){
        initNotificationChannel("ring", getString(R.string.notificationchannel_ring_title),
                getString(R.string.notificationchannel_ring_description),
                NotificationManager.IMPORTANCE_DEFAULT);
        initNotificationChannel("locate", getString(R.string.notificationchannel_locate_title),
                getString(R.string.notificationchannel_locate_description),
                NotificationManager.IMPORTANCE_LOW);
    }
    private void initNotificationChannel(String id, String name, String description, int importance){
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}