package fr.themsou.monitorinternetless.ui.commands;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Switch;

import androidx.core.util.Consumer;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.PermissionRequester;
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
    public Command(String nameId, int icon, int title, int description, boolean enabled, Context context, String... permissions) {
        this.nameId = nameId;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.permissions = permissions;
        this.enabled = enabled && hasPermission(context);
    }

    public Command(String nameId, int icon, int title, int description, Context context) {
        this.nameId = nameId;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.enabled = getEnableStatus(context);
    }
    public Command(String nameId, int icon, int title, int description, Context context, String... permissions) {
        this.nameId = nameId;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.permissions = permissions;
        this.enabled = getEnableStatus(context) && hasPermission(context);

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

    public boolean hasPermission(Context context){
        if(permissions == null) return true;
        return PermissionRequester.isGranted(context);
    }

    public String execute(String[] args, Context context){
        if(enabled){
            if(hasPermission(context)){

                return "La command ea été exécuté !";

            }else return context.getString(R.string.command_error_no_permission);
        }else return context.getString(R.string.command_error_not_enabled);

    }

    private void updateEnableStatus(Context context){

        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isCommandEnabled." + nameId, isEnabled());
        editor.commit();
    }
    private boolean getEnableStatus(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isCommandEnabled." + nameId, hasPermission(context));
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
