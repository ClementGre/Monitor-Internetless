package fr.themsou.monitorinternetless;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUESTER_START = 2000;
    private static final int PERMISSION_REQUESTER_END = 3999;
    private String TAG = "MainActivity";
    public PermissionRequester permissionRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        permissionRequester = new PermissionRequester(this);

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_numbers, R.id.navigation_logs, R.id.navigation_settings).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        getSupportActionBar().setCustomView(R.layout.top_toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

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
            //Log.d(TAG, "onRequestPermissionsResult: grantResults = " + Arrays.toString(grantResults));
            //Log.d(TAG, "onRequestPermissionsResult: permissions  = " + Arrays.toString(permissions));
            permissionRequester.receiveActivityResult(requestCode, grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }
}