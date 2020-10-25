package fr.themsou.monitorinternetless.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import fr.themsou.monitorinternetless.MainActivity;
import fr.themsou.monitorinternetless.R;
import fr.themsou.monitorinternetless.ui.commands.CommandsListAdapter;
import fr.themsou.monitorinternetless.ui.numbers.Number;
import fr.themsou.monitorinternetless.ui.numbers.NumbersListAdapter;

public class SettingsFragment extends Fragment {

    private ListView listView;
    private SettingsListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getTopToolBar().setTitle(getString(R.string.title_settings));

        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final LayoutInflater layoutInflater = LayoutInflater.from(getContext());

        listView = root.findViewById(R.id.settings_listview);

        adapter = new SettingsListAdapter(getContext(), ((MainActivity) getActivity()), getSettings((MainActivity) getActivity()));
        listView.setAdapter(adapter);

        return root;
    }

    public static ArrayList<Setting> getSettings(Context context){
        ArrayList<Setting> settings = new ArrayList<>();

        settings.add(new Setting("everyone-allowed", R.string.setting_everyone_allowed_title, R.string.setting_everyone_allowed_description, context, false));
        settings.add(new Setting("password", R.string.setting_password_title, R.string.setting_password_description, context, true));

        return settings;
    }
}