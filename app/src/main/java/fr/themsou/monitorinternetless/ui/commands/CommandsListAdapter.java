package fr.themsou.monitorinternetless.ui.commands;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.core.util.Consumer;
import java.util.ArrayList;
import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class CommandsListAdapter extends BaseAdapter {

    private Context context;
    private MainActivity activity;
    private ArrayList<Command> items;

    public CommandsListAdapter(Context context, MainActivity activity, ArrayList<Command> items) {
        this.context = context;
        this.activity = activity;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }
    @Override
    public Command getItem(int position) {
        return items.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.listitem_commands, parent, false);
        }

        final Command command = getItem(position);

        ((ImageView) convertView.findViewById(R.id.command_iconView)).setImageResource(command.getIcon());
        ((TextView) convertView.findViewById(R.id.command_title)).setText(command.getTitle());
        ((TextView) convertView.findViewById(R.id.command_desc)).setText(command.getDescription());

        final Switch switchEnable = ((Switch) convertView.findViewById(R.id.command_switch));
        switchEnable.setChecked(command.isEnabled());
        switchEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            boolean lastChecked = command.isEnabled();
            @Override public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked){
                if(lastChecked != isChecked){
                    command.switchChange(isChecked, activity, switchEnable, new Consumer<Void>() {
                        @Override public void accept(Void aVoid) {
                            lastChecked = isChecked;
                        }
                    });
                }
            }
        });

        return convertView;
    }
}
