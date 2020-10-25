package fr.themsou.monitorinternetless.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;

import fr.themsou.monitorinternetless.R;

public class Setting {


    private int title;
    private int description;

    private final String nameId;
    private String value = null;
    private boolean enabled;

    public Setting(String nameId, Context context){
        this.nameId = nameId;
        this.value = getValue(context);
        this.enabled = getEnabled(context);
    }
    public Setting(String nameId, int title, int description, Context context, boolean hasValue){
        this.nameId = nameId;
        this.title = title;
        if(hasValue) this.value = getValue(context);
        this.enabled = getEnabled(context);
        this.description = description;
    }


    public String getValue() {
        return value;
    }
    public void setValue(String value, Context context){
        this.value = value;
        updateValue(context);
    }
    private void updateValue(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("settings." + nameId + ".value", value);
        editor.commit();
    }
    private String getValue(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString("settings." + nameId + ".value", "");
    }

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled, Context context){
        this.enabled = enabled;
        updateEnabled(context);
    }
    private void updateEnabled(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("settings." + nameId + ".enabled", enabled);
        editor.commit();
    }
    private Boolean getEnabled(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("settings." + nameId + ".enabled", false);
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

    public String getNameId() {
        return nameId;
    }
}
