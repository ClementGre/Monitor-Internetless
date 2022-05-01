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
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;

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

        Toolbar toolBar = (Toolbar) getSupportActionBar().getCustomView();
        toolBar.getMenu().getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
            @Override public boolean onMenuItemClick(MenuItem item){
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fab_slide_in_from_right, R.anim.fab_slide_out_to_left);
                return true;
            }
        });

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1);
        if(RingCommandExecutor.mediaPlayer != null && RingCommandExecutor.mediaPlayer.isPlaying()){
            RingCommandExecutor.mediaPlayer.stop();
            final AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, RingCommandExecutor.oldVolume, 0);
        }

        initNotificationsChannels();
        checkBasePermissions(this);
        //checkAdvancedPermissions(this);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("authorizednumbers");
        editor.commit();

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
                                        doRestart();
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

    public void doRestart(){
        finish();
        startActivity(getIntent());
        // Animation
        overridePendingTransition(0, 0);
    }

    private void initNotificationsChannels(){
        CharSequence name = "Sonnerie";
        String description = "Notification pour alerter de la sonnerie du téléphone avec la commande !ring";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("ring", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}