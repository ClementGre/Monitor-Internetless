package fr.themsou.monitorinternetless.ui.settings;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Set;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;
import fr.themsou.monitorinternetless.ui.commands.Command;

public class SettingsListAdapter extends BaseAdapter {

    private static final String TAG = "SettingsListAdapter";
    private Context context;
    private MainActivity activity;
    private ArrayList<Setting> items;

    public SettingsListAdapter(Context context, MainActivity activity, ArrayList<Setting> items) {
        this.context = context;
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }
    @Override
    public Setting getItem(int position) {
        return items.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.listitem_settings, parent, false);

            final Setting setting = getItem(position);

            ((TextView) convertView.findViewById(R.id.setting_title)).setText(setting.getTitle());
            ((TextView) convertView.findViewById(R.id.setting_desc)).setText(setting.getDescription());

            final Switch switchEnable = ((Switch) convertView.findViewById(R.id.setting_switch));
            switchEnable.setChecked(setting.isEnabled());

            final EditText textField = ((EditText) convertView.findViewById(R.id.setting_field));
            if(setting.getValue() == null){
                ((ViewManager) textField.getParent()).removeView(textField);
            }else{
                textField.setText(setting.getValue());
            }

            switchEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                boolean lastChecked = setting.isEnabled();
                @Override public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked){
                    if(lastChecked != isChecked){
                        setting.setEnabled(isChecked, activity);
                        lastChecked = isChecked;
                    }
                }
            });

            textField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count){}
                @Override
                public void afterTextChanged(Editable s){
                    setting.setValue(s.toString(), context);
                }
            });
            textField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_DONE) {
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                    }
                    return false;
                }
            });
        }
        return convertView;
    }
}