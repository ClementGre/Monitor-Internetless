package fr.themsou.monitorinternetless.ui.commands;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;

public class CommandsFragment extends Fragment {

    private ListView listView;
    private CommandsListAdapter adapter;
    private static final String TAG = "CommandsFragment";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getTopToolBar().setTitle(getString(R.string.title_commands));

        View root = inflater.inflate(R.layout.fragment_commands, container, false);
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        listView = root.findViewById(R.id.command_listview);
        listView.addHeaderView(layoutInflater.inflate(R.layout.header_commands, null));

        adapter = new CommandsListAdapter(getContext(), ((MainActivity) getActivity()), getCommands(getActivity()));
        listView.setAdapter(adapter);


        return root;
    }

    public static ArrayList<Command> getCommands(Context context){
        ArrayList<Command> commands = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            commands.add(new Command("info", R.drawable.ic_baseline_info_24, R.string.command_title_info, R.string.command_desc_info,
                    context, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION));
            commands.add(new Command("locate", R.drawable.ic_baseline_gps_fixed_24, R.string.command_title_locate, R.string.command_desc_locate,
                    context, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION));
        }else{
            commands.add(new Command("info", R.drawable.ic_baseline_info_24, R.string.command_title_info, R.string.command_desc_info,
                    context, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION));
            commands.add(new Command("locate", R.drawable.ic_baseline_gps_fixed_24, R.string.command_title_locate, R.string.command_desc_locate,
                    context, Manifest.permission.ACCESS_FINE_LOCATION));
        }

        commands.add(new Command("ring", R.drawable.ic_baseline_music_note_24, R.string.command_title_ring, R.string.command_desc_ring,
                context));

        return commands;
    }
}