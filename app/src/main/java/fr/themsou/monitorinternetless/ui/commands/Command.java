package fr.themsou.monitorinternetless.ui.commands;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageDecoder;
import android.util.Log;
import android.widget.Switch;

import androidx.core.util.Consumer;
import androidx.preference.SwitchPreferenceCompat;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class Command {

    private String nameId;

    private int icon;
    private int title;
    private int description;

    private boolean enabled;
    private String[] permissions;

    public Command(String nameId, int icon, int title, int description, boolean enabled) {
        this.nameId = nameId;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.enabled = enabled;
    }
    public Command(String nameId, int icon, int title, int description, boolean enabled, MainActivity activity, String... permissions) {
        this.nameId = nameId;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.enabled = enabled && hasPermission(activity);
        this.permissions = permissions;
    }

    public Command(String nameId, int icon, int title, int description, MainActivity activity) {
        this.nameId = nameId;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.enabled = getEnableStatus(activity);
    }
    public Command(String nameId, int icon, int title, int description, MainActivity activity, String... permissions) {
        this.nameId = nameId;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.enabled = getEnableStatus(activity) && hasPermission(activity);
        this.permissions = permissions;
    }

    public void switchChange(final boolean isChecked, final MainActivity activity, final Switch switchEnable, final Consumer<Void> acceptChange) {
        if(isChecked){
            if(hasPermission(activity)){
                enabled = true;
                updateEnableStatus(activity.getApplicationContext());
                acceptChange.accept(null);
            }else{
                activity.permissionRequester.grantSome(permissions, new Consumer<Boolean>() {
                    @Override public void accept(Boolean accepted) {
                        if(accepted){
                            enabled = true;
                            updateEnableStatus(activity);
                            acceptChange.accept(null);
                        }else{
                            switchEnable.setChecked(false);
                        }
                    }
                });
            }
        }else{
            enabled = false;
            updateEnableStatus(activity.getApplicationContext());
            acceptChange.accept(null);
        }
    }

    public boolean hasPermission(MainActivity activity){
        if(permissions == null) return true;
        return activity.permissionRequester.isGranted(permissions);
    }

    private void updateEnableStatus(Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isCommandEnabled." + nameId, isEnabled());
        editor.commit();
    }
    private boolean getEnableStatus(MainActivity activity){
        SharedPreferences sharedPref = activity.getSharedPreferences(activity.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isCommandEnabled." + nameId, hasPermission(activity));
    }

    public int getIcon() {
        return icon;
    }
    public void setIcon(int icon) {
        this.icon = icon;
    }
    public int getTitle() {
        return title;
    }
    public void setTitle(int title) {
        this.title = title;
    }
    public int getDescription() {
        return description;
    }
    public void setDescription(int description) {
        this.description = description;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
